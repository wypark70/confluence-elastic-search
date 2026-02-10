package com.atsoft.confluence.plugin.elasticsearch.rest;

import com.atsoft.confluence.plugin.elasticsearch.service.PluginConfigService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/pluginConfig")
@Slf4j
public class PluginConfigResource {
    private final PluginConfigService pluginConfigService;

    @Inject
    public PluginConfigResource(PluginConfigService pluginConfigService) {
        this.pluginConfigService = pluginConfigService;
    }

    @GET
    @Produces("application/json")
    public Response getPluginConfig() {
        return Response.ok(pluginConfigService.getPluginConfig()).build();
    }
}
