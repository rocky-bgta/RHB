package com.rhbgroup.dcpbo.customer.config;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
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
        excludeFilters = @ComponentScan.Filter(BoRepo.class),
        entityManagerFactoryRef = "dcpEntityManager",
        transactionManagerRef = "dcpTransactionManager"
)
@EnableTransactionManagement
public class DcpDatasourceConfig {

    @Autowired
    DbConfigProperties dbConfigProperties;

    @Primary
    @Profile({"local", "test"})
    @Bean(name = "dcpDataSource")
    public DataSource dcpLocalDataSource() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dbConfigProperties.getDriverClassName());
        dataSource.setUrl(dbConfigProperties.getDcp().getUrl());
        dataSource.setUsername(dbConfigProperties.getDcp().getUsername());
        dataSource.setPassword(dbConfigProperties.getDcp().getPassword());

        return dataSource;
    }

    @Primary
    @Profile({"default", "dev", "sit", "sit2", "uat", "trn", "preprod", "sit_pilot", "uat_pilot", "dev_pilot", "prod_02", "prod_05", "uat2"})
    @Bean(name = "dcpDataSource")
    public DataSource dcpDataSource() throws NamingException {

        JndiDataSourceLookup dataSource = new JndiDataSourceLookup();
        return dataSource.getDataSource(dbConfigProperties.getDcp().getJndiName());

    }

    @Primary
    @Bean(name = "dcpEntityManager")
    public LocalContainerEntityManagerFactoryBean dcpEntityManager(final EntityManagerFactoryBuilder builder,
                                                                   final @Qualifier("dcpDataSource") DataSource dcpDataSource) {
        return builder
                .dataSource(dcpDataSource)
                .packages("com.rhbgroup.dcpbo.customer.model")
                .build();
    }

    @Primary
    @Bean(name = "dcpTransactionManager")
    public PlatformTransactionManager dcpTransactionManager(@Qualifier("dcpEntityManager")EntityManagerFactory dcpEntityManager) {
        return new JpaTransactionManager(dcpEntityManager);
    }
}

