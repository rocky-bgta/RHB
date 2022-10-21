package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedMergeCISDetailTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedMergeCISRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UserProfileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.MergeCISValidatorTasklet;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class, MergeCISValidatorTasklet.class})
@ActiveProfiles("test")
public class MergeCISValidatorTaskletTest extends BaseJobTest {
	private static final String JOB_NAME = "MergeCISJob";
	
	@Autowired
	private MergeCISValidatorTasklet mergeCISValidatorTasklet;
	
	@MockBean
	private BatchStagedMergeCISRepositoryImpl mockBatchStagedMergeCISRepositoryImpl;

	@MockBean
	private UserProfileRepositoryImpl mockUserProfileRepositoryImpl;
	
	@Mock
	private ChunkContext chunkContext;
	
	@Mock
	private StepContext stepContext;
	
	@Mock
	private StepExecution stepExecution;
	
	@Mock
	private JobExecution jobExecution;
	
	@Mock
	private ExecutionContext executionContext;
	
	@Mock
	private StepContribution stepContribution;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;
	
	@Test
	public void testPositiveJob() throws Exception {
		String jobExecutionId = "1000";
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, "1000")
				.toJobParameters();
		
		when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        
        List<BatchStagedMergeCISDetailTxn> cisDetailTxnList = new ArrayList<>();
        BatchStagedMergeCISDetailTxn cisDetail = new BatchStagedMergeCISDetailTxn();
        cisDetail.setCisNo("cisNo");
        cisDetail.setFileName("fileName");
        cisDetail.setJobExecutionId(jobExecutionId);
        cisDetail.setNewCISNo("newCISNo");
        cisDetail.setProcessingDate("processingDate");
        cisDetail.setSpace("space");
        cisDetailTxnList.add(cisDetail);
        
        when(mockBatchStagedMergeCISRepositoryImpl.getUnproccessedStagedCIS(jobExecutionId)).thenReturn(cisDetailTxnList);
        
        Assert.assertEquals(mergeCISValidatorTasklet.execute(stepContribution, chunkContext), RepeatStatus.FINISHED);
	}
	
	@Test
	public void testNegativeBlankCisJob() throws Exception {
		String jobExecutionId = "1000";
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, "1000")
				.toJobParameters();
		
		when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        
        List<BatchStagedMergeCISDetailTxn> cisDetailTxnList = new ArrayList<>();
        BatchStagedMergeCISDetailTxn cisDetail = new BatchStagedMergeCISDetailTxn();
        cisDetail.setCisNo("");
        cisDetail.setFileName("fileName");
        cisDetail.setJobExecutionId(jobExecutionId);
        cisDetail.setNewCISNo("newCISNo");
        cisDetail.setProcessingDate("processingDate");
        cisDetail.setSpace("space");
        cisDetailTxnList.add(cisDetail);
        
        when(mockBatchStagedMergeCISRepositoryImpl.getUnproccessedStagedCIS(jobExecutionId)).thenReturn(cisDetailTxnList);
        when(mockBatchStagedMergeCISRepositoryImpl.updateProcessStatus(cisDetail, 1)).thenReturn(1);
        
        mergeCISValidatorTasklet.execute(stepContribution, chunkContext);
        
        String logMessage = "cisNo=, newCISNo=newCISNo, processingDate=processingDate, jobExecutionId=1000, suspenseType=ERROR, suspenseMessage=CIS number is null or empty";
        verify(mockAppender, atLeastOnce()).doAppend((LoggingEvent)captorLoggingEvent.capture());
        LoggingEvent loggingEvent = (LoggingEvent)captorLoggingEvent.getAllValues().get(4);
        assertEquals(logMessage, loggingEvent.getMessage());
	}
	
	@Test
	public void testNegativeCisNoNotFoundJob() throws Exception {
		String jobExecutionId = "1000";
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, "1000")
				.toJobParameters();
		
		when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        
        List<BatchStagedMergeCISDetailTxn> cisDetailTxnList = new ArrayList<>();
        BatchStagedMergeCISDetailTxn cisDetail = new BatchStagedMergeCISDetailTxn();
        cisDetail.setCisNo("xxxx");
        cisDetail.setFileName("fileName");
        cisDetail.setJobExecutionId(jobExecutionId);
        cisDetail.setNewCISNo("newCISNo");
        cisDetail.setProcessingDate("processingDate");
        cisDetail.setSpace("space");
        cisDetailTxnList.add(cisDetail);
        
        when(mockBatchStagedMergeCISRepositoryImpl.getUnproccessedStagedCIS(jobExecutionId)).thenReturn(cisDetailTxnList);
        when(mockUserProfileRepositoryImpl.updateUserStatusCISNo("cisNo", "I")).thenReturn(0);
        when(mockBatchStagedMergeCISRepositoryImpl.updateProcessStatus(cisDetail, 1)).thenReturn(1);
        
        mergeCISValidatorTasklet.execute(stepContribution, chunkContext);
        
        String logMessage = "cisNo=xxxx, newCISNo=newCISNo, processingDate=processingDate, jobExecutionId=1000, suspenseType=ERROR, suspenseMessage=CIS number not found from TBL_USER_PROFILE";
        verify(mockAppender, atLeastOnce()).doAppend((LoggingEvent)captorLoggingEvent.capture());
        LoggingEvent loggingEvent = (LoggingEvent)captorLoggingEvent.getAllValues().get(5);
        assertEquals(logMessage, loggingEvent.getMessage());
	}
	
	@Test(expected = Exception.class)
	public void testNegativeExceptionJob() throws Exception {
		String jobExecutionId = "1000";
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, "1000")
				.toJobParameters();
		
		when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        
        List<BatchStagedMergeCISDetailTxn> cisDetailTxnList = new ArrayList<>();
        BatchStagedMergeCISDetailTxn cisDetail = new BatchStagedMergeCISDetailTxn();
        cisDetail.setCisNo("xxxx");
        cisDetail.setFileName("fileName");
        cisDetail.setJobExecutionId(jobExecutionId);
        cisDetail.setNewCISNo("newCISNo");
        cisDetail.setProcessingDate("processingDate");
        cisDetail.setSpace("space");
        cisDetailTxnList.add(cisDetail);
        
        when(mergeCISValidatorTasklet.execute(stepContribution, chunkContext)).thenThrow(new Exception());
        
        mergeCISValidatorTasklet.execute(stepContribution, chunkContext);
	}

}
