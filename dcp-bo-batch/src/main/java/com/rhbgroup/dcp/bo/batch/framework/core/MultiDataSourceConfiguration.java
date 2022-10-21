package com.rhbgroup.dcp.bo.batch.framework.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Lazy
@Configuration
public class MultiDataSourceConfiguration {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource-dcp.url}")
    private String urlDCP;
    @Value("${spring.datasource-dcp.username}")
    private String usernameDCP;
    @Value("${spring.datasource-dcp.password}")
    private String passwordDCP;
    @Value("${spring.datasource-dcp.driver-class-name}")
    private String driverClassNameDCP;
	
	/* TECH-205 : Change to new datasource (dcparchive) for LDCPM4318F - Monthly SMS OTP Notification Count job */
    @Value("${spring.datasource-dcparchive.url}")
    private String urlDCPArchive;
    @Value("${spring.datasource-dcparchive.username}")
    private String usernameDCPArchive;
    @Value("${spring.datasource-dcparchive.password}")
    private String passwordDCPArchive;
    @Value("${spring.datasource-dcparchive.driver-class-name}")
    private String driverClassNameDCPArchive;

    @Bean
    @Primary
    protected DataSource dataSource(){
        return  DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    @Bean(name = "dataSourceDCP")
    protected DataSource dataSourceDCP(){
        return  DataSourceBuilder.create()
                .url(urlDCP)
                .driverClassName(driverClassNameDCP)
                .username(usernameDCP)
                .password(passwordDCP)
                .build();
    }
	
	/* TECH-205 : Change to new datasource (dcparchive) for LDCPM4318F - Monthly SMS OTP Notification Count job */
    @Bean(name = "dataSourceDCPArchive")
    protected DataSource dataSourceDCPArchive(){
        return  DataSourceBuilder.create()
                .url(urlDCPArchive)
                .driverClassName(driverClassNameDCPArchive)
                .username(usernameDCPArchive)
                .password(passwordDCPArchive)
                .build();
    }
}