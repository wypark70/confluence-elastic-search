package com.atsoft.confluence.plugin.elasticsearch.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
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
    private static final String LABELIT_PATH = "/rest/rabelit/1.0/items";
    private static final String LABELIT_BASE_URL = JIRA_BASE_URL + LABELIT_PATH;

    // 차단할 헤더 목록 (엄격하게 적용)
    private static final Set<String> BLOCKED_HEADERS = Set.of(
            HttpHeaders.COOKIE.toLowerCase(),
            HttpHeaders.SET_COOKIE.toLowerCase(),
            "set-cookie2",
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            HttpHeaders.CONTENT_LENGTH.toLowerCase(),
            HttpHeaders.CONTENT_ENCODING.toLowerCase(),
            HttpHeaders.HOST.toLowerCase()
    );

    @GET
    @Path("/items")
    public Response getItems(@Context HttpServletRequest req) {
        String targetUrl = LABELIT_BASE_URL;
        if (req.getQueryString() != null) targetUrl += "?" + req.getQueryString();
        return executeBufferedProxy(req, targetUrl, HttpMethod.GET, HttpRequest.BodyPublishers.noBody());
    }

    @PUT
    @Path("/items")
    public Response putItems(@Context HttpServletRequest req) {
        // PUT 요청은 Body를 읽어야 하므로 스트림으로 받지만, 즉시 전송합니다.
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofInputStream(() -> {
            try {
                return req.getInputStream();
            } catch (IOException e) {
                throw new WebApplicationException(e);
            }
        });
        return executeBufferedProxy(req, LABELIT_BASE_URL, HttpMethod.PUT, bodyPublisher);
    }

    @DELETE
    @Path("/items/{labelId}")
    public Response deleteItem(@Context HttpServletRequest req, @PathParam("labelId") String labelId) {
        String targetUrl = LABELIT_BASE_URL + "/" + labelId;
        return executeBufferedProxy(req, targetUrl, HttpMethod.DELETE, HttpRequest.BodyPublishers.noBody());
    }

    /**
     * [핵심 변경] executeBufferedProxy
     * 스트리밍(StreamingOutput)을 제거하고, byte[]로 다 받은 뒤 연결을 끊습니다.
     */
    private Response executeBufferedProxy(HttpServletRequest req, String targetUrl, String method, HttpRequest.BodyPublisher body) {
        // 1. 매 요청마다 완전히 독립된 HttpClient 생성 (쿠키 없음, 리다이렉트 없음)
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // HTTP/1.1 강제
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER) // 리다이렉트 자동 따르기 금지 (보안 강화)
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_NONE)) // 쿠키 저장소 원천 봉쇄
                .build();

        try {
            // 2. 요청 빌더 (헤더 복사 및 정리)
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .method(method, body)
                    .header("Connection", "close"); // [중요] Keep-Alive 비활성화

            // 3. 헤더 복사 (Auth 토큰 포함, 쿠키 제외)
            copyRequestHeaders(req, builder);

            // 4. 요청 전송 (동기 블로킹)
            HttpResponse<InputStream> upstreamResponse = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());

            // 5. 응답 데이터 처리 (메모리에 로드)
            // 스트리밍을 하지 않고, 여기서 데이터를 모두 읽어버린 후 소켓을 닫습니다.
            byte[] responseData = readAndDecompressEntity(upstreamResponse);
            
            // [중요] 여기서 이미 Jira와의 연결은 끝났습니다. 세션이 섞일 틈이 없습니다.

            // 6. 클라이언트 응답 생성
            Response.ResponseBuilder response = Response.status(upstreamResponse.statusCode());

            // 캐시 방지 헤더 (브라우저가 이전 사용자 데이터를 보여주는 것 방지)
            response.header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, max-age=0");
            response.header(HttpHeaders.EXPIRES, "0");
            response.header("Pragma", "no-cache");

            // 허용된 응답 헤더 복사
            upstreamResponse.headers().map().forEach((key, values) -> {
                if (isAllowedHeader(key)) {
                    values.forEach(val -> response.header(key, val));
                }
            });

            // 읽어둔 데이터를 응답으로 설정
            if (responseData != null && responseData.length > 0) {
                response.entity(new ByteArrayInputStream(responseData));
            }

            return response.build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Proxy Interrupted", e);
            return Response.serverError().entity("Proxy Error: Interrupted").build();
        } catch (Exception e) {
            log.error("Proxy Failed: " + e.getMessage(), e);
            return Response.serverError().entity("Proxy Error: " + e.getMessage()).build();
        }
    }

    // 요청 헤더 복사 로직 분리
    private void copyRequestHeaders(HttpServletRequest req, HttpRequest.Builder builder) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (isAllowedHeader(name)) {
                // 특정 헤더(Auth) 로그 찍어서 확인 (디버깅용, 운영 시 제거)
                if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                    String authVal = req.getHeader(name);
                    log.debug("Proxying Auth Header: {}...", authVal.substring(0, Math.min(10, authVal.length()))); 
                }
                builder.header(name, req.getHeader(name));
            }
        }
    }

    // 압축 해제 및 바이트 배열 변환 (동기 처리)
    private byte[] readAndDecompressEntity(HttpResponse<InputStream> response) throws IOException {
        String encoding = response.headers().firstValue(HttpHeaders.CONTENT_ENCODING).orElse("");
        
        try (InputStream rawStream = response.body()) {
            if (rawStream == null) return new byte[0];

            InputStream wrappedStream = rawStream;
            if ("gzip".equalsIgnoreCase(encoding)) {
                wrappedStream = new GZIPInputStream(rawStream);
            } else if ("deflate".equalsIgnoreCase(encoding)) {
                wrappedStream = new InflaterInputStream(rawStream);
            }
            
            // 모든 바이트를 읽어서 반환 (Java 9+)
            return wrappedStream.readAllBytes();
        }
    }

    private boolean isAllowedHeader(String name) {
        return name != null && !name.isBlank() && !BLOCKED_HEADERS.contains(name.trim().toLowerCase());
    }
}
