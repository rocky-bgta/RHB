package com.rhbgroup.dcpbo.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.audit.AuditAdditionalDataFactory;
import com.rhbgroup.dcpbo.common.audit.AuditAdditionalDataRetriever;
import com.rhbgroup.dcpbo.common.audit.BoAuditLogQueue;
import com.rhbgroup.dcpbo.common.audit.BoControllerAuditAdvice;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.common.exception.ConfigErrorInterface;
import com.rhbgroup.dcpbo.user.common.BOAuditAdditionalDataRetriever;

import feign.Feign;


/**
 * Cross cutting concern need to be park here.
 * Anything which related to app but doesn't bring benefit to the domain problem we tried to solve.
 * Example: logging, exception, audit and etc.
 * @author faisal
 */
@Configuration
@EnableAspectJAutoProxy
public class AspectBeanConfiguration {


    @Bean("boControllerAuditAspect")
    public BoControllerAuditAdvice getBoControllerAuditAspect(@Qualifier("boAuditLogger") BoAuditLogQueue boAuditLogger,
                                                             AuditAdditionalDataFactory auditAdditionalDataFactory) {
        return new BoControllerAuditAdvice(boAuditLogger, auditAdditionalDataFactory);
    }

    /*@Bean("searchCustomerExceptionAdvice")
    public SearchCustomerExceptionAdvice getSearchCustomerExceptionAdvice() {
        return new SearchCustomerExceptionAdvice();
    }

    @Bean("boLoggingAspect")
    public BoLoggingAdvice getBoLoggingAspect() {
        return new BoLoggingAdvice();
    }*/



    @Bean("commonExceptionAdvice")
    public CommonExceptionAdvice getCommonExceptionAdvice() {
        return new CommonExceptionAdvice();
    }

    @Bean("additionalDataHolder")
    public AdditionalDataHolder getAdditionalDataHolder() {
        return new AdditionalDataHolder();
    }

}
