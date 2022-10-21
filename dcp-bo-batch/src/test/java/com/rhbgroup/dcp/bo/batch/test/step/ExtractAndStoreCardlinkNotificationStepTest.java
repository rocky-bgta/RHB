package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.CardlinkNotification;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchCardlinkNotificationsRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class ExtractAndStoreCardlinkNotificationStepTest extends BaseJobTest {
	
	private static final Logger logger = Logger.getLogger(ExtractAndStoreCardlinkNotificationStepTest.class);

	private static final String STEP_NAME = "ExtractCardlinkNotificationStep";
	
    //column [for view notification]
	private String FILE_NAME = "101020180516.txt";
	private String PROCESS_DATE = "20181009";
	private String EVENT_CODE = "50000";
	private String KEY_TYPE ="CC";
	private String SYSTEM_DATE = "20181010";
	private String SYSTEM_TIME = "101010";
	private String CARD_NUMBER = "0004570660000114040";
	private String PAYMENT_DUE_DATE = "2018111";
	private String CARD_TYPE = "P";
	private String MINIMUM_AMOUNT = "150.00";
	private String OUTSTANDING_AMOUNT = "1200.00";
	private String STATEMENT_AMOUNT = "330.00";
	private String STATEMENT_DATE = "20181010";
	private Long NOTIFICATION_RAW_ID = 0L;
	private Long USER_ID= 3L;
	
	private String deleteSQL = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION_RAW";

	
    private StepExecution stepExecution;

    public StepExecution getStepExection() {
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
        return stepExecution;
    }
    
	@MockBean
	private BatchCardlinkNotificationsRepositoryImpl mockCardlinkNotificationsRepositoryImpl;

	@Autowired
    @Qualifier(STEP_NAME + ".ItemReader")
    private ItemReader<CardlinkNotification> itemReader;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<CardlinkNotification,CardlinkNotification> itemProcessor;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<CardlinkNotification> itemWriter;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@After
	public void cleanup() {
		Mockito.reset(mockCardlinkNotificationsRepositoryImpl);
	}
	
    private void insertViewNotificationData(String processDate) {
    	logger.info("insert into vw_batch_cardlink_notification..");
		try {
			String insertSQL = String.format("insert into vw_batch_cardlink_notification (FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,SYSTEM_DATE,SYSTEM_TIME,CARD_NUMBER,PAYMENT_DUE_DATE,CARD_TYPE,MINIMUM_AMOUNT,OUTSTANDING_AMOUNT,STATEMENT_AMOUNT,STATEMENT_DATE,NOTIFICATION_RAW_ID,USER_ID) " +
				" values ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,%d)"
				, FILE_NAME, processDate, EVENT_CODE, KEY_TYPE, SYSTEM_DATE, SYSTEM_TIME, CARD_NUMBER, PAYMENT_DUE_DATE, CARD_TYPE, MINIMUM_AMOUNT, OUTSTANDING_AMOUNT, STATEMENT_AMOUNT, STATEMENT_DATE, 1, USER_ID);

			String insertSQL2 = String.format("insert into vw_batch_cardlink_notification (FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,SYSTEM_DATE,SYSTEM_TIME,CARD_NUMBER,PAYMENT_DUE_DATE,CARD_TYPE,MINIMUM_AMOUNT,OUTSTANDING_AMOUNT,STATEMENT_AMOUNT,STATEMENT_DATE,NOTIFICATION_RAW_ID,USER_ID) " +
				" values ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,%d)"
				, FILE_NAME, processDate, EVENT_CODE, KEY_TYPE, SYSTEM_DATE, SYSTEM_TIME, "0004570660000114041", PAYMENT_DUE_DATE, "S", MINIMUM_AMOUNT, "3000.00", "400.00", STATEMENT_DATE, 2, USER_ID);
			
			String insertSQL3 = String.format("insert into vw_batch_cardlink_notification (FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,SYSTEM_DATE,SYSTEM_TIME,CARD_NUMBER,PAYMENT_DUE_DATE,CARD_TYPE,MINIMUM_AMOUNT,OUTSTANDING_AMOUNT,STATEMENT_AMOUNT,STATEMENT_DATE,NOTIFICATION_RAW_ID,USER_ID) " +
				" values ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,%d)"
				, FILE_NAME, processDate, EVENT_CODE, KEY_TYPE, SYSTEM_DATE, SYSTEM_TIME, "0004570660000114042", PAYMENT_DUE_DATE, "S", MINIMUM_AMOUNT, "50000.00", "500.00", STATEMENT_DATE, 3, USER_ID);
			
			int []rows = jdbcTemplate.batchUpdate(insertSQL, insertSQL2, insertSQL3);
			logger.info("added row="+rows.length);
		}catch(Exception ex) {
			logger.info("insert into vw_batch_cardlink_notification exception:"  + ex.getMessage());
		}
    }
    
    private CardlinkNotification createCardlinkNotification(long notificationRawId,long userId) {
    	CardlinkNotification cardlink = new CardlinkNotification();
    	cardlink.setCardNumber("99034023942039");
    	cardlink.setCardType("P");
    	cardlink.setKeyType("CC");
    	cardlink.setEventCode("50000");
    	cardlink.setFileName("123123123.txt");
    	cardlink.setMinimumAmount("1000.00");
    	cardlink.setNotificationRawId(notificationRawId);
    	cardlink.setOutstandingAmount("5000.00");
    	cardlink.setPaymentDueDate("2019-10-10");
    	cardlink.setStatementAmount("1231231.00");
    	cardlink.setStatementDate("2018-10-10");
    	cardlink.setSystemDate("20181010");
    	cardlink.setSystemTime("101010");
    	cardlink.setUserId(userId);
    	
    	return cardlink;
    }
    
	@Test
	public void testPositiveReaderAllNotProcessYet() throws Exception {
		String insertSQL = String.format("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW (JOB_EXECUTION_ID,FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,DATA_6,DATA_7,DATA_8,DATA_9,DATA_10,IS_PROCESSED,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY) " +
				 " values (%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
				 ,0 , FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE,"1","2","3","4","5","6","7","8","9","10","0","2018-10-10 10:10:10","admin","2018-10-10 10:10:10","admin");
		String insertSQL2 = String.format("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW (JOB_EXECUTION_ID,FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,DATA_6,DATA_7,DATA_8,DATA_9,DATA_10,IS_PROCESSED,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY) " +
				" values (%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
				,0 , FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE,"1","2","3","4","5","6","7","8","9","10","0","2018-10-10 10:10:10","admin","2018-10-10 10:10:10","admin");

		String insertSQL3 = String.format("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW (JOB_EXECUTION_ID,FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,DATA_6,DATA_7,DATA_8,DATA_9,DATA_10,IS_PROCESSED,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY) " +
				" values (%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
				,0 , FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE,"1","2","3","4","5","6","7","8","9","10","0","2018-10-10 10:10:10","admin","2018-10-10 10:10:10","admin");

		jdbcTemplate.batchUpdate(deleteSQL);
		jdbcTemplate.batchUpdate(insertSQL, insertSQL2, insertSQL3);    	
		insertViewNotificationData("20180807");

    	Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-06");

		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
    	
    	List<CardlinkNotification> results = 
    		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<CardlinkNotification>>() {
				public List<CardlinkNotification> call() throws Exception {
					CardlinkNotification cardlinkNotification;
					List<CardlinkNotification> cardlinkNotificationList = new ArrayList<>();
					while((cardlinkNotification = itemReader.read()) != null) {
						cardlinkNotificationList.add(cardlinkNotification);
					}
					return cardlinkNotificationList;
				}
		});
    	
    	assertEquals(0, results.size());    	
    	jdbcTemplate.batchUpdate(deleteSQL);
	}
	
	@Test
	public void testPositiveReaderMixedInSameDay() throws Exception {
		String insertSQL = String.format("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW (JOB_EXECUTION_ID,FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,DATA_6,DATA_7,DATA_8,DATA_9,DATA_10,IS_PROCESSED,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY) " +
				 " values (%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
				 			,0 , FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE,"1","2","3","4","5","6","7","8","9","10","0","2018-10-10 10:10:10","admin","2018-10-10 10:10:10","admin");
		String insertSQL2 = String.format("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW (JOB_EXECUTION_ID,FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,DATA_6,DATA_7,DATA_8,DATA_9,DATA_10,IS_PROCESSED,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY) " +
				" values (%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
				,0 , FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE,"1","2","3","4","5","6","7","8","9","10","0","2018-10-10 10:10:10","admin","2018-10-10 10:10:10","admin");

		String insertSQL3 = String.format("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW (JOB_EXECUTION_ID,FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,DATA_6,DATA_7,DATA_8,DATA_9,DATA_10,IS_PROCESSED,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY) " +
				" values (%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
				,0 , FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE,"1","2","3","4","5","6","7","8","9","10","0","2018-10-10 10:10:10","admin","2018-10-10 10:10:10","admin");
		
		jdbcTemplate.batchUpdate(deleteSQL);
		jdbcTemplate.batchUpdate(insertSQL, insertSQL2, insertSQL3);
    	
		insertViewNotificationData(PROCESS_DATE);
		
    	Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		//jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, "20181010");

		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-10-10");

		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
    	
    	List<CardlinkNotification> results = 
    		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<CardlinkNotification>>() {
				public List<CardlinkNotification> call() throws Exception {
					CardlinkNotification cardlinkNotification;
					List<CardlinkNotification> cardlinkNotificationList = new ArrayList<>();
					while((cardlinkNotification = itemReader.read()) != null) {
						cardlinkNotificationList.add(cardlinkNotification);
					}
					return cardlinkNotificationList;
				}
		});
    	
    	assertEquals(3, results.size());    	
    	jdbcTemplate.batchUpdate(deleteSQL);
	}

/*	simon - disable this test - causing performance slowdown
	@Test

	//@Test
	public void testPositiveReaderMoreThan10000() throws Exception {
		for(int i = 0; i < 11000; i++) {
			insertViewNotificationData(PROCESS_DATE);
		}
		
    	Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);

		Map<String, Object> executionContextMap = new HashMap<>();
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-10-10");

		StepExecution stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
    	
    	List<CardlinkNotification> results = 
    		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<CardlinkNotification>>() {
				public List<CardlinkNotification> call() throws Exception {
					CardlinkNotification cardlinkNotification;
					List<CardlinkNotification> cardlinkNotificationList = new ArrayList<>();
					while((cardlinkNotification = itemReader.read()) != null) {
						cardlinkNotificationList.add(cardlinkNotification);
					}
					return cardlinkNotificationList;
				}
		});
    	
    	assertEquals(1000, results.size());    	
    	jdbcTemplate.batchUpdate(deleteSQL);
	}
*/
	@Test
	public void testPositiveProcessor() throws Exception {
		CardlinkNotification cardlinkNotification = createCardlinkNotification(4,1);
    	assertEquals(cardlinkNotification, itemProcessor.process(cardlinkNotification));
	}
    
	@Test
	public void testWriterFail() throws Exception{
		List<CardlinkNotification> notificationList = new ArrayList<>();
		notificationList.add(createCardlinkNotification(5,2));
		when(mockCardlinkNotificationsRepositoryImpl.addIntoNotificationsStaging(Mockito.anyLong() , Mockito.anyString()
				, Mockito.anyString(), (CardlinkNotification)Mockito.any())).thenThrow(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		itemWriter.write(notificationList);
		assertTrue(stepExecution.getJobExecution().getFailureExceptions().size() > 0);
	}
	
	@Test
	public void testWriterSuccess() throws Exception{
		
		CardlinkNotification cardlinkNotification1 = createCardlinkNotification(1,1);
		CardlinkNotification cardlinkNotification2 = createCardlinkNotification(2,1);		
		List<CardlinkNotification> notificationList = new ArrayList<>();
		notificationList.add(cardlinkNotification1);
		notificationList.add(cardlinkNotification2);
		
		when(mockCardlinkNotificationsRepositoryImpl.addIntoNotificationsStaging(Mockito.any(Long.class) , Mockito.any(String.class) , Mockito.any(String.class), Mockito.any(CardlinkNotification.class))).thenReturn(new Integer(1));
    	when(mockCardlinkNotificationsRepositoryImpl.updateIsProcessed(Mockito.any(Long.class) , Mockito.any(String.class) , Mockito.any(CardlinkNotification.class))).thenReturn(new Integer(1));

		itemWriter.write(notificationList);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String currentDate = format.format(new Date());
		verify(mockCardlinkNotificationsRepositoryImpl, times(1)).addIntoNotificationsStaging(new Long("1"),currentDate,"LDCPD6003B",notificationList.get(0));
		verify(mockCardlinkNotificationsRepositoryImpl, times(1)).addIntoNotificationsStaging(new Long("1"),currentDate,"LDCPD6003B",cardlinkNotification2);
    	verify(mockCardlinkNotificationsRepositoryImpl, times(1)).updateIsProcessed(new Long("1"),"LDCPD6003B",cardlinkNotification1);
    	verify(mockCardlinkNotificationsRepositoryImpl, times(1)).updateIsProcessed(new Long("1"),"LDCPD6003B",cardlinkNotification2);

	}
}