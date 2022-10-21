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
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustAccountHoldingRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustAccountRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustCustomerRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustAccountFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustAccountHoldingFileToDBStepBuilder;
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
public class LoadEMUnitTrustAccountHldFileStepTest {
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustAccountHldFileStepTest.class);
	private static final String STEP_NAME = "LoadEMUnitTrustAccountHoldingFileToDBStep";
	
	@Autowired
	LoadEMUnitTrustJobConfigProperties configProperties;
	
	@Autowired
	LoadEMUnitTrustAccountHoldingFileToDBStepBuilder loadAccountStep;
	
	@MockBean(name="utAccountHldRepoImpl")
	UnitTrustAccountHoldingRepositoryImpl mockUTAccountRepoImpl;
	
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
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		List<UnitTrustFileAbs> utFile = new ArrayList<>();
		utFile.add((UnitTrustFileAbs)account);
		when(mockJobControlRepoImpl.updateTblAccountHldStatus(Mockito.any(BatchUnitTrustJobStatusControl.class)))
			.thenThrow(BadSqlGrammarException.class);
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
		when(mockUTAccountRepoImpl.deleteAllRecords(1)).thenThrow(BadSqlGrammarException.class);
		loadAccountStep.utReader(stepExecution);
	}

	@Test
	public void testAccountEmpty() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testFundIdEmpty() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testHoldingEmpty() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	@Test
	public void testFundCurrMarketVal() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testFundCurrGainLoss() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("123");
		account.setFundCurrUnrealisedGainLoss("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testFundCurrGainLossPercent() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("123");
		account.setFundCurrUnrealisedGainLoss("123");
		account.setFundCurrUnrealisedGainLossPercent("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testCurrInvestAmnt() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("123");
		account.setFundCurrUnrealisedGainLoss("123");
		account.setFundCurrUnrealisedGainLossPercent("123");
		account.setFundCurrInvestAmnt("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testCurrAvgUnitPrice() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("123");
		account.setFundCurrUnrealisedGainLoss("123");
		account.setFundCurrUnrealisedGainLossPercent("123");
		account.setFundCurrInvestAmnt("123");
		account.setFundCurrAvgUnitPrice("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testMyrMarketVal() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("123");
		account.setFundCurrUnrealisedGainLoss("123");
		account.setFundCurrUnrealisedGainLossPercent("123");
		account.setFundCurrInvestAmnt("123");
		account.setFundCurrAvgUnitPrice("123");
		account.setFundMyrMarketVal("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testMyrUnrealisedGainLoss() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("123");
		account.setFundCurrUnrealisedGainLoss("123");
		account.setFundCurrUnrealisedGainLossPercent("123");
		account.setFundCurrInvestAmnt("123");
		account.setFundCurrAvgUnitPrice("123");
		account.setFundMyrMarketVal("123");
		account.setFundMyrUnrealisedGainLoss("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testMyrUnrealisedGainLossPercent() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("123");
		account.setFundCurrUnrealisedGainLoss("123");
		account.setFundCurrUnrealisedGainLossPercent("123");
		account.setFundCurrInvestAmnt("123");
		account.setFundCurrAvgUnitPrice("123");
		account.setFundMyrMarketVal("123");
		account.setFundMyrUnrealisedGainLoss("123");
		account.setFundMyrUnrealisedGainLossPercent("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testFundMyrInvestAmnt() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("123");
		account.setFundCurrUnrealisedGainLoss("123");
		account.setFundCurrUnrealisedGainLossPercent("123");
		account.setFundCurrInvestAmnt("123");
		account.setFundCurrAvgUnitPrice("123");
		account.setFundMyrMarketVal("123");
		account.setFundMyrUnrealisedGainLoss("123");
		account.setFundMyrUnrealisedGainLossPercent("123");
		account.setFundMyrInvestAmnt("");		
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
	}
	
	@Test
	public void testMyrAvgUnitPrice() throws Exception {
		UnitTrustAccountHoldingDetail account = new UnitTrustAccountHoldingDetail();
		account.setAcctNo("123");
		account.setFundId("123");
		account.setHoldingUnit("123");
		account.setFundCurrMarketVal("123");
		account.setFundCurrUnrealisedGainLoss("123");
		account.setFundCurrUnrealisedGainLossPercent("123");
		account.setFundCurrInvestAmnt("123");
		account.setFundCurrAvgUnitPrice("123");
		account.setFundMyrMarketVal("123");
		account.setFundMyrUnrealisedGainLoss("123");
		account.setFundMyrUnrealisedGainLossPercent("123");
		account.setFundMyrInvestAmnt("123");
		account.setFundMyrAvgUnitPrice("");
		Assert.assertNull(itemProcessor.process((UnitTrustFileAbs)account));
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
