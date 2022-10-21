package com.rhbgroup.dcp.bo.batch.job.config;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.ExternalInterfacesCheckTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.ExternalInterfacesCheckWriteDBTasklet;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Lazy
@EnableTransactionManagement
public class ExternalInterfacesCheckJobConfiguration extends BaseJobConfiguration {

    private static final Logger logger = Logger.getLogger(ExternalInterfacesCheckJobConfiguration.class);
    private static final String JOB_NAME = "ExternalInterfacesCheckJob";
    
	@Autowired
	private ExternalInterfacesCheckTasklet checkExternalInterfacesTasklet;

	@Autowired
	private ExternalInterfacesCheckWriteDBTasklet checkExternalInterfacesWriteDBTasklet;
	
	protected Step checkExternalInterfacesWriteDBTasklet() {
		return getStepBuilderFactory().get("checkExternalInterfacesWriteDBTasklet")
                .tasklet(this.checkExternalInterfacesWriteDBTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}

	protected Step checkExternalInterfacesTasklet() {
		return getStepBuilderFactory().get("checkExternalInterfacesTasklet")
				.tasklet(this.checkExternalInterfacesTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}
    
    @Bean(name = JOB_NAME)
    public Job buildJob() {
		SimpleJobBuilder job = getDefaultJobBuilder(JOB_NAME);
		job.next(checkExternalInterfacesWriteDBTasklet());
		job.next(checkExternalInterfacesTasklet());
      	logger.info(String.format("[%s] job build successfully", JOB_NAME));
		return job.build();
    }
}
