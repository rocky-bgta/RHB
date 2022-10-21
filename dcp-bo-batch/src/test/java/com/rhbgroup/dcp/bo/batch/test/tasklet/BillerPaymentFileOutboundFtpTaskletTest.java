package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;
import static org.mockito.Mockito.when;

import org.junit.After;
import static org.junit.Assert.assertEquals;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.BillerPaymentFileOutboundFtpTasklet;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BillerPaymentFileOutboundFtpTasklet.class})
@ActiveProfiles("test")
public class BillerPaymentFileOutboundFtpTaskletTest {
	
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
	BillerPaymentFileOutboundFtpTasklet billerPaymentFileFtpTasklet;

	@Test
	public void testPrevStatus(){
		System.out.println("test previous status");
		when(chunkContext.getStepContext()).thenReturn(stepContext);
		when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS)).thenReturn(BatchSystemConstant.ExitCode.FAILED );
		assertEquals(RepeatStatus.FINISHED, billerPaymentFileFtpTasklet.execute(stepContribution, chunkContext));
	}
	
	@Test
	public void testException() {
		System.out.println("test Exception");
		when(chunkContext.getStepContext()).thenReturn(stepContext);
		when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS)).thenReturn(BatchSystemConstant.ExitCode.SUCCESS );
		assertEquals(RepeatStatus.FINISHED, billerPaymentFileFtpTasklet.execute(stepContribution, chunkContext));
	}
	
	@Before
	public void setup() {
		
	}
	
	@After
	public void cleanup() {
		
	}
}
