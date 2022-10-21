package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("ftpibkprepaid")
@EnableConfigurationProperties
@Getter
@Setter
public class FTPIBKPrepaidConfigProperties extends FTPConfigProperties {
}
