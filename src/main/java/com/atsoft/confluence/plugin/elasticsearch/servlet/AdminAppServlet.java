package com.atsoft.confluence.plugin.elasticsearch.servlet;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminAppServlet extends HttpServlet {
    private final TemplateRenderer templateRenderer;

    @Inject
    public AdminAppServlet(@ComponentImport TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        
        try {
            // Serve admin-app index.html with Confluence admin decoration
            String html = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\" />\n" +
                "    <meta name=\"decorator\" content=\"atl.admin\" />\n" +
                "    $webResourceManager.requireResource(\"com.atsoft.confluence.plugin.elasticsearch:elastic-search:admin-app-resources\")\n" +
                "    <title>Elastic Search - Admin Configuration</title>\n" +
                "</head>\n" +
                "<body class=\"aui-page-focused aui-page-focused-large\">\n" +
                "    <div id=\"app-root\" class=\"admin-app-container\"></div>\n" +
                "</body>\n" +
                "</html>";
            
            resp.getWriter().write(html);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load admin app");
        }
    }
}