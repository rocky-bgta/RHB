package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.naming.NamingException;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.model.Capsule;
import freemarker.template.Configuration;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
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
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.jms.InvalidDestinationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.JMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBGRejectTxnRespositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

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
public class SendIBGRejectedNotificationStepTest extends BaseJobTest {

	private static final String STEP_NAME = "SendIBGRejectedNotificationStep";

	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedIBGRejectTxn> itemReader;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<BatchStagedIBGRejectTxn, BatchStagedIBGRejectTxn> itemProcessor;
    
	@Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<BatchStagedIBGRejectTxn> itemWriter;

	@MockBean
	private BatchSuspenseRepositoryImpl batchSuspenseRepository;
	
	@MockBean
	private BatchStagedIBGRejectTxnRespositoryImpl batchStagedIBGRejectTxnRespository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	private StepExecution stepExecution;
	
	@Before
	public void beforeLocalTest() throws SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, SecurityException {
		super.beforeTest();
		Mockito.reset(batchSuspenseRepository);
		Mockito.reset(batchStagedIBGRejectTxnRespository);
	}
	
	@After
	public void afterLocalTest() {
		stepExecution.getJobExecution().getExecutionContext().remove(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID);
	}
	
	public StepExecution getStepExection() {
		stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.getJobExecution().getExecutionContext().putString(
			BatchJobParameter.BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID,
			"9999");
		return stepExecution;
	}
	
	/*
	 * Test reader to ensure it able to retrieve correct amount of records from DB
	 */
	@Test
	public void testPositiveReader() throws UnexpectedInputException, ParseException, Exception {
		String jobExecutionId = "9999";

		String beforeSQL1 = String.format("DELETE FROM TBL_BATCH_STAGED_IBG_REJECT_TXN WHERE JOB_EXECUTION_ID = %s", jobExecutionId);
    	String beforeSQL2 = String.format("DELETE FROM TBL_BATCH_JOB_EXECUTION WHERE JOB_EXECUTION_ID = %s", jobExecutionId);
		String beforeSQL3 = String.format("INSERT INTO TBL_BATCH_JOB_EXECUTION (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME) VALUES(%s, 1, 1, NOW())", jobExecutionId);
    	String beforeSQL4 = String.format("INSERT INTO TBL_BATCH_STAGED_IBG_REJECT_TXN (JOB_EXECUTION_ID, DATE, TELLER, TRACE, REF1, NAME, AMOUNT, REJECT_CODE, REJECT_DESCRIPTION, ACCOUNT_NO, USER_ID, BENE_NAME, BENE_ACCOUNT, IS_PROCESSED, IS_NOTIFICATION_SENT, CREATED_TIME, UPDATED_TIME, FILE_NAME) VALUES(%s, 20180831, 111111, 111111, 'TESTER1_REF', 'TESTER1', 10000, 'R01', 'A/C CLOSED', 111111, 111111, 'TESTER1_BENE', 111111, 1, 0, NOW(), NOW(), 'TESTER1_FILE')", jobExecutionId);
    	String beforeSQL5 = String.format("INSERT INTO TBL_BATCH_STAGED_IBG_REJECT_TXN (JOB_EXECUTION_ID, DATE, TELLER, TRACE, REF1, NAME, AMOUNT, REJECT_CODE, REJECT_DESCRIPTION, ACCOUNT_NO, USER_ID, BENE_NAME, BENE_ACCOUNT, IS_PROCESSED, IS_NOTIFICATION_SENT, CREATED_TIME, UPDATED_TIME, FILE_NAME) VALUES(%s, 20180831, 222222, 222222, 'TESTER2_REF', 'TESTER2', 20000, 'R02', 'NETWORK ISSUES', 222222, 222222, 'TESTER2_BENE', 222222, 1, 0, NOW(), NOW(), 'TESTER2_FILE')", jobExecutionId);
    	String beforeSQL6 = String.format("INSERT INTO TBL_BATCH_STAGED_IBG_REJECT_TXN (JOB_EXECUTION_ID, DATE, TELLER, TRACE, REF1, NAME, AMOUNT, REJECT_CODE, REJECT_DESCRIPTION, ACCOUNT_NO, USER_ID, BENE_NAME, BENE_ACCOUNT, IS_PROCESSED, IS_NOTIFICATION_SENT, CREATED_TIME, UPDATED_TIME, FILE_NAME) VALUES(%s, 20180831, 333333, 333333, 'TESTER3_REF', 'TESTER3', 30000, 'R03', '', 333333, 333333, 'TESTER3_BENE', 333333, 1, 0, NOW(), NOW(), 'TESTER3_FILE')", jobExecutionId);
    	String beforeSQL7 = String.format("INSERT INTO TBL_BATCH_STAGED_IBG_REJECT_TXN (JOB_EXECUTION_ID, DATE, TELLER, TRACE, REF1, NAME, AMOUNT, REJECT_CODE, REJECT_DESCRIPTION, ACCOUNT_NO, USER_ID, BENE_NAME, BENE_ACCOUNT, IS_PROCESSED, IS_NOTIFICATION_SENT, CREATED_TIME, UPDATED_TIME, FILE_NAME) VALUES(%s, 20180831, 444444, 444444, 'TESTER4_REF', 'TESTER4', 40000, 'R04', 'UNSUFFICIENT MONEY', 444444, 444444, 'TESTER4_BENE', 444444, 0, 0, NOW(), NOW(), 'TESTER4_FILE')", jobExecutionId);
    	jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5, beforeSQL6, beforeSQL7);
		
    	List<BatchStagedIBGRejectTxn> results = 
    		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedIBGRejectTxn>>() {
				public List<BatchStagedIBGRejectTxn> call() throws Exception {
					BatchStagedIBGRejectTxn batchStagedIBGRejectTxn;
					List<BatchStagedIBGRejectTxn> batchStagedIBGRejectTxns = new ArrayList<>();
					while((batchStagedIBGRejectTxn = itemReader.read()) != null) {
						batchStagedIBGRejectTxns.add(batchStagedIBGRejectTxn);
					}
					return batchStagedIBGRejectTxns;
				}
		});
		
    	assertEquals(3, results.size());
    	BatchStagedIBGRejectTxn expectedBatchStagedIBGRejectTxn1 = createBatchStagedIBGRejectTxn(
    		111111, "10000", "TESTER1_BENE", "20180831", "A/C CLOSED");
    	BatchStagedIBGRejectTxn expectedBatchStagedIBGRejectTxn2 = createBatchStagedIBGRejectTxn(
    		222222, "20000", "TESTER2_BENE", "20180831", "NETWORK ISSUES");
    	BatchStagedIBGRejectTxn expectedBatchStagedIBGRejectTxn3 = createBatchStagedIBGRejectTxn(
    		333333, "30000", "TESTER3_BENE", "20180831", "");
		
    	compareBatchStagedIBGRejectTxn(expectedBatchStagedIBGRejectTxn1, results.get(0));
    	compareBatchStagedIBGRejectTxn(expectedBatchStagedIBGRejectTxn2, results.get(1));
    	compareBatchStagedIBGRejectTxn(expectedBatchStagedIBGRejectTxn3, results.get(2));
    	
		String afterSQL1 = beforeSQL1;
    	String afterSQL2 = beforeSQL2;
    	jdbcTemplate.batchUpdate(afterSQL1, afterSQL2);
	}

	private void compareBatchStagedIBGRejectTxn(BatchStagedIBGRejectTxn expected, BatchStagedIBGRejectTxn actual) {
		assertEquals(expected.getUserId(), actual.getUserId());
		assertEquals(expected.getAmount(), actual.getAmount());
		assertEquals(expected.getBeneName(), actual.getBeneName());
		assertEquals(expected.getDate(), actual.getDate());
		assertEquals(expected.getUserId(), actual.getUserId());
	}
	
	private BatchStagedIBGRejectTxn createBatchStagedIBGRejectTxn(
		int userId, String amount, String beneName, String date, String rejectDescription) {
		
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = new BatchStagedIBGRejectTxn();
		// Id might change due to sequence or identity, we cant compare it
//		batchStagedIBGRejectTxn.setId(id);
		batchStagedIBGRejectTxn.setUserId(userId);
		batchStagedIBGRejectTxn.setAmount(amount);
		batchStagedIBGRejectTxn.setBeneName(beneName);
		batchStagedIBGRejectTxn.setDate(date);
		batchStagedIBGRejectTxn.setRejectDescription(rejectDescription);
		
		return batchStagedIBGRejectTxn;
	}
	
	/*
	 * Test to ensure the processor shall not alter the object at all
	 */
	@Test
	public void testPositiveProcessor() throws Exception {
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn("id=4307, jobExecutionId=0, date=20180831, teller=null, trace=null, ref1=null, name=null, amount=10000, rejectCode=null, rejectDescription=A/C CLOSED, accountNo=null, userId=111111, beneName=TESTER1_BENE, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null");
		assertEquals(batchStagedIBGRejectTxn, itemProcessor.process(batchStagedIBGRejectTxn));
	}
	
	
	
	// Not possible to compare empty string against number type of column in embedded datasource like HSQL
	// it will it casting exception due to that ignore this test
//	@Test
//	public void testNegativeReaderEmptyJobExecutionId() throws Exception {    	
//		stepExecution.getJobExecution().getExecutionContext().putString(
//			BatchJobParameter.BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID,
//			"");
//		
//		int count = StepScopeTestUtils.doInStepScope(stepExecution, new Callable<Integer>() {
//			public Integer call() throws Exception {
//				int count = 0;
//				while (itemReader.read() != null) {
//					count++;
//				}
//				return count;
//			}
//		});
//		assertEquals(0, count);
//	}
	
	/*
	 * Test to ensure processor shall treat the object invalid if either of it mandatory columns is empty
	 */
	@Test
	public void testNegativeProcessorEmptyInMandatoryColumns() throws Exception {
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn("id=4309, jobExecutionId=0, date=, teller=, trace=, ref1=null, name=null, amount=, rejectCode=null, rejectDescription=, accountNo=null, userId=333333, beneName=, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null");
		assertNull(itemProcessor.process(batchStagedIBGRejectTxn));
		verify(batchSuspenseRepository, times(1)).addBatchSuspenseToDB(Mockito.<BatchSuspense>any());
	}
	
	/*
	 * Test to ensure processor shall treat the object invalid if either of it mandatory columns is null
	 */
	@Test
	public void testNegativeProcessorNullInMandatoryColumns() throws Exception {
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn("id=4309, jobExecutionId=0, date=null, teller=null, trace=null, ref1=null, name=null, amount=null, rejectCode=null, rejectDescription=null, accountNo=null, userId=null, beneName=null, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null");
		assertNull(itemProcessor.process(batchStagedIBGRejectTxn));
		verify(batchSuspenseRepository, times(1)).addBatchSuspenseToDB(Mockito.<BatchSuspense>any());
	}
	
	/*
	 * Test to ensure processor shall treat the object invalid if either of it mandatory columns is empty or null
	 */
	@Test
	public void testNegativeProcessorEmptyOrNullInMandatoryColumns() throws Exception {
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn("id=4309, jobExecutionId=0, date=, teller=null, trace=null, ref1=null, name=null, amount=, rejectCode=null, rejectDescription=null, accountNo=null, userId=null, beneName=null, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null");
		assertNull(itemProcessor.process(batchStagedIBGRejectTxn));
		verify(batchSuspenseRepository, times(1)).addBatchSuspenseToDB(Mockito.<BatchSuspense>any());
	}
	
	/*
	 * Test to ensure if DB service down, the processor just skip the current object and proceed to the next one without break the JOB
	 */
	@Test
	public void testNegativeProcessorDBServiceDown() throws Exception {
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn("id=4309, jobExecutionId=0, date=null, teller=null, trace=null, ref1=null, name=null, amount=null, rejectCode=null, rejectDescription=null, accountNo=null, userId=333333, beneName=null, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null");
		when(batchSuspenseRepository.addBatchSuspenseToDB(Mockito.<BatchSuspense>any())).thenThrow(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		assertNull(itemProcessor.process(batchStagedIBGRejectTxn));
	}
	
	/*
	 * Test to ensure the writer manage to send message to JMS queue and insert the record for the object into the DB
	 */
	@Test
	public void tesPositiveWriter() throws Exception {
    	PowerMockito.mockStatic(JMSUtils.class);
    	
    	BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn("id=4307, jobExecutionId=0, date=20180831, teller=null, trace=null, ref1=null, name=null, amount=10000, rejectCode=null, rejectDescription=A/C CLOSED, accountNo=null, userId=111111, beneName=TESTER1_BENE, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null");
    	List<BatchStagedIBGRejectTxn> batchStagedIBKPaymentTxns = new ArrayList<>();
		batchStagedIBKPaymentTxns.add(batchStagedIBGRejectTxn);
    	
		itemWriter.write(batchStagedIBKPaymentTxns);
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		// Beside of the event timestamp other info is static and we can compare them

		JMSUtils.sendCapsuleMessageToJMS(Mockito.any(Capsule.class),Mockito.any());
		verify(batchStagedIBGRejectTxnRespository, times(1)).updateIsNotificationSent(batchStagedIBGRejectTxn);
	}
	
	/*
	 * Test to ensure when DB service is down, the writer still manage to close the JMS connection at the end
	 */
	@Test
	public void tesNegativetWriterDBServiceDown() throws Exception {
    	PowerMockito.mockStatic(JMSUtils.class);
    	
    	BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn("id=4307, jobExecutionId=0, date=20180831, teller=null, trace=null, ref1=null, name=null, amount=10000, rejectCode=null, rejectDescription=A/C CLOSED, accountNo=null, userId=111111, beneName=TESTER1_BENE, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null");
    	List<BatchStagedIBGRejectTxn> batchStagedIBKPaymentTxns = new ArrayList<>();
		batchStagedIBKPaymentTxns.add(batchStagedIBGRejectTxn);
		
    	when(batchStagedIBGRejectTxnRespository.updateIsNotificationSent(batchStagedIBGRejectTxn)).thenThrow(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		itemWriter.write(batchStagedIBKPaymentTxns);
		
		verify(mockAppender, times(3)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent = (LoggingEvent)captorLoggingEvent.getAllValues().get(2);
        //Check log level
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        //Check the message being logged
        //assertTrue(loggingEvent.getRenderedMessage().contains("Error happened while pushing notification payload to JMS queue for BatchStagedIBGRejectTxn [BatchStagedIBGRejectTxn(id=4307, jobExecutionId=0, date=20180831, teller=null, trace=null, ref1=null, name=null, amount=100.00, rejectCode=null, rejectDescription=A/C CLOSED, accountNo=null, userId=111111, beneName=TESTER1_BENE, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null, fileName=null)]"));

		String expectedJobExecutionId = "9999";
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID), expectedJobExecutionId);
	}
	
	/*
	 * Test to ensure even when JMS service is down, the writer still proceed like normal
	 */
	@Test
	public void tesNegativetWriterJMSServiceDown() throws Exception {
    	PowerMockito.mockStatic(JMSUtils.class);
    	
    	BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn("id=4307, jobExecutionId=0, date=20180831, teller=null, trace=null, ref1=null, name=null, amount=10000, rejectCode=null, rejectDescription=A/C CLOSED, accountNo=null, userId=111111, beneName=TESTER1_BENE, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null");
    	List<BatchStagedIBGRejectTxn> batchStagedIBKPaymentTxns = new ArrayList<>();
		batchStagedIBKPaymentTxns.add(batchStagedIBGRejectTxn);
		
		PowerMockito.when(JMSUtils.class, "sendMessageToJMS", Mockito.anyString(), Mockito.<JMSConfig>any()).thenThrow(InvalidDestinationException.class);
		
    	itemWriter.write(batchStagedIBKPaymentTxns);
		
    	verify(mockAppender, times(2)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent = (LoggingEvent)captorLoggingEvent.getAllValues().get(1);
        //Check log level
        //assertEquals(Level.ERROR, loggingEvent.getLevel());
        //Check the message being logged
        //assertTrue(loggingEvent.getRenderedMessage().contains("Error happened while pushing notification payload to JMS queue for BatchStagedIBGRejectTxn [BatchStagedIBGRejectTxn(id=4307, jobExecutionId=0, date=20180831, teller=null, trace=null, ref1=null, name=null, amount=100.00, rejectCode=null, rejectDescription=A/C CLOSED, accountNo=null, userId=111111, beneName=TESTER1_BENE, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null, fileName=null)]"));

		String expectedJobExecutionId = "9999";
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID), expectedJobExecutionId);
	}
	
	/*
	 * Test to ensure even when JMS service is down, even though all JMS interactions failed, the writer still proceed like normal
	 * NOTES: No longer valid since we let Spring to help close the resource after job end
	 */
//	@Test
	public void tesNegativetWriterAllJMSServiceInteractionsFailed() throws Exception {
    	PowerMockito.mockStatic(JMSUtils.class);
    	
    	BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn("id=4307, jobExecutionId=0, date=20180831, teller=null, trace=null, ref1=null, name=null, amount=10000, rejectCode=null, rejectDescription=A/C CLOSED, accountNo=null, userId=111111, beneName=TESTER1_BENE, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null");
    	List<BatchStagedIBGRejectTxn> batchStagedIBKPaymentTxns = new ArrayList<>();
		batchStagedIBKPaymentTxns.add(batchStagedIBGRejectTxn);
		
		PowerMockito.when(JMSUtils.class, "setupJMS", Mockito.<JMSConfig>any(), Mockito.anyString(), Mockito.<JMSConfigProperties>any()).thenThrow(NamingException.class);
//		PowerMockito.when(JMSUtils.class, "shutdownJMS", Mockito.<JMSConfig>any()).thenThrow(NamingException.class);
		
    	itemWriter.write(batchStagedIBKPaymentTxns);
		
    	verify(mockAppender, times(4)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent1 = (LoggingEvent)captorLoggingEvent.getAllValues().get(2);
    	LoggingEvent loggingEvent2 = (LoggingEvent)captorLoggingEvent.getAllValues().get(3);
        //Check log level
        assertEquals(Level.ERROR, loggingEvent1.getLevel());
        assertEquals(Level.ERROR, loggingEvent2.getLevel());
        //Check the message being logged
        assertTrue(loggingEvent1.getRenderedMessage().contains("Error happened while pushing notification payload to JMS queue for BatchStagedIBGRejectTxn [BatchStagedIBGRejectTxn(id=4307, jobExecutionId=0, date=20180831, teller=null, trace=null, ref1=null, name=null, amount=100.00, rejectCode=null, rejectDescription=A/C CLOSED, accountNo=null, userId=111111, beneName=TESTER1_BENE, beneAccount=null, isProcessed=false, isNotificationSent=false, createdTime=null, updatedTime=null, fileName=null)]"));
        assertTrue(loggingEvent2.getRenderedMessage().contains(String.format("Failed to close the JMSContext / NamingContext in [%s:%s]", smsJMSConfigProperties.getHost(), smsJMSConfigProperties.getPort())));
        
		String expectedJobExecutionId = "9999";
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID), expectedJobExecutionId);
	}
	
	private Map<String, String> getKeyValueMap(String line) {
		Map<String, String> keyValueMap = new HashMap<>();
		String[] keyValuePairs = line.split(",");
		for(String keyValuePair : keyValuePairs) {
			String[] values = keyValuePair.split("=");
			if(values.length == 1) {
				keyValueMap.put(values[0].trim(), "");
			} else {
				keyValueMap.put(values[0].trim(), values[1].trim());
			}
			
		}
		return keyValueMap;
	}
	
	private BatchStagedIBGRejectTxn createBatchStagedIBGRejectTxn(String line) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, SecurityException {
		Map<String, String> keyValueMap = getKeyValueMap(line);
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = new BatchStagedIBGRejectTxn();
		for(Entry<String, String> entry : keyValueMap.entrySet()) {
			if(!entry.getValue().equals("null")) {
				Field field = batchStagedIBGRejectTxn.getClass().getDeclaredField(entry.getKey());
				Class<?> type = field.getType();
				Object value = castStringToType(entry.getValue(), type);
				if(type.equals(Boolean.class) || type.getName().equals("boolean")) {
					field.setAccessible(true);
					field.set(batchStagedIBGRejectTxn, value);
				} else {
					PropertyUtils.setProperty(batchStagedIBGRejectTxn, entry.getKey(), value);
				}
			}
		}
		return batchStagedIBGRejectTxn;
	}
	
	protected Object castStringToType(String value, Class<?> type) {
		if(type.equals(String.class)) {
			return value;
		} else if (type.equals(Integer.class) || type.getName().equals("int")) {
			return Integer.parseInt(value);
		} else if (type.equals(Long.class) || type.getName().equals("long")) {
			return Long.parseLong(value);
		} else if (type.equals(Boolean.class) || type.getName().equals("boolean")) {
			return Boolean.parseBoolean(value);
		} else {
			return null;
		}
	}
}
