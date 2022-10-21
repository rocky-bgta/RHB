package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.JompayFailureValidationExtractionJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedJompayFailureTxn;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(DateUtils.class)
@PowerMockIgnore(value= { "javax.net.ssl.*" })
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class FailedJompayTxnsFromDBToFileStepTest extends BaseJobTest {

	private static final String STEP_NAME = "FailedJompayTxnsFromDBToFileStep";

	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedJompayFailureTxn> itemReader;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedJompayFailureTxn, BatchStagedJompayFailureTxn> itemProcessor;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedJompayFailureTxn> itemWriter;
	
	@Autowired
	JompayFailureValidationExtractionJobConfigProperties jobConfigProperties;
	
	@Value(SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderPath;
	
	private StepExecution stepExecution;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	public StepExecution getStepExection() {
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
		
		return stepExecution;
	}
	
	@Before
	public void beforeLocalTest() throws IOException {
		super.beforeTest();
		cleanupFolder();
	}
	
	@After
	public void afterLocalTest() throws IOException {
		stepExecution.getJobExecution().getExecutionContext().remove(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY);
		stepExecution.getJobExecution().getExecutionContext().remove(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		cleanupFolder();
	}
	
	/*
	 * Test to ensure DB config batch system date get use if not external job param process dates configured
	 */
	@Test
	public void testPositiveReaderWithDBConfigParamBatchSystemDate() throws Exception {
		String beforeSQL1 = "DELETE FROM VW_BATCH_JOMPAY_FAILURE_VALIDATION WHERE BILLER_CODE = '4600' AND PAYMENT_CHANNEL = '3'";
		String beforeSQL2 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Timeout','2018-08-06 11:11:10')";
		String beforeSQL3 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Network Issue','2018-08-06 22:22:20')";
		String beforeSQL4 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Not Sufficient Money','2018-08-13 11:11:30')";
		String beforeSQL5 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Unexpected Issues','2018-08-13 22:22:40')";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-13");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
		
		List<BatchStagedJompayFailureTxn> results = getItemReaderResult(stepExecution);
		
		assertEquals(2, results.size());
		
		Date date1 = Date.from(LocalDateTime.of(2018, 8, 6, 11, 11, 10, 0).atZone(ZoneId.systemDefault()).toInstant());
		BatchStagedJompayFailureTxn expectedBatchJompayFailureValidation1 = createBatchJompayFailureValidation(
			"4600", "3", "Timeout", date1, "RHB2186");
		Date date2 = Date.from(LocalDateTime.of(2018, 8, 6, 22, 22, 20, 0).atZone(ZoneId.systemDefault()).toInstant());
		BatchStagedJompayFailureTxn expectedBatchJompayFailureValidation2 = createBatchJompayFailureValidation(
			"4600", "3", "Network Issue", date2, "RHB2186");
		
		compareBatchJompayFailureValidation(expectedBatchJompayFailureValidation1, results.get(0));
		compareBatchJompayFailureValidation(expectedBatchJompayFailureValidation2, results.get(1));
		
		String afterSQL1 = beforeSQL1;
    	jdbcTemplate.batchUpdate(afterSQL1);
	}
	
	/*
	 * Test to ensure the reader using the job params process dates instead from the DB if it is set in context
	 */
	@Test
	public void testPositiveReaderWithJobParamsProcessDates() throws Exception {
		String beforeSQL1 = "DELETE FROM VW_BATCH_JOMPAY_FAILURE_VALIDATION WHERE BILLER_CODE = '4600' AND PAYMENT_CHANNEL = '3'";
		String beforeSQL2 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Timeout','2018-08-06 11:11:10')";
		String beforeSQL3 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Network Issue','2018-08-06 22:22:20')";
		String beforeSQL4 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Not Sufficient Money','2018-08-13 11:11:30')";
		String beforeSQL5 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Unexpected Issues','2018-08-13 22:22:40')";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY, "(20180806,20180812)");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
		
		List<BatchStagedJompayFailureTxn> results = getItemReaderResult(stepExecution);
		
		assertEquals(2, results.size());
		
		Date date1 = Date.from(LocalDateTime.of(2018, 8, 6, 11, 11, 10, 0).atZone(ZoneId.systemDefault()).toInstant());
		BatchStagedJompayFailureTxn expectedBatchJompayFailureValidation1 = createBatchJompayFailureValidation(
			"4600", "3", "Timeout", date1, "RHB2186");
		Date date2 = Date.from(LocalDateTime.of(2018, 8, 6, 22, 22, 20, 0).atZone(ZoneId.systemDefault()).toInstant());
		BatchStagedJompayFailureTxn expectedBatchJompayFailureValidation2 = createBatchJompayFailureValidation(
			"4600", "3", "Network Issue", date2, "RHB2186");
		
		compareBatchJompayFailureValidation(expectedBatchJompayFailureValidation1, results.get(0));
		compareBatchJompayFailureValidation(expectedBatchJompayFailureValidation2, results.get(1));
		
		String afterSQL1 = beforeSQL1;
    	jdbcTemplate.batchUpdate(afterSQL1);
	}
	
	/*
	 * Test to ensure From date must always be Monday
	 */
	@Test
	public void testNegativeReaderFromDateIsNotMonday() throws Exception {
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		// From date is Sunday
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY, "(20180805,20180811)");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
		
		expectedEx.expect(BeanCreationException.class);
    	expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE));
		
    	executeSimpleItemReader(stepExecution, itemReader);
	}
	
	/*
	 * Test to ensure To date is always on Sunday
	 */
	@Test
	public void testNegativeReaderToDateIsNotSunday() throws Exception {Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		// To date is Saturday
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY, "(20180806,20180811)");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
		
		expectedEx.expect(BeanCreationException.class);
    	expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE));
		
    	executeSimpleItemReader(stepExecution, itemReader);
	}
	
	/*
	 * Test to ensure From date cannot be after To date
	 */
	@Test
	public void testNegativeReaderFromDateIsAfterBeforeDate() throws Exception {Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		// From date is 7 days after the To date
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY, "(20180812,20180806)");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
		
		expectedEx.expect(BeanCreationException.class);
    	expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE));
		
    	executeSimpleItemReader(stepExecution, itemReader);
	}
	
	/*
	 * Test to ensure the max duration between From and To dates must be 7 days only
	 */
	@Test
	public void testNegativeReaderNotSevenDays() throws Exception {Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		// The duration is 14 days instead of 7 days
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY, "(20180806,20180819)");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
		
		expectedEx.expect(BeanCreationException.class);
    	expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE));
		
    	executeSimpleItemReader(stepExecution, itemReader);
	}
	
	/* 
	 * Test to ensure in case something went wrong in the DateUtils, we shall not proceed further when we using job params process dates
	 */
	@Test
	public void testNegativeReaderFailedToProcessJobParamProcessDates() throws Exception {
		PowerMockito.mockStatic(DateUtils.class);
		
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY, "(20180806,20180812)");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
		
		expectedEx.expect(BeanCreationException.class);
    	expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE));
		
    	PowerMockito.when(DateUtils.class, "formatDateString", Mockito.any(Date.class), Mockito.anyString()).thenReturn("20180831");
    	PowerMockito.when(DateUtils.class, "getJobParameterFromToDateTimes", Mockito.anyString()).thenThrow(DateTimeParseException.class);
    	
    	executeSimpleItemReader(stepExecution, itemReader);
	}
	
	/* 
	 * Test to ensure in case something went wrong in the DateUtils, we shall not proceed further when we using the DB config batch system date
	 */
	@Test
	public void testNegativeReaderFailedToGetLocalDateFromDBBatchSystemDate() throws Exception {
		PowerMockito.mockStatic(DateUtils.class);
		
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-13");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
		
		expectedEx.expect(BeanCreationException.class);
    	expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE));
		
    	PowerMockito.when(DateUtils.class, "formatDateString", Mockito.any(Date.class), Mockito.anyString()).thenReturn("20180831");
    	PowerMockito.when(DateUtils.class, "getLocalDateFromString", Mockito.anyString(), Mockito.anyString()).thenThrow(DateTimeParseException.class);
    	
    	executeSimpleItemReader(stepExecution, itemReader);
	}
	
	/*
	 * Test to ensure the processor working fine, it shall return the same object without any modification
	 */
	@Test
	public void testPositiveProcessor() throws Exception {
		Date date1 = Date.from(LocalDateTime.of(2018, 8, 6, 11, 11, 10, 0).atZone(ZoneId.systemDefault()).toInstant());
		BatchStagedJompayFailureTxn expectedBatchJompayFailureValidation1 = createBatchJompayFailureValidation(
			"4600", "3", "Timeout", date1, "RHB2186");
		assertEquals(expectedBatchJompayFailureValidation1, itemProcessor.process(expectedBatchJompayFailureValidation1));
	}
	
	/*
	 * Test to ensure writer able to handle the objects successfully and generate the required file as expected
	 */
	@Test
	public void testPositiveWriterWithBatchJompayFailureValidations() throws Exception {
		Date date1 = Date.from(LocalDateTime.of(2018, 8, 6, 11, 11, 10, 0).atZone(ZoneId.systemDefault()).toInstant());
		BatchStagedJompayFailureTxn batchJompayFailureValidation1 = createBatchJompayFailureValidation(
			"4600", "3", "Timeout", date1, "RHB2186");
		Date date2 = Date.from(LocalDateTime.of(2018, 8, 6, 22, 22, 20, 0).atZone(ZoneId.systemDefault()).toInstant());
		BatchStagedJompayFailureTxn batchJompayFailureValidation2 = createBatchJompayFailureValidation(
			"4600", "3", "Network Issue", date2, "RHB2186");
		List<BatchStagedJompayFailureTxn> batchJompayFailureValidations = new ArrayList<>();
		batchJompayFailureValidations.add(batchJompayFailureValidation1);
		batchJompayFailureValidations.add(batchJompayFailureValidation2);
		
		// Simulate how reader generate the base file
		File file = getResourceFile("batch/output/LDCPD8919B_template.txt");
		Date currentDate = new Date();
        String batchSystemDate = DateUtils.formatDateString(currentDate, jobConfigProperties.getNameDateFormat());
        String filename = jobConfigProperties.getName().replace(BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, batchSystemDate);
		File targetFile = Paths.get(outputFolderPath, TEST_JOB, filename).toFile();
		FileUtils.copyFile(file, targetFile);
		
		// This writer is using the stepExecution generated by getStepExection()
		itemWriter.write(batchJompayFailureValidations);
		
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY), targetFile.getAbsolutePath());
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY), DateUtils.formatDateString(currentDate, "yyyy-MM-dd"));
		
		File expectedFile = getResourceFile("batch/output/LDCPD8919B_with_data.txt");
		AssertFile.assertFileEquals(expectedFile, targetFile);
	}
	
	/*
	 * Test to ensure writer able to work properly even though no objects created and the original output file with header should remain there
	 */
	@Test
	public void testPositiveWriterWithNoBatchJompayFailureValidation() throws Exception {
		List<BatchStagedJompayFailureTxn> batchJompayFailureValidations = new ArrayList<>();
		
		// Simulate how reader generate the base file
		File file = getResourceFile("batch/output/LDCPD8919B_template.txt");
		Date currentDate = new Date();
        String batchSystemDate = DateUtils.formatDateString(currentDate, jobConfigProperties.getNameDateFormat());
        String filename = jobConfigProperties.getName().replace(BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, batchSystemDate);
		File targetFile = Paths.get(outputFolderPath, TEST_JOB, filename).toFile();
		FileUtils.copyFile(file, targetFile);
		
		itemWriter.write(batchJompayFailureValidations);
		
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY), targetFile.getAbsolutePath());
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY), DateUtils.formatDateString(currentDate, "yyyy-MM-dd"));
		
		File expectedFile = getResourceFile("batch/output/LDCPD8919B_without_data.txt");
		AssertFile.assertFileEquals(expectedFile, targetFile);
	}
	
	private void compareBatchJompayFailureValidation(BatchStagedJompayFailureTxn expected, BatchStagedJompayFailureTxn actual) {
		assertEquals(expected.getBillerCode(), actual.getBillerCode());
		assertEquals(expected.getPaymentChannel(), actual.getPaymentChannel());
		assertEquals(expected.getReasonForFailure(), actual.getReasonForFailure());
		assertEquals(expected.getRequestTime(), actual.getRequestTime());
	}
	
	private BatchStagedJompayFailureTxn createBatchJompayFailureValidation(
		String billerCode, String paymentChannel, String reasonForFailure, Date requestTime, String fromBankId) {
		BatchStagedJompayFailureTxn batchJompayFailureValidation = new BatchStagedJompayFailureTxn();
		batchJompayFailureValidation.setBillerCode(billerCode);
		batchJompayFailureValidation.setPaymentChannel(paymentChannel);
		batchJompayFailureValidation.setReasonForFailure(reasonForFailure);
		batchJompayFailureValidation.setRequestTime(requestTime);
		return batchJompayFailureValidation;
	}
	
	private void cleanupFolder() throws IOException {
    	String workingDir = System.getProperty("user.dir");
    	String fileFolderPath = generateFolderPath(workingDir, "target", "batch", "output", TEST_JOB);
    	File folder = new File(fileFolderPath);
    	if(folder.exists() && folder.isDirectory()) {
    		FileUtils.deleteDirectory(folder);
    	}
    }
	
	private List<BatchStagedJompayFailureTxn> getItemReaderResult(StepExecution stepExecution) throws Exception {
		return StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedJompayFailureTxn>>() {
				public List<BatchStagedJompayFailureTxn> call() throws Exception {
					BatchStagedJompayFailureTxn batchJompayFailureValidation;
					List<BatchStagedJompayFailureTxn> batchJompayFailureValidations = new ArrayList<>();
					while((batchJompayFailureValidation = itemReader.read()) != null) {
						batchJompayFailureValidations.add(batchJompayFailureValidation);
					}
					return batchJompayFailureValidations;
				}
		});
	}
	
}
