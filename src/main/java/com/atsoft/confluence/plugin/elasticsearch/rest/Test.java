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
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

@Path("/labelit")
public class PerfectProxyResource {

    private static final Logger log = LoggerFactory.getLogger(PerfectProxyResource.class);
    private static final String JIRA_BASE_URL = "https://your-jira-instance.com";
    // ADMIN_TOKEN 제거 -> 사용자 토큰을 직접 사용

    private static final String LABELIT_PATH = "/rest/rabelit/1.0/items";
    private static final String LABELIT_BASE_URL = JIRA_BASE_URL + LABELIT_PATH;

    // [전략 1] Static 유지 (메모리 효율성)
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1) // HTTP/1.1 고정 (멀티플렉싱 방지)
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_NONE)) // 쿠키 아예 안 받음
            .build();

    private static final Set<String> BLOCKED_HEADERS = Set.of(
            HttpHeaders.COOKIE.toLowerCase(),
            HttpHeaders.SET_COOKIE.toLowerCase(),
            "set-cookie2",
            // Authorization은 전달해야 하므로 차단 목록에서 제거함
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
        if (req.getQueryString() != null) targetUrl += "?" + req.getQueryString();
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

    private Response executeSmartProxy(HttpServletRequest req, String targetUrl, String method, HttpRequest.BodyPublisher body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .method(method, body)
                    .header(HttpHeaders.ACCEPT_ENCODING, "identity")
                    // [전략 2] Connection: close 헤더 추가
                    // Static Client가 커넥션 풀을 가지고 있어도, 이 헤더가 있으면
                    // 요청이 끝난 후 즉시 소켓을 끊어버립니다. (세션 섞임 방지 핵심)
                    .header("Connection", "close"); 

            // [전략 3] 원래 요청자(Original Requester)의 인증 정보 전달
            // 기존 ADMIN_TOKEN 대신 들어온 요청의 Authorization 헤더를 그대로 넘깁니다.
            String userAuth = req.getHeader(HttpHeaders.AUTHORIZATION);
            if (userAuth != null) {
                builder.header(HttpHeaders.AUTHORIZATION, userAuth);
            } else {
                // 로그인하지 않은 요청에 대한 처리 (필요 시 예외 처리 또는 익명 허용)
                // builder.header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN); // 필요하다면 폴백 사용
            }

            // 나머지 허용 헤더 복사
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                // Authorization은 위에서 수동 처리했으므로 중복 방지
                if (isAllowedHeader(name) && !name.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                    builder.header(name, req.getHeader(name));
                }
            }

            HttpResponse<InputStream> upstreamResponse = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());

            // --- 이하 응답 처리 로직은 동일 ---
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
    }

    private boolean isAllowedHeader(String name) {
        return name != null && !name.isBlank() && !BLOCKED_HEADERS.contains(name.trim().toLowerCase());
    }
}
