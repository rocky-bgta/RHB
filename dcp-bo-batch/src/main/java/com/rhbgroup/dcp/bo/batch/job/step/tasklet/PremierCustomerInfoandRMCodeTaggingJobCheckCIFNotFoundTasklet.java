package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.PremierCustomerInfoandRMCodeTaggingDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.PremierCustomerInfoandRMCodeTaggingRepositoryImpl;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;

@Component
@Lazy
public class PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet implements Tasklet, InitializingBean {

	private static final Logger logger = Logger.getLogger(PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet.class);

	@Autowired
	private PremierCustomerInfoandRMCodeTaggingRepositoryImpl premierCustomerInfoandRMCodeTaggingRepository;

	private String jobExecutionId="";
	private	String jobName="";

	private static String SUSPENSE_TYPE_WARN="WARNING";

	private String tableBatchSuspense = "TBL_BATCH_SUSPENSE";

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {

		jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId().toString();
		jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);

		// Get the number of difference from DCP table
		List <PremierCustomerInfoandRMCodeTaggingDetail> cifNotFoundRecordList = premierCustomerInfoandRMCodeTaggingRepository.getCIFNotFoundRecord(jobExecutionId);
		for(PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail : cifNotFoundRecordList){
			BatchSuspense batchSuspense = new BatchSuspense();

			try {
				batchSuspense.setBatchJobName(jobName);
				batchSuspense.setJobExecutionId(Long.parseLong(jobExecutionId));
				batchSuspense.setCreatedTime(new Date());
				String suspenseRecord = (new StringBuilder()
						.append(premierCustomerInfoandRMCodeTaggingDetail.getCifNo()).append("|"))
						.toString();
				batchSuspense.setSuspenseRecord(suspenseRecord);
				String logMessage = String.format("Staging PremierCustomerInfoandRMCodeTaggingDetail job execution id=%s, cif_no is not found in TBL_USER_PROFILE.", jobExecutionId);
				String suspenseColumn = "cif_no";

				logInsertSuspsenseData(batchSuspense, logMessage, suspenseColumn, SUSPENSE_TYPE_WARN,premierCustomerInfoandRMCodeTaggingDetail.getCifNo(), "", false);
			}catch(Exception ex){
				logger.info(String.format("Validation of premier detail exception=%s", ex.getMessage()));
				logger.error(ex);
				continue;
			}
		}

		logger.info(String.format("Total record inserted into suspense due to CIF not found in TBL_USER_PROFILE = %s", cifNotFoundRecordList.size()));

		return RepeatStatus.FINISHED;
	}

	// Log error message and insert into suspense log
	public void logInsertSuspsenseData (BatchSuspense batchSuspense, String logMessage, String suspenseColumn, String suspenseType, String value, String group, boolean isLookup)
	{
		// Log error to logger
		logger.info(logMessage);

		// Batch suspense set
		batchSuspense.setSuspenseColumn(suspenseColumn);
		batchSuspense.setSuspenseType(suspenseType);

		String message = (isLookup) ? "Lookup for column value " + value + " in staging table."+ suspenseColumn + " where group = " + group + " failed" : "Column \"" + suspenseColumn + "\" with value \"" + value + "\" is not in TBL_USER_PROFILE";
		batchSuspense.setSuspenseMessage(message);

		logger.info(String.format("Insert into: %s success insert into suspense: %s", tableBatchSuspense, premierCustomerInfoandRMCodeTaggingRepository.insertTblBatchSuspense(batchSuspense)));
	}

	@Override
	public void afterPropertiesSet() {
		// Do nothing because this is the implementation of Spring Batch
	}
}