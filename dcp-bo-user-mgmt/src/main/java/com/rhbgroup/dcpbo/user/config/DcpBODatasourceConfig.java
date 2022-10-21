package com.rhbgroup.dcpbo.user.config;

import com.rhbgroup.dcpbo.user.annotations.DcpRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.rhbgroup.dcpbo.user",
        excludeFilters = @ComponentScan.Filter(DcpRepo.class),
        entityManagerFactoryRef = "dcpboEntityManager",
        transactionManagerRef = "dcpboTransactionManager"
)
@EnableTransactionManagement
public class DcpBODatasourceConfig {

    @Autowired
    private DbConfigProperties dbConfigProperties;

    @Primary
    @Profile({"local", "test"})
    @Bean(name = "boDataSource")
    public DataSource boLocalDataSource() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dbConfigProperties.getDriverClassName());
        dataSource.setUrl(dbConfigProperties.getDcpbo().getUrl());
        dataSource.setUsername(dbConfigProperties.getDcpbo().getUsername());
        dataSource.setPassword(dbConfigProperties.getDcpbo().getPassword());

        return dataSource;
    }

    @Primary
    @Profile({"default", "dev", "sit","sit2", "uat","uat2", "drl1_02", "drl1_05", "drl2_02", "drl2_05", "trn", "preprod", "dev_pilot", "sit_pilot", "uat_pilot", "prod_02", "prod_05"})
    @Bean(name = "boDataSource")
    public DataSource boDataSource() throws NamingException {

        JndiDataSourceLookup dataSource = new JndiDataSourceLookup();
        return dataSource.getDataSource(dbConfigProperties.getDcpbo().getJndiName());

    }


    @Primary
    @Bean(name = "dcpboEntityManager")
    public LocalContainerEntityManagerFactoryBean dcpboEntityManager(final EntityManagerFactoryBuilder builder,
                                                                     final @Qualifier("boDataSource") DataSource boDataSource) {
        return builder
                .dataSource(boDataSource)
                .packages("com.rhbgroup.dcpbo.user.common.model.bo",
                        "com.rhbgroup.dcpbo.user.function.model.bo",
                        "com.rhbgroup.dcpbo.user.info.model.bo")
                .build();
    }

    @Primary
    @Bean(name = "dcpboTransactionManager")
    public PlatformTransactionManager dcpboTransactionManager(@Qualifier("dcpboEntityManager")EntityManagerFactory dcpboEntityManager) {
        return new JpaTransactionManager(dcpboEntityManager);
    }
}