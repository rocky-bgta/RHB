package com.rhbgroup.dcpbo.system;

import com.rhbgroup.dcpbo.system.config.DcpBODatasourceConfig;
import com.rhbgroup.dcpbo.system.config.DcpDatasourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * Main class and main configuration of this microservice.
 */
@EnableAutoConfiguration
@SpringBootConfiguration
@EnableFeignClients(basePackages = {"com.rhbgroup.dcpbo.common"})
@Import({
	DataBeanConfiguration.class,
	ControllerBeanConfiguration.class,
	ServiceBeanConfiguration.class,
	AspectBeanConfiguration.class,
	CloudBeanConfiguration.class,
	FactoryBeanConfiguration.class,
	IntegrationBeanConfiguration.class,
	ParameterBeanConfiguration.class,
	SwaggerConfiguration.class,
	WebAppConfiguration.class,
    DcpBODatasourceConfig.class,
    DcpDatasourceConfig.class
})
public class BoSystemManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoSystemManagementApplication.class, args);
    }
}
