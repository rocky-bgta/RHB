package com.rhbgroup.dcp.bo.batch.test.tasklet;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.IBGRejectStatusGetFileTasklet;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {IBGRejectStatusGetFileTasklet.class})
@ActiveProfiles("test")
public class IBGRejectStatusGetFileTaskletTest {
	
	String JOB_NAME="UpdateIBGRejectedStatusJob";
	String JOB_EXEC_ID="";
	String SUCCESS_RUN_WINDOW="w1";
	String FAILED_RUN_WINDOW="w2";
	String jobprocessdate = "2018-08-25";
	
	@MockBean(name="ftpConfig")
	private FTPConfigProperties ftpConfig;
	
	@Mock
	ChunkContext chunkContext;
	
	@Mock
	StepContext stepContext;
	
	@Mock
	StepExecution stepExecution;
	
	@Mock
	JobExecution jobExecution;
	
	@Mock
	ExecutionContext executionContext;
	
	
	@Mock
	StepContribution stepContribution;
	
	@Autowired
	IBGRejectStatusGetFileTasklet ibgGetFileTasklet;
	
	@Test(expected=Exception.class)
	public void testSystemDateNull() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        ibgGetFileTasklet.execute(stepContribution, chunkContext);
	}
	
	@Test(expected=Exception.class)
	public void testInvalidDate() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("xxyyzz");
        ibgGetFileTasklet.execute(stepContribution, chunkContext);
	}
	
	@Test(expected=BatchException.class)
	public void testDownloadFail() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn(jobprocessdate);
        ibgGetFileTasklet.execute(stepContribution, chunkContext);
    }
	
	@Test(expected=Exception.class)
	public void testRunWindowNull() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn(jobprocessdate);
        ibgGetFileTasklet.execute(stepContribution, chunkContext);
	}
	
}
