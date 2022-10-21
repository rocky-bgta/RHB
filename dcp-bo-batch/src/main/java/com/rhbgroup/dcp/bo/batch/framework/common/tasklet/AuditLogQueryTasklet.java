package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.AuditLogRepositoryImpl;

@Component
@Lazy
public class AuditLogQueryTasklet implements Tasklet, InitializingBean {
	
    @Autowired
    AuditLogRepositoryImpl auditLogRepositoryImpl;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {    	
         auditLogRepositoryImpl.runAuditLogQuery();
        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Do nothing because this is the implementation of Spring Batch
    }
}
