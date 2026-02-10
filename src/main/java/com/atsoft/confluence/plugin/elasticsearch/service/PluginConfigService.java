package com.atsoft.confluence.plugin.elasticsearch.service;

import com.atsoft.confluence.plugin.elasticsearch.model.PluginConfig;

public interface PluginConfigService {
    PluginConfig getPluginConfig();

    PluginConfig setPluginConfig(PluginConfig pluginConfig);
}
