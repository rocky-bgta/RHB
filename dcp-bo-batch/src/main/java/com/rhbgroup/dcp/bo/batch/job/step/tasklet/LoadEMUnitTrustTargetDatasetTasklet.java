package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import java.util.Date;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.TARGET_DATA_SET;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_INITIAL;

@Component
@Lazy
public class LoadEMUnitTrustTargetDatasetTasklet implements Tasklet {
    static final Logger logger = Logger.getLogger(LoadEMUnitTrustTargetDatasetTasklet.class);
    
    @Autowired
    BatchUnitTrustJobStatusControlRepositoryImpl utJobStatusControlRepoImpl;
    
    @Autowired
    LoadEMUnitTrustJobConfigProperties configProperties;
    
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		try {
			String maxTargetSetStr = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(configProperties.getMaxTargetSetKey());
			int maxTargetSet  = Integer.parseInt(maxTargetSetStr);
			int targetSet = utJobStatusControlRepoImpl.getTargetDataSet();
			long jobExecutionId=chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
			int finalTargetSet = (targetSet % maxTargetSet) + 1;
			logger.info(String.format("Getting target data set maxTargetSet=%s,targetSet=%s, finalTargetSet=%s",maxTargetSet, targetSet, finalTargetSet));
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putInt(TARGET_DATA_SET,finalTargetSet);
			BatchUnitTrustJobStatusControl utJobControl = createUTJobControl();
			utJobControl.setJobExecutionId(jobExecutionId);
			utJobControl.setTargetDataset(finalTargetSet);
			int addRow = utJobStatusControlRepoImpl.addRecord(utJobControl);
			logger.info(String.format("Adding %s row in UT Job Status control, jobExecutionId=%s",addRow, jobExecutionId));
		} catch (Exception ex) {
			logger.error("Exception while getting target set", ex);
			throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, ex );
		}
        return RepeatStatus.FINISHED;
	}
	
	private BatchUnitTrustJobStatusControl createUTJobControl() {
		BatchUnitTrustJobStatusControl utJobControl = new BatchUnitTrustJobStatusControl();
		utJobControl.setBatchProcessDate(new Date());
		utJobControl.setBatchEndDatetime(new Date());
		utJobControl.setStatus(STATUS_INITIAL);
		utJobControl.setCreatedTime(new Date());
		utJobControl.setCreatedBy(configProperties.getBatchCode());
		utJobControl.setUpdatedTime(new Date());
		utJobControl.setUpdatedBy(configProperties.getBatchCode());
		return utJobControl;
	}
}
