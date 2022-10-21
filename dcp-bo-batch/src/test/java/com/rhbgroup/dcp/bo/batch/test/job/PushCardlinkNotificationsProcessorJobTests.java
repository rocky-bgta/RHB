package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.config.PushCardlinkNotificationsJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotification;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchCardlinkNotificationsRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import com.rhbgroup.dcp.model.Capsule;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(JMSUtils.class)
@PowerMockIgnore(value= {"javax.net.ssl.*"})
@SpringBootTest(classes = { BatchTestConfigHSQL.class, PushCardlinkNotificationsJobConfiguration.class })
@ActiveProfiles("test")
public class PushCardlinkNotificationsProcessorJobTests extends BaseJobTest {

	public static final String JOB_NAME = "PushCardlinkNotificationsProcessorJob";
	
	public static final String JOB_LAUNCHER_UTILS = "PushCardlinkNotificationsProcessorJobLauncherTestUtils";
	
	private static final String BATCH_CODE = "LDCPD6003B";
	
	private Instant eventTimestamp = Instant.now().minusSeconds(86400);
	
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private BatchCardlinkNotificationsRepositoryImpl batchCardlinkNotificationsRepositoryImpl;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testJob() throws Exception{
		Date datetMinus1 = Date.from(eventTimestamp);
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
		String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999901, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345678, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, '" + BATCH_CODE + "', NOW(), '" + BATCH_CODE + "', NOW())";
		String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999902, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1,BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345679, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, '" + BATCH_CODE + "', NOW(), '" + BATCH_CODE + "', NOW())";
		String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999903, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1,BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345680, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, '" + BATCH_CODE + "', NOW(), '" + BATCH_CODE + "', NOW())";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4);
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		
		assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testPositiveJobWithJobParamProcessingDate() throws Exception {
		Date datetMinus1 = Date.from(eventTimestamp);
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
		String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999901, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345678, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, '" + BATCH_CODE + "', NOW(), '" + BATCH_CODE + "', NOW())";
		String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999902, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345679, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, '" + BATCH_CODE + "', NOW(), '" + BATCH_CODE + "', NOW())";
		String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999903, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345680, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, '" + BATCH_CODE + "', NOW(), '" + BATCH_CODE + "', NOW())";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4);
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.toJobParameters();
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		
		assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(Mockito.any(Capsule.class), Mockito.any());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(Mockito.any(Capsule.class), Mockito.any());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(Mockito.any(Capsule.class), Mockito.any());
		
		BatchStagedNotification batchStagedNotification = createBatchStagedNotification(BATCH_CODE, "20180926", "101010", "4363452500001002", "20180926", "P", "1234.56", 
				"1234.00", "1234.56", "20180825", "", "50000", "101020180516.txt", 1L, 1L, "CC", false, BATCH_CODE, 1L);
		batchCardlinkNotificationsRepositoryImpl.updateIsProcessed(BATCH_CODE, batchStagedNotification);
		
		String afterSQL = beforeSQL1;
		jdbcTemplate.batchUpdate(afterSQL);
	}
	
	@Test
	public void testNegativeJobWithJobParamProcessingDate() throws Exception {
		Date datetMinus1 = Date.from(eventTimestamp);
		String beforeSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
		String beforeSQL2 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999901, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345678, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, '" + BATCH_CODE + "', NOW(), '" + BATCH_CODE + "', NOW())";
		String beforeSQL3 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999902, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345679, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, '" + BATCH_CODE + "', NOW(), '" + BATCH_CODE + "', NOW())";
		String beforeSQL4 = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (JOB_EXECUTION_ID, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, USER_ID, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, IS_PROCESSED, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME) "
				+ "VALUES (999903, '101020180516.txt', '" + DateUtils.formatDateString(datetMinus1, BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT) + "', '50000', 'CC', 12345680, '20180926', '101010', '4363452500001009', '20180926', 'P', '1234.56', '1234.00', '1234.56', '20180825', '', 0, '" + BATCH_CODE + "', NOW(), '" + BATCH_CODE + "', NOW())";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4);
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		
		assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(Mockito.any(Capsule.class), Mockito.any());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(Mockito.any(Capsule.class), Mockito.any());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(Mockito.any(Capsule.class), Mockito.any());
		
		BatchStagedNotification batchStagedNotification = createBatchStagedNotification(null, null, null, null, null, null, null, null, null, null, null, null, null, 0L, 0L, null, false, null, 0L);
		batchCardlinkNotificationsRepositoryImpl.updateIsProcessed(BATCH_CODE, batchStagedNotification);
		
		String afterSQL = beforeSQL1;
		jdbcTemplate.batchUpdate(afterSQL);
	}
	
	@After
	public void cleanup() {
		deleteTblData();
	}
	
	private void deleteTblData() {
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION";
    	jdbcTemplate.batchUpdate(afterSQL1);
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

}
