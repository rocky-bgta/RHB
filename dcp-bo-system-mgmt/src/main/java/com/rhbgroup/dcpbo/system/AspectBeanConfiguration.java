package com.rhbgroup.dcpbo.system;

import com.rhbgroup.dcpbo.system.exception.EpullEnrollmentControllerExceptionAdvice;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.audit.AuditAdditionalDataFactory;
import com.rhbgroup.dcpbo.common.audit.BoAuditLogQueue;
import com.rhbgroup.dcpbo.common.audit.BoControllerAuditAdvice;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;


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

    @Bean("commonExceptionAdvice")
    public CommonExceptionAdvice getCommonExceptionAdvice() {
        return new CommonExceptionAdvice();
    }
    
    @Bean("downtimeExceptionAdvice")
    public DowntimeExceptionAdvice getDowntimeExceptionAdvice() {
        return new DowntimeExceptionAdvice();
    }

    @Bean("additionalDataHolder")
    public AdditionalDataHolder getAdditionalDataHolder() {
        return new AdditionalDataHolder();
    }

    @Bean("epullEnrollmentControllerExceptionAdvice")
    public EpullEnrollmentControllerExceptionAdvice getEpullEnrollmentControllerExceptionAdvice() {
        return new EpullEnrollmentControllerExceptionAdvice();
    }

}
