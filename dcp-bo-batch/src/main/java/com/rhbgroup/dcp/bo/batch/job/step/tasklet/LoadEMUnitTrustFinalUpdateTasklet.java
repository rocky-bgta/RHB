package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_SUCCESS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.TARGET_DATA_SET;

import java.util.Date;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties.UTFile;
import com.rhbgroup.dcp.bo.batch.job.model.AccountBatchInfo;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import com.rhbgroup.dcp.bo.batch.job.repository.AccountBatchInfoRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;

@Lazy
@Component
public class LoadEMUnitTrustFinalUpdateTasklet implements Tasklet {
    static final Logger logger = Logger.getLogger(LoadEMUnitTrustFinalUpdateTasklet.class);

    @Autowired
    LoadEMUnitTrustJobConfigProperties configProperties;
    
    @Autowired
	BatchUnitTrustJobStatusControlRepositoryImpl utJobControlRepoImpl;

    @Autowired
    AccountBatchInfoRepositoryImpl acctBatchInfoRepoImpl;
    
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
		int targetDataset = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(TARGET_DATA_SET);
		logger.info(String.format("Updating Batch UT Job Final Status jobExecutionId=%s, targetDataset=%s", jobExecutionId, targetDataset));
		updateJobStatus(jobExecutionId);
		updateEndAccountBatchInfo(targetDataset);
		return RepeatStatus.FINISHED;
	}
	
	@SneakyThrows
    private void updateEndAccountBatchInfo(int targetDataset) {
    	for(UTFile utFile : configProperties.getUtFiles()) {
    		if(utFile.getStatus()!=BatchSystemConstant.EMUnitTrustParameter.STATUS_SUCCESS) {
    			logger.warn(String.format("Not update Batch Account End Time- Process file unsuccessfully, %s", utFile.getDownloadFilePath()));
    			return;
    		}
    	}
    	AccountBatchInfo batchInfo = new AccountBatchInfo();
    	batchInfo.setEndTime(new Date());
    	batchInfo.setTargetDataset(String.valueOf(targetDataset));
    	batchInfo.setUpdatedTime(new Date());
    	batchInfo.setUpdatedBy( configProperties.getBatchCode() );
    	batchInfo.setAccountType(configProperties.getUtBatchAccountInfoKey());
    	int row= acctBatchInfoRepoImpl.updateUTBatchInfoEnd(batchInfo);
        logger.info(String.format("Update Account Batch Info End row=%s", row));
    }

	@SneakyThrows
	private int updateJobStatus(long jobExecutionId )  {
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl();
		utJobStatusControl.setJobExecutionId(jobExecutionId);
		utJobStatusControl.setStatus(STATUS_SUCCESS);
		utJobStatusControl.setTblUtCustomerStatus(STATUS_SUCCESS);
		utJobStatusControl.setTblUtCustomerRelStatus(STATUS_SUCCESS);
		utJobStatusControl.setTblUtAccountStatus(STATUS_SUCCESS);
		utJobStatusControl.setTblUtAccountHoldingStatus(STATUS_SUCCESS);
		utJobStatusControl.setTblUtFundMasterStatus(STATUS_SUCCESS);
		utJobStatusControl.setBatchEndDatetime(new Date());
		utJobStatusControl.setUpdatedBy(configProperties.getBatchCode());
		utJobStatusControl.setUpdatedTime(new Date());
		return utJobControlRepoImpl.updateCompleteJobStatus(utJobStatusControl);
	}
}
