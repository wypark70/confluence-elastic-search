package com.atsoft.confluence.plugin.elasticsearch.config;

import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atsoft.confluence.plugin.elasticsearch.service.PluginConfigService;
import com.atsoft.confluence.plugin.elasticsearch.service.impl.PluginConfigServiceImpl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
        ModuleFactoryBean.class,
        PluginAccessorBean.class
})
public class PluginJavaConfig {

    // imports ApplicationProperties from OSGi
    @Bean
    public ApplicationProperties applicationProperties() {
        return importOsgiService(ApplicationProperties.class);
    }

    @Bean
    public PluginSettingsFactory pluginSettingsFactory() {
        return importOsgiService(PluginSettingsFactory.class);
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return importOsgiService(TransactionTemplate.class);
    }

    @Bean
    public PluginConfigService pluginConfigService() {
        return new PluginConfigServiceImpl(pluginSettingsFactory(), transactionTemplate());
    }
}
