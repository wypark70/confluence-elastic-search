package com.atsoft.confluence.plugin.elasticsearch.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Labelit API 프록시 리소스 (세션 격리 강화 버전)
 */
@Path("/labelit")
public class PerfectProxyResource {

    private static final Logger log = LoggerFactory.getLogger(PerfectProxyResource.class);
    private static final String JIRA_BASE_URL = "https://your-jira-instance.com";
    private static final String ADMIN_TOKEN = "Bearer YOUR_ADMIN_TOKEN";
    private static final String LABELIT_PATH = "/rest/rabelit/1.0/items";
    private static final String LABELIT_BASE_URL = JIRA_BASE_URL + LABELIT_PATH;

    // [중요] HttpClient를 static으로 공유하지 않습니다.
    // 대신 가벼운 스레드 풀을 공유하여 HttpClient 생성 시 활용할 수 있습니다.
    private static final ExecutorService proxyExecutor = Executors.newCachedThreadPool();

    private static final Set<String> BLOCKED_HEADERS = Set.of(
            HttpHeaders.COOKIE.toLowerCase(),
            HttpHeaders.SET_COOKIE.toLowerCase(),
            "set-cookie2",
            HttpHeaders.AUTHORIZATION.toLowerCase(),
            "connection",
            "keep-alive",
            "transfer-encoding",
            "upgrade",
            HttpHeaders.CONTENT_LENGTH.toLowerCase(),
            HttpHeaders.CONTENT_ENCODING.toLowerCase(),
            HttpHeaders.HOST.toLowerCase(),
            HttpHeaders.CACHE_CONTROL.toLowerCase(),
            HttpHeaders.EXPIRES.toLowerCase(),
            "pragma");

    @GET
    @Path("/items")
    public Response getItems(@Context HttpServletRequest req) {
        String targetUrl = LABELIT_BASE_URL;
        if (req.getQueryString() != null)
            targetUrl += "?" + req.getQueryString();
        return executeSmartProxy(req, targetUrl, HttpMethod.GET, HttpRequest.BodyPublishers.noBody());
    }

    @PUT
    @Path("/items")
    public Response putItems(@Context HttpServletRequest req) {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofInputStream(() -> {
            try {
                return req.getInputStream();
            } catch (IOException e) {
                throw new WebApplicationException(e);
            }
        });
        return executeSmartProxy(req, LABELIT_BASE_URL, HttpMethod.PUT, bodyPublisher);
    }

    @DELETE
    @Path("/items/{labelId}")
    public Response deleteItem(@Context HttpServletRequest req, @PathParam("labelId") String labelId) {
        String targetUrl = LABELIT_BASE_URL + "/" + labelId;
        return executeSmartProxy(req, targetUrl, HttpMethod.DELETE, HttpRequest.BodyPublishers.noBody());
    }

    private Response executeSmartProxy(HttpServletRequest req, String targetUrl, String method,
            HttpRequest.BodyPublisher body) {
        // [핵심 변경 1] 요청마다 새로운 HttpClient 생성 (Connection Isolation)
        // 비용이 들지만 세션 꼬임을 원천 차단하는 가장 확실한 방법입니다.
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // [핵심 변경 2] HTTP/1.1 강제 (Multiplexing 방지)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_NONE))
                .executor(proxyExecutor) // 스레드 관리는 공유 풀 사용
                .build();

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .method(method, body)
                    .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                    .header(HttpHeaders.ACCEPT_ENCODING, "identity")
                    // [핵심 변경 3] 연결 재사용 방지 명시
                    .header("Connection", "close");

            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (isAllowedHeader(name)) {
                    builder.header(name, req.getHeader(name));
                }
            }

            HttpResponse<InputStream> upstreamResponse = client.send(builder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());

            String contentEncoding = upstreamResponse.headers().firstValue(HttpHeaders.CONTENT_ENCODING).orElse("");
            InputStream rawStream = upstreamResponse.body();

            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                rawStream = new GZIPInputStream(rawStream);
            } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
                rawStream = new InflaterInputStream(rawStream);
            }

            Response.ResponseBuilder response = Response.status(upstreamResponse.statusCode());

            response.header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            response.header(HttpHeaders.EXPIRES, "0");
            response.header("Pragma", "no-cache");

            upstreamResponse.headers().map().forEach((key, values) -> {
                if (isAllowedHeader(key)) {
                    values.forEach(val -> response.header(key, val));
                }
            });

            if (upstreamResponse.statusCode() != Response.Status.NO_CONTENT.getStatusCode()) {
                final InputStream finalStream = rawStream;
                response.entity((StreamingOutput) output -> {
                    try (InputStream in = finalStream) {
                        in.transferTo(output);
                    }
                });
            }

            return response.build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Proxy interrupted", e);
            return Response.serverError().build();
        } catch (Exception e) {
            log.error("Proxy failed", e);
            return Response.serverError().build();
        }
        // client는 지역 변수이므로 메서드 종료 시 GC 대상이 되어 연결이 확실히 정리됩니다.
    }

    private boolean isAllowedHeader(String name) {
        return name != null && !name.isBlank() && !BLOCKED_HEADERS.contains(name.trim().toLowerCase());
    }
}
