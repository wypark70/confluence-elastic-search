package com.atsoft.confluence.plugin.elasticsearch.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ResourceServingHelper {

    private static final int BUFFER_SIZE = 4096;

    public static void serveResource(HttpServletRequest req, HttpServletResponse resp, String appBasePath,
            String decorator) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // Default to index.html for root or empty path
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            serveIndexHtml(resp, appBasePath, decorator);
            return;
        }

        // Try to find the resource in the classpath
        String resourcePath = appBasePath + pathInfo;
        InputStream resourceStream = ResourceServingHelper.class.getResourceAsStream(resourcePath);

        if (resourceStream == null) {
            // SPA Fallback: If resource not found and it looks like a page request (not
            // .js, .css, etc.), serve index.html
            if (!pathInfo.contains(".")) {
                serveIndexHtml(resp, appBasePath, decorator);
                return;
            }

            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Serve the resource
        try (InputStream is = resourceStream) {
            String mimeType = URLConnection.guessContentTypeFromName(resourcePath);
            // Fallback for common types if guessContentTypeFromName fails or is
            // insufficient
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
                    mimeType = "application/octet-stream";
            }

            resp.setContentType(mimeType);

            // Set cache headers for static assets (hashed files can be cached forever)
            if (pathInfo.startsWith("/_app/immutable/")) {
                resp.setHeader("Cache-Control", "public, max-age=31536000, immutable");
            } else {
                resp.setHeader("Cache-Control", "no-cache");
            }

            copy(is, resp.getOutputStream());
        }
    }

    private static void serveIndexHtml(HttpServletResponse resp, String appBasePath, String decorator)
            throws IOException {
        String resourcePath = appBasePath + "/index.html";
        InputStream resourceStream = ResourceServingHelper.class.getResourceAsStream(resourcePath);

        if (resourceStream == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Application index.html not found at " + resourcePath);
            return;
        }

        String html;
        try (Scanner scanner = new Scanner(resourceStream, StandardCharsets.UTF_8.name())) {
            html = scanner.useDelimiter("\\A").next();
        }

        // Inject Confluence Decorator Meta Tag
        // We look for <head> and insert the meta tag right after it
        String decorationMeta = "<meta name=\"decorator\" content=\"" + decorator + "\" />\n";

        if (html.contains("<head>")) {
            html = html.replace("<head>", "<head>\n" + decorationMeta);
        } else {
            // Fallback if no head tag (unlikely for valid HTML)
            html = decorationMeta + html;
        }

        resp.setContentType("text/html;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.getWriter().write(html);
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }
}
