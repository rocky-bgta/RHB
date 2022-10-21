package com.rhbgroup.dcp.bo.batch.job.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseRunReportJobConfiguration;

@Configuration
@Lazy
public class SampleRunReportJobConfiguration extends BaseRunReportJobConfiguration {


	@Bean(name = "SampleRunReportJob")
	public Job BuildJob() {
		SimpleJobBuilder jobBuilder = getDefaultRunReportJobBuilder("SampleRunReportJob")
				.next(runReport());
		return jobBuilder.build();
	}
}
