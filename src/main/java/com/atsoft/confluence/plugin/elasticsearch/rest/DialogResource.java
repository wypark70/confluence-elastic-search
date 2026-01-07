package com.atsoft.confluence.plugin.elasticsearch.rest;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.confluence.util.velocity.VelocityUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/dialog")
public class DialogResource {

    @GET
    @Path("/view")
    @Produces(MediaType.TEXT_HTML)
    @XsrfProtectionExcluded
    public Response getDialogHtml() {
        try {
            Map<String, Object> context = new HashMap<>();
            context.put("title", "동적 다이얼로그");

            String html = VelocityUtils.getRenderedTemplate("templates/dialog-panel.vm", context);

            return Response.ok(html).build();
        } catch (Exception e) {
            return Response.status(500)
                    .entity("Error: " + e.getClass().getName() + " - " + e.getMessage())
                    .build();
        }
    }
}