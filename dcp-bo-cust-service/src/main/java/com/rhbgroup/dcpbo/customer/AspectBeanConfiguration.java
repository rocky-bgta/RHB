package com.rhbgroup.dcpbo.customer;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.audit.collector.AuditAdditionalDataFactory;
import com.rhbgroup.dcpbo.customer.audit.collector.BoAuditLogQueue;
import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAuditAdvice;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

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

    @Bean("additionalDataHolder")
    public AdditionalDataHolder getAdditionalDataHolder() {
        return new AdditionalDataHolder();
    }
}
