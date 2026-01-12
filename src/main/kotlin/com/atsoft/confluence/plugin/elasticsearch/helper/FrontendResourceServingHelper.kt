package com.atsoft.confluence.plugin.elasticsearch.helper

import java.io.IOException
import java.net.URLConnection
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 정적 리소스를 Confluence 서블릿 환경에서 제공하기 위한 헬퍼 객체입니다.
 *
 * <p>
 * 이 객체는 React, Svelte, Vue 등으로 빌드된 SPA(Single Page Application)의
 * 정적 에셋(HTML, JS, CSS 등)을 JAR 파일의 클래스패스(Classpath)에서 로드하여
 * Confluence의 데코레이터(레이아웃)와 함께 안전하고 효율적으로 서빙합니다.
 * </p>
 *
 * <h3>주요 기능 및 특징:</h3>
 * <ul>
 * <li><b>보안 강화 (Security):</b> Path Traversal(`..`) 공격을 감지하고 차단합니다.</li>
 * <li><b>성능 최적화 (Performance):</b> `ETag` 및 `304 Not Modified`를 지원하여 대역폭을 절약합니다.</li>
 * <li><b>스마트 캐싱 (Smart Caching):</b> 불변(Immutable) 리소스는 장기 캐시를, 일반 리소스는 유효성 검증(Revalidation)을 수행합니다.</li>
 * <li><b>SPA 지원 (SPA Routing):</b> 클라이언트 라우팅 경로 요청 시 `index.html`을 폴백(Fallback)으로 반환합니다.</li>
 * <li><b>Confluence 통합:</b> `index.html` 로드 시 동적으로 데코레이터 메타 태그를 주입합니다.</li>
 * </ul>
 *
 * @author AtSoft
 * @since 1.1.0
 */
object FrontendResourceServingHelper {

    // =========================================================================
    //  Constants (상수 정의)
    // =========================================================================

    private const val HEADER_CACHE_CONTROL = "Cache-Control"
    private const val HEADER_ETAG = "ETag"
    private const val HEADER_IF_NONE_MATCH = "If-None-Match"

    /** 불변 리소스(해시 포함 파일)를 위한 캐시 설정 (1년) */
    private const val CACHE_IMMUTABLE = "public, max-age=31536000, immutable"

    /** 변경 가능 리소스(HTML 등)를 위한 캐시 설정 (매번 서버 검증) */
    private const val CACHE_NO_CACHE = "no-cache"

    private const val CONTENT_TYPE_HTML = "text/html;charset=UTF-8"
    private const val INDEX_HTML = "/index.html"

    /** 데코레이터 주입을 위한 메타 태그 템플릿 */
    private const val META_DECORATOR_TEMPLATE = """<meta name="decorator" content="%s" />"""

    // =========================================================================
    //  Public API
    // =========================================================================

    /**
     * 요청된 경로를 기반으로 정적 리소스 또는 SPA 엔트리 포인트(index.html)를 반환합니다.
     *
     * <p>
     * 이 메서드는 다음 순서로 요청을 처리합니다:
     * 1. 경로 정규화 및 보안 검사 (`..` 차단)
     * 2. 루트 요청 처리 (`index.html` 반환)
     * 3. 리소스 메타데이터 확인 및 `304 Not Modified` 검사
     * 4. 리소스 스트리밍 및 응답 헤더 설정
     * </p>
     *
     * @param req         요청 정보를 담고 있는 [HttpServletRequest] 객체
     * @param resp        응답을 처리할 [HttpServletResponse] 객체
     * @param appBasePath 클래스패스 상에서 정적 리소스가 위치한 루트 경로 (예: `/frontend/app`)
     * @param decorator   `index.html`에 주입할 Confluence 데코레이터 이름 (예: `atl.general`)
     * @throws ServletException 서블릿 처리 중 예외 발생 시
     * @throws IOException      입출력 처리 중 예외 발생 시
     */
    @Throws(ServletException::class, IOException::class)
    fun serveResource(
        req: HttpServletRequest,
        resp: HttpServletResponse,
        appBasePath: String,
        decorator: String
    ) {
        // 1. 경로 정규화 (Null-safe)
        val pathInfo = req.pathInfo.orEmpty().ifBlank { "/" }

        // [Security] Path Traversal 공격 방지
        if (pathInfo.contains("..")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid path")
            return
        }

        // 2. 루트 요청 처리 (Early Return)
        if (pathInfo == "/") {
            return resp.serveIndexHtml(req, appBasePath, decorator)
        }

        // 3. 리소스 URL 확보 (URL 객체로 메타데이터 접근)
        val resourcePath = "$appBasePath$pathInfo"
        // 4. 리소스 부재 시 처리 (SPA 라우팅 또는 404)
        val resourceUrl = this::class.java.getResource(resourcePath)
            ?: return handleResourceNotFound(
                req,
                resp,
                pathInfo,
                appBasePath,
                decorator
            )


        // 5. 리소스 메타데이터 확인 및 ETag 생성
        val connection = resourceUrl.openConnection()
        val lastModified = connection.lastModified
        val contentLength = connection.contentLengthLong

        // Weak ETag 생성 (파일 크기 + 수정 시간)
        val eTag = "W/\"$contentLength-$lastModified\""

        // [Performance] 6. 조건부 요청(304) 처리
        if (checkNotModified(req, resp, eTag)) {
            return // 본문 전송 없이 종료
        }

        // 7. 리소스 전송 (200 OK)
        connection.getInputStream().use { stream ->
            resp.apply {
                contentType = resourcePath.guessMimeType
                setHeader(HEADER_CACHE_CONTROL, pathInfo.cacheControlValue)
                setHeader(HEADER_ETAG, eTag)
                if (contentLength > 0) setContentLengthLong(contentLength)
            }
            stream.copyTo(resp.outputStream)
        }
    }

    // =========================================================================
    //  Internal Logic
    // =========================================================================

    /**
     * 리소스를 찾지 못했을 때의 처리 로직입니다.
     * 파일 확장자 유무에 따라 SPA 라우팅인지 실제 404인지 판단합니다.
     */
    private fun handleResourceNotFound(
        req: HttpServletRequest,
        resp: HttpServletResponse,
        pathInfo: String,
        appBasePath: String,
        decorator: String
    ) {
        if (pathInfo.hasFileExtension) {
            // 실제 정적 파일(이미지 등)이 없는 경우 -> 404
            resp.sendError(HttpServletResponse.SC_NOT_FOUND)
        } else {
            // SPA 라우팅(URL 경로)인 경우 -> index.html 반환
            resp.serveIndexHtml(req, appBasePath, decorator)
        }
    }

    /**
     * 클라이언트의 `If-None-Match` 헤더와 현재 리소스의 ETag를 비교합니다.
     * 일치할 경우 304 상태 코드를 설정하고 true를 반환합니다.
     *
     * @return true if 304 Not Modified, false otherwise
     */
    private fun checkNotModified(req: HttpServletRequest, resp: HttpServletResponse, eTag: String): Boolean {
        val ifNoneMatch = req.getHeader(HEADER_IF_NONE_MATCH)
        if (ifNoneMatch != null && ifNoneMatch == eTag) {
            resp.status = HttpServletResponse.SC_NOT_MODIFIED
            return true
        }
        return false
    }

    // =========================================================================
    //  Extensions & Helpers (확장 함수 및 유틸리티)
    // =========================================================================

    /**
     * [HttpServletResponse] 확장 함수.
     * `index.html`을 읽어 데코레이터를 주입하고, 내용 기반 ETag를 생성하여 응답합니다.
     */
    private fun HttpServletResponse.serveIndexHtml(
        req: HttpServletRequest,
        appBasePath: String,
        decorator: String
    ) {
        val resourcePath = "$appBasePath$INDEX_HTML"
        val inputStream = ResourceServingHelper::class.java.getResourceAsStream(resourcePath)

        if (inputStream == null) {
            sendError(HttpServletResponse.SC_NOT_FOUND, "index.html not found at $resourcePath")
            return
        }

        inputStream.use { stream ->
            // 1. HTML 로드 및 변환 (String 변환)
            val rawHtml = stream.reader(Charsets.UTF_8).readText()
            val finalHtml = rawHtml.injectDecorator(decorator)

            // 2. ETag 생성 (내용 기반 해시)
            // 동적으로 주입되는 데코레이터에 따라 내용이 달라지므로, 변환된 결과의 해시를 사용해야 함
            val eTag = "W/\"${finalHtml.hashCode()}\""

            // 3. 조건부 요청 확인 (304)
            if (checkNotModified(req, this, eTag)) {
                return
            }

            // 4. 응답 전송
            contentType = CONTENT_TYPE_HTML
            // HTML은 항상 최신 여부를 확인해야 하므로 no-cache 설정
            setHeader(HEADER_CACHE_CONTROL, CACHE_NO_CACHE)
            setHeader(HEADER_ETAG, eTag)

            writer.write(finalHtml)
        }
    }

    /**
     * [String] 확장 프로퍼티.
     * 경로에 파일 확장자(점 `.`)가 포함되어 있는지 확인합니다.
     */
    private val String.hasFileExtension: Boolean
        get() = contains('.')

    /**
     * [String] 확장 프로퍼티.
     * 파일 경로 문자열을 기반으로 적절한 MIME 타입을 추론합니다.
     */
    private val String.guessMimeType: String
        get() = URLConnection.guessContentTypeFromName(this) ?: when {
            endsWith(".js") -> "application/javascript"
            endsWith(".css") -> "text/css"
            endsWith(".svg") -> "image/svg+xml"
            endsWith(".json") -> "application/json"
            endsWith(".woff2") -> "font/woff2"
            else -> "application/octet-stream"
        }

    /**
     * [String] 확장 프로퍼티.
     * 요청 경로 패턴에 따라 적절한 `Cache-Control` 값을 반환합니다.
     * `_app/immutable/` 경로는 내용 불변이 보장되므로 1년 캐시를 적용합니다.
     */
    private val String.cacheControlValue: String
        get() = if (startsWith("/_app/immutable/")) CACHE_IMMUTABLE else CACHE_NO_CACHE

    /**
     * [String] 확장 함수.
     * 정규식을 사용하여 HTML의 `<head>` 태그를 찾아 메타 태그를 주입합니다.
     * 대소문자(<HEAD>)와 속성(<head lang="ko">)이 있는 경우도 안전하게 처리합니다.
     */
    private fun String.injectDecorator(decorator: String): String {
        val metaTag = META_DECORATOR_TEMPLATE.format(decorator)
        val headPattern = Regex("<head[^>]*>", RegexOption.IGNORE_CASE)

        return headPattern.find(this)?.let { match ->
            // 찾은 <head...> 태그 바로 뒤에 개행 후 메타 태그 삽입
            replaceRange(match.range, "${match.value}\n$metaTag")
        } ?: run {
            // <head> 태그가 없는 경우(Fragment 등), 안전하게 맨 앞에 추가
            "$metaTag\n$this"
        }
    }
}