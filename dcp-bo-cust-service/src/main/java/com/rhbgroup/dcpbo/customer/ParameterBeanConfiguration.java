package com.rhbgroup.dcpbo.customer;

import io.ebean.config.ServerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


@Configuration
public class ParameterBeanConfiguration {

    @Profile({"default", "dev", "sit", "sit2", "uat", "trn", "drl1_02", "drl1_05", "drl2_02", "drl2_05", "prod_02", "prod_05", "preprod", "sit_pilot", "uat_pilot", "dev_pilot", "uat2"})
    @Bean("eBeanServerConfig")
    public ServerConfig getEBeanServerConfig() {
        ServerConfig config = new ServerConfig();
        config.setName("db");
        config.setDataSourceJndiName("java:/dcp");
        config.setDefaultServer(true);
        config.setAllQuotedIdentifiers(true);
        config.setAutoCommitMode(false);
        config.setDatabasePlatformName("sqlserver16");

        List<String> packages = new ArrayList<String>();
        packages.add("com.rhbgroup.dcp.data.entity");

        config.setPackages(packages);
        return config;
    }
}