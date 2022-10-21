package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PremierCustomerInfoandRMCodeTaggingJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.PremierCustomerInfoandRMCodeTaggingDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.PremierCustomerInfoandRMCodeTaggingRepositoryImpl;

@Component
@Lazy
public class PremierCustomerInfoandRMCodeTaggingJobValidatorTasklet implements Tasklet, InitializingBean {

	private static final Logger logger = Logger.getLogger(PremierCustomerInfoandRMCodeTaggingJobValidatorTasklet.class);
	
	@Autowired
	private PremierCustomerInfoandRMCodeTaggingJobConfigProperties configProperties;

	@Autowired
	private PremierCustomerInfoandRMCodeTaggingRepositoryImpl premierCustomerInfoandRMCodeTaggingRepository;

	private String jobExecutionId="";
	private	String jobName="";

	private String tableBatchSuspense = "TBL_BATCH_SUSPENSE";

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws BatchException {

		jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId().toString();
		jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
		int processedRecordToUserProfile = 0;
		int failProcessedRecordToUserProfile = 0;
		int counter = 0;
		int chunkSizeBlock = 1;
		int chunkSize = configProperties.getChunkSize() * chunkSizeBlock;
		List <PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingDetailToBeUpdatedList= new ArrayList<PremierCustomerInfoandRMCodeTaggingDetail>();

		// Get the number of difference from DCP table
		List <PremierCustomerInfoandRMCodeTaggingDetail> premierNewUpdatedValue = premierCustomerInfoandRMCodeTaggingRepository.getBatchPremierNeworUpdatedValue(jobExecutionId);
		for(PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail : premierNewUpdatedValue){
			BatchSuspense batchSuspense = new BatchSuspense();
			boolean processUpdate = false;

			try {
				batchSuspense.setBatchJobName(jobName);
				batchSuspense.setJobExecutionId(Long.parseLong(jobExecutionId));
				batchSuspense.setCreatedTime(new Date());
				String suspenseRecord = (new StringBuilder()
						.append(premierCustomerInfoandRMCodeTaggingDetail.getCifNo()).append("|")
						.append(premierCustomerInfoandRMCodeTaggingDetail.getFullNm()).append("|")
						.append(premierCustomerInfoandRMCodeTaggingDetail.getIdNo()).append("|")).toString();
				batchSuspense.setSuspenseRecord(suspenseRecord);

				processUpdate = validateUpdate(premierCustomerInfoandRMCodeTaggingDetail);

				if (processUpdate) {
					// Update premier staging if premier flag is not set
					if (premierCustomerInfoandRMCodeTaggingDetail.getOldIsPremier() == null ||
							(premierCustomerInfoandRMCodeTaggingDetail.getOldIsPremier() != null &&
							!premierCustomerInfoandRMCodeTaggingDetail.getOldIsPremier().equalsIgnoreCase("1"))) {
						premierCustomerInfoandRMCodeTaggingDetailToBeUpdatedList.add(premierCustomerInfoandRMCodeTaggingDetail);
						processedRecordToUserProfile++;
					} 
					counter++;
				} else {
					failProcessedRecordToUserProfile++;
					chunkContext.getStepContext().getStepExecution().getJobExecution().setExitStatus(ExitStatus.FAILED);
				}
			} catch(Exception ex){
				logger.error(String.format("Validation of premier detail exception=%s", ex.getMessage()));
				logger.error(ex);
				failProcessedRecordToUserProfile++;
			}
			if (counter >= chunkSize || counter >= (premierNewUpdatedValue.size() - failProcessedRecordToUserProfile)) {
				//perform batch update for user profile
				try {
					premierCustomerInfoandRMCodeTaggingRepository.updateUserProfileBatch(premierCustomerInfoandRMCodeTaggingDetailToBeUpdatedList);
				} catch(Exception ex) {
  					String errorMsg = String.format("Exception: exception=%s",ex.getMessage());
  					logger.error(errorMsg);
  					chunkContext.getStepContext().getStepExecution().getJobExecution().setExitStatus(ExitStatus.FAILED);
  					throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
  				}
				//increase next chunkSizeBlock
				chunkSizeBlock++;
				chunkSize =  configProperties.getChunkSize() * chunkSizeBlock;
				premierCustomerInfoandRMCodeTaggingDetailToBeUpdatedList.clear();
			}
		}
		logger.info(String.format("Successfully process data into TBL_USER_PROFILE = %s", processedRecordToUserProfile));
		logger.info(String.format("Unsuccessfully process data into TBL_USER_PROFILE = %s", failProcessedRecordToUserProfile));

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

		String message = (isLookup) ? "Lookup for column value " + value + " in staging table."+ suspenseColumn + " where group = " + group + " failed" : "Column value \"" + suspenseColumn + "\" should not be null/empty or invalid";
		batchSuspense.setSuspenseMessage(message);

		logger.info(String.format("Insert into: %s,  %s", tableBatchSuspense, premierCustomerInfoandRMCodeTaggingRepository.insertTblBatchSuspense(batchSuspense)));
	}

	// Validate input from interface file and update batch suspense if any failure occured
	public boolean validateUpdate(PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail)
	{
		String logMessage = "";
		boolean processUpdate = true;

		//step 1: Check CIF No null/empty or a valid number
		if (premierCustomerInfoandRMCodeTaggingDetail.getCifNo().isEmpty()) {
			logMessage = String.format("Staging PremierCustomerInfoandRMCodeTaggingDetail job execution id=%s, cif_no is null/empty.", jobExecutionId);
			logger.error(logMessage);
			processUpdate = false;
		} 

		return processUpdate;
	}

	// Update to premier database if different
	public boolean updatePremierStaging(PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail) throws ParseException {
		// Status of the update config
		boolean status = false;

		// Check whether if this is the first to be updated
		int columnToUpdate = 0;

		String setSQLString = " SET ";
		List<String> parameterToUpdate = new ArrayList<>();

		// If previously is not premier then will update
		if (premierCustomerInfoandRMCodeTaggingDetail.getOldIsPremier() != null){
			if (!premierCustomerInfoandRMCodeTaggingDetail.getOldIsPremier()
					.equalsIgnoreCase("1")) {
				setSQLString += " IS_PREMIER = ? ";
				columnToUpdate++;

				String isPremier = "1";
				parameterToUpdate.add(isPremier);
			}
		}
		else{
			setSQLString += " IS_PREMIER = ? ";
			columnToUpdate++;

			String isPremier = "1";
			parameterToUpdate.add(isPremier);
		}

		if(columnToUpdate > 0)
		{
			// Update Set Time and Where set parameter
			parameterToUpdate.add(premierCustomerInfoandRMCodeTaggingDetail.getId());

			// Update the DCP Userprofile
			status = premierCustomerInfoandRMCodeTaggingRepository.updateTableUserProfile(setSQLString, parameterToUpdate);
		}

		// Update status in staging
		if(status)
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
			String formatDateTime = LocalDateTime.now().format(formatter);

			List<String> processToUpdate = new ArrayList<>();
			processToUpdate.add(formatDateTime);
			processToUpdate.add(premierCustomerInfoandRMCodeTaggingDetail.getCifNo());
			processToUpdate.add(jobExecutionId);

			premierCustomerInfoandRMCodeTaggingRepository.updateProcessStatus(processToUpdate);
		}

		return status;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing because this is the implementation of Spring Batch
	}
}