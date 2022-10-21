package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.step.IBGRejectStatusGetFileStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.IBGRejectStatusLoadFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.IBGRejectStatusMoveFileStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.IBGRejectStatusValidatorStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.IBGRejectStatusCheckJobStatus;

@Configuration
@Lazy
public class UpdateIBGRejectStatusJobConfiguration extends BaseFileToDBJobConfiguration{
	
	private static final Logger logger = Logger.getLogger(UpdateIBGRejectStatusJobConfiguration.class);
	
	private static final String JOB_NAME = "UpdateIBGRejectedStatusJob";
	
	@Autowired
	private DcpBatchApplicationContext dcpBatchApplicationContext;
	
	@Autowired
	private IBGRejectStatusGetFileStepBuilder getIBGRejectFileStep;
	
	@Autowired
	private IBGRejectStatusLoadFileToStagingStepBuilder loadFileToStagingStep;
	
	@Autowired 
	private IBGRejectStatusValidatorStepBuilder ibgRejectStatusValidator;
	
	@Autowired
	private IBGRejectStatusMoveFileStepBuilder ibgRejectStatusMoveFile;
	
	@Autowired
	private IBGRejectStatusCheckJobStatus ibgRejectCheckJobStatus;
	
	@Bean(JOB_NAME)
	public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		SimpleJobBuilder job = getDefaultJobBuilder(JOB_NAME);
		Map<String,String> initialJobArgs = dcpBatchApplicationContext.getInitialJobArguments();
		String jobexecutionid="";
		if(null !=initialJobArgs && initialJobArgs.containsKey(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY)) {
			jobexecutionid = initialJobArgs.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
		}
		if( StringUtils.isEmpty(jobexecutionid) ) {
			logger.info(String.format ("%s Job id is not set in job parameter, download FTP file and load into staging table", JOB_NAME));
			job.next(this.getIBGRejectFileStep.buildStep());
			job.next(this.loadFileToStagingStep.buildStep());
		}
		job.next(this.ibgRejectStatusValidator.buildStep());

		if(StringUtils.isEmpty(jobexecutionid)) {
			logger.info(String.format ("%s Job id is not set in job parameter, remove file after processing", JOB_NAME));
			job.next(this.ibgRejectStatusMoveFile.buildStep());
		}
		job.next(checkJobStatusStep());
		return job.build();
	}
	
	private Step checkJobStatusStep() {
		return getStepBuilderFactory().get("checkIBGRejectStatusJob")
				.tasklet(this.ibgRejectCheckJobStatus)
				.listener(this.batchJobCommonStepListener).build();
	}
	
}
