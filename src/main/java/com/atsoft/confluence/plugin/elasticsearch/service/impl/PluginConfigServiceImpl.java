package com.atsoft.confluence.plugin.elasticsearch.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atsoft.confluence.plugin.elasticsearch.model.PluginConfig;
import com.atsoft.confluence.plugin.elasticsearch.service.PluginConfigService;

@Service
public class PluginConfigServiceImpl implements PluginConfigService {
    private static final String PLUGIN_SETTINGS_KEY = PluginConfigService.class.getName();
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;

    @Inject
    public PluginConfigServiceImpl(@ComponentImport PluginSettingsFactory pluginSettingsFactory,
            @ComponentImport TransactionTemplate transactionTemplate) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public PluginConfig getPluginConfig() {
        return transactionTemplate.execute(() -> {
            PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
            PluginConfig pluginConfig = PluginConfig.builder()
                    .baseUrl((String) pluginSettings.get(PLUGIN_SETTINGS_KEY + ".baseUrl"))
                    .apiPath((String) pluginSettings.get(PLUGIN_SETTINGS_KEY + ".apiPath"))
                    .apiKey((String) pluginSettings.get(PLUGIN_SETTINGS_KEY + ".apiKey"))
                    .build();
            return pluginConfig;
        });
    }

    @Override
    public PluginConfig setPluginConfig(PluginConfig pluginConfig) {
        return transactionTemplate.execute(() -> {
            PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
            pluginSettings.put(PLUGIN_SETTINGS_KEY + ".baseUrl", pluginConfig.getBaseUrl());
            pluginSettings.put(PLUGIN_SETTINGS_KEY + ".apiPath", pluginConfig.getApiPath());
            pluginSettings.put(PLUGIN_SETTINGS_KEY + ".apiKey", pluginConfig.getApiKey());
            return getPluginConfig();
        });
    }
}
