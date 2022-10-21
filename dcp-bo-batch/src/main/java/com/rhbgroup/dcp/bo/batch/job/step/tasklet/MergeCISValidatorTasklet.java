package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedMergeCISRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UserProfileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.enums.SuspenseType;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedMergeCISDetailTxn;

@Component
@Lazy
public class MergeCISValidatorTasklet implements Tasklet {
	private static final Logger logger = Logger.getLogger(MergeCISValidatorTasklet.class);

	@Autowired
	private BatchStagedMergeCISRepositoryImpl batchStagedMergeCISRepositoryImpl;

	@Autowired
	private UserProfileRepositoryImpl userProfileRepositoryImpl;

	String jobExecutionId;
	String jobname;
	String logMsg;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		logger.info("Tasklet processing Merge CIS number");
		jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
		if(!StringUtils.isEmpty(jobExecutionId)){
			logMsg = String.format("Rerun task with jobExecutionId=%s", jobExecutionId);
			logger.info(logMsg);
		}else {
			Long id = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
			jobExecutionId = id.toString();
			logMsg = String.format("Job context jobExecutionId=%s", jobExecutionId);
			logger.info(logMsg);
		}
		jobname = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
		List<BatchStagedMergeCISDetailTxn> cisDetailTxnList = batchStagedMergeCISRepositoryImpl.getUnproccessedStagedCIS(jobExecutionId);
		for (BatchStagedMergeCISDetailTxn cisDetailTnx : cisDetailTxnList) {
			try {
				logMsg = String.format("Processing Merge CIS item jobexecutionid=%s, cisNo=%s, newCISNo=%s, processingDate=%s",
						cisDetailTnx.getJobExecutionId(), cisDetailTnx.getCisNo(), cisDetailTnx.getNewCISNo(), cisDetailTnx.getProcessingDate());
				logger.info(logMsg);
				String cisNo = cisDetailTnx.getCisNo();
				String newCISNo = cisDetailTnx.getNewCISNo();
				String processingDate = cisDetailTnx.getProcessingDate();
				String auditDate = DateUtils.formatDateString(new Date(), "ddMMyyyy") ;
				logMsg = String.format("Validate CIS detail record CIS No=%s, new CIS No=%s, processing date=%s", cisNo, newCISNo, processingDate);
				logger.info(logMsg);
				if(StringUtils.isBlank(cisNo)) {
					String suspenseMessage = "CIS number is null or empty";
					logger.info(insertBatchSuspenseLog(cisNo, newCISNo, processingDate, suspenseMessage));
					updateCompleteBatchStagedMergeCIS(cisDetailTnx);
					continue;
				}
				if (softDeleteUserCISNo(cisNo) == 0) {
					String suspenseMessage = "CIS number not found from TBL_USER_PROFILE";
					logger.info(insertBatchSuspenseLog(cisNo, newCISNo, processingDate, suspenseMessage));
				}else {
					logMsg = String.format("CIS ID %s of this customer is no longer in use as of %s", cisNo, auditDate);
					logger.info(logMsg);
				}
				updateCompleteBatchStagedMergeCIS(cisDetailTnx);
			}catch(Exception ex) {
				String suspenseMessage = String.format("Skip to next item. Exception while processing Merge CIS item %s", ex.getMessage());
				logger.info(insertBatchSuspenseLog(cisDetailTnx.getCisNo(), cisDetailTnx.getNewCISNo(), cisDetailTnx.getProcessingDate(), 
						suspenseMessage));
				continue;
			}
		}
		return RepeatStatus.FINISHED;
	}

	private int updateCompleteBatchStagedMergeCIS(BatchStagedMergeCISDetailTxn mergeCISDetailTnx) {
		int processed=1;
		logMsg=String.format("Update Batch Staged Merge CIS is_processed=%s",processed);
		logger.info(logMsg);
		return batchStagedMergeCISRepositoryImpl.updateProcessStatus(mergeCISDetailTnx, processed);
	}

	private int softDeleteUserCISNo(String cisNo) {
		String INACTIVE="I";
		logMsg = String.format("Soft delete user profile cis no=%s, user status=%s", cisNo, INACTIVE);
		logger.info(logMsg);
		return userProfileRepositoryImpl.updateUserStatusCISNo(cisNo, INACTIVE);
	}
	
	private String insertBatchSuspenseLog(String cisNo, String newCISNo, String processingDate, String suspenseMessage) {
		logMsg = String.format("cisNo=%s, newCISNo=%s, processingDate=%s, jobExecutionId=%s, suspenseType=%s, suspenseMessage=%s", 
				cisNo, newCISNo, processingDate, jobExecutionId, SuspenseType.ERROR.name(), suspenseMessage);
		return logMsg;
	}

}
