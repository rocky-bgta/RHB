package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static org.mockito.Mockito.when;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Assert;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.LoadIBKJompayEmatchValidateFileTasklet;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, LoadIBKJompayEmatchValidateFileTasklet.class })
@ActiveProfiles("test")
public class LoadIBKJompayEmatchValidateFileTaskletTest {
	//filePath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
	@Autowired
	LoadIBKJompayEmatchValidateFileTasklet validateFileTasklet;
	
	@Mock
	ChunkContext chunkContext ;
	
	@Mock
	StepContribution stepContribution;
	
	@Mock 
	StepExecution stepExecution;
	
	@Mock
	StepContext stepContext ;
	
	@Mock
	JobExecution jobExecution ;
	
	@Mock
	ExecutionContext executionContext;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test(expected=BatchException.class)
	public void testInvalidFile() throws Exception {
		when(chunkContext.getStepContext()).thenReturn(stepContext);
		when(stepContext.getStepExecution()).thenReturn(stepExecution);
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		when(jobExecution.getExecutionContext()).thenReturn(executionContext);
		when(executionContext.getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn("ftp/ibk_ematch_to/WIBKD8922B_181005.txt");
		validateFileTasklet.execute(stepContribution, chunkContext);
	}
	
	@Before
	public void setup() {
		
	}
	
	@After
	public void cleanup() {
		
	}
}
