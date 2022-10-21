package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchLookup;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.GSTCentralizedFileUpdateDetail;
import com.rhbgroup.dcp.bo.batch.job.model.SuspenseDetailsBuilder;
import com.rhbgroup.dcp.bo.batch.job.repository.GSTCentralizedFileUpdateRepositoryImpl;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.GSTCentralizedFileUpdateParameter.BATCH_GST_CENTRALIZED_FILE_UPDATE_VALIDATING_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExitCode.FAILED;

@Component
@Lazy
public class GSTCentralizedFileUpdateJobValidatorTasklet implements Tasklet, InitializingBean {

	private static final Logger logger = Logger.getLogger(GSTCentralizedFileUpdateJobValidatorTasklet.class);
	private static final String SET_SQL_STRING = "setSQLString";
	private static final String FIRST_SET = "firstSet";
	private static final String COLUMN_TO_UPDATE = "columnToUpdate";


	@Autowired
	private GSTCentralizedFileUpdateRepositoryImpl gstCentralizedFileUpdateRepositoryImpl;

	private String jobExecutionId="";
	private	String jobName="";

	private static String SUSPENSE_TYPE_ERR="ERROR";

	private String tableBatchSuspense = "TBL_BATCH_SUSPENSE";

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {

		Long id = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();

		jobExecutionId = id.toString();
		jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);

		String logMsg = String.format("Validating GSTCentralizedFileUpdateDetail job execution id=%s", jobExecutionId);
		logger.info(logMsg);

		//
		// THIS IS TO INSERT INTO TBL_GST_CONFIG ANY NEW DATA FROM INTERFACE FILE
		//
		List<GSTCentralizedFileUpdateDetail> differenceGSTDetailRecord = gstCentralizedFileUpdateRepositoryImpl.checkGSTConfigNewRecord(jobExecutionId);
		for(GSTCentralizedFileUpdateDetail gstDetailToInsert : differenceGSTDetailRecord)
		{
			try
			{
				int newGSTMaxUniqueId = Integer.parseInt(gstDetailToInsert.getNewGstMaxUniqueId());
				int oldGSTMaxUniqueId = Integer.parseInt(gstDetailToInsert.getOldGstMaxUniqueId());

				// Get the number of times to iterate insert
				int uniqueIdToIterate = newGSTMaxUniqueId - oldGSTMaxUniqueId;
				int uniqueIdCounter = oldGSTMaxUniqueId + 1;

				ItereateAndUpdateGSTCentralizedFile(gstDetailToInsert, newGSTMaxUniqueId, uniqueIdToIterate, uniqueIdCounter);
			}catch(Exception ex) {
				logger.error(ex.getMessage());
			}
		}

		//
		// THIS IS TO VALIDATE STAGING AND UPDATE ANY DATA FROM INTERFACE FILE ON TBL_GST_CONFIG IF ANY DIFFERENCE FOUND
		//

		// Check for item from both arraylist one of staging table and the other one from dcp table
		// Take all the updated file
		List<GSTCentralizedFileUpdateDetail> gstCentralizedFileStagingList = gstCentralizedFileUpdateRepositoryImpl.getUnprocessedGSTCentralizedStatusFromStaging(jobExecutionId);
		List<GSTCentralizedFileUpdateDetail> gstDCPList = gstCentralizedFileUpdateRepositoryImpl.getGSTFromDCP();

		List<GSTCentralizedFileUpdateDetail> gstStagingDCPtoUpdateList = new ArrayList<GSTCentralizedFileUpdateDetail>();
		List<GSTCentralizedFileUpdateDetail> gstOriginalDCPtoUpdateList = new ArrayList<GSTCentralizedFileUpdateDetail>();

		int rowContains = 0;

		// Check any for gst that can be taken for update
		// gstDCPComparison from gstDCPList
		// gstStagingComparison from gstCentralizedFileStagingList
		checkGstThatRequredUpdate(gstCentralizedFileStagingList, gstDCPList, gstStagingDCPtoUpdateList, gstOriginalDCPtoUpdateList, rowContains);

		int counterRow = 0;
		// Data sanity for update and will update the one that passes validation, else will go to batch suspense
		for (GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail : gstStagingDCPtoUpdateList) {
			BatchSuspense batchSuspense = new BatchSuspense();
			boolean processUpdate = false;

			try {
				batchSuspense.setBatchJobName(jobName);
				batchSuspense.setJobExecutionId(Long.parseLong(jobExecutionId));
				batchSuspense.setCreatedTime(new Date());
				String suspenseRecord = (new StringBuilder()
						.append(gstCentralizedFileUpdateDetail.getUniqueId()).append("|")
						.append(gstCentralizedFileUpdateDetail.getSourceSystemId()).append("|")
						.append(gstCentralizedFileUpdateDetail.getTransactionIdentifier()).append("|")
						.append(gstCentralizedFileUpdateDetail.getGstRate()).append("|")
						.append(gstCentralizedFileUpdateDetail.getTreatmentType()).append("|")
						.append(gstCentralizedFileUpdateDetail.getTaxCode()).append("|")
						.append(gstCentralizedFileUpdateDetail.getCalculationMethod()).append("|")
						.append(gstCentralizedFileUpdateDetail.getStartDate()).append("|")
						.append(gstCentralizedFileUpdateDetail.getEndDate())).toString();
				batchSuspense.setSuspenseRecord(suspenseRecord);

				processUpdate = validateUpdate(chunkContext, gstCentralizedFileUpdateDetail, batchSuspense);
				boolean updateStatus = false;

				if(processUpdate){

					// gstOriginalDCPtoUpdateList from gstDCPComparison
					// gstStagingDCPtoUpdateList from gstStagingComparison
					updateStatus = updateGSTConfig (gstCentralizedFileUpdateDetail, gstOriginalDCPtoUpdateList.get(counterRow));
				}else{
					chunkContext.getStepContext().getStepExecution().getJobExecution().setExitStatus(ExitStatus.FAILED);
				}

				if(!processUpdate || !updateStatus)
				{
					chunkContext.getStepContext().getStepExecution().getJobExecution().setExitStatus(ExitStatus.FAILED);
				}
			} catch (Exception ex) {
				logMsg = String.format("Validation of GSTCentralizedFileUpdateDetail exception=%s", ex.getMessage());
				logger.info(logMsg);
				logger.error(ex);
				continue;
			}
			counterRow++;
		}
		return RepeatStatus.FINISHED;
	}

	private void checkGstThatRequredUpdate(List<GSTCentralizedFileUpdateDetail> gstCentralizedFileStagingList, List<GSTCentralizedFileUpdateDetail> gstDCPList, List<GSTCentralizedFileUpdateDetail> gstStagingDCPtoUpdateList, List<GSTCentralizedFileUpdateDetail> gstOriginalDCPtoUpdateList, int rowContains) {
		for(GSTCentralizedFileUpdateDetail gstDCPComparison : gstDCPList){
			for(GSTCentralizedFileUpdateDetail gstStagingComparison : gstCentralizedFileStagingList)
			{
				boolean uniqueIDEquals = gstStagingComparison.getUniqueId().equalsIgnoreCase(gstDCPComparison.getUniqueId());
				boolean sourceSystemEquals = gstStagingComparison.getSourceSystemId().equalsIgnoreCase(gstDCPComparison.getSourceSystem());
				boolean transactionIdentifierEquals = gstStagingComparison.getTransactionIdentifier().equalsIgnoreCase(gstDCPComparison.getTxnIdentifier());

				// Take the staging data to be taken as the latest
				if(uniqueIDEquals && sourceSystemEquals && transactionIdentifierEquals){

					// gstOriginalDCPtoUpdateList from gstDCPComparison
					// gstStagingDCPtoUpdateList from gstStagingComparison
					gstStagingDCPtoUpdateList.add(gstStagingComparison);
					gstOriginalDCPtoUpdateList.add(gstDCPComparison);
					rowContains++;
				}
			}
		}
	}

	private void ItereateAndUpdateGSTCentralizedFile(GSTCentralizedFileUpdateDetail gstDetailToInsert, int newGSTMaxUniqueId, int uniqueIdToIterate, int uniqueIdCounter) throws BatchException {
		for (int j = 0; j < uniqueIdToIterate; j++){
			// Get the row and value from old value
			boolean insertStatus = false;
			int indexToUpdate = 0;
			List<GSTCentralizedFileUpdateDetail> gstOldValueExtraction = gstCentralizedFileUpdateRepositoryImpl.getGSTConfigEssentialData(gstDetailToInsert.getOldGstMaxUniqueId(), gstDetailToInsert.getOldGstSourceSystem(), gstDetailToInsert.getOldGstTxnIdentifier());
			for(int i = 0; i < gstOldValueExtraction.size(); i++){
				// Get the new value from staging
				GSTCentralizedFileUpdateDetail gstNewValueExtracted = gstCentralizedFileUpdateRepositoryImpl.getGSTNewValue(String.valueOf(newGSTMaxUniqueId), gstDetailToInsert.getOldGstSourceSystem(), gstDetailToInsert.getOldGstTxnIdentifier(), jobExecutionId);

				gstOldValueExtraction.get(i).setUniqueId(String.valueOf(uniqueIdCounter));

				// Check if entity code & indicator is empty or not?
				String newEntityCode = (gstNewValueExtracted.getEntityCode().isEmpty()) ? null : gstNewValueExtracted.getEntityCode();
				String newEntityIndicator = (gstNewValueExtracted.getEntityIndicator().isEmpty()) ? null : gstNewValueExtracted.getEntityIndicator();

				// Setting new value from staging to be in insert sentence
				gstOldValueExtraction.get(i).setEntityCode(newEntityCode);
				gstOldValueExtraction.get(i).setEntityIndicator(newEntityIndicator);
				gstOldValueExtraction.get(i).setSourceSystemId(String.valueOf(gstNewValueExtracted.getSourceSystemId()));
				gstOldValueExtraction.get(i).setTransactionIdentifier(gstNewValueExtracted.getTransactionIdentifier());
				gstOldValueExtraction.get(i).setTransactionDescription(gstNewValueExtracted.getTransactionDescription());
				gstOldValueExtraction.get(i).setGstRate(gstNewValueExtracted.getGstRate());
				gstOldValueExtraction.get(i).setTreatmentType(gstNewValueExtracted.getTreatmentType());
				gstOldValueExtraction.get(i).setTaxCode(gstNewValueExtracted.getTaxCode());
				gstOldValueExtraction.get(i).setCalculationMethod(gstNewValueExtracted.getCalculationMethod());
				gstOldValueExtraction.get(i).setGlAccountCodeCharges(gstNewValueExtracted.getGlAccountCodeCharges());
				gstOldValueExtraction.get(i).setStartDate(gstNewValueExtracted.getStartDate());
				gstOldValueExtraction.get(i).setEndDate(gstNewValueExtracted.getEndDate());

				insertStatus = gstCentralizedFileUpdateRepositoryImpl.insertNewGSTToDB(gstOldValueExtraction.get(i));
				indexToUpdate = i;
			}

			if(insertStatus)
			{
				List<String> processToUpdate = new ArrayList<>();
				processToUpdate.add(gstOldValueExtraction.get(indexToUpdate).getUniqueId());
				processToUpdate.add(gstOldValueExtraction.get(indexToUpdate).getSourceSystemId());
				processToUpdate.add(gstOldValueExtraction.get(indexToUpdate).getTransactionIdentifier());
				processToUpdate.add(jobExecutionId);
				gstCentralizedFileUpdateRepositoryImpl.updateProcessStatus(processToUpdate);
			}
			// Increase unique id to iterate
			uniqueIdCounter++;

		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
	}

	// Log error message and insert into suspense log
	public void logInsertSuspsenseData(SuspenseDetailsBuilder suspenseDetailsBuilder)
	{
		// Set default value to suspense
		// Log error to logger
		logger.info(suspenseDetailsBuilder.getLogMessage());

		// Mark record as fail
		suspenseDetailsBuilder.getChunkContext().getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_GST_CENTRALIZED_FILE_UPDATE_VALIDATING_STATUS,FAILED);

		// Batch suspense set
		suspenseDetailsBuilder.getBatchSuspense().setSuspenseColumn(suspenseDetailsBuilder.getSuspenseColumn());
		suspenseDetailsBuilder.getBatchSuspense().setSuspenseType(suspenseDetailsBuilder.getSuspenseType());

		String message = (suspenseDetailsBuilder.isLookUp()) ? "Lookup for column value " + suspenseDetailsBuilder.getValue() + " in table TBL_BATCH_STAGED_GST_UPDATE."+ suspenseDetailsBuilder.getSuspenseColumn() + " where group = " + suspenseDetailsBuilder.getGroup() + " failed" : "Column value \"" + suspenseDetailsBuilder.getSuspenseColumn() + "\" should not be null/empty or invalid";
		suspenseDetailsBuilder.getBatchSuspense().setSuspenseMessage(message);

		logger.info(String.format("Insert into %d record(s) into %s" , gstCentralizedFileUpdateRepositoryImpl.insertTblBatchSuspense(suspenseDetailsBuilder.getBatchSuspense()), tableBatchSuspense));
	}

	// Validate input from interface file and update batch suspense if any failure occured
	public boolean validateUpdate(ChunkContext chunkContext, GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail, BatchSuspense batchSuspense)
	{
		String logMessage = "";
		boolean processUpdate = true;

		//step 1: Check GST Rate is not null/empty or a valid number
		boolean invalidNumberGSTRate = false;
		double gstRateVal = 0;
		
		SuspenseDetailsBuilder suspenseDetailsBuilder = new SuspenseDetailsBuilder().chunkContext(chunkContext).batchSuspense(batchSuspense);
		try{
			gstRateVal = Double.parseDouble(gstCentralizedFileUpdateDetail.getGstRate());
			logger.info(String.format("Staging GSTCentralizedFileUpdateDetail gst rate value =%s ", gstRateVal));
		}catch (Exception err)
		{
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, GST Rate is invalid number.", jobExecutionId);
			logger.error(logMessage);
			invalidNumberGSTRate = true;
		}
		processUpdate = isProcessUpdate(gstCentralizedFileUpdateDetail, batchSuspense, processUpdate, invalidNumberGSTRate, suspenseDetailsBuilder);
		//step 4: check on calcualtion method
		if (null == gstCentralizedFileUpdateDetail.getCalculationMethod() || gstCentralizedFileUpdateDetail.getCalculationMethod().isEmpty()) {
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, calculationMethod is null/empty.", jobExecutionId);
			String suspenseColumn = "calculation_method";
			logInsertSuspsenseData(suspenseDetailsBuilder.logMessage(logMessage).suspenseColumn(suspenseColumn)
					.suspenseType(SUSPENSE_TYPE_ERR).value("").group("").lookUp(false));
			processUpdate = false;
		}
		//step 5: check on start date
		boolean invalidStartDate = false;
		Date startDate;
		try{
			startDate = DateUtils.getDateFromString(gstCentralizedFileUpdateDetail.getStartDate(), DEFAULT_JOB_PARAMETER_DATE_FORMAT);
			logger.info(String.format("Staging GSTCentralizedFileUpdateDetail start date value =%s ", startDate));
		}catch (Exception err)
		{
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, Start date is invalid.", jobExecutionId);
			logger.error(logMessage);
			invalidStartDate = true;
		}
		if (null == gstCentralizedFileUpdateDetail.getStartDate() || gstCentralizedFileUpdateDetail.getStartDate().isEmpty() || invalidStartDate) {
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, startDate is null/empty.", jobExecutionId);
			String suspenseColumn = "start_date";
			logInsertSuspsenseData(suspenseDetailsBuilder.logMessage(logMessage).suspenseColumn(suspenseColumn).suspenseType(SUSPENSE_TYPE_ERR)
					.value("").group("").lookUp(false));
			processUpdate = false;
		}
		//step 6: check on end date
		boolean invalidEndDate = false;
		Date endDate;
		try{
			endDate = DateUtils.getDateFromString(gstCentralizedFileUpdateDetail.getEndDate(), DEFAULT_JOB_PARAMETER_DATE_FORMAT);
			logger.info(String.format("Staging GSTCentralizedFileUpdateDetail end date value =%s ", endDate));
		}catch (Exception err){
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, End date is invalid.", jobExecutionId);
			logger.error(logMessage);
			invalidEndDate = true;
		}
		if (null == gstCentralizedFileUpdateDetail.getEndDate() || gstCentralizedFileUpdateDetail.getEndDate().isEmpty() || invalidEndDate) {
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, endDate is null/empty.", jobExecutionId);
			String suspenseColumn = "end_date";
			logInsertSuspsenseData(suspenseDetailsBuilder.logMessage(logMessage).suspenseColumn(suspenseColumn)
					.suspenseType(SUSPENSE_TYPE_ERR).value("").group("").lookUp(false));
			processUpdate = false;
		}
		// Left with LookUP : -treatmentType, -taxCode, -calculationMethod
		// Get all lookup value from list
		List<BatchLookup> lookUpGSTTreatmentType = gstCentralizedFileUpdateRepositoryImpl.getBatchLookUpValue("DCP_GST_TREATMENT_TYPE");
		boolean treatmentTypeValid = false;
		for(BatchLookup gstTreatmentType : lookUpGSTTreatmentType){
			if(gstCentralizedFileUpdateDetail.getTreatmentType().equalsIgnoreCase(gstTreatmentType.getValue())) {
				treatmentTypeValid = true;
				break;
			}
		}
		if(!treatmentTypeValid){
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, Lookup for treatment_type failed.", jobExecutionId);
			String suspenseColumn = "treatment_type";
			logInsertSuspsenseData(suspenseDetailsBuilder.logMessage(logMessage).suspenseColumn(suspenseColumn)
					.suspenseType(SUSPENSE_TYPE_ERR).value(gstCentralizedFileUpdateDetail.getTreatmentType())
					.group("DCP_GST_TREATMENT_TYPE").lookUp(true));
			processUpdate = false;
		}

		List<BatchLookup> lookUpGSTTaxCode = gstCentralizedFileUpdateRepositoryImpl.getBatchLookUpValue("DCP_GST_TAX_CODE");
		boolean taxCodeValid = false;
		taxCodeValid = isTaxCodeValid(gstCentralizedFileUpdateDetail, lookUpGSTTaxCode, taxCodeValid);
		if(!taxCodeValid){
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, Lookup for tax_code failed.", jobExecutionId);
			String suspenseColumn = "tax_code";
			logInsertSuspsenseData(suspenseDetailsBuilder.logMessage(logMessage).suspenseColumn(suspenseColumn).suspenseType(SUSPENSE_TYPE_ERR)
					.value(gstCentralizedFileUpdateDetail.getTaxCode()).group("DCP_GST_TAX_CODE").lookUp(true));
			processUpdate = false;
		}

		List<BatchLookup> lookUpGSTCalculationMethod = gstCentralizedFileUpdateRepositoryImpl.getBatchLookUpValue("DCP_GST_CALCULATION_METHOD");
		boolean calculationMethodValid = false;
		calculationMethodValid = isCalculationGSTMethodValid(gstCentralizedFileUpdateDetail, lookUpGSTCalculationMethod, calculationMethodValid);
		if(!calculationMethodValid){
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, Lookup for calculation_method failed.", jobExecutionId);
			String suspenseColumn = "calculation_method";
			logInsertSuspsenseData(suspenseDetailsBuilder.logMessage(logMessage).suspenseColumn(suspenseColumn)
					.suspenseType(SUSPENSE_TYPE_ERR).value(gstCentralizedFileUpdateDetail.getCalculationMethod())
					.group("DCP_GST_CALCULATION_METHOD").lookUp(true));
			processUpdate = false;
		}

		return processUpdate;
	}

	private boolean isCalculationGSTMethodValid(GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail, List<BatchLookup> lookUpGSTCalculationMethod, boolean calculationMethodValid) {
		for(BatchLookup gstCalculationMethodLookup : lookUpGSTCalculationMethod){
			if(gstCentralizedFileUpdateDetail.getCalculationMethod().equalsIgnoreCase(gstCalculationMethodLookup.getValue())) {
				calculationMethodValid = true;
				break;
			}
		}
		return calculationMethodValid;
	}

	private boolean isTaxCodeValid(GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail, List<BatchLookup> lookUpGSTTaxCode, boolean taxCodeValid) {
		for(BatchLookup gstTaxCode : lookUpGSTTaxCode){
			if(gstCentralizedFileUpdateDetail.getTaxCode().equalsIgnoreCase(gstTaxCode.getValue())) {
				taxCodeValid = true;
				break;
			}
		}
		return taxCodeValid;
	}

	private boolean isProcessUpdate(GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail, BatchSuspense batchSuspense, boolean processUpdate, boolean invalidNumberGSTRate, SuspenseDetailsBuilder suspenseDetailsBuilder) {
		String logMessage;
		if (null == gstCentralizedFileUpdateDetail.getGstRate() || gstCentralizedFileUpdateDetail.getGstRate().isEmpty() || invalidNumberGSTRate) {
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, gstRate is null/empty.", jobExecutionId);
			String suspenseColumn = "gst_rate";

			logInsertSuspsenseData(suspenseDetailsBuilder.logMessage(logMessage).suspenseColumn(suspenseColumn)
					.suspenseType(SUSPENSE_TYPE_ERR).value("").group("").lookUp(false));
			processUpdate = false;
		}
		//step 2: check on treatment type
		if (null == gstCentralizedFileUpdateDetail.getTreatmentType() || gstCentralizedFileUpdateDetail.getTreatmentType().isEmpty()) {
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, treatmentType is null/empty.", jobExecutionId);
			String suspenseColumn = "treatment_type";
			logInsertSuspsenseData(suspenseDetailsBuilder.batchSuspense(batchSuspense).logMessage(logMessage)
					.suspenseColumn(suspenseColumn).suspenseType(SUSPENSE_TYPE_ERR).value("").group("").lookUp(false));
			processUpdate = false;
		}
		//step 3: check on tax code
		if (null == gstCentralizedFileUpdateDetail.getTaxCode() || gstCentralizedFileUpdateDetail.getTaxCode().isEmpty()) {
			logMessage = String.format("Staging GSTCentralizedFileUpdateDetail job execution id=%s, tax code is null/empty.", jobExecutionId);
			String suspenseColumn = "tax_code";
			logInsertSuspsenseData(suspenseDetailsBuilder.batchSuspense(batchSuspense).logMessage(logMessage)
					.suspenseColumn(suspenseColumn).suspenseType(SUSPENSE_TYPE_ERR).value("").group("").lookUp(false));
			processUpdate = false;
		}
		return processUpdate;
	}

	// Update to GST database if different
	public boolean updateGSTConfig(GSTCentralizedFileUpdateDetail updateGSTDetail, GSTCentralizedFileUpdateDetail originalDetail) throws ParseException
	{
		// Status of the update config
		boolean status = false;

		// Check whether if this is the first to be updated
		List<String> parameterToUpdate = new ArrayList<>();
		Map<String,String> parameters = new HashMap<>();
		parameters.put(SET_SQL_STRING, " SET ");
		parameters.put(FIRST_SET, "true");
		parameters.put(COLUMN_TO_UPDATE,"0");

		// Value to be updated will not be same then adjust sql set
		setGSTCodeAndIndicator(updateGSTDetail, originalDetail, parameterToUpdate, parameters);

		double updateGSTRate = (updateGSTDetail.getTreatmentType().equalsIgnoreCase("01")) ? Double.parseDouble(updateGSTDetail.getGstRate()) : 0;
		double originalGSTRate = Double.parseDouble(originalDetail.getGstRate());
		if(updateGSTRate != originalGSTRate)
		{
			buildQueryAndIncrementColumnUpdate(parameters,parameterToUpdate,"GST_RATE",String.valueOf(updateGSTRate));
		}

		if(stringCompareIsValid(updateGSTDetail.getTreatmentType(), originalDetail.getTreatmentType()) &&
				!updateGSTDetail.getTreatmentType().equalsIgnoreCase(originalDetail.getTreatmentType()))
		{
			buildQueryAndIncrementColumnUpdate(parameters,parameterToUpdate,"TREATMENT_TYPE",updateGSTDetail.getTreatmentType());
		}

		if(stringCompareIsValid(updateGSTDetail.getTaxCode(), originalDetail.getTaxCode()) &&
				!updateGSTDetail.getTaxCode().equalsIgnoreCase(originalDetail.getTaxCode()))
		{
			buildQueryAndIncrementColumnUpdate(parameters,parameterToUpdate,"TAX_CODE",updateGSTDetail.getTaxCode());
		}

		if(stringCompareIsValid(updateGSTDetail.getCalculationMethod(), originalDetail.getCalculationMethod()) &&
				!updateGSTDetail.getCalculationMethod().equalsIgnoreCase(originalDetail.getCalculationMethod()))
		{
			buildQueryAndIncrementColumnUpdate(parameters,parameterToUpdate,"CALCULATION_METHOD",updateGSTDetail.getCalculationMethod());
		}

		DateFormat sdfSource = new SimpleDateFormat(DEFAULT_JOB_PARAMETER_DATE_FORMAT);
		SimpleDateFormat sdfDestination = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		String startDate = sdfDestination.format(sdfSource.parse(updateGSTDetail.getStartDate()));

		Date beginDateRaw = DateUtils.getDateFromString(originalDetail.getBeginDate(), "yyyy-MM-dd HH:mm:ss.S");
		String beginDate = sdfDestination.format(beginDateRaw);

		if(stringCompareIsValid(startDate, beginDate) && !startDate.equalsIgnoreCase(beginDate))
		{
			buildQueryAndIncrementColumnUpdate(parameters,parameterToUpdate,"BEGIN_DATE",startDate);
		}

		String updateEndDate = sdfDestination.format(sdfSource.parse(updateGSTDetail.getEndDate()));

		Date originalEndDateRaw = DateUtils.getDateFromString(originalDetail.getEndDate(), "yyyy-MM-dd HH:mm:ss.S");
		String originalEndDate = sdfDestination.format(originalEndDateRaw);

		if(stringCompareIsValid(updateEndDate, originalEndDate) && !updateEndDate.equalsIgnoreCase(originalEndDate))
		{
			buildQueryAndIncrementColumnUpdate(parameters,parameterToUpdate,"END_DATE",updateGSTDetail.getEndDate());
		}

		if(Integer.parseInt(parameters.get(COLUMN_TO_UPDATE)) > 0)
		{
			// Update Set Time and Where set parameter
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
			String formatDateTime = LocalDateTime.now().format(formatter);
			String updateUser = "LDCPD8923B";

			parameterToUpdate.add(updateUser);
			parameterToUpdate.add(formatDateTime);
			parameterToUpdate.add(updateGSTDetail.getUniqueId());
			parameterToUpdate.add(updateGSTDetail.getSourceSystemId());
			parameterToUpdate.add(updateGSTDetail.getTransactionIdentifier());

			status = gstCentralizedFileUpdateRepositoryImpl.updateGSTDCP(parameters.get(SET_SQL_STRING), parameterToUpdate);
		}

		// Update status in staging
		if(status)
		{
			List<String> processToUpdate = new ArrayList<>();
			processToUpdate.add(updateGSTDetail.getUniqueId());
			processToUpdate.add(updateGSTDetail.getSourceSystemId());
			processToUpdate.add(updateGSTDetail.getTransactionIdentifier());
			processToUpdate.add(jobExecutionId);
			gstCentralizedFileUpdateRepositoryImpl.updateProcessStatus(processToUpdate);
		}

		return status;
	}

	private void setGSTCodeAndIndicator(GSTCentralizedFileUpdateDetail updateGSTDetail, GSTCentralizedFileUpdateDetail originalDetail, List<String> parameterToUpdate, Map<String, String> parameters) {
		if(stringCompareIsValid(updateGSTDetail.getEntityCode(), originalDetail.getEntityCode()) &&
				!updateGSTDetail.getEntityCode().equalsIgnoreCase(originalDetail.getEntityCode()))
		{
			buildQueryAndIncrementColumnUpdate(parameters,parameterToUpdate,"ENTITY_CODE",updateGSTDetail.getEntityCode());
		}

		if(stringCompareIsValid(updateGSTDetail.getEntityIndicator(), originalDetail.getEntityIndicator()) &&
				!updateGSTDetail.getEntityIndicator().equalsIgnoreCase(originalDetail.getEntityIndicator()))
		{
			buildQueryAndIncrementColumnUpdate(parameters,parameterToUpdate,"ENTITY_INDICATOR",updateGSTDetail.getEntityIndicator());
		}
	}

	private void buildQueryAndIncrementColumnUpdate(Map<String,String> parameters,
													List<String> parameterToUpdate,
													String appendQuery,
													String gstDetail){
		if(Boolean.parseBoolean(parameters.get(FIRST_SET)))
			parameters.put(FIRST_SET,Boolean.FALSE.toString());
		else parameters.put(SET_SQL_STRING,parameters.get(SET_SQL_STRING)+",");

		parameters.put(SET_SQL_STRING,parameters.get(SET_SQL_STRING)+" "+ appendQuery + " = ? ");
		parameters.put(COLUMN_TO_UPDATE,String.valueOf(Integer.parseInt(parameters.get(COLUMN_TO_UPDATE)+1)));
		parameterToUpdate.add(gstDetail);
	}

	// Compare value from original or updated value is valid
	// Will not proceed if the udpatedString is null or empty
	public boolean stringCompareIsValid(String updatedString, String originalString)
	{
		if(updatedString != null){
			if(updatedString.isEmpty())
				return false;
			if(originalString == null)
				return false;
			if(originalString.isEmpty())
				return false;
		}else{
			return false;
		}

		return true;
	}

}
