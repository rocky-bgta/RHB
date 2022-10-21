package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static org.mockito.Mockito.when;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
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

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.LoadIBKJompayFailureDeleteExistingBatchTasklet;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, LoadIBKJompayFailureDeleteExistingBatchTasklet.class })
@ActiveProfiles("test")
public class LoadIBKJompayFailureDeleteExistingBatchTest {
	private static final Logger logger = Logger.getLogger(LoadIBKJompayFailureDeleteExistingBatchTest.class);

	@Autowired
	private LoadIBKJompayFailureDeleteExistingBatchTasklet deleteExistingBatchTasklet;
	
	@Mock
	private ChunkContext chunkContext ;
	
	@Mock
	private StepContribution stepContribution;
	
	@Mock 
	private StepExecution stepExecution;
	
	@Mock
	private StepContext stepContext ;
	
	@Mock
	private JobExecution jobExecution ;
	
	@Mock
	private ExecutionContext executionContext;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testDeleteNonExistingFile() throws Exception {
		when(chunkContext.getStepContext()).thenReturn(stepContext);
		when(stepContext.getStepExecution()).thenReturn(stepExecution);
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		when(jobExecution.getExecutionContext()).thenReturn(executionContext);
		when(executionContext.getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn("ftp/nbps_channel_to/IBKUD041_20180611.txt");
		deleteExistingBatchTasklet.execute(stepContribution, chunkContext);
	}
	
	@Before
	public void setup() {
		
	}
	
	@After
	public void cleanup() {
		
	}
}
