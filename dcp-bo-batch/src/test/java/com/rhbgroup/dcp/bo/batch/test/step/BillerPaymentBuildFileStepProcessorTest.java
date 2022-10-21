package com.rhbgroup.dcp.bo.batch.test.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Queue;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundTxn;
import com.rhbgroup.dcp.bo.batch.job.step.BillerPaymentFileJobStepBuilder;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class BillerPaymentBuildFileStepProcessorTest {
	
	@Autowired
	private BillerPaymentFileJobStepBuilder billerPaymentFileJobStepBuilder;
	
	@Autowired
	@Qualifier("BillerPaymentFileJob.ItemProcessor")
	private ItemProcessor<BillerPaymentOutboundTxn, BillerPaymentOutboundDetail> itemProcessor;
	
	@MockBean(name="BillPaymentConfigOutboundQueue")
	private Queue<BillerPaymentOutboundConfig> queue ;
	
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
	
	@Test
	public void testProcessorException() throws Exception {
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		when(stepExecution.getJobExecution().getExecutionContext()).thenReturn(executionContext);
		BillerPaymentOutboundTxn outboundTxn = new BillerPaymentOutboundTxn();
		outboundTxn.setTxnAmount("xx");
		assertNull(itemProcessor.process(outboundTxn));
	}
}
