package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.naming.NamingException;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.jms.InvalidDestinationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.JMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDailyDeltaNewProfile;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedDailyDeltaNewProfileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import com.rhbgroup.dcp.model.Capsule;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(JMSUtils.class)
@PowerMockIgnore(value= { "javax.net.ssl.*" })
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class SendDailyDeltaProfileSMSStepTest extends BaseJobTest {

	private static final String STEP_NAME = "SendDailyDeltaProfileSMSStep";

	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedDailyDeltaNewProfile> itemReader;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedDailyDeltaNewProfile, BatchStagedDailyDeltaNewProfile> itemProcessor;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedDailyDeltaNewProfile> itemWriter;

	@MockBean
	private BatchStagedDailyDeltaNewProfileRepositoryImpl batchStagedDailyDeltaNewProfileRepository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Before
	public void beforeLocalTest() throws IOException {
		super.beforeTest();
	}
	
	private StepExecution stepExecution;
	
	public StepExecution getStepExection() {
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
		
		return stepExecution;
	}
	
	/*
	 * Test of all records in the same day not process yet
	 */
	@Test
	public void testPositiveReaderAllNotProcessYet() throws Exception {
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE";
    	String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-06', '9991', NOW(), 0, NOW())";
    	String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-06', '9992', NOW(), 0, NOW())";
    	String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-07', '9993', NOW(), 1, NOW())";
    	String beforeSQL5 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-07', '9994', NOW(), 0, NOW())";
    	jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
    	
    	Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-06");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
    	
    	List<BatchStagedDailyDeltaNewProfile> results = 
    		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedDailyDeltaNewProfile>>() {
				public List<BatchStagedDailyDeltaNewProfile> call() throws Exception {
					BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile;
					List<BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfiles = new ArrayList<>();
					while((batchStagedDailyDeltaNewProfile = itemReader.read()) != null) {
						batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile);
					}
					return batchStagedDailyDeltaNewProfiles;
				}
		});
    	
    	assertEquals(2, results.size());
    	
    	Date date = Date.from(LocalDate.of(2018, 8, 6).atStartOfDay(ZoneId.systemDefault()).toInstant());
    	BatchStagedDailyDeltaNewProfile expectedBatchStagedDailyDeltaNewProfile1 = createBatchStagedDailyDeltaNewProfile(9991, date, false);
    	BatchStagedDailyDeltaNewProfile expectedBatchStagedDailyDeltaNewProfile2 = createBatchStagedDailyDeltaNewProfile(9992, date, false);
    	
    	compareBatchStagedDailyDeltaNewProfile(expectedBatchStagedDailyDeltaNewProfile1, results.get(0));
    	compareBatchStagedDailyDeltaNewProfile(expectedBatchStagedDailyDeltaNewProfile2, results.get(1));
    	
    	String afterSQL1 = beforeSQL1;
    	jdbcTemplate.batchUpdate(afterSQL1);
	}
	
	/*
	 * Test of some records is processed and some don't
	 */
	@Test
	public void testPositiveReaderMixedInSameDay() throws Exception {
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE";
    	String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-06', '9991', NOW(), 0, NOW())";
    	String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-06', '9992', NOW(), 0, NOW())";
    	String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-07', '9993', NOW(), 1, NOW())";
    	String beforeSQL5 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-07', '9994', NOW(), 0, NOW())";
    	jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
    	
    	Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-07");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
    	
    	List<BatchStagedDailyDeltaNewProfile> results = 
    		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedDailyDeltaNewProfile>>() {
				public List<BatchStagedDailyDeltaNewProfile> call() throws Exception {
					BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile;
					List<BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfiles = new ArrayList<>();
					while((batchStagedDailyDeltaNewProfile = itemReader.read()) != null) {
						batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile);
					}
					return batchStagedDailyDeltaNewProfiles;
				}
		});
    	
    	assertEquals(1, results.size());
    	
    	Date date = Date.from(LocalDate.of(2018, 8, 7).atStartOfDay(ZoneId.systemDefault()).toInstant());
    	BatchStagedDailyDeltaNewProfile expectedBatchStagedDailyDeltaNewProfile1 = createBatchStagedDailyDeltaNewProfile(9994, date, false);
    	
    	compareBatchStagedDailyDeltaNewProfile(expectedBatchStagedDailyDeltaNewProfile1, results.get(0));
    	
    	String afterSQL1 = beforeSQL1;
    	jdbcTemplate.batchUpdate(afterSQL1);
	}
	
	/*
	 * Test of all records in the same day already been processed
	 */
	@Test
	public void testPositiveReaderAllProcessed() throws Exception {
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE";
    	String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-06', '9991', NOW(), 0, NOW())";
    	String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-06', '9992', NOW(), 0, NOW())";
    	String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-07', '9993', NOW(), 1, NOW())";
    	String beforeSQL5 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES ('2018-08-07', '9994', NOW(), 0, NOW())";
    	jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
    	
    	Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-10");
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
    	
    	List<BatchStagedDailyDeltaNewProfile> results = 
    		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedDailyDeltaNewProfile>>() {
				public List<BatchStagedDailyDeltaNewProfile> call() throws Exception {
					BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile;
					List<BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfiles = new ArrayList<>();
					while((batchStagedDailyDeltaNewProfile = itemReader.read()) != null) {
						batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile);
					}
					return batchStagedDailyDeltaNewProfiles;
				}
		});
    	
    	assertEquals(0, results.size());
    	
    	String afterSQL1 = beforeSQL1;
    	jdbcTemplate.batchUpdate(afterSQL1);
	}
	
	/*
	 * Test to ensure the object go into the processor and the one return is exact same
	 */
	@Test
	public void testPositiveProcessor() throws Exception {
		Date date = Date.from(LocalDate.of(2018, 8, 7).atStartOfDay(ZoneId.systemDefault()).toInstant());
    	BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile = createBatchStagedDailyDeltaNewProfile(9994, date, false);
    	assertEquals(batchStagedDailyDeltaNewProfile, itemProcessor.process(batchStagedDailyDeltaNewProfile));
	}
	
	/*
	 * Test to ensure the writer manage to send the correct message to the JMS
	 */
	@Test
	public void testPositiveWriterSendJMSSuccessfully() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		Date date = Date.from(LocalDate.of(2018, 8, 6).atStartOfDay(ZoneId.systemDefault()).toInstant());
    	BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile1 = createBatchStagedDailyDeltaNewProfile(9991, date, false);
    	BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile2 = createBatchStagedDailyDeltaNewProfile(9992, date, false);
    	List<BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfiles = new ArrayList<>();
    	batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile1);
    	batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile2);
    	
    	when(batchStagedDailyDeltaNewProfileRepository.updateIsProcessed(batchStagedDailyDeltaNewProfile1)).thenReturn(1);
    	when(batchStagedDailyDeltaNewProfileRepository.updateIsProcessed(batchStagedDailyDeltaNewProfile2)).thenReturn(1);
    	
    	itemWriter.write(batchStagedDailyDeltaNewProfiles);

    	// Belo we are skipping compare the event timestamp since it is system generated which not really important
    	PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(2));
    	JMSUtils.sendCapsuleMessageToJMS(
    			(Capsule)Mockito.any(),
    			Mockito.any());
    	PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(2));
		JMSUtils.sendCapsuleMessageToJMS(
				(Capsule) Mockito.any(),
				Mockito.any());

    	verify(batchStagedDailyDeltaNewProfileRepository, times(1)).updateIsProcessed(batchStagedDailyDeltaNewProfile1);
    	verify(batchStagedDailyDeltaNewProfileRepository, times(1)).updateIsProcessed(batchStagedDailyDeltaNewProfile2);
	}
	
	/*
	 * Test to ensure JMS service still able to try for shutdown when JMS issue happened
	 */
	@Test
	public void testNegativeWriterJMSServiceDown() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		Date date = Date.from(LocalDate.of(2018, 8, 6).atStartOfDay(ZoneId.systemDefault()).toInstant());
    	BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile1 = createBatchStagedDailyDeltaNewProfile(9991, date, false);
    	BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile2 = createBatchStagedDailyDeltaNewProfile(9992, date, false);
    	List<BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfiles = new ArrayList<>();
    	batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile1);
    	batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile2);
    	
    	PowerMockito.when(JMSUtils.class, "sendMessageToJMS", Mockito.anyString(), Mockito.<JMSConfig>any()).thenThrow(InvalidDestinationException.class);

    	itemWriter.write(batchStagedDailyDeltaNewProfiles);
    	
    	verify(mockAppender, times(4)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent1 = (LoggingEvent)captorLoggingEvent.getAllValues().get(2);
    	LoggingEvent loggingEvent2 = (LoggingEvent)captorLoggingEvent.getAllValues().get(3);
        //Check log level
        assertEquals(Level.ERROR, loggingEvent1.getLevel());
        assertEquals(Level.ERROR, loggingEvent2.getLevel());
        //Check the message being logged
		//assertTrue(loggingEvent1.getRenderedMessage().contains(String.format("Error happened while pushing notification to JMS queue for BatchStagedDailyDeltaNewProfile [BatchStagedDailyDeltaNewProfile(id=0, processingDate=%s, userId=9991, isProcessed=false, createdTime=null, updatedTime=null, jobExecutionId=1)]", date)));
		//assertTrue(loggingEvent2.getRenderedMessage().contains(String.format("Error happened while pushing notification to JMS queue for BatchStagedDailyDeltaNewProfile [BatchStagedDailyDeltaNewProfile(id=0, processingDate=%s, userId=9992, isProcessed=false, createdTime=null, updatedTime=null, jobExecutionId=1)]", date)));
	}
	
	/*
	 * Test to ensure even when JMS service is down, even though all JMS interactions failed, the writer still proceed like normal
	 * NOTES: No longer valid since we let Spring to help close the resource after job end
	 */
//	@Test
	public void testNegativeWriterAllJMSServiceInteractionsFailed() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		Date date = Date.from(LocalDate.of(2018, 8, 6).atStartOfDay(ZoneId.systemDefault()).toInstant());
    	BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile1 = createBatchStagedDailyDeltaNewProfile(9991, date, false);
    	BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile2 = createBatchStagedDailyDeltaNewProfile(9992, date, false);
    	List<BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfiles = new ArrayList<>();
    	batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile1);
    	batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile2);
    	
    	PowerMockito.when(JMSUtils.class, "setupJMS", Mockito.<JMSConfig>any(), Mockito.anyString(), Mockito.<JMSConfigProperties>any()).thenThrow(NamingException.class);
//		PowerMockito.when(JMSUtils.class, "shutdownJMS", Mockito.<JMSConfig>any()).thenThrow(NamingException.class);
		
    	itemWriter.write(batchStagedDailyDeltaNewProfiles);
    	
    	verify(mockAppender, times(5)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent1 = (LoggingEvent)captorLoggingEvent.getAllValues().get(2);
    	LoggingEvent loggingEvent2 = (LoggingEvent)captorLoggingEvent.getAllValues().get(3);
    	LoggingEvent loggingEvent3 = (LoggingEvent)captorLoggingEvent.getAllValues().get(4);
        //Check log level
        assertEquals(Level.ERROR, loggingEvent1.getLevel());
        assertEquals(Level.ERROR, loggingEvent2.getLevel());
        assertEquals(Level.ERROR, loggingEvent3.getLevel());
        //Check the message being logged
        assertTrue(loggingEvent1.getRenderedMessage().contains(String.format("Error happened while pushing notification to JMS queue for BatchStagedDailyDeltaNewProfile [BatchStagedDailyDeltaNewProfile(id=0, processingDate=%s, userId=9991, isProcessed=false, createdTime=null, updatedTime=null, jobExecutionId=1)]", date)));
        assertTrue(loggingEvent2.getRenderedMessage().contains(String.format("Error happened while pushing notification to JMS queue for BatchStagedDailyDeltaNewProfile [BatchStagedDailyDeltaNewProfile(id=0, processingDate=%s, userId=9992, isProcessed=false, createdTime=null, updatedTime=null, jobExecutionId=1)]", date)));
        assertTrue(loggingEvent3.getRenderedMessage().contains(String.format("Failed to close the JMSContext / NamingContext in [%s:%s]", smsJMSConfigProperties.getHost(), smsJMSConfigProperties.getPort())));
	}
	
	/*
	 * Test to ensure process not break even DB failure but message must be log to track this
	 */
	@Test
	public void testNegativeWriterDBServiceDown() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		Date date = Date.from(LocalDate.of(2018, 8, 6).atStartOfDay(ZoneId.systemDefault()).toInstant());
    	BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile1 = createBatchStagedDailyDeltaNewProfile(9991, date, false);
    	BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile2 = createBatchStagedDailyDeltaNewProfile(9992, date, false);
    	List<BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfiles = new ArrayList<>();
    	batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile1);
    	batchStagedDailyDeltaNewProfiles.add(batchStagedDailyDeltaNewProfile2);
    	
    	when(batchStagedDailyDeltaNewProfileRepository.updateIsProcessed(batchStagedDailyDeltaNewProfile1)).thenThrow(BatchException.class);
    	when(batchStagedDailyDeltaNewProfileRepository.updateIsProcessed(batchStagedDailyDeltaNewProfile2)).thenThrow(BatchException.class);
    	
    	itemWriter.write(batchStagedDailyDeltaNewProfiles);
    	
    	PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(2));
    	JMSUtils.sendCapsuleMessageToJMS((Capsule)Mockito.any(), Mockito.any());
    	PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(2));
    	JMSUtils.sendCapsuleMessageToJMS((Capsule)Mockito.any(), Mockito.any());
		
    	verify(mockAppender, times(4)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent1 = (LoggingEvent)captorLoggingEvent.getAllValues().get(2);
    	LoggingEvent loggingEvent2 = (LoggingEvent)captorLoggingEvent.getAllValues().get(3);
        //Check log level
        assertEquals(Level.ERROR, loggingEvent1.getLevel());
        assertEquals(Level.ERROR, loggingEvent2.getLevel());
        //Check the message being logged
		assertTrue(loggingEvent1.getRenderedMessage().contains(String.format("Error happened while updating record IsProcessed status in DB for BatchStagedDailyDeltaNewProfile [BatchStagedDailyDeltaNewProfile(id=0, processingDate=%s, userId=9991, isProcessed=false, createdTime=null, updatedTime=null, jobExecutionId=1)]", date)));
		assertTrue(loggingEvent2.getRenderedMessage().contains(String.format("Error happened while updating record IsProcessed status in DB for BatchStagedDailyDeltaNewProfile [BatchStagedDailyDeltaNewProfile(id=0, processingDate=%s, userId=9992, isProcessed=false, createdTime=null, updatedTime=null, jobExecutionId=1)]", date)));
	}
	
	private void compareBatchStagedDailyDeltaNewProfile(BatchStagedDailyDeltaNewProfile expected, BatchStagedDailyDeltaNewProfile actual) {
		assertEquals(expected.getUserId(), actual.getUserId());
		assertEquals(expected.getProcessingDate(), actual.getProcessingDate());
		assertEquals(expected.isProcessed(), actual.isProcessed());
	}
	
	private BatchStagedDailyDeltaNewProfile createBatchStagedDailyDeltaNewProfile(int userId, Date processingDate, boolean isProcessed) {
		BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile = new BatchStagedDailyDeltaNewProfile();
		batchStagedDailyDeltaNewProfile.setUserId(userId);
		batchStagedDailyDeltaNewProfile.setProcessingDate(processingDate);
		batchStagedDailyDeltaNewProfile.setProcessed(isProcessed);
		return batchStagedDailyDeltaNewProfile;
	}
}
