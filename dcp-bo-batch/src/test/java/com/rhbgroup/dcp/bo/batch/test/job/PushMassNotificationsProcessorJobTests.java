package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.junit.Assert.assertEquals;

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
import org.springframework.context.annotation.Lazy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.config.PushMassNotificationsProcessorJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotifMass;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotifMassRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import com.rhbgroup.dcp.model.Capsule;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(JMSUtils.class)
@PowerMockIgnore(value= {"javax.net.ssl.*"})
@SpringBootTest(classes = { BatchTestConfigHSQL.class, PushMassNotificationsProcessorJobConfiguration.class })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class PushMassNotificationsProcessorJobTests extends BaseJobTest {

	public static final String JOB_NAME = "PushMassNotificationsProcessorJob";
	
	public static final String JOB_LAUNCHER_UTILS = "PushMassNotificationsProcessorJobLauncherTestUtils";
	
	private static final String BATCH_CODE = "LDCPA6006B";
	
	@Lazy
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private BatchStagedNotifMassRepositoryImpl repository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testPositiveJob() throws Exception{
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
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		
		assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testPositiveUpdateJob() throws Exception {
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
		
		BatchStagedNotifMass batchStagedNotifMass = 
				createBatchStagedNotifMass(1L, 999901L, "90002", 12345678, "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", false);
		repository.updateIsProcessed(BATCH_CODE, batchStagedNotifMass);
		
		String afterSQL = beforeSQL1;
		jdbcTemplate.batchUpdate(afterSQL);
	}
	
	@Test
	public void testNegativeUpdateJob() throws Exception {
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
		
		BatchStagedNotifMass batchStagedNotifMass = createBatchStagedNotifMass(0L, 0L, null, 0L, null, false);
		repository.updateIsProcessed(BATCH_CODE, batchStagedNotifMass);
		
		String afterSQL = beforeSQL1;
		jdbcTemplate.batchUpdate(afterSQL);
	}
	
	@After
	public void cleanup() {
		deleteTblData();
	}
	
	private void deleteTblData() {
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_NOTIF_MASS";
    	jdbcTemplate.batchUpdate(afterSQL1);
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
		batchStagedNotifMass.setCreatedBy(BATCH_CODE);
		batchStagedNotifMass.setUpdatedTime(new Date());
		batchStagedNotifMass.setUpdatedBy(BATCH_CODE);
		batchStagedNotifMass.setProcessed(isProcessed);
		return batchStagedNotifMass;
	}

}
