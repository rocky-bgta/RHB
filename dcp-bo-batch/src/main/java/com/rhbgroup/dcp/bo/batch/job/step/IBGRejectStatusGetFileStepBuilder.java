package com.rhbgroup.dcp.bo.batch.job.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.IBGRejectStatusGetFileTasklet;

@Component
@Lazy
public class IBGRejectStatusGetFileStepBuilder extends BaseStepBuilder{
	private static final Logger logger = Logger.getLogger(IBGRejectStatusGetFileStepBuilder.class);
	@Autowired
	private IBGRejectStatusGetFileTasklet getIBGRejectFileTasklet;
	
	private static String STEP_NAME="IBGRejectStatusGetFileStep";
	
	@Bean("IBGRejectStatusGetFileStep")
	public Step buildStep() {
		logger.info(String.format("Build step %s", STEP_NAME));
		Step downloadFileFromFtp = getDefaultStepBuilder(STEP_NAME)
				.tasklet(this.getIBGRejectFileTasklet)
				.build();
		return downloadFileFromFtp;
	}


}
