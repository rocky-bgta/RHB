package com.rhbgroup.dcpbo.user.config;

import com.rhbgroup.dcpbo.user.annotations.DcpRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
        includeFilters = @ComponentScan.Filter(DcpRepo.class),
        entityManagerFactoryRef = "dcpEntityManager",
        transactionManagerRef = "dcpTransactionManager"
)
@EnableTransactionManagement
public class DcpDatasourceConfig {

    @Autowired
    private DbConfigProperties dbConfigProperties;

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

    @Profile({"default", "dev", "sit","sit2", "uat","uat2", "drl1_02", "drl1_05", "drl2_02", "drl2_05", "trn", "preprod", "dev_pilot", "sit_pilot", "uat_pilot", "prod_02", "prod_05"})
    @Bean(name = "dcpDataSource")
    public DataSource dcpDataSource() throws NamingException {

        JndiDataSourceLookup dataSource = new JndiDataSourceLookup();
        return dataSource.getDataSource(dbConfigProperties.getDcp().getJndiName());

    }

    @Bean(name = "dcpEntityManager")
    public LocalContainerEntityManagerFactoryBean dcpEntityManager(final EntityManagerFactoryBuilder builder,
                                                                   final @Qualifier("dcpDataSource") DataSource dcpDataSource) {
        return builder
                .dataSource(dcpDataSource)
                .packages("com.rhbgroup.dcpbo.user.common.model.dcp")
                .build();
    }

    @Bean(name = "dcpTransactionManager")
    public PlatformTransactionManager dcpTransactionManager(@Qualifier("dcpEntityManager")EntityManagerFactory dcpEntityManager) {
        return new JpaTransactionManager(dcpEntityManager);
    }
}
