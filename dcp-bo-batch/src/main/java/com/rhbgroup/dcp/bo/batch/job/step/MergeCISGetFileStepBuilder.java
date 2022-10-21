package com.rhbgroup.dcp.bo.batch.job.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.MergeCISGetFileTasklet;

@Component
@Lazy
public class MergeCISGetFileStepBuilder extends BaseStepBuilder{
	private static final String STEP_NAME="MergeCISGetFileStep";
	
	@Autowired
	private MergeCISGetFileTasklet mergeCISGetFileTasklet;

	@Override
    @Bean(STEP_NAME)
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME)
				.tasklet(mergeCISGetFileTasklet)
				.build();
	}
}
