package com.rhbgroup.dcp.bo.batch.test.tasklet;

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
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.BillerPaymentRemoveQueueItemTasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.JOB_EXECUTION_STATUS;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BillerPaymentRemoveQueueItemTasklet.class})
@ActiveProfiles("test")
public class BillerPaymentRemoveQueueItemTaskletTest {

	@MockBean(name="BillPaymentConfigOutboundQueue")
	private Queue<BillerPaymentOutboundConfig> queue ;
	
	@Autowired
	BillerPaymentRemoveQueueItemTasklet removeQueueItem;
	
	@Mock
	ChunkContext chunkContext;
	
	@Mock
	StepContext stepContext;
	
	@Mock
	StepExecution stepExecution ;

	@Mock
	JobExecution jobExecution ;

	@Mock
	StepContribution stepContribution;
	
	@Mock
	ExecutionContext executionContext;
	
	@Test
	public void testException() throws Exception {
		when(queue.size()).thenReturn(0);
		assertEquals(RepeatStatus.FINISHED, removeQueueItem.execute(stepContribution, chunkContext));
	}
	
	@Test(expected=BatchException.class)
	public void testPrevStepStatus() throws Exception {
/*		chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS );
*/	
		when(chunkContext.getStepContext()).thenReturn(stepContext);
		when(stepContext.getStepExecution()).thenReturn(stepExecution);
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		when(jobExecution.getExecutionContext()).thenReturn(executionContext);
		when(executionContext.getInt(STEP_EXECUTION_STATUS)).thenReturn(BatchSystemConstant.ExitCode.FAILED);
		when(executionContext.containsKey(JOB_EXECUTION_STATUS)).thenReturn(true);
		when(executionContext.getInt(JOB_EXECUTION_STATUS)).thenReturn(BatchSystemConstant.ExitCode.FAILED);
		removeQueueItem.execute(stepContribution, chunkContext);
	}
}
