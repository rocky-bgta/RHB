package com.rhbgroup.dcpbo.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "workflow.overview")
public class WorkflowConfig {
    private final List<ExclusionItem> exclusions = new ArrayList<>();
}
