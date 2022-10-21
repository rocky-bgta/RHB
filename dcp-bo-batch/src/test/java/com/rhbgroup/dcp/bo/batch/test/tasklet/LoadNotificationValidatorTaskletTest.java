package com.rhbgroup.dcp.bo.batch.test.tasklet;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotificationRawRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.LoadNotificationValidatorTasklet;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import org.junit.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class,LoadNotificationValidatorTasklet.class})
@ActiveProfiles("test")
public class LoadNotificationValidatorTaskletTest {
	private static final Logger logger = Logger.getLogger(LoadNotificationValidatorTaskletTest.class);
	private static final String JOB_NAME = "LoadCardlinkNotificationsJob";

	@Autowired
	private LoadNotificationValidatorTasklet validatorTasklet;
	
	@MockBean
	private BatchStagedNotificationRawRepositoryImpl mockRawRepositoryImpl;
	
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
	public void testPositiveNewFile() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.toJobParameters();
		String fileName="DCP_LDCPD6002T_20181025.txt";
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(fileName);
		when(mockRawRepositoryImpl.findNotificationFileLoaded(fileName)).thenReturn(0);
        Assert.assertEquals(validatorTasklet.execute(stepContribution, chunkContext),RepeatStatus.FINISHED);
	}
	
	
	
	@Test(expected=BatchException.class)
	public void testNegFileExist() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.toJobParameters();
		String fileName="DCP_LDCPD6002T_20181024";
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(fileName);
        when(mockRawRepositoryImpl.findNotificationFileLoaded(fileName)).thenReturn(1);
        validatorTasklet.execute(stepContribution, chunkContext);
	}
	
	@Test(expected=BatchException.class)
	public void testNegDBException() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.toJobParameters();
		String fileName="DCP_LDCPD6002T_20181024";
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(fileName);
        when(mockRawRepositoryImpl.findNotificationFileLoaded(fileName)).thenThrow(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
        validatorTasklet.execute(stepContribution, chunkContext);
	}
	
	@Before
	public void setup() {
	}
	
	@After
	public void cleanup() {}
}
