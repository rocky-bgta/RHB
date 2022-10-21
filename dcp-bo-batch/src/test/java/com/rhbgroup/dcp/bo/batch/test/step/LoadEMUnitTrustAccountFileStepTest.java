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
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccountDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustAccountRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustCustomerRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustAccountFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustCustomerFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class LoadEMUnitTrustAccountFileStepTest {
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustAccountFileStepTest.class);
	private static final String STEP_NAME = "LoadEMUnitTrustAccountFileToDBStep";
	
	@Autowired
	LoadEMUnitTrustJobConfigProperties configProperties;
	
	@Autowired
	LoadEMUnitTrustAccountFileToDBStepBuilder loadAccountStep;
	
	@MockBean(name="utAccountRepoImpl")
	UnitTrustAccountRepositoryImpl mockUTAccountRepoImpl;
	
	@MockBean(name="utJobControlRepoImpl")
	BatchUnitTrustJobStatusControlRepositoryImpl mockJobControlRepoImpl;
	
	
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
	
	@Test(expected=Exception.class)
	public void testReaderException() throws Exception {
		for(UTFile utFile: configProperties.getUtFiles()) {
			utFile.setDownloadFilePath("/target/file.txt");
		}
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		when(stepExecution.getJobExecution().getExecutionContext()).thenReturn(executionContext);
		when(stepExecution.getJobExecution().getExecutionContext().getInt(TARGET_DATA_SET)).thenReturn(1);
		when(stepExecution.getJobExecution().getId()).thenReturn(1L);
		when(mockUTAccountRepoImpl.deleteAllRecords(1)).thenThrow(BadSqlGrammarException.class);
		loadAccountStep.utReader(stepExecution);
	}
	
	@Test(expected=BatchException.class)
	public void testWriterException() throws Exception {
		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
		account.setAccountNo("11223");
		account.setSignatoryCode("11223");
		account.setSignatoryDescription("11223");
		account.setAccountType("11223");
		account.setAccountStatusCode("111");
		account.setAccountStatusDesc("121212");
		account.setAccountInvestProduct("121212");
		account.setLastPerformedTxnDate("20181105");
		List<UnitTrustFileAbs> utFile = new ArrayList<>();
		utFile.add((UnitTrustFileAbs)account);
		when(mockJobControlRepoImpl.updateTblAccountStatus(Mockito.any(BatchUnitTrustJobStatusControl.class)))
		.thenThrow(BadSqlGrammarException.class);
		itemWriter.write(utFile);
	}

	@Test
	public void testAccountEmpty() throws Exception {
		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
		account.setAccountNo("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
//	@Test
//	public void testSignCodeEmpty() throws Exception{
//		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
//		account.setAccountNo("11223");
//		account.setSignatoryCode("");
//		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
//	}
	
//	@Test
//	public void testSignDescEmpty() throws Exception{
//		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
//		account.setAccountNo("11223");
//		account.setSignatoryCode("11223");
//		account.setSignatoryDescription("");
//		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
//	}
	
	@Test
	public void testAccountTypeEmpty() throws Exception{
		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
		account.setAccountNo("11223");
		account.setSignatoryCode("11223");
		account.setSignatoryDescription("11223");
		account.setAccountType("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testAccountStatusCodeEmpty() throws Exception{
		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
		account.setAccountNo("11223");
		account.setSignatoryCode("11223");
		account.setSignatoryDescription("11223");
		account.setAccountType("11223");
		account.setAccountStatusCode("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testAccountStatusDescEmpty() throws Exception{
		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
		account.setAccountNo("11223");
		account.setSignatoryCode("11223");
		account.setSignatoryDescription("11223");
		account.setAccountType("11223");
		account.setAccountStatusCode("111");
		account.setAccountStatusDesc("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testAccountInvestEmpty() throws Exception{
		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
		account.setAccountNo("11223");
		account.setSignatoryCode("11223");
		account.setSignatoryDescription("11223");
		account.setAccountType("11223");
		account.setAccountStatusCode("111");
		account.setAccountStatusDesc("121212");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
//	@Test
//	public void testLastDateEmpty() throws Exception{
//		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
//		account.setAccountNo("11223");
//		account.setSignatoryCode("11223");
//		account.setSignatoryDescription("11223");
//		account.setAccountType("11223");
//		account.setAccountStatusCode("111");
//		account.setAccountStatusDesc("121212");
//		account.setAccountInvestProduct("121212");
//		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
//	}
	
//	@Test
//	public void testLastDateNonNumeric() throws Exception{
//		UnitTrustAccountDetail account = new UnitTrustAccountDetail();
//		account.setAccountNo("11223");
//		account.setSignatoryCode("11223");
//		account.setSignatoryDescription("11223");
//		account.setAccountType("11223");
//		account.setAccountStatusCode("111");
//		account.setAccountStatusDesc("121212");
//		account.setAccountInvestProduct("121212");
//		account.setLastPerformedTxnDate("2018-12-12");
//		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
//	}
	
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
