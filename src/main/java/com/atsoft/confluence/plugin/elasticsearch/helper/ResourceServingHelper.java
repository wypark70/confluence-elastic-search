package com.atsoft.confluence.plugin.elasticsearch.helper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 정적 리소스를 Confluence 서블릿을 통해 제공하기 위한 헬퍼 클래스입니다.
 * <p>
 * 이 클래스는 주로 SPA(Single Page Application) 프레임워크(Svelte, React 등)로 빌드된
 * 정적 파일들(HTML, JS, CSS, 이미지 등)을 JAR 파일의 클래스패스(Classpath)에서 읽어와
 * 클라이언트에게 안전하고 효율적으로 전달하는 역할을 수행합니다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 * <li><b>리소스 서빙:</b> 요청된 경로에 해당하는 파일을 클래스패스에서 찾아 응답합니다.</li>
 * <li><b>SPA 라우팅 지원:</b> 요청된 리소스가 존재하지 않고 파일 확장자가 없는 경우(예: /dashboard),
 * `index.html`을 반환하여 클라이언트 측 라우팅이 작동하도록 합니다.</li>
 * <li><b>MIME 타입 감지:</b> 파일 확장자를 기반으로 올바른 Content-Type을 설정합니다.</li>
 * <li><b>캐싱 제어:</b> 정적 에셋(Vite 빌드 결과물 등)에는 긴 캐시 수명을,
 * HTML 파일에는 캐시 없음을 설정하여 성능과 최신성을 모두 보장합니다.</li>
 * <li><b>데코레이터 주입:</b> Confluence의 레이아웃(헤더, 사이드바 등)이 적용되도록
 * `index.html`에 데코레이터 메타 태그를 동적으로 주입합니다.</li>
 * </ul>
 */
public class ResourceServingHelper {

    /** 데이터 전송 시 사용할 스트림 버퍼 크기 (4KB) */
    private static final int BUFFER_SIZE = 4096;

    /**
     * 요청된 리소스 경로를 분석하여 적절한 정적 파일을 찾아 클라이언트에게 응답합니다.
     * <p>
     * 이 메서드는 서블릿의 `doGet` 메서드에서 호출되어야 하며, 요청된 URI를 기반으로
     * 리소스를 찾거나 SPA 폴백 처리를 수행합니다.
     * </p>
     * 
     * @param req         HttpServletRequest 객체 (요청 경로 정보를 얻기 위함)
     * @param resp        HttpServletResponse 객체 (응답을 보내기 위함)
     * @param appBasePath 리소스가 위치한 클래스패스 상의 기본 경로 (예: `/frontend/user-app`).
     *                    이 경로는 `src/main/resources` 아래의 실제 폴더 구조와 일치해야 합니다.
     * @param decorator   적용할 Confluence 데코레이터 이름 (예: `atl.general`, `atl.admin`).
     *                    이 값은 `index.html`의 `<meta name="decorator">` 태그에 주입됩니다.
     * @throws IOException      입출력 처리 중 오류가 발생한 경우
     */
    public static void serveResource(HttpServletRequest req, HttpServletResponse resp, String appBasePath,
            String decorator) throws IOException {

        String pathInfo = req.getPathInfo();

        // 1. 루트 경로 요청 처리 (예: /plugins/servlet/app-name/)
        // 루트 요청인 경우 메인 페이지(index.html)를 반환합니다.
        if (isRootRequest(pathInfo)) {
            serveIndexHtml(resp, appBasePath, decorator);
            return;
        }

        // 2. 리소스 스트림 확보
        // 요청된 경로(pathInfo)를 기반으로 실제 리소스 파일의 InputStream을 찾습니다.
        String resourcePath = appBasePath + pathInfo;
        InputStream resourceStream = resolveResourceStream(resourcePath);

        if (resourceStream == null) {
            // SPA(Single Page Application) 라우팅 지원:
            // 요청된 리소스가 물리적으로 존재하지 않지만, 파일 확장자가 없는 경로(예: /users/123)인 경우
            // 이는 서버 사이드 리소스가 아닌 클라이언트 라우트일 확률이 높습니다.
            // 따라서 index.html을 대신 제공하여 브라우저가 라우팅을 처리하도록 합니다.
            if (!hasFileExtension(pathInfo)) {
                serveIndexHtml(resp, appBasePath, decorator);
                return;
            }

            // 실제 파일(이미지, 스크립트 등)이 정말로 없는 경우 404 에러를 반환합니다.
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 3. 응답 헤더 설정 및 데이터 전송
        // try-with-resources 구문을 사용하여 InputStream을 자동으로 닫습니다.
        try (InputStream is = resourceStream) {
            configureResponseHeaders(resp, resourcePath, pathInfo);
            copy(is, resp.getOutputStream());
        }
    }

    /**
     * 요청 경로가 루트 경로("/" 또는 null/empty)인지 확인합니다.
     *
     * @param pathInfo 요청 URL의 PathInfo
     * @return 루트 요청이면 true, 아니면 false
     */
    private static boolean isRootRequest(String pathInfo) {
        return pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty();
    }

    /**
     * 경로에 파일 확장자가 포함되어 있는지 확인합니다.
     * SPA 라우트인지 정적 파일 요청인지 구분하는 단순한 휴리스틱으로 사용됩니다.
     *
     * @param path 검사할 경로 문자열
     * @return 점(.)을 포함하고 있으면 true
     */
    private static boolean hasFileExtension(String path) {
        return path.contains(".");
    }

    /**
     * 주어진 리소스 경로에 해당하는 InputStream을 클래스패스에서 찾아 반환합니다.
     *
     * @param resourcePath 클래스패스 상의 리소스 전체 경로
     * @return 리소스의 InputStream, 찾지 못한 경우 null
     */
    private static InputStream resolveResourceStream(String resourcePath) {
        return ResourceServingHelper.class.getResourceAsStream(resourcePath);
    }

    /**
     * 응답 객체에 적절한 `Content-Type `Cache-Control` 헤더를 설정합니다.
     *
     * @param resp         헤더를 설정할 HttpServletResponse 객체
     * @param resourcePath 리소스 파일 경로 (MIME 타입 추론용)
     * @param pathInfo     요청 경로 (캐싱 정책 결정용)
     */
    private static void configureResponseHeaders(HttpServletResponse resp, String resourcePath, String pathInfo) {
        // MIME 타입 설정
        String mimeType = determineMimeType(resourcePath);
        resp.setContentType(mimeType);

        // 캐시 정책 설정
        if (isImmutableResource(pathInfo)) {
            // 불변 리소스(해시가 포함된 파일 등)는 브라우저에 오랫동안(1년) 캐시하도록 지시합니다.
            // 'immutable' 디렉티브는 리소스가 변경되지 않음을 명시하여 재검증 요청을 방지합니다.
            resp.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        } else {
            // HTML 파일이나 일반 리소스는 캐시하지 않고 매번 서버에 확인하도록 합니다.
            resp.setHeader("Cache-Control", "no-cache");
        }
    }

    /**
     * 파일 이름을 기반으로 MIME 타입을 추론합니다.
     * Java의 기본 `guessContentTypeFromName 실패할 경우를 대비해
     * , 자주 사용되는 웹 리소스 확장자에 대한 폴백 로직을 제공합니다.
     *
     * @param resourcePath 리소스 파일 경로
     * @return 추론된 MIME 타입 문자열
     */
    private static String determineMimeType(String resourcePath) {
        String mimeType = URLConnection.guessContentTypeFromName(resourcePath);
        if (mimeType != null) {
            return mimeType;
        }

        // URLConnection이 식별하지 못하는 일반적인 웹 확장자 처리
        if (resourcePath.endsWith(".js"))
            return "application/javascript";
        if (resourcePath.endsWith(".css"))
            return "text/css";
        if (resourcePath.endsWith(".svg"))
            return "image/svg+xml";
        if (resourcePath.endsWith(".json"))
            return "application/json";

        // 알 수 없는 파일은 기본 바이너리 스트림으로 처리
        return "application/octet-stream";
    }

    /**
     * 해당 리소스가 불변(Immutable)인지 판단합니다.
     * Vite 등의 최신 빌드 도구는 빌드 시 파일명에 해시를 포함시키며(예: _app/immutable/...)
     * 해당 파일들은 내용이 절대 변하지 않음을 보장합니다.
     *
     * @param pathInfo 요청 경로
     * @return 불변 리소스 경로 패턴과 일치하면 true
     */
    private static boolean isImmutableResource(String pathInfo) {
        return pathInfo != null && pathInfo.startsWith("/_app/immutable/");
    }

    /**
     * `index.html` 파일을 읽어 Confluence 환경에 맞게 데코레이터 메타 태그를 주입한 후 응답합니다.
     * 
     * @param resp        HttpServletResponse 객체
     * @param appBasePath 앱의 기본 경로
     * @param decorator   주입할 데코레이터 이름 (atl.general 등)
     * @throws IOException 파일 읽기/쓰기 오류 발생 시
     */
    private static void serveIndexHtml(HttpServletResponse resp, String appBasePath, String decorator)
            throws IOException {
        String resourcePath = appBasePath + "/index.html";

        try (InputStream resourceStream = ResourceServingHelper.class.getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                // index.html조차 찾을 수 없는 심각한 경우 404 에러 처리
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Application index.html not found at " + resourcePath);
                return;
            }

            // 1. HTML 파일을 문자열로 읽어옵니다.
            String html = readStreamToString(resourceStream);

            // 2. Confluence 데코레이터 메타 태그를 주입합니다.
            String decoratedHtml = injectDecorator(html, decorator);

            // 3. 응답 헤더 설정 (HTML은 캐시하지 않음)
            resp.setContentType("text/html;charset=UTF-8");
            resp.setHeader("Cache-Control", "no-cache");

            // 4. 변환된 HTML 전송
            resp.getWriter().write(decoratedHtml);
        }
    }

    /**
     * InputStream의 모든 내용을 읽어 String으로 변환합니다.
     * Scanner의 "\A" 패턴(Input 시작)을 사용하여 전체 스트림을 한 번에 읽습니다.
     *
     * @param inputStream 읽을 입력 스트림
     * @return 스트림의 전체 문자열 내용
     */
    private static String readStreamToString(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    /**
     * HTML 문자열의 <head> 태그 내에 Confluence 데코레이터 메타 태그를 삽입합니다.
     * 예: <meta name="decorator" content="atl.general" />
     *
     * @param html      원본 HTML 문자열
     * @param decorator 데코레이터 이름
     * @return 데코레이터가 주입된 HTML 문자열
     */
    private static String injectDecorator(String html, String decorator) {
        String decorationMeta = "<meta name=\"decorator\" content=\"" + decorator + "\" />\n";

        // <head> 태그 바로 뒤에 메타 태그를 삽입하여 Confluence가 레이아웃을 적용하도록 합니다.
        if (html.contains("<head>")) {
            return html.replace("<head>", "<head>\n" + decorationMeta);
        }
        // <head>가 없는 경우(예외적 상황) 맨 앞에 붙입니다.
        return decorationMeta + html;
    }

    /**
     * 입력 스트림의 데이터를 버퍼를 사용하여 출력 스트림으로 복사합니다.
     * 대용량 파일 전송 시 메모리 효율성을 위해 버퍼링을 사용합니다.
     *
     * @param input  읽을 입력 스트림
     * @param output 쓸 출력 스트림
     * @throws IOException 입출력 오류 발생 시
     */
    private static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }
}
