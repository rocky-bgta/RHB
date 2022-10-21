package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JsonUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PushMassNotificationsProcessorJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotifMass;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotifMassRepositoryImpl;
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
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class SendPushMassNotificationsStepTest extends BaseJobTest {
	
	private static final String STEP_NAME = "SendPushMassNotificationsStep";
	
	private Instant now = Instant.now();
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedNotifMass> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedNotifMass, BatchStagedNotifMass> itemProcessor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedNotifMass> itemWriter;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private PushMassNotificationsProcessorJobConfigProperties jmsConfigProperties;
	
	@MockBean
	private BatchStagedNotifMassRepositoryImpl repository;

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
	
	/**
	 * Positive test reader to ensure it able to retrieve correct amount of records from DB for throttle size -1
	 * @throws Exception
	 */
	@Test
	public void testPositiveReaderAllThrottleNegative1() throws Exception {
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIF_MASS";
		String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_NOTIF_MASS "
				+ "(JOB_EXECUTION_ID, FILE_NAME, EVENT_CODE, CONTENT, USER_ID, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999901, '101020180516.txt', '90002', 'RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.', 12345678, 0, 'LDCPA6006B', NOW(), 'LDCPA6006B', NOW())";
		String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_NOTIF_MASS "
				+ "(JOB_EXECUTION_ID, FILE_NAME, EVENT_CODE, CONTENT, USER_ID, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999902, '101020180516.txt', '90002', 'RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.', 12345679, 0, 'LDCPA6006B', NOW(), 'LDCPA6006B', NOW())";
		String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_NOTIF_MASS "
				+ "(JOB_EXECUTION_ID, FILE_NAME, EVENT_CODE, CONTENT, USER_ID, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999903, '101020180516.txt', '90002', 'RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.', 12345680, 0, 'LDCPA6006B', NOW(), 'LDCPA6006B', NOW())";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4);
		
		// Throttle size = -1
		jmsConfigProperties.setThrottleSize(-1);
		
		List<BatchStagedNotifMass> results = 
				StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedNotifMass>>() {
					public List<BatchStagedNotifMass> call() throws Exception {
						BatchStagedNotifMass batchStagedNotifMass;
						List<BatchStagedNotifMass> batchStagedNotifMassList = new ArrayList<>();
						while((batchStagedNotifMass = itemReader.read()) != null) {
							batchStagedNotifMassList.add(batchStagedNotifMass);
						}
						return batchStagedNotifMassList;
					}
					
				});
		
		assertEquals(3, results.size());
		
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIF_MASS";
    	jdbcTemplate.batchUpdate(afterSQL1);
	}
	
	/**
	 * Positive test reader to ensure it able to retrieve correct amount of records from DB for throttle size greater than 1
	 * @throws Exception
	 */
	@Test
	public void testPositiveReaderAllThrottleGreaterThan1() throws Exception {
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIF_MASS";
		String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_NOTIF_MASS "
				+ "(JOB_EXECUTION_ID, FILE_NAME, EVENT_CODE, CONTENT, USER_ID, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999901, '101020180516.txt', '90002', 'RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.', 12345678, 0, 'LDCPA6006B', NOW(), 'LDCPA6006B', NOW())";
		String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_NOTIF_MASS "
				+ "(JOB_EXECUTION_ID, FILE_NAME, EVENT_CODE, CONTENT, USER_ID, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999902, '101020180516.txt', '90002', 'RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.', 12345679, 0, 'LDCPA6006B', NOW(), 'LDCPA6006B', NOW())";
		String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_NOTIF_MASS "
				+ "(JOB_EXECUTION_ID, FILE_NAME, EVENT_CODE, CONTENT, USER_ID, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999903, '101020180516.txt', '90002', 'RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.', 12345680, 0, 'LDCPA6006B', NOW(), 'LDCPA6006B', NOW())";
		String beforeSQL5 = "INSERT INTO TBL_BATCH_STAGED_NOTIF_MASS "
				+ "(JOB_EXECUTION_ID, FILE_NAME, EVENT_CODE, CONTENT, USER_ID, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999904, '101020180516.txt', '90002', 'RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.', 12345681, 0, 'LDCPA6006B', NOW(), 'LDCPA6006B', NOW())";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		
		// Throttle size = 4
		jmsConfigProperties.setThrottleSize(4);
		
		List<BatchStagedNotifMass> results = 
				StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedNotifMass>>() {
					public List<BatchStagedNotifMass> call() throws Exception {
						BatchStagedNotifMass batchStagedNotifMass;
						List<BatchStagedNotifMass> batchStagedNotifMassList = new ArrayList<>();
						while((batchStagedNotifMass = itemReader.read()) != null) {
							batchStagedNotifMassList.add(batchStagedNotifMass);
						}
						return batchStagedNotifMassList;
					}
					
				});
		
		assertEquals(4, results.size());
		
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIF_MASS";
    	jdbcTemplate.batchUpdate(afterSQL1);
	}
	
	/**
	 * Positive test to ensure the writer manage to send the correct message to the JMS
	 * @throws Exception
	 */
	@Test
	public void testPositiveWriterSendJMSSuccessfully() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		BatchStagedNotifMass batchStagedNotifMass1 = 
				createBatchStagedNotifMass(1L, 999901L, "90002", 12345678, "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", false);
		BatchStagedNotifMass batchStagedNotifMass2 = 
				createBatchStagedNotifMass(2L, 999902L, "90002", 12345679, "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", false);
    	List<BatchStagedNotifMass> batchStagedNotifMassList = new ArrayList<>();
    	batchStagedNotifMassList.add(batchStagedNotifMass1);
    	batchStagedNotifMassList.add(batchStagedNotifMass2);
    	
    	when(repository.updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotifMass1)).thenReturn(1);
    	when(repository.updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotifMass2)).thenReturn(1);
    	
    	itemWriter.write(batchStagedNotifMassList);

    	PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(2));
    	JMSUtils.sendCapsuleMessageToJMS((Capsule)Mockito.any(), Mockito.any());
    	PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(2));
		JMSUtils.sendCapsuleMessageToJMS((Capsule) Mockito.any(), Mockito.any());
    	    	
    	verify(repository).updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotifMass1);
    	verify(repository).updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotifMass2);
	}
	
	/**
	 * Negative test if ItemWriter failed
	 * @throws Exception
	 */
	@Test
	public void testWriterUpdateFail() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		BatchStagedNotifMass batchStagedNotifMass = 
				createBatchStagedNotifMass(1L, 999901L, "90002", 12345678, "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", false);
    	List<BatchStagedNotifMass> batchStagedNotifMassList = new ArrayList<>();
    	batchStagedNotifMassList.add(batchStagedNotifMass);
    	
    	when(repository.updateIsProcessed(jmsConfigProperties.getBatchCode(), batchStagedNotifMass)).thenThrow(BatchException.class);
    	
    	itemWriter.write(batchStagedNotifMassList);
    	
    	assertTrue(stepExecution.getJobExecution().getAllFailureExceptions().size()>0);
	}
	
	/**
	 * Negative test if JMS server shutdown
	 * @throws Exception
	 */
	@Test
	public void testShutdownFail() throws Exception {
		PowerMockito.mockStatic(JMSUtils.class);
		
		BatchStagedNotifMass batchStagedNotifMass1 = 
				createBatchStagedNotifMass(1L, 999901L, "90002", 12345678, "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", false);
		BatchStagedNotifMass batchStagedNotifMass2 = 
				createBatchStagedNotifMass(2L, 999902L, "90002", 12345679, "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", false);
    	List<BatchStagedNotifMass> batchStagedNotifMassList = new ArrayList<>();
    	batchStagedNotifMassList.add(batchStagedNotifMass1);
    	batchStagedNotifMassList.add(batchStagedNotifMass2);
    	
    	PowerMockito.when(JMSUtils.class, "sendCapsuleMessageToJMS", Mockito.any(Capsule.class), Mockito.any(JMSConfig.class)).thenThrow(InvalidDestinationException.class);
    	
    	itemWriter.write(batchStagedNotifMassList);
    	
    	verify(mockAppender, times(4)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent1 = (LoggingEvent)captorLoggingEvent.getAllValues().get(2);
    	LoggingEvent loggingEvent2 = (LoggingEvent)captorLoggingEvent.getAllValues().get(3);
    	
    	assertEquals(Level.ERROR, loggingEvent1.getLevel());
    	assertEquals(Level.ERROR, loggingEvent2.getLevel());
	}
	
	/**
	 * Positive test on notification payload
	 * @throws Exception
	 */
	@Test
	public void testGenerateNotificationPayload() throws Exception {
		BatchStagedNotifMass batchStagedNotifMass = 
				createBatchStagedNotifMass(1L, 999901L, "90002", 12345678, "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", false);
    	NotificationPayload actual = generateNotificationPayload(batchStagedNotifMass);
    	
    	NotificationPayload expected = new NotificationPayload(12345678, "90002", now);
    	Map<String,String> data =new HashMap<String,String>();
    	data.put("msgContent", batchStagedNotifMass.getContent());
		expected.setData(data);
    	
    	assertEquals(JsonUtils.convertObjectToString(expected), JsonUtils.convertObjectToString(actual));
	}
	
	private BatchStagedNotifMass createBatchStagedNotifMass(long id, long jobExecutionId, String eventCode, long userId, String content, boolean isProcessed) {
		BatchStagedNotifMass batchStagedNotifMass = new BatchStagedNotifMass();
		batchStagedNotifMass.setId(id);
		batchStagedNotifMass.setJobExecutionId(jobExecutionId);
		batchStagedNotifMass.setFileName("test.txt");
		batchStagedNotifMass.setEventCode(eventCode);
		batchStagedNotifMass.setContent(content);
		batchStagedNotifMass.setUserId(userId);
		batchStagedNotifMass.setCreatedTime(new Date());
		batchStagedNotifMass.setCreatedBy(jmsConfigProperties.getBatchCode());
		batchStagedNotifMass.setUpdatedTime(new Date());
		batchStagedNotifMass.setUpdatedBy(jmsConfigProperties.getBatchCode());
		batchStagedNotifMass.setProcessed(isProcessed);
		return batchStagedNotifMass;
	}
	
	private NotificationPayload generateNotificationPayload(BatchStagedNotifMass batchStagedNotifMass) {
		Integer userId = Integer.parseInt(Long.toString(batchStagedNotifMass.getUserId()));
		String eventCode = batchStagedNotifMass.getEventCode();
		NotificationPayload notificationPayload = new NotificationPayload(userId, eventCode, now);
		Map<String,String> data =new HashMap<String,String>();
		data.put("msgContent", batchStagedNotifMass.getContent());
		notificationPayload.setData(data);
		return notificationPayload;
	}

}
