package com.rhbgroup.dcp.bo.batch.test.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.TARGET_DATA_SET;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties.UTFile;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerRelationDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustCustomerRelationshipRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustCustomerRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustCustomerFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustCustomerRelationFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class LoadEMUnitTrustCustomerRelFileStepTest {
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustCustomerRelFileStepTest.class);
	private static final String STEP_NAME = "LoadEMUnitTrustCustomerRelationFileToDBStep";
	
	@Autowired
	LoadEMUnitTrustCustomerRelationFileToDBStepBuilder loadCustomerRelStep;
	
	@MockBean(name="utCustomerRelRepoImpl")
	UnitTrustCustomerRelationshipRepositoryImpl mockCustomerRelRepoImpl;
	
	@MockBean(name="utJobControlRepoImpl")
	BatchUnitTrustJobStatusControlRepositoryImpl mockJobControlRepoImpl;

	@Autowired
	LoadEMUnitTrustJobConfigProperties configProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<UnitTrustFileAbs> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<UnitTrustFileAbs, UnitTrustFileAbs> itemProcessor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<UnitTrustFileAbs> itemWriter;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test(expected=BatchException.class)
	public void testWriterException() throws Exception {
		UnitTrustCustomerRelationDetail cust = new UnitTrustCustomerRelationDetail();
		cust.setCisNo("1111");
		cust.setAccountNo("1111000");
		cust.setJoinType("P");
		List<UnitTrustFileAbs> utFile = new ArrayList<>();
		utFile.add((UnitTrustFileAbs)cust);
		when(mockJobControlRepoImpl.updateTblCustomerRelStatus(Mockito.any(BatchUnitTrustJobStatusControl.class))).thenThrow(BatchException.class);
		itemWriter.write(utFile);
	}
	
	@Test(expected=Exception.class)
	public void testReaderException() throws Exception {
		for(UTFile utFile: configProperties.getUtFiles()) {
			utFile.setDownloadFilePath("/target/file.txt");
		}
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		when(stepExecution.getJobExecution().getExecutionContext()).thenReturn(executionContext);
		when(stepExecution.getJobExecution().getExecutionContext().getInt(TARGET_DATA_SET)).thenReturn(1);
		when(stepExecution.getJobExecution().getId()).thenReturn(1L);
		when(mockCustomerRelRepoImpl.deleteAllRecords(1)).thenThrow(BadSqlGrammarException.class);
		loadCustomerRelStep.utReader(stepExecution);
	}

	@Test
	public void testCISEmpty() throws Exception {
		UnitTrustCustomerRelationDetail cust = new UnitTrustCustomerRelationDetail();
		cust.setCisNo("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)cust));
	}
	
	@Test
	public void testAccountEmpty() throws Exception{
		UnitTrustCustomerRelationDetail cust = new UnitTrustCustomerRelationDetail();
		cust.setCisNo("0001122233");
		cust.setAccountNo("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)cust));
	}
	
	@Test
	public void testJoinEmpty() throws Exception{
		UnitTrustCustomerRelationDetail cust = new UnitTrustCustomerRelationDetail();
		cust.setCisNo("0001122233");
		cust.setAccountNo("112233");
		cust.setJoinType("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)cust));
	}
	
	@Before
	public void setup() throws Exception{
	}
	
	@After
	public void cleanup() throws Exception {
	}
	
	@Mock
	StepExecution stepExecution;
	
	@Mock
	JobExecution jobExecution;
	
	@Mock
	ExecutionContext executionContext;

}
