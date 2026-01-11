package com.atsoft.confluence.plugin.elasticsearch.helper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 정적 리소스를 서블릿을 통해 제공하기 위한 헬퍼 클래스입니다.
 * 주로 SPA(Single Page Application)의 빌드 결과물(JS, CSS, HTML 등)을
 * JAR 파일의 클래스패스에서 읽어와 클라이언트에게 전달하는 역할을 합니다.
 */
public class ResourceServingHelper {

    // 데이터 전송 시 사용할 버퍼 크기 (4KB)
    private static final int BUFFER_SIZE = 4096;

    /**
     * 요청된 리소스를 찾아 응답으로 전송합니다.
     * 
     * @param req         HttpServletRequest 객체
     * @param resp        HttpServletResponse 객체
     * @param appBasePath 리소스가 위치한 클래스패스 상의 기본 경로 (예: /frontend/user-app)
     * @param decorator   Confluence 데코레이터 이름 (예: atl.general, atl.admin)
     */
    public static void serveResource(HttpServletRequest req, HttpServletResponse resp, String appBasePath,
            String decorator) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // 루트 경로("/") 또는 경로가 없는 경우 기본값으로 index.html을 제공합니다.
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            serveIndexHtml(resp, appBasePath, decorator);
            return;
        }

        // 클래스패스에서 요청된 리소스를 찾습니다.
        String resourcePath = appBasePath + pathInfo;
        InputStream resourceStream = ResourceServingHelper.class.getResourceAsStream(resourcePath);

        if (resourceStream == null) {
            // SPA(Single Page Application) 폴백 처리:
            // 요청된 리소스가 없고, 파일 확장자가 없는 경로(예: /dashboard, /users)인 경우
            // 클라이언트 사이드 라우팅을 위해 index.html을 대신 제공합니다.
            if (!pathInfo.contains(".")) {
                serveIndexHtml(resp, appBasePath, decorator);
                return;
            }

            // 실제 파일(이미지, 스크립트 등)이 없는 경우 404 에러를 반환합니다.
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 리소스를 클라이언트로 전송합니다.
        try (InputStream is = resourceStream) {
            // 파일 이름을 기반으로 MIME 타입을 추측합니다.
            String mimeType = URLConnection.guessContentTypeFromName(resourcePath);

            // guessContentTypeFromName이 식별하지 못하는 경우를 위한 일반적인 타입 처리
            if (mimeType == null) {
                if (resourcePath.endsWith(".js"))
                    mimeType = "application/javascript";
                else if (resourcePath.endsWith(".css"))
                    mimeType = "text/css";
                else if (resourcePath.endsWith(".svg"))
                    mimeType = "image/svg+xml";
                else if (resourcePath.endsWith(".json"))
                    mimeType = "application/json";
                else
                    mimeType = "application/octet-stream"; // 기본 바이너리 타입
            }

            resp.setContentType(mimeType);

            // 캐시 헤더 설정:
            // 빌드 도구(Vite 등)에 의해 해시가 포함된 파일(예: /_app/immutable/...)은
            // 내용이 변하지 않으므로 매우 긴 캐시 기간(1년)을 설정합니다.
            if (pathInfo.startsWith("/_app/immutable/")) {
                resp.setHeader("Cache-Control", "public, max-age=31536000, immutable");
            } else {
                // 그 외의 파일(index.html 등)은 항상 최신 버전을 확인하도록 캐시를 비활성화합니다.
                resp.setHeader("Cache-Control", "no-cache");
            }

            copy(is, resp.getOutputStream());
        }
    }

    /**
     * index.html 파일을 읽어 Confluence 데코레이터 정보를 주입한 후 응답합니다.
     */
    private static void serveIndexHtml(HttpServletResponse resp, String appBasePath, String decorator)
            throws IOException {
        String resourcePath = appBasePath + "/index.html";
        InputStream resourceStream = ResourceServingHelper.class.getResourceAsStream(resourcePath);

        if (resourceStream == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Application index.html not found at " + resourcePath);
            return;
        }

        // InputStream 내용을 문자열로 읽어옵니다.
        String html;
        try (Scanner scanner = new Scanner(resourceStream, StandardCharsets.UTF_8.name())) {
            html = scanner.useDelimiter("\\A").next();
        }

        // Confluence 데코레이터 메타 태그 주입:
        // Confluence 페이지 레이아웃(헤더, 사이드바 등)을 적용하기 위해 필요한 메타 태그를 <head> 태그 직후에 삽입합니다.
        String decorationMeta = "<meta name=\"decorator\" content=\"" + decorator + "\" />\n";

        if (html.contains("<head>")) {
            html = html.replace("<head>", "<head>\n" + decorationMeta);
        } else {
            // <head> 태그가 없는 경우(거의 없겠지만) 맨 앞에 붙입니다.
            html = decorationMeta + html;
        }

        resp.setContentType("text/html;charset=UTF-8");
        // HTML 파일은 언제든 변경될 수 있으므로 캐시하지 않도록 설정합니다.
        resp.setHeader("Cache-Control", "no-cache");
        resp.getWriter().write(html);
    }

    /**
     * 입력 스트림의 데이터를 출력 스트림으로 복사하는 유틸리티 메서드입니다.
     */
    private static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }
}
