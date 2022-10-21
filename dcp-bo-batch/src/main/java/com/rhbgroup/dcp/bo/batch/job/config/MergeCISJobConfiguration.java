package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.step.MergeCISGetFileStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.MergeCISLoadFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.MergeCISValidatorStepBuilder;

@Component
@Lazy
public class MergeCISJobConfiguration extends BaseFileToDBJobConfiguration {

	private static final Logger logger = Logger.getLogger(MergeCISJobConfiguration.class);
	private static final String JOB_NAME = "MergeCISJob";
	
	@Autowired
	private DcpBatchApplicationContext dcpBatchApplicationContext;
	
	@Autowired
	private MergeCISGetFileStepBuilder mergeCISGetFileStep;
	
	@Autowired
	private MergeCISLoadFileToStagingStepBuilder mergeCISLoadFileToStagingStep;
	
	@Autowired
	private MergeCISValidatorStepBuilder mergeCISValidatorStep;
	
	@Bean(JOB_NAME)
	public Job buildJob() {
		String logMsg = String.format("Building job=%s",JOB_NAME);
		logger.info(logMsg);
		String jobexecutionid="";
		SimpleJobBuilder job = getDefaultJobBuilder(JOB_NAME);
		Map<String, String> initialArgs = dcpBatchApplicationContext.getInitialJobArguments();
		if(null != initialArgs && initialArgs.containsKey(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY)) {
			jobexecutionid = initialArgs.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
		}
		
		if(!StringUtils.isEmpty(jobexecutionid)) {			
			logMsg = String.format("Rerun job with jobexecutionid=%s",jobexecutionid);
			logger.info(logMsg);
		}else {
			logger.info("Starting new job");
			job.next(mergeCISGetFileStep.buildStep());
			job.next(mergeCISLoadFileToStagingStep.buildStep());
		}
		job.next(this.mergeCISValidatorStep.buildStep());
		return job.build();
	}

}

