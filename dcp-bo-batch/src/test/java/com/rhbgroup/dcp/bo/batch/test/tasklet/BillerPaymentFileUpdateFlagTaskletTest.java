package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Queue;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerPaymentConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.BillerPaymentFileUpdateFlagTasklet;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BillerPaymentFileUpdateFlagTasklet.class})
@ActiveProfiles("test")
public class BillerPaymentFileUpdateFlagTaskletTest {
	
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
	BillerPaymentFileUpdateFlagTasklet updateFlagTasklet;
	
	@MockBean(name = "BillPaymentConfigOutboundQueue")
	private Queue<BillerPaymentOutboundConfig> queue;
	
	@MockBean(name = "batchBillerPaymentConfigRepositoryImpl")
	BatchBillerPaymentConfigRepositoryImpl mockBillerPaymentConfigRepositoryImpl;
	
	@Test
	public void testPrevStatus(){
		System.out.println("test previous status");
		when(chunkContext.getStepContext()).thenReturn(stepContext);
		when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS)).thenReturn(BatchSystemConstant.ExitCode.FAILED );
		assertEquals(RepeatStatus.FINISHED, updateFlagTasklet.execute(stepContribution, chunkContext));
	}
	
	@Test
	public void testException() {
		System.out.println("test Exception");
		when(chunkContext.getStepContext()).thenReturn(stepContext);
		when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
		when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS)).thenReturn(BatchSystemConstant.ExitCode.SUCCESS );
		assertEquals(RepeatStatus.FINISHED, updateFlagTasklet.execute(stepContribution, chunkContext));
	}
}
