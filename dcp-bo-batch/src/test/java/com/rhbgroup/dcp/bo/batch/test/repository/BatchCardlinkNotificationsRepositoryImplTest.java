package com.rhbgroup.dcp.bo.batch.test.repository;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotification;
import com.rhbgroup.dcp.bo.batch.job.model.CardlinkNotification;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchCardlinkNotificationsRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BatchCardlinkNotificationsRepositoryImplTest extends BaseJobTest {

	@Autowired
	BatchCardlinkNotificationsRepositoryImpl cardlinkNotificationRepoImpl;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;


	
	@Test
	public void testPositiveAddIntoNotificationsStaging() throws Exception {
		CardlinkNotification cardlinkNotification = createCardlinkNotification("4363452500001000", "P", "50000", "101020180516.txt", "CC", "1234.56", 1L, "1234.00",
				"20180926", "1234.56", "20180825", "20180926", "101010", 1L);
		
		String processedDateStr = DateUtils.formatDateString(new Date(), DEFAULT_JOB_PARAMETER_DATE_FORMAT);
		
		when(mockJdbcTemplate.update(
				Mockito.anyString(), 
				Mockito.anyLong(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.anyLong(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.anyBoolean(), 
				(Date) Mockito.any(), 
				Mockito.any(), 
				(Date) Mockito.any(), 
				Mockito.any())).thenReturn(1);
		
		assertEquals(1, cardlinkNotificationRepoImpl.addIntoNotificationsStaging(999901, processedDateStr, "LDCPD6003B", cardlinkNotification));
	}
	
	@Test(expected = BatchException.class)
	public void testNegativeAddIntoNotificationsStaging() throws Exception {
		CardlinkNotification cardlinkNotification = createCardlinkNotification(null, null, null, null, null, null, 0L, null, null, null, null, null, null, 0L);
		
		when(mockJdbcTemplate.update(
				Mockito.anyString(), 
				Mockito.anyLong(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.anyLong(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.anyBoolean(), 
				(Date) Mockito.any(), 
				Mockito.any(), 
				(Date) Mockito.any(), 
				Mockito.any())).thenThrow(UncategorizedSQLException.class);
		
		cardlinkNotificationRepoImpl.addIntoNotificationsStaging(999901, null, null, cardlinkNotification);
	}
	
	@Test
	public void testUpdateAfterNotificationSuccess() throws Exception {
		String batchCode = "LDCPD6003B";
		
		BatchStagedNotification batchStagedNotification = createBatchStagedNotification(batchCode, "20180926", "101010", "4363452500001002", "20180926", "P", "1234.56", 
				"1234.00", "1234.56", "20180825", "", "50000", "101020180516.txt", 1L, 1L, "CC", false, batchCode, 1L);
		
		when(mockJdbcTemplate.update(Mockito.anyString(), (Date) Mockito.any(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
				.thenReturn(1);
		
		assertEquals(1, cardlinkNotificationRepoImpl.updateIsProcessed(batchCode, batchStagedNotification));
	}
	
	@Test(expected = BatchException.class)
	public void testUpdateAfterNotificationFailed() throws Exception {
		BatchStagedNotification batchStagedNotification = new BatchStagedNotification();
		batchStagedNotification.setId(1);
		String batchCode="CARDLINK";
		when(mockJdbcTemplate.update(Mockito.anyString(), (Date) Mockito.any(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
				.thenThrow(BadSqlGrammarException.class);
		cardlinkNotificationRepoImpl.updateIsProcessed(batchCode, batchStagedNotification);
	}
	
	private CardlinkNotification createCardlinkNotification(String cardNumber, String cardType, String eventCode, String fileName, String keyType, String minimumAmount, 
			long notificationRawId, String outstandingAmount, String paymentDueDate, String statementAmount, String statementDate, String systemDate, String systemTime, 
			long userId) {
		CardlinkNotification cardlinkNotification = new CardlinkNotification();
		cardlinkNotification.setCardNumber(cardNumber);
		cardlinkNotification.setCardType(cardType);
		cardlinkNotification.setEventCode(eventCode);
		cardlinkNotification.setFileName(fileName);
		cardlinkNotification.setKeyType(keyType);
		cardlinkNotification.setMinimumAmount(minimumAmount);
		cardlinkNotification.setNotificationRawId(notificationRawId);
		cardlinkNotification.setOutstandingAmount(outstandingAmount);
		cardlinkNotification.setPaymentDueDate(paymentDueDate);
		cardlinkNotification.setStatementAmount(statementAmount);
		cardlinkNotification.setStatementDate(statementDate);
		cardlinkNotification.setSystemDate(systemDate);
		cardlinkNotification.setSystemTime(systemTime);
		cardlinkNotification.setUserId(userId);
		
		return cardlinkNotification;
	}
	
	private BatchStagedNotification createBatchStagedNotification(String createdBy, String data1, String data2, String data3, String data4, String data5, String data6, 
			String data7, String data8, String data9, String data10, String eventCode, String fileName, long id, long jobExecutionId, String keyType, boolean isProcessed, 
			String updateBy, long userId) {
		BatchStagedNotification batchStagedNotification = new BatchStagedNotification();
		batchStagedNotification.setCreatedBy(createdBy);
		batchStagedNotification.setCreatedTime(new Date());
		batchStagedNotification.setData1(data1);
		batchStagedNotification.setData2(data2);
		batchStagedNotification.setData3(data3);
		batchStagedNotification.setData4(data4);
		batchStagedNotification.setData5(data5);
		batchStagedNotification.setData6(data6);
		batchStagedNotification.setData7(data7);
		batchStagedNotification.setData8(data8);
		batchStagedNotification.setData9(data9);
		batchStagedNotification.setData10(data10);
		batchStagedNotification.setEventCode(eventCode);
		batchStagedNotification.setFileName(fileName);
		batchStagedNotification.setId(id);
		batchStagedNotification.setJobExecutionId(jobExecutionId);
		batchStagedNotification.setKeyType(keyType);
		batchStagedNotification.setProcessed(isProcessed);
		batchStagedNotification.setUpdateBy(updateBy);
		batchStagedNotification.setUpdatedTime(new Date());
		batchStagedNotification.setUserId(userId);
		
		return batchStagedNotification;
	}
	
	@Test(expected=BatchException.class)
	public void testAddRecordFail() throws BatchException {
		CardlinkNotification notification = new CardlinkNotification();
		long jobExecutionId=0L;
		String processedDate="20181010";
		String batchCode="50000";
		when(mockJdbcTemplate.update(Mockito.anyString(), (Object)Mockito.any() )).thenThrow(BadSqlGrammarException.class);
		cardlinkNotificationRepoImpl.addIntoNotificationsStaging(jobExecutionId, processedDate, batchCode, notification);
	}
	
	@Test(expected=BatchException.class)
	public void testUpdateRecordFail() throws BatchException {
		CardlinkNotification notification = new CardlinkNotification();
		long jobExecutionId=0L;
		String batchCode="50000";
		when( mockJdbcTemplate.update(Mockito.anyString(), 
        		Mockito.anyString(),
        		Mockito.anyString(),
        		Mockito.anyLong(),
        		(CardlinkNotification)Mockito.any())).thenThrow(BadSqlGrammarException.class);
		cardlinkNotificationRepoImpl.updateIsProcessed(jobExecutionId, batchCode, notification);
	}
	
	@Test
	public void testAddRecordSuccess() throws BatchException {
		CardlinkNotification notification = new CardlinkNotification();
		long jobExecutionId=0L;
		String processedDate="20181010";
		String batchCode="50000";
		when(mockJdbcTemplate.update(Mockito.anyString(), (Object)Mockito.any() )).thenReturn(1);
		assertEquals(1, cardlinkNotificationRepoImpl.addIntoNotificationsStaging(jobExecutionId, processedDate, batchCode, notification));
	}
	
	@Test
	public void testUpdateRecordSuccess() throws BatchException {
		CardlinkNotification notification = new CardlinkNotification();
		long jobExecutionId=0L;
		String batchCode="50000";
		when( mockJdbcTemplate.update(Mockito.anyString(), 
        		Mockito.anyString(),
        		Mockito.anyString(),
        		Mockito.anyLong(),
        		(CardlinkNotification)Mockito.any())).thenReturn(1);
		assertEquals(new Integer(1),cardlinkNotificationRepoImpl.updateIsProcessed(jobExecutionId, batchCode, notification));
	}
	
}
