package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.InvalidDestinationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JsonUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PushCardlinkNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotification;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchCardlinkNotificationsRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.notifications.models.NotificationPayload;

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
public class SendPushCardlinkNotificationStepTest extends BaseJobTest {
	
	private static final String STEP_NAME = "SendPushCardlinkNotificationStep";
	
	private Instant eventTimestamp = Instant.now().minusSeconds(86400);
	
	private Instant now = Instant.now();
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedNotification> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedNotification, BatchStagedNotification> itemProcessor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedNotification> itemWriter;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private PushCardlinkNotificationsJobConfigProperties jmsConfigProperties;
	
	@MockBean
	private BatchCardlinkNotificationsRepositoryImpl batchCardlinkNotificationRepository;

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
	 * Test reader to ensure it able to retrieve correct amount of records from DB for throttle size -1
	 */
	@Test
	public void testPositiveReaderAllThrottleNegative1() throws Exception {
		Date datetMinus1 = Date.from(eventTimestamp);
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
		String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999901, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345678, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999902, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345679, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999903, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345680, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4);
		
		// Throttle size = -1
		jmsConfigProperties.setThrottleSize(-1);
		
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY,DateUtils.formatDateString(new Date(), BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT));
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
		
		List<BatchStagedNotification> results = 
				StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedNotification>>() {
					public List<BatchStagedNotification> call() throws Exception {
						BatchStagedNotification batchStagedNotification;
						List<BatchStagedNotification> batchStagedNotifications = new ArrayList<>();
						while((batchStagedNotification = itemReader.read()) != null) {
							batchStagedNotifications.add(batchStagedNotification);
						}
						return batchStagedNotifications;
					}
					
				});
		
		assertEquals(3, results.size());
		
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
    	jdbcTemplate.batchUpdate(afterSQL1);
	}
	
	/*
	 * Test reader to ensure it able to retrieve correct amount of records from DB for throttle size greater than -1
	 */
	@Test
	public void testPositiveReaderAllThrottleGreaterThan1() throws Exception {
		Date datetMinus1 = Date.from(eventTimestamp);
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
		String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999901, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345678, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999902, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345679, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999903, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345680, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		String beforeSQL5 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999904, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345681, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		
		// Throttle size = 4
		jmsConfigProperties.setThrottleSize(4);
		
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY,DateUtils.formatDateString(new Date(), BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT));
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
		
		List<BatchStagedNotification> results = 
				StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedNotification>>() {
					public List<BatchStagedNotification> call() throws Exception {
						BatchStagedNotification batchStagedNotification;
						List<BatchStagedNotification> batchStagedNotifications = new ArrayList<>();
						while((batchStagedNotification = itemReader.read()) != null) {
							batchStagedNotifications.add(batchStagedNotification);
						}
						return batchStagedNotifications;
					}
					
				});
		
		assertEquals(4, results.size());
		
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
    	jdbcTemplate.batchUpdate(afterSQL1);
	}
	
	/*
	 * Test reader to ensure it able to retrieve correct amount of records from DB
	 */
	@Test
	public void testPositiveReaderAllProcessDate() throws Exception {
		Date datetMinus1 = Date.from(eventTimestamp);
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
		String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999901, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345678, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999902, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345679, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999903, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345680, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		String beforeSQL5 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION ("
				+ "JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999904, '101020180516.txt', '" + DateUtils.formatDateString(new Date(), BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345681, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, 'LDCPD6004B', NOW(), 'LDCPD6004B', NOW())";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		
		// Throttle size = -1
		jmsConfigProperties.setThrottleSize(-1);
		
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY,DateUtils.formatDateString(new Date(), BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT));
		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
		
		List<BatchStagedNotification> results = 
				StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedNotification>>() {
					public List<BatchStagedNotification> call() throws Exception {
						BatchStagedNotification batchStagedNotification;
						List<BatchStagedNotification> batchStagedNotifications = new ArrayList<>();
						while((batchStagedNotification = itemReader.read()) != null) {
							batchStagedNotifications.add(batchStagedNotification);
						}
						return batchStagedNotifications;
					}
					
				});
		
		assertEquals(3, results.size());
		
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
    	jdbcTemplate.batchUpdate(afterSQL1);
	}	
	
	/*
	 * Test to ensure the writer manage to send the correct message to the JMS
	 */
	@Test
	public void testPositiveWriterSendJMSSuccessfully() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		BatchStagedNotification batchStagedNotification1 = createBatchStagedNotification(1L, 999901L, 9991L, "4363452500001009", "20180926", false);
		BatchStagedNotification batchStagedNotification2 = createBatchStagedNotification(2L, 999902L, 9992L, "4363452500001010", "20180926", false);
    	List<BatchStagedNotification> batchStagedNotifications = new ArrayList<>();
    	batchStagedNotifications.add(batchStagedNotification1);
    	batchStagedNotifications.add(batchStagedNotification2);
    	
    	when(batchCardlinkNotificationRepository.updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotification1)).thenReturn(1);
    	when(batchCardlinkNotificationRepository.updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotification2)).thenReturn(1);
    	
    	itemWriter.write(batchStagedNotifications);

    	PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(2));
    	JMSUtils.sendCapsuleMessageToJMS((Capsule)Mockito.any(), Mockito.any());
    	PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(2));
		JMSUtils.sendCapsuleMessageToJMS((Capsule) Mockito.any(), Mockito.any());
    	    	
    	verify(batchCardlinkNotificationRepository).updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotification1);
    	verify(batchCardlinkNotificationRepository).updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotification2);
	}
	
	@Test
	public void testWriterUpdateFail() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		BatchStagedNotification batchStagedNotification = createBatchStagedNotification(1L, 999901L, 9991L, "4363452500001009", "20180926", false);
    	List<BatchStagedNotification> batchStagedNotifications = new ArrayList<>();
    	batchStagedNotifications.add(batchStagedNotification);
    	
    	when(batchCardlinkNotificationRepository.updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotification)).thenThrow(BatchException.class);
    	
    	itemWriter.write(batchStagedNotifications);
    	
    	assertTrue(stepExecution.getJobExecution().getAllFailureExceptions().size()>0);
	}
	
	@Test
	public void testShutdownFail() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		BatchStagedNotification batchStagedNotification1 = createBatchStagedNotification(1L, 999901L, 9991L, "4363452500001009", "20180926", false);
		BatchStagedNotification batchStagedNotification2 = createBatchStagedNotification(2L, 999902L, 9992L, "4363452500001010", "20180926", false);
    	List<BatchStagedNotification> batchStagedNotifications = new ArrayList<>();
    	batchStagedNotifications.add(batchStagedNotification1);
    	batchStagedNotifications.add(batchStagedNotification2);
    	
    	PowerMockito.when(JMSUtils.class, "sendCapsuleMessageToJMS", Mockito.any(Capsule.class), Mockito.any(JMSConfig.class)).thenThrow(InvalidDestinationException.class);
    	
    	itemWriter.write(batchStagedNotifications);
    	
    	verify(mockAppender, times(4)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent1 = (LoggingEvent)captorLoggingEvent.getAllValues().get(2);
    	LoggingEvent loggingEvent2 = (LoggingEvent)captorLoggingEvent.getAllValues().get(3);
    	
    	assertEquals(Level.ERROR, loggingEvent1.getLevel());
    	assertEquals(Level.ERROR, loggingEvent2.getLevel());
	}
	
	@Test
	public void testGenerateNotificationPayload() throws Exception {
		BatchStagedNotification batchStagedNotification = createBatchStagedNotification(1L, 999901L, 9991L, "4363452500001009", "20180926", false);    	
    	NotificationPayload actual = generateNotificationPayload(batchStagedNotification);
    	
    	NotificationPayload expected = new NotificationPayload(9991, jmsConfigProperties.getEventCode(), now);
    	expected.setActionableRefData("4363452500001009");
    	Map<String,String> data =new HashMap<String,String>();
		data.put("cardNo", "4363452500001009");
		//data.put("dueDate", "20180926");
		data.put("dueDate", "1537916400000");
		expected.setData(data);
    	
    	assertEquals(JsonUtils.convertObjectToString(expected), JsonUtils.convertObjectToString(actual));
	}
	
	private BatchStagedNotification createBatchStagedNotification(long id, long jobExecutionId, long userId, String data3, String data4, boolean isProcessed) {
		BatchStagedNotification batchStagedNotification = new BatchStagedNotification();
		batchStagedNotification.setId(id);
		batchStagedNotification.setJobExecutionId(jobExecutionId);
		batchStagedNotification.setFileName("test.txt");
		batchStagedNotification.setEventCode(jmsConfigProperties.getEventCode());
		batchStagedNotification.setKeyType("CC");
		batchStagedNotification.setUserId(userId);
		batchStagedNotification.setData3(data3);
		batchStagedNotification.setData4(data4);
		batchStagedNotification.setCreatedTime(new Date());
		batchStagedNotification.setCreatedBy("LDCPD6004B");
		batchStagedNotification.setUpdatedTime(new Date());
		batchStagedNotification.setUpdateBy("LDCPD6004B");
		batchStagedNotification.setProcessed(isProcessed);
		return batchStagedNotification;
	}
	
	private NotificationPayload generateNotificationPayload(BatchStagedNotification batchStagedNotification) {
		Integer userId = Integer.parseInt(Long.toString(batchStagedNotification.getUserId()));
		String eventCode = batchStagedNotification.getEventCode();
		NotificationPayload notificationPayload = new NotificationPayload(userId, eventCode, now);
		String actionableRefData = batchStagedNotification.getData3();
		notificationPayload.setActionableRefData(actionableRefData);
		Map<String,String> data =new HashMap<String,String>();
		data.put("cardNo", batchStagedNotification.getData3());
		//data.put("dueDate", batchStagedNotification.getData4());
		String data4 = batchStagedNotification.getData4() + " 00:00";
		LocalDateTime ldt = LocalDateTime.parse(data4, DateTimeFormatter.ofPattern("yyyyMMdd HH:mm"));
		Instant i = ldt.atZone(ZoneId.systemDefault()).toInstant();
		long dueDate = i.toEpochMilli();
		data.put("dueDate", Long.toString(dueDate));
		notificationPayload.setData(data);
		return notificationPayload;
	}

}
