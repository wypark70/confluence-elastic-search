package com.atsoft.confluence.plugin.elasticsearch.servlet;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atsoft.confluence.plugin.elasticsearch.helper.ResourceServingHelper;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminAppServlet extends HttpServlet {
    private final UserManager userManager;

    @Inject
    public AdminAppServlet(@ComponentImport UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        ResourceServingHelper.serveResource(req, resp, "/admin-app", "atl.admin");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private boolean isAdmin(HttpServletRequest request) {
        UserProfile userProfile = userManager.getRemoteUser(request);
        if (userProfile == null) {
            return false;
        }
        return userManager.isSystemAdmin(userProfile.getUserKey());
    }
}