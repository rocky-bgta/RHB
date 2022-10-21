package com.rhbgroup.dcpbo.customer.config;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
@EnableJpaRepositories(basePackages = "com.rhbgroup.dcpbo.customer.repository",
        includeFilters = @ComponentScan.Filter(BoRepo.class),
        entityManagerFactoryRef = "dcpboEntityManager",
        transactionManagerRef = "dcpboTransactionManager"
)
@EnableTransactionManagement
public class DcpBODatasourceConfig {

    @Autowired
    DbConfigProperties dbConfigProperties;

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

    @Profile({"default", "dev", "sit", "sit2", "uat", "trn", "preprod", "sit_pilot", "uat_pilot", "dev_pilot", "prod_02", "prod_05", "uat2"})
    @Bean(name = "boDataSource")
    public DataSource boDataSource() throws NamingException {

        JndiDataSourceLookup dataSource = new JndiDataSourceLookup();
        return dataSource.getDataSource(dbConfigProperties.getDcpbo().getJndiName());

    }

    @Bean(name = "dcpboEntityManager")
    public LocalContainerEntityManagerFactoryBean dcpboEntityManager(final EntityManagerFactoryBuilder builder,
                                                                     final @Qualifier("boDataSource") DataSource boDataSource) {
        return builder
                .dataSource(boDataSource)
                .packages("com.rhbgroup.dcpbo.customer.dcpbo")
                .build();
    }

    @Bean(name = "dcpboTransactionManager")
    public PlatformTransactionManager dcpboTransactionManager(@Qualifier("dcpboEntityManager")EntityManagerFactory dcpboEntityManager) {
        return new JpaTransactionManager(dcpboEntityManager);
    }
}
