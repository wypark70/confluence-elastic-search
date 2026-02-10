package com.atsoft.confluence.plugin.elasticsearch.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class PluginConfig {
    private String baseUrl;
    private String apiPath;
    private String apiKey;
}
