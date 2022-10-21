package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.AuditJMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.SmsJMSConfigProperties;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBKPaymentTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.DeleteExistingIBKBillerTxnsTasklet;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DeleteExistingIBKBillerTxnsTasklet.class })
@ActiveProfiles("test")
public class DeleteExistingIBKBillerTxnsTaskletTest extends BaseJobTest {

	@MockBean
	SmsJMSConfigProperties smsJMSConfigProperties;

	@MockBean
	AuditJMSConfigProperties auditJMSConfigProperties;

	@Autowired
	private DeleteExistingIBKBillerTxnsTasklet deleteExistingIBKBillerTxnsTasklet;
	
	@MockBean
	private BatchStagedIBKPaymentTxnRepositoryImpl batchStagedIBKPaymentTxnRepository;
	
	@MockBean
	private JdbcTemplate mockJdbcTemplate;
	
	/*
	 * Test to ensure the if file is found in context the tasklet shall complete without issue
	 */
	@Test
	public void testPositiveDeleteRecSuccessfully() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn("TEST.txt");
    	
    	assertEquals(RepeatStatus.FINISHED, deleteExistingIBKBillerTxnsTasklet.execute(mockStepContribution, mockChunkContext));
	}
	
	/*
	 * Test to ensure the if file is not found in context the tasklet shall complete without issue as well
	 */
	@Test
	public void testPositiveDeleteWhileFileNotFound() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(false);
    	
    	assertEquals(RepeatStatus.FINISHED, deleteExistingIBKBillerTxnsTasklet.execute(mockStepContribution, mockChunkContext));
	}
	
	/*
	 * Test to ensure the if DB service is down the tasklet shall complete without issue, because it is low priority
	 */
	@Test
	public void testNegativeDeleteWhileDBServiceDown() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn("TEST.txt");
    	
    	when(batchStagedIBKPaymentTxnRepository.deleteExistingBatchStagedIBKPaymentTxns(Mockito.anyString()))
			.thenThrow(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
    	expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
    	assertEquals(RepeatStatus.FINISHED, deleteExistingIBKBillerTxnsTasklet.execute(mockStepContribution, mockChunkContext));
	}
}
