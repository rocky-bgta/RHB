package com.rhbgroup.dcp.bo.batch.test.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_EXEC_FILE_NAME;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.UpdateIBGRejectStatusJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.IBGRejectStatusLoadFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class,UpdateIBGRejectStatusJobConfiguration.class})
@ActiveProfiles("test")
public class IBGRejectStatusLoadFileToStagingStepBuilderTest {
	String JOB_NAME="UpdateIBGRejectedStatusJob";
	
	@Autowired
	IBGRejectStatusLoadFileToStagingStepBuilder loadFileToStaging;
	
	@Mock
	StepExecution stepExecution;
	
	@Mock
	JobExecution jobExecution;
	
	@Mock
	ExecutionContext executionContext;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test(expected=BatchException.class)
	public void testReaderException() throws Exception{
		loadFileToStaging.ibgRejectStatusReader(stepExecution);
	}
	
	@Test(expected=BatchException.class)
	public void testFileNotFound() throws Exception{
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.toJobParameters();
    	when(stepExecution.getJobExecution()).thenReturn(jobExecution);
    	when(stepExecution.getJobExecution().getExecutionContext()).thenReturn(executionContext);
    	when(stepExecution.getJobExecution().getExecutionContext().getString(BATCH_IBG_REJECT_STATUS_EXEC_FILE_NAME)).thenReturn("xxx.txt");
    	when(stepExecution.getJobExecution().getJobParameters()).thenReturn(jobParameters);
		loadFileToStaging.ibgRejectStatusReader(stepExecution);
	}
	
	@Before
	public void setup() {
		
	}
}
