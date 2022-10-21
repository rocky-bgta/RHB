package com.rhbgroup.dcp.bo.batch.test.job;

import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.junit.Assert;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.config.JompayValidationFailureReportJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import com.rhbgroup.dcp.model.Capsule;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(JMSUtils.class)
@PowerMockIgnore(value= { "javax.net.ssl.*" })
@SpringBootTest(classes= {BatchTestConfigHSQL.class, JompayValidationFailureReportJobConfiguration.class})
@ActiveProfiles("test")
public class DailyDeltaProfileSMSJobTests extends BaseJobTest {
	// WONG TODO
	public static final String JOB_NAME = "DailyDeltaProfileSMSJob";
	public static final String JOB_LAUNCHER_UTILS = "DailyDeltaProfileSMSJobLauncherTestUtils";
	
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private BatchParameterRepositoryImpl batchParameterRepository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	/*
	 * Test JOB execution using the DB config batch system date
	 */
	@Test
    public void testPositiveJobWithDBConfigBatchSystemDate() throws Exception {
		BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		
		String beforeSql1 = "DELETE FROM TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE WHERE PROCESSING_DATE = '2018-08-21'";
		String beforeSql2 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99991, '2018-08-21', 99991, NOW(), 0, NOW())";
		String beforeSql3 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99992, '2018-08-21', 99992, NOW(), 1, NOW())";
		String beforeSql4 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99993, '2018-08-21', 99993, NOW(), 0, NOW())";
		String beforeSql5 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99994, '2018-08-21', 99994, NOW(), 1, NOW())";
		String beforeSql6 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99995, '2018-08-21', 99995, NOW(), 0, NOW())";
		jdbcTemplate.batchUpdate(beforeSql1, beforeSql2, beforeSql3, beforeSql4, beforeSql5, beforeSql6);
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-21");
		
		PowerMockito.mockStatic(JMSUtils.class);
    	
		JobParameters jobParameters = new JobParametersBuilder()
    		.addDate("now", new Date())
    		.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
    		.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        
        PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS((Capsule)Mockito.any(),
    		Mockito.any());
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(
				(Capsule)Mockito.any(),
    		Mockito.any());
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS((Capsule)Mockito.any(),
    		Mockito.any());
        
        String afterSql1 = "DELETE FROM TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE WHERE PROCESSING_DATE = '2018-08-21'";
        jdbcTemplate.batchUpdate(afterSql1);
        batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
    }
	
	/*
	 * Test JOB execution using the external job parameter processing date
	 */
	@Test
    public void testPositiveJobWithJobParamProcessingDate() throws Exception {
		String beforeSql1 = "DELETE FROM TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE WHERE PROCESSING_DATE = '2018-08-21'";
		String beforeSql2 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99991, '2018-08-21', 99991, NOW(), 0, NOW())";
		String beforeSql3 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99992, '2018-08-21', 99992, NOW(), 1, NOW())";
		String beforeSql4 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99993, '2018-08-21', 99993, NOW(), 0, NOW())";
		String beforeSql5 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99994, '2018-08-21', 99994, NOW(), 1, NOW())";
		String beforeSql6 = "INSERT INTO TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE (ID, PROCESSING_DATE, USER_ID, CREATED_TIME, IS_PROCESSED, UPDATED_TIME) VALUES (99995, '2018-08-21', 99995, NOW(), 0, NOW())";
		jdbcTemplate.batchUpdate(beforeSql1, beforeSql2, beforeSql3, beforeSql4, beforeSql5, beforeSql6);
		
		PowerMockito.mockStatic(JMSUtils.class);
    	
		JobParameters jobParameters = new JobParametersBuilder()
    		.addDate("now", new Date())
    		.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
    		.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, "20180821")
    		.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        
        PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(
			(Capsule)Mockito.any(),
    		Mockito.any());
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(
			(Capsule)Mockito.any(),
    		Mockito.any());
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(3));
		JMSUtils.sendCapsuleMessageToJMS(
			(Capsule)Mockito.any(),
    		Mockito.any());
        
        String afterSql1 = "DELETE FROM TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE WHERE PROCESSING_DATE = '2018-08-21'";
        jdbcTemplate.batchUpdate(afterSql1);
    }
}
