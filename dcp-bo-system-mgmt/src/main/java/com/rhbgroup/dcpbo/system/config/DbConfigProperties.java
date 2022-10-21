package com.rhbgroup.dcpbo.system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@Getter
@Setter
public class DbConfigProperties {

    private final Dcp dcp = new Dcp();
    private final Dcpbo dcpbo = new Dcpbo();

    private String driverClassName;

    @Getter
    @Setter
    static class Dcp{
        private String url;
        private String username;
        private String password;
        private String jndiName;
    }

    @Getter
    @Setter
    static class Dcpbo{
        private String url;
        private String username;
        private String password;
        private String jndiName;
    }
}
