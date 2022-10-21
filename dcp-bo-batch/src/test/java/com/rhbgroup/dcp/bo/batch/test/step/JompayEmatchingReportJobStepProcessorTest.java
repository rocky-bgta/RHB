package com.rhbgroup.dcp.bo.batch.test.step;

import static org.mockito.Mockito.when;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.job.model.JompayEmatchingReportOutDetail;
import com.rhbgroup.dcp.bo.batch.job.model.JompayEmatchingReportPaymentTxn;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class JompayEmatchingReportJobStepProcessorTest {
	// JompayEmatchingReportJobStepBuilder

	@Autowired
	@Qualifier("JompayEmatchingReportJobStep.ItemProcessor")
	private ItemProcessor<JompayEmatchingReportPaymentTxn, JompayEmatchingReportOutDetail> itemProcessor;
	
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
	public void testInvalidTxn() throws Exception{
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		when(stepExecution.getJobExecution().getExecutionContext()).thenReturn(executionContext);
		JompayEmatchingReportPaymentTxn jompayDetailTxn = new JompayEmatchingReportPaymentTxn();
		assertNotNull(itemProcessor.process(jompayDetailTxn)) ;
	}

}
