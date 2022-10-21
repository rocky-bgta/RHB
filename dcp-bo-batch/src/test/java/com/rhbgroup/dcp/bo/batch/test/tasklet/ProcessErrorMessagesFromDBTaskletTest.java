package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.ProcessErrorMessagesFromDBTasklet;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.enums.SuspenseType;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class ProcessErrorMessagesFromDBTaskletTest extends BaseJobTest {

	@MockBean
	private BatchSuspenseRepositoryImpl batchSuspenseRepository;
	
	@Autowired
	private ProcessErrorMessagesFromDBTasklet processErrorMessagesFromDBTasklet;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Value("${job.common.error.messages.max.limit}")
    private int errorMessagesMaxLimit;
	
	@Before
	public void beforeLocalTest() throws IOException {
		super.beforeTest();
	}
	
	/*
	 * Test when there is no batch suspense record in the DB
	 */
	@Test
	public void testPositiveBatchSuspensesIsEmpty() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	String jobExecutionId = "9999";

    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID)).thenReturn(jobExecutionId);
    	
    	assertEquals(RepeatStatus.FINISHED, processErrorMessagesFromDBTasklet.execute(mockStepContribution, mockChunkContext));
	}
	
	/*
	 * Test when there are some batch suspense record found in the DB
	 */
	@Test
	public void testPositiveBatchSuspensesIsNotEmpty() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	String jobExecutionId = "9999";

    	BatchSuspense batchSuspense1 = new BatchSuspense();
    	batchSuspense1.setBatchJobName(TEST_JOB);
    	batchSuspense1.setCreatedTime(new Date());
    	batchSuspense1.setId(9998);
    	batchSuspense1.setJobExecutionId(Long.parseLong(jobExecutionId));
    	batchSuspense1.setSuspenseColumn("AMOUNT,DATE,BENE_NAME,REJECT_DESCRIPTION");
    	batchSuspense1.setSuspenseMessage("Column(s) AMOUNT,DATE,BENE_NAME,REJECT_DESCRIPTION values shall not be empty/null");
    	batchSuspense1.setSuspenseRecord("|||333333|");
    	batchSuspense1.setSuspenseType(SuspenseType.ERROR.toString());
    	
    	BatchSuspense batchSuspense2 = new BatchSuspense();
    	batchSuspense2.setBatchJobName(TEST_JOB);
    	batchSuspense2.setCreatedTime(new Date());
    	batchSuspense2.setId(9999);
    	batchSuspense2.setJobExecutionId(Long.parseLong(jobExecutionId));
    	batchSuspense2.setSuspenseColumn("AMOUNT,DATE,BENE_NAME,REJECT_DESCRIPTION");
    	batchSuspense2.setSuspenseMessage("Column(s) AMOUNT,DATE,BENE_NAME,REJECT_DESCRIPTION values shall not be empty/null");
    	batchSuspense2.setSuspenseRecord("|||333333|");
    	batchSuspense2.setSuspenseType(SuspenseType.ERROR.toString());
    	
    	List<BatchSuspense> batchSuspenses = new ArrayList<>();
    	batchSuspenses.add(batchSuspense1);
    	batchSuspenses.add(batchSuspense2);
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID)).thenReturn(jobExecutionId);
    	when(batchSuspenseRepository.getByJobNameAndJobExecutionId(TEST_JOB, jobExecutionId, errorMessagesMaxLimit)).thenReturn(batchSuspenses);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
    	
    	processErrorMessagesFromDBTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test when there is job execution id found in the context
	 */
	@Test
	public void testNegativeJobExecutionIdIsNotSet() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID)).thenReturn(false);
    	
    	assertEquals(RepeatStatus.FINISHED, processErrorMessagesFromDBTasklet.execute(mockStepContribution, mockChunkContext));
    	
    	verify(mockAppender, times(1)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent = (LoggingEvent)captorLoggingEvent.getAllValues().get(0);
        //Check log level
        assertEquals(Level.INFO, loggingEvent.getLevel());
        //Check the message being logged
        assertTrue(loggingEvent.getRenderedMessage().contains("No last processed job execution detected in the context, finishing the tasklet"));
	}
	
}
