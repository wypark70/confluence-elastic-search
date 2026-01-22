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

/**
 * Labelit API와의 통신을 담당하는 프록시 리소스 클래스입니다.
 * <p>
 * 이 클래스는 다음과 같은 주요 기능을 수행하여 안정적인 프록시 통신을 지원합니다:
 * <ul>
 * <li><strong>세션 혼선 방지:</strong> HttpClient의 쿠키 저장을 차단하고 관련 헤더를 필터링하여 세션 꼬임 문제를
 * 예방합니다.</li>
 * <li><strong>스마트 압축 해제:</strong> 응답 데이터의 압축 방식(GZIP 등)을 감지하여 자동으로 해제합니다.</li>
 * <li><strong>메모리 최적화:</strong> {@link StreamingOutput}을 사용하여 데이터를 스트리밍 방식으로
 * 처리함으로써 메모리 사용을 최적화합니다.</li>
 * </ul>
 */
@Path("/labelit")
public class PerfectProxyResource {

    private static final Logger log = LoggerFactory.getLogger(PerfectProxyResource.class);
    private static final String JIRA_BASE_URL = "https://your-jira-instance.com";
    private static final String ADMIN_TOKEN = "Bearer YOUR_ADMIN_TOKEN";
    private static final String LABELIT_PATH = "/rest/rabelit/1.0/items";
    private static final String LABELIT_BASE_URL = JIRA_BASE_URL + LABELIT_PATH;

    /**
     * 세션 정보 유지를 방지하기 위해 쿠키 관리를 비활성화한 HttpClient 인스턴스입니다.
     * <p>
     * 쿠키 정책을 {@link CookiePolicy#ACCEPT_NONE}으로 설정하여 이전 요청의 세션 정보가
     * 재사용되는 것을 원천적으로 차단합니다.
     */
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_NONE)) // 쿠키 저장 비활성화
            .build();
    /**
     * 프록시 통신 시 차단할 헤더 목록입니다.
     * 보안, 네트워크 제어, 데이터 변형과 관련된 헤더들을 포함합니다.
     */
    private static final Set<String> BLOCKED_HEADERS = Set.of(
            // 1. [보안] 세션 및 인증 관련
            HttpHeaders.COOKIE.toLowerCase(),
            HttpHeaders.SET_COOKIE.toLowerCase(),
            "set-cookie2",
            HttpHeaders.AUTHORIZATION.toLowerCase(),

            // 2. [네트워크] Hop-by-hop 헤더 (프록시가 직접 처리해야 함)
            "connection",
            "keep-alive",
            "transfer-encoding",
            "upgrade",

            // 3. [데이터] 메타데이터 (압축 해제 등으로 인해 유효하지 않게 된 정보)
            HttpHeaders.CONTENT_LENGTH.toLowerCase(),
            HttpHeaders.CONTENT_ENCODING.toLowerCase(),

            // 4. [기타] 제어용 헤더
            HttpHeaders.HOST.toLowerCase(),
            HttpHeaders.CACHE_CONTROL.toLowerCase(),
            HttpHeaders.EXPIRES.toLowerCase(),
            "pragma");

    /**
     * Labelit 항목 목록을 조회합니다.
     *
     * @param req 클라이언트의 HttpServletRequest
     * @return 원격 서버로부터 받은 항목 목록 응답
     */
    @GET
    @Path("/items")
    public Response getItems(@Context HttpServletRequest req) {
        String targetUrl = LABELIT_BASE_URL;
        if (req.getQueryString() != null) targetUrl += "?" + req.getQueryString();
        return executeSmartProxy(req, targetUrl, HttpMethod.GET, HttpRequest.BodyPublishers.noBody());
    }

    /**
     * Labelit 항목을 생성하거나 업데이트합니다.
     * <p>
     * 클라이언트의 요청 본문을 스트리밍 방식으로 원격 서버에 전달합니다.
     *
     * @param req 클라이언트의 HttpServletRequest
     * @return 처리 결과 응답
     */
    @PUT
    @Path("/items")
    public Response putItems(@Context HttpServletRequest req) {
        // 요청 본문 스트리밍 연결
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofInputStream(() -> {
            try {
                return req.getInputStream();
            } catch (IOException e) {
                throw new WebApplicationException(e);
            }
        });
        return executeSmartProxy(req, LABELIT_BASE_URL, HttpMethod.PUT, bodyPublisher);
    }

    /**
     * 지정된 ID를 가진 Labelit 항목을 삭제합니다.
     *
     * @param req     클라이언트의 HttpServletRequest
     * @param labelId 삭제할 항목의 ID
     * @return 처리 결과 응답
     */
    @DELETE
    @Path("/items/{labelId}")
    public Response deleteItem(@Context HttpServletRequest req, @PathParam("labelId") String labelId) {
        String targetUrl = LABELIT_BASE_URL + "/" + labelId;
        return executeSmartProxy(req, targetUrl, HttpMethod.DELETE, HttpRequest.BodyPublishers.noBody());
    }

    /**
     * 프록시 요청을 실행하고 응답을 처리하는 핵심 메서드입니다.
     * <p>
     * 요청 헤더를 필터링하고, 응답 본문의 압축을 필요에 따라 해제하며,
     * 클라이언트에게 안전하게 데이터를 전달합니다.
     *
     * @param req       클라이언트의 HttpServletRequest
     * @param targetUrl 대상 원격 서버의 URL
     * @param method    HTTP 메서드 (GET, PUT, DELETE 등)
     * @param body      요청 본문 (BodyPublisher)
     * @return 처리가 완료된 JAX-RS Response 객체
     */
    private Response executeSmartProxy(HttpServletRequest req, String targetUrl, String method, HttpRequest.BodyPublisher body) {
        try {
            // 1. 요청 빌더 구성
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .method(method, body)
                    .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                    .header(HttpHeaders.ACCEPT_ENCODING, "identity"); // 압축되지 않은 응답을 선호

            // 2. 허용된 헤더 복사 (Client -> Server)
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (isAllowedHeader(name)) {
                    builder.header(name, req.getHeader(name));
                }
            }

            // 3. 원격 서버로 요청 전송
            HttpResponse<InputStream> upstreamResponse = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());

            // 4. 응답 본문 처리 (Smart Decompression)
            // 서버가 압축된 데이터를 반환한 경우, 이를 감지하여 투명하게 압축을 해제합니다.
            // 이를 통해 클라이언트는 항상 압축이 해제된 데이터를 수신할 수 있습니다.
            String contentEncoding = upstreamResponse.headers().firstValue(HttpHeaders.CONTENT_ENCODING).orElse("");
            InputStream rawStream = upstreamResponse.body();

            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                log.debug("GZIP 감지됨: {} 에 대한 응답 압축을 해제합니다.", targetUrl);
                rawStream = new GZIPInputStream(rawStream);
            } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
                rawStream = new InflaterInputStream(rawStream);
            }

            // 5. 응답 빌더 생성
            Response.ResponseBuilder response = Response.status(upstreamResponse.statusCode());

            // 6. 보안 헤더 설정 (캐시 방지)
            // 브라우저 캐시로 인한 세션 정보 잔류를 방지합니다.
            response.header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            response.header(HttpHeaders.EXPIRES, "0");
            response.header("Pragma", "no-cache");

            // 7. 허용된 응답 헤더 복사 (Server -> Client)
            // Content-Encoding, Length, Set-Cookie 등의 헤더는 필터링됩니다.
            upstreamResponse.headers().map().forEach((key, values) -> {
                if (isAllowedHeader(key)) {
                    values.forEach(val -> response.header(key, val));
                }
            });

            // 8. 응답 본문 스트리밍
            if (upstreamResponse.statusCode() != Response.Status.NO_CONTENT.getStatusCode()) {
                final InputStream finalStream = rawStream;
                response.entity((StreamingOutput) output -> {
                    // 스트리밍이 완료되면 InputStream을 닫아 리소스를 정리합니다.
                    try (InputStream in = finalStream) {
                        in.transferTo(output);
                    }
                });
            }

            return response.build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // SonarQube: 인터럽트 상태 복구
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