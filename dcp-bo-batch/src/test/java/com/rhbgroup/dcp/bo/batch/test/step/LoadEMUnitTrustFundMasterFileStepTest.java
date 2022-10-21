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
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccountHoldingDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFundMasterDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustAccountHoldingRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustAccountRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustCustomerRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustFundMasterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustAccountFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustAccountHoldingFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustCustomerFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustFundMasterFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class LoadEMUnitTrustFundMasterFileStepTest {
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustFundMasterFileStepTest.class);
	private static final String STEP_NAME = "LoadEMUnitTrustFundMasterFileToDBStep";
	
	@Autowired
	LoadEMUnitTrustJobConfigProperties configProperties;
	
	@Autowired
	LoadEMUnitTrustFundMasterFileToDBStepBuilder loadFundMasterStep;
	
	@MockBean(name="utFundMasterRepoImpl")
	UnitTrustFundMasterRepositoryImpl mockUTFundRepoImpl;
	
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
	public void testWriterException() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		List<UnitTrustFileAbs> utFile = new ArrayList<>();
		utFile.add((UnitTrustFileAbs)fund);
		when(mockJobControlRepoImpl.updateTblFundMasterStatus(Mockito.any(BatchUnitTrustJobStatusControl.class)))
				.thenThrow(BatchException.class);
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
		when(mockUTFundRepoImpl.deleteAllRecords(1)).thenThrow(BadSqlGrammarException.class);
		loadFundMasterStep.utReader(stepExecution);
	}

	@Test
	public void testFundIdEmpty() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
	}
	
	@Test
	public void testFundNameEmpty() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("123");
		fund.setFundName("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
	}
	
	@Test
	public void testFundCurrEmpty() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("123");
		fund.setFundName("123");
		fund.setFundCurr("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
	}
	
	@Test
	public void testFundCurrNavPriceEmpty() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("123");
		fund.setFundName("123");
		fund.setFundCurr("123");
		fund.setFundCurrNavPrice("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
	}
	
	@Test
	public void testNavDate() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("123");
		fund.setFundName("123");
		fund.setFundCurr("123");
		fund.setFundCurrNavPrice("123");
		fund.setNavDate("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
	}
	
	@Test
	public void testProdCatCode() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("123");
		fund.setFundName("123");
		fund.setFundCurr("123");
		fund.setFundCurrNavPrice("123");
		fund.setNavDate("123");
		fund.setProdCategoryCode("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
	}
	
	@Test
	public void testProdCatDesc() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("123");
		fund.setFundName("123");
		fund.setFundCurr("123");
		fund.setFundCurrNavPrice("123");
		fund.setNavDate("123");
		fund.setProdCategoryCode("123");
		fund.setProdCategoryDesc("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
	}
	
	@Test
	public void testRiskCode() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("123");
		fund.setFundName("123");
		fund.setFundCurr("123");
		fund.setFundCurrNavPrice("123");
		fund.setNavDate("123");
		fund.setProdCategoryCode("123");
		fund.setProdCategoryDesc("123");
		fund.setRiskLevelCode("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
	}
	
	@Test
	public void testRiskDesc() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("123");
		fund.setFundName("123");
		fund.setFundCurr("123");
		fund.setFundCurrNavPrice("123");
		fund.setNavDate("123");
		fund.setProdCategoryCode("123");
		fund.setProdCategoryDesc("123");
		fund.setRiskLevelCode("123");
		fund.setRiskLevelDesc("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
	}
	
	@Test
	public void testMyrNavPrice() throws Exception {
		UnitTrustFundMasterDetail fund = new UnitTrustFundMasterDetail();
		fund.setFundId("123");
		fund.setFundName("123");
		fund.setFundCurr("123");
		fund.setFundCurrNavPrice("123");
		fund.setNavDate("123");
		fund.setProdCategoryCode("123");
		fund.setProdCategoryDesc("123");
		fund.setRiskLevelCode("123");
		fund.setRiskLevelDesc("123");
		fund.setMyrNavPrice("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)fund));
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
