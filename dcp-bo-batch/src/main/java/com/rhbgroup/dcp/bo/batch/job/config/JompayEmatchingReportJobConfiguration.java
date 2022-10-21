package com.rhbgroup.dcp.bo.batch.job.config;


import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.JompayEmatchingReportJobStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.JompayEmatchingReportFtpTasklet;
import org.springframework.batch.core.Step;

@Component
@Lazy
public class JompayEmatchingReportJobConfiguration extends BaseDBToFileJobConfiguration {

	static final Logger logger = Logger.getLogger(JompayEmatchingReportJobConfiguration.class);
	private static final String JOB_NAME = "JompayEmatchingReportJob";
	
	@Autowired
	private JompayEmatchingReportJobStepBuilder jompayStepBuilder;

	@Autowired 
	private JompayEmatchingReportFtpTasklet jompayFtpUploadTasklet;
	
	@Bean(JOB_NAME)
	public Job BuildJob() {
		logger.info("JompayEmatchingReportJobConfiguration building job");
		Job job = getDefaultJobBuilder(JOB_NAME)
				.next(this.jompayStepBuilder.buildStep())
				.next(uploadFileToFtp())
				.build();
		return job;
	}
	
	private Step uploadFileToFtp() {
		return getStepBuilderFactory().get("JompayEmatchingReportFtpUpload")
				.tasklet(this.jompayFtpUploadTasklet)
				.listener(this.batchJobCommonStepListener)
				.build();
	}
}
