package com.rhbgroup.dcp.bo.batch.test.job;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.model.Capsule;
import freemarker.template.Configuration;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.config.IBGRejectedNotificationJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(JMSUtils.class)
@PowerMockIgnore(value= { "javax.net.ssl.*" })
@SpringBootTest(classes={ BatchTestConfigHSQL.class, IBGRejectedNotificationJobConfiguration.class })
@ActiveProfiles("test")
public class IBGRejectedNotificationJobTests extends BaseJobTest {

	public static final String JOB_NAME = "IBGRejectedNotificationJob";
	public static final String JOB_LAUNCHER_UTILS = "IBGRejectedNotificationJobLauncherTestUtils";
	
    @Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
	private JdbcTemplate jdbcTemplate;

    @Autowired
	private BatchParameterRepositoryImpl batchParameterRepository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
    
    /*
     * Test job using the DB config IBG Job Execution Id
     */
    @Test
    public void testPositiveJobWithDBConfigParams() throws Exception {
    	BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID);
    	
    	String jobExecutionId = "9999";    	
    	String beforeSQL1 = String.format("DELETE FROM TBL_BATCH_SUSPENSE WHERE JOB_EXECUTION_ID = %s", jobExecutionId);
    	String beforeSQL2 = String.format("DELETE FROM TBL_BATCH_STAGED_IBG_REJECT_TXN WHERE JOB_EXECUTION_ID = %s", jobExecutionId);
    	String beforeSQL3 = String.format("DELETE FROM TBL_BATCH_JOB_EXECUTION WHERE JOB_EXECUTION_ID = %s", jobExecutionId);
    	jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3);
    	
    	String beforeSQL4 = String.format("INSERT INTO TBL_BATCH_JOB_EXECUTION (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME) VALUES(%s, 1, 1, NOW())", jobExecutionId);
    	String beforeSQL5 = String.format("INSERT INTO TBL_BATCH_STAGED_IBG_REJECT_TXN (JOB_EXECUTION_ID, DATE, TELLER, TRACE, REF1, NAME, AMOUNT, REJECT_CODE, REJECT_DESCRIPTION, ACCOUNT_NO, USER_ID, BENE_NAME, BENE_ACCOUNT, IS_PROCESSED, IS_NOTIFICATION_SENT, CREATED_TIME, UPDATED_TIME, FILE_NAME) VALUES(%s, 20180831, 111111, 111111, 'TESTER1_REF', 'TESTER1', 100.00, 'R01', 'A/C CLOSED', 111111, 111111, 'TESTER1_BENE', 111111, 1, 0, NOW(), NOW(), 'TESTER1_FILE')", jobExecutionId);
    	String beforeSQL6 = String.format("INSERT INTO TBL_BATCH_STAGED_IBG_REJECT_TXN (JOB_EXECUTION_ID, DATE, TELLER, TRACE, REF1, NAME, AMOUNT, REJECT_CODE, REJECT_DESCRIPTION, ACCOUNT_NO, USER_ID, BENE_NAME, BENE_ACCOUNT, IS_PROCESSED, IS_NOTIFICATION_SENT, CREATED_TIME, UPDATED_TIME, FILE_NAME) VALUES(%s, 20180831, 222222, 222222, 'TESTER2_REF', 'TESTER2', 200.00, 'R02', 'NETWORK ISSUES', 222222, 222222, 'TESTER2_BENE', 222222, 1, 0, NOW(), NOW(), 'TESTER2_FILE')", jobExecutionId);
    	String beforeSQL7 = String.format("INSERT INTO TBL_BATCH_STAGED_IBG_REJECT_TXN (JOB_EXECUTION_ID, DATE, TELLER, TRACE, REF1, NAME, AMOUNT, REJECT_CODE, REJECT_DESCRIPTION, ACCOUNT_NO, USER_ID, BENE_NAME, BENE_ACCOUNT, IS_PROCESSED, IS_NOTIFICATION_SENT, CREATED_TIME, UPDATED_TIME, FILE_NAME) VALUES(%s, 20180831, 333333, 333333, 'TESTER3_REF', 'TESTER3', 300.00, 'R03', '', 333333, 333333, 'TESTER3_BENE', 333333, 1, 0, NOW(), NOW(), 'TESTER3_FILE')", jobExecutionId);
    	String beforeSQL8 = String.format("INSERT INTO TBL_BATCH_STAGED_IBG_REJECT_TXN (JOB_EXECUTION_ID, DATE, TELLER, TRACE, REF1, NAME, AMOUNT, REJECT_CODE, REJECT_DESCRIPTION, ACCOUNT_NO, USER_ID, BENE_NAME, BENE_ACCOUNT, IS_PROCESSED, IS_NOTIFICATION_SENT, CREATED_TIME, UPDATED_TIME, FILE_NAME) VALUES(%s, 20180831, 444444, 444444, 'TESTER4_REF', 'TESTER4', 400.00, 'R04', 'UNSUFFICIENT MONEY', 444444, 444444, 'TESTER4_BENE', 444444, 0, 0, NOW(), NOW(), 'TESTER4_FILE')", jobExecutionId);
    	jdbcTemplate.batchUpdate(beforeSQL4, beforeSQL5, beforeSQL6, beforeSQL7, beforeSQL8);
    	batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID, jobExecutionId);
    	
    	PowerMockito.mockStatic(JMSUtils.class);
    	
        JobParameters jobParameters = new JobParametersBuilder()
    		.addDate("now", new Date())
    		.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
    		.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        assertEquals(JOB_FAILED, jobExecution.getExitStatus().getExitCode());

        PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(2));
		JMSUtils.sendCapsuleMessageToJMS(Mockito.any(Capsule.class),Mockito.any());

        
        String selectSQL1 = String.format("SELECT COUNT(*) COUNTER FROM TBL_BATCH_STAGED_IBG_REJECT_TXN WHERE JOB_EXECUTION_ID='%s' AND IS_NOTIFICATION_SENT=1", jobExecutionId);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSQL1);
        long counter = (long)results.get(0).get("COUNTER");
		//assertEquals(2, counter);
		
		String selectSQL2 = String.format("SELECT COUNT(*) COUNTER FROM TBL_BATCH_SUSPENSE WHERE JOB_EXECUTION_ID='%s'", jobExecutionId);
        List<Map<String, Object>> results2 = jdbcTemplate.queryForList(selectSQL2);
        long counter2 = (long)results2.get(0).get("COUNTER");
		//assertEquals(1, counter2);
        
        String afterSQL1 = beforeSQL1;
    	String afterSQL2 = beforeSQL2;
    	String afterSQL3 = beforeSQL3;
    	jdbcTemplate.batchUpdate(afterSQL1, afterSQL2, afterSQL3);
    	batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
    }
    
    /*
     * Test job using empty IBG Job Execution Id, it is still consider valid even the value is empty
     */
    @Test
    public void testPositiveEmptyIBGJobExecutionId() throws Exception {
    	BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID);
    	
    	String jobExecutionId = "";
    	batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID, jobExecutionId);
    	
        JobParameters jobParameters = new JobParametersBuilder()
    		.addDate("now", new Date())
    		.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
    		.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());
    	
    	batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
    }
}