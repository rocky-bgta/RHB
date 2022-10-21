package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.rhbgroup.dcp.bo.batch.framework.utils.EAIUtils;
//import com.rhbgroup.dcp.bo.batch.framework.utils.JsonUtils;
import com.rhbgroup.dcp.bo.batch.job.config.ExtractAndUpdateExchangeRateJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractAndUpdateExchangeRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateExchangeRate;
import com.rhbgroup.dcp.bo.batch.job.model.EAIExchangeRateResponse;
//import com.rhbgroup.dcp.bo.batch.job.model.EAISessionTokenResponse;
import com.rhbgroup.dcp.bo.batch.job.model.Rate;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUpdateExchangeRateRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateCurrencyRateConfigStepBuilder;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(EAIUtils.class)
@PowerMockIgnore(value = { "javax.net.ssl.*" })
@SpringBootTest(classes = {
		BatchTestConfigHSQL.class,
		BatchUpdateExchangeRate.class,
		EAIExchangeRateResponse.class,
		ExtractAndUpdateExchangeRateJobConfiguration.class
		})
@TestExecutionListeners({
	DependencyInjectionTestExecutionListener.class,
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class UpdateCurrencyRateConfigStepTest {

	private static final Logger logger = Logger.getLogger(UpdateCurrencyRateConfigStepTest.class);
	public static final String JOB_NAME = "ExtractAndUpdateExchangeRateJob";
	public static final String JOB_LAUNCHER_UTILS = "ExtractAndUpdateExchangeRateJobLauncherTestUtils";
	private static final String STEP_NAME = "UpdateCurrencyRateConfigStep";

	private ArrayList<BatchUpdateExchangeRate> batchUpdateExchangeRateList = new ArrayList<BatchUpdateExchangeRate>();

	@Autowired
	private ExtractAndUpdateExchangeRateJobConfigProperties jobConfigProperties;
	
	@MockBean(name="batchUpdateExchangeRateRepositoryImpl")
	private BatchUpdateExchangeRateRepositoryImpl mockUpdateExchangeRateRepositoryImpl;
	
	@Mock
	private StepExecution stepExecution;
	@Mock
	private JobExecution jobExecution;
	@Mock	
	protected JobParameters jobParams;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	BatchUpdateExchangeRate batchUpdateExchangeRate1 = new BatchUpdateExchangeRate();
	BatchUpdateExchangeRate batchUpdateExchangeRate2 = new BatchUpdateExchangeRate();
	BatchUpdateExchangeRate batchUpdateExchangeRate3 = new BatchUpdateExchangeRate();
	BatchUpdateExchangeRate batchUpdateExchangeRate4 = new BatchUpdateExchangeRate();
	BatchUpdateExchangeRate batchUpdateExchangeRate5 = new BatchUpdateExchangeRate();
	BatchUpdateExchangeRate batchUpdateExchangeRate6 = new BatchUpdateExchangeRate();
	BatchUpdateExchangeRate batchUpdateExchangeRate7 = new BatchUpdateExchangeRate();
	EAIExchangeRateResponse eaiExchangeRateAUD = new EAIExchangeRateResponse();
	EAIExchangeRateResponse eaiExchangeRateBDT = new EAIExchangeRateResponse();
	EAIExchangeRateResponse eaiExchangeRateBND = new EAIExchangeRateResponse();
	EAIExchangeRateResponse eaiExchangeRateCAD = new EAIExchangeRateResponse();
	EAIExchangeRateResponse eaiExchangeRateCNY = new EAIExchangeRateResponse();
	EAIExchangeRateResponse eaiExchangeRateDKK = new EAIExchangeRateResponse();
	EAIExchangeRateResponse eaiExchangeRateIDR = new EAIExchangeRateResponse();
	EAIExchangeRateResponse eaiExchangeRateXXX = new EAIExchangeRateResponse();


	@Autowired
	private UpdateCurrencyRateConfigStepBuilder stepBuilder;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchUpdateExchangeRate> reader;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchUpdateExchangeRate, BatchUpdateExchangeRate> processor;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	public ItemWriter<BatchUpdateExchangeRate> writer;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

//	@MockBean
//	private JdbcPagingItemReader ItemReader;

	@Before
	public void setup() throws Exception {
		createGetDateFunction();
		createItemReaderDBTable();
		insertItemReaderDBData();
		setupBatchUpdateExchangeRate();
		MockitoAnnotations.initMocks(this);
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		when(stepExecution.getJobExecution().getId()).thenReturn(1L);
	}

	@After
	public void cleanupDB() {
		cleanupItemReaderDB();
		cleanupItemReaderDBFunction();
	}

	/*
	 * Testing end to end for UpdateCurrencyRateConfigStepBuilder.
	 */
	@Test
	public void testBuildStep() throws Exception {
		String myStepName = stepBuilder.buildStep().getName();
		assertEquals(STEP_NAME, myStepName);
	}

	/*
	 * Testing iItemReader for UpdateCurrencyRateConfigStepBuilder.
	 */
	//@Test
	public void testItemReader() throws Exception {
		List<BatchUpdateExchangeRate> results = StepScopeTestUtils.doInStepScope(stepExecution,
				new Callable<List<BatchUpdateExchangeRate>>() {
					public List<BatchUpdateExchangeRate> call() throws Exception {
						BatchUpdateExchangeRate batchUpdateExchangeRate;
						List<BatchUpdateExchangeRate> batchUpdateExchangeRates = new ArrayList<>();
						while ((batchUpdateExchangeRate = reader.read()) != null) {
							batchUpdateExchangeRates.add(batchUpdateExchangeRate);
						}
						return batchUpdateExchangeRates;
					}
				});
		assertEquals(7, results.size());
	}
	
	//@Test
	public void testItemReaderEmptyRecord() throws Exception {
		cleanupItemReaderDB();
		List<BatchUpdateExchangeRate> results = StepScopeTestUtils.doInStepScope(stepExecution,
				new Callable<List<BatchUpdateExchangeRate>>() {
					public List<BatchUpdateExchangeRate> call() throws Exception {
						BatchUpdateExchangeRate batchUpdateExchangeRate;
						List<BatchUpdateExchangeRate> batchUpdateExchangeRates = new ArrayList<>();
						while ((batchUpdateExchangeRate = reader.read()) != null) {
							batchUpdateExchangeRates.add(batchUpdateExchangeRate);
						}
						return batchUpdateExchangeRates;
					}
				});
		assertEquals(0, results.size());
	}

	/*
	 * Testing ItemProcessor for UpdateCurrencyRateConfigStepBuilder. Invalid Currency
	 */
	@Test
	public void testItemProcessorInvalidCurrency() throws Exception {
		PowerMockito.mockStatic(EAIUtils.class);
		
		BatchUpdateExchangeRate batchUpdateExchangeRate1  = new BatchUpdateExchangeRate();
		batchUpdateExchangeRate1.setCode("XXX");
		batchUpdateExchangeRate1.setBuyTt(0.00);
		batchUpdateExchangeRate1.setUnit(0);
		
//		PowerMockito.when(EAIUtils.class,"getEAIExchangeRate", Mockito.anyString(),Mockito.anyString() ).thenThrow(RestClientException.class);
		PowerMockito.when(EAIUtils.class, "getEAIExchangeRate", 
				Mockito.anyString(), 
				Mockito.anyString(), 
				Mockito.anyString(),
				Mockito.any()).thenThrow(RestClientException.class);
		
		logger.info(String.format("testItemProcessorInvalidCurrency - BatchUpdateExchangeRate: %s", batchUpdateExchangeRate1.toString()));
		
		BatchUpdateExchangeRate actual = processor.process(batchUpdateExchangeRate1);
		Assert.assertFalse(actual.isProcessed());
	}
	
	/*
	 * Testing ItemProcessor for UpdateCurrencyRateConfigStepBuilder.
	 */
	//@Test
	public void testItemProcessor() throws Exception {
		PowerMockito.mockStatic(EAIUtils.class);
		Map<String, EAIExchangeRateResponse> eaiExhangeRateMap = setupEAIExchangeRateResult();
		
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("AUD"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq(""), Mockito.any())).thenReturn(eaiExhangeRateMap.get("AUD"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("BDT"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq(""), Mockito.any())).thenReturn(eaiExhangeRateMap.get("BDT"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("BND"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq(""), Mockito.any())).thenReturn(eaiExhangeRateMap.get("BND"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("CAD"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq(""), Mockito.any())).thenReturn(eaiExhangeRateMap.get("CAD"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("CNY"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq(""), Mockito.any())).thenReturn(eaiExhangeRateMap.get("CNY"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("DKK"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq(""), Mockito.any())).thenReturn(eaiExhangeRateMap.get("DKK"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("IDR"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq(""), Mockito.any())).thenReturn(eaiExhangeRateMap.get("IDR"));
		for (BatchUpdateExchangeRate batchUpdateExchangeRate : batchUpdateExchangeRateList) {
			logger.info(String.format("BatchUpdateExchangeRate: %s", batchUpdateExchangeRate.toString()));
			BatchUpdateExchangeRate actual = processor.process(batchUpdateExchangeRate);
			assertNotNull(actual.getBuyTt());
			assertNotNull(actual.getUnit());
			assertTrue(actual.isProcessed());
		}
	}

	/*
	 * Testing negative case for ItemProcessor in
	 * UpdateCurrencyRateConfigStepBuilder.
	 */
	//@Test
	public void testItemProcessorWithNullValue() throws Exception {
		PowerMockito.mockStatic(EAIUtils.class);
		int noOfIsProcessed = 0;
		int expectedNoOfIsProcessed = 6;
		
		for (BatchUpdateExchangeRate batchUpdateExchangeRate : batchUpdateExchangeRateList) {
			logger.info(String.format("BatchUpdateExchangeRate: %s", batchUpdateExchangeRate.toString()));
			BatchUpdateExchangeRate actual = processor.process(batchUpdateExchangeRate);
			if(actual.isProcessed()) noOfIsProcessed++;
		}
		
		assertEquals(expectedNoOfIsProcessed, noOfIsProcessed);
	}

	/*
	 * Testing ItemWriter for UpdateCurrencyRateConfigStepBuilder.
	 */
	@Test
	public void testItemWriter() throws Exception {
		for (BatchUpdateExchangeRate batchUpdateExchangeRate : batchUpdateExchangeRateList) {
			batchUpdateExchangeRate.setUnit(batchUpdateExchangeRate.getUnit() * 10);
			batchUpdateExchangeRate.setBuyTt(batchUpdateExchangeRate.getBuyTt() * 14);
			batchUpdateExchangeRate.setProcessed(true);
			when(mockUpdateExchangeRateRepositoryImpl.updateStagingBatchUpdateExchangeRate(batchUpdateExchangeRate)).thenReturn(1);
		}
		writer.write(batchUpdateExchangeRateList);
	}
	
	/*
	 * Testing ItemWriter for UpdateCurrencyRateConfigStepBuilder. Exception
	 */
	@Test
	public void testItemWriterException () throws Exception {
		for (BatchUpdateExchangeRate batchUpdateExchangeRate : batchUpdateExchangeRateList) {
			batchUpdateExchangeRate.setUnit(batchUpdateExchangeRate.getUnit() * 10);
			batchUpdateExchangeRate.setBuyTt(batchUpdateExchangeRate.getBuyTt() * 14);
			batchUpdateExchangeRate.setProcessed(true);
			when(mockUpdateExchangeRateRepositoryImpl.updateStagingBatchUpdateExchangeRate(batchUpdateExchangeRate)).
				thenThrow(BadSqlGrammarException.class);
		}
		writer.write(batchUpdateExchangeRateList);
	}

	private void setupBatchUpdateExchangeRate() {
		batchUpdateExchangeRate1.setCode("AUD");
		batchUpdateExchangeRate1.setBuyTt(0.012);
		batchUpdateExchangeRate1.setUnit(1);

		batchUpdateExchangeRate2.setCode("BDT");
		batchUpdateExchangeRate2.setBuyTt(0.2410000);
		batchUpdateExchangeRate2.setUnit(1);

		batchUpdateExchangeRate3.setCode("BND");
		batchUpdateExchangeRate3.setBuyTt(0.0240000);
		batchUpdateExchangeRate3.setUnit(10);

		batchUpdateExchangeRate4.setCode("CAD");
		batchUpdateExchangeRate4.setBuyTt(0.1240000);
		batchUpdateExchangeRate4.setUnit(10);

		batchUpdateExchangeRate5.setCode("CNY");
		batchUpdateExchangeRate5.setBuyTt(0.0323000);
		batchUpdateExchangeRate5.setUnit(100);

		batchUpdateExchangeRate6.setCode("DKK");
		batchUpdateExchangeRate6.setBuyTt(0.0633000);
		batchUpdateExchangeRate6.setUnit(100);

		batchUpdateExchangeRate7.setCode("IDR");
		batchUpdateExchangeRate7.setBuyTt(0.4240000);
		batchUpdateExchangeRate7.setUnit(100);

		batchUpdateExchangeRateList.add(batchUpdateExchangeRate1);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate2);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate3);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate4);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate5);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate6);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate7);
	}

	public void createGetDateFunction() throws IOException {
		String createFunction = "CREATE FUNCTION GETDATE() RETURNS TIMESTAMP RETURN NOW()";
		jdbcTemplate.execute(createFunction);
	}
	
	private Map<String, EAIExchangeRateResponse> setupEAIExchangeRateResult() {
		
		Map<String, EAIExchangeRateResponse> eaiExhangeRateMap = new HashMap<>();
		Rate rate = new Rate();
		List<Rate> listRate = new ArrayList<>();

		rate.setBuyTT(3.00);
		rate.setSellTT(4.00);

		rate.setUnit(1);
		rate.setCode("AUD");
		listRate.clear();
		listRate.add(rate);
		eaiExchangeRateAUD.setRate(listRate);

		rate.setBuyTT(1.00);
		rate.setSellTT(2.00);

		rate.setUnit(1);
		rate.setCode("BDT");
		listRate.clear();
		listRate.add(rate);
		eaiExchangeRateBDT.setRate(listRate);

		rate.setBuyTT(0.3454);
		rate.setSellTT(0.4000);

		rate.setUnit(1);
		rate.setCode("BND");
		listRate.clear();
		listRate.add(rate);
		eaiExchangeRateBND.setRate(listRate);

		rate.setBuyTT(0.00245);
		rate.setSellTT(0.00345);

		rate.setUnit(10);
		rate.setCode("CAD");
		listRate.clear();
		listRate.add(rate);
		eaiExchangeRateCAD.setRate(listRate);

		rate.setBuyTT(0.9855);
		rate.setSellTT(1.0000);

		rate.setUnit(100);
		rate.setCode("CNY");
		listRate.clear();
		listRate.add(rate);
		eaiExchangeRateCNY.setRate(listRate);

		rate.setBuyTT(0.0016534);
		rate.setSellTT(0.0026534);

		rate.setUnit(100);
		rate.setCode("DKK");
		listRate.clear();
		listRate.add(rate);
		eaiExchangeRateDKK.setRate(listRate);

		rate.setBuyTT(0.55763);
		rate.setSellTT(0.65763);

		rate.setUnit(100);
		rate.setCode("IDR");
		listRate.clear();
		listRate.add(rate);
		eaiExchangeRateIDR.setRate(listRate);

		eaiExhangeRateMap.put("AUD", eaiExchangeRateAUD);
		eaiExhangeRateMap.put("BDT", eaiExchangeRateBDT);
		eaiExhangeRateMap.put("BND", eaiExchangeRateBND);
		eaiExhangeRateMap.put("CAD", eaiExchangeRateCAD);
		eaiExhangeRateMap.put("CNY", eaiExchangeRateCNY);
		eaiExhangeRateMap.put("DKK", eaiExchangeRateDKK);
		eaiExhangeRateMap.put("IDR", eaiExchangeRateIDR);

		setupGetExchangeRateWhenReturn();

		return eaiExhangeRateMap;
	}

	private void setupGetExchangeRateWhenReturn() {
//		when(EAIUtils.getEAIExchangeRate("AUD")).thenReturn(eaiExchangeRateAUD);
//		when(EAIUtils.getEAIExchangeRate("BDT")).thenReturn(eaiExchangeRateBDT);
//		when(EAIUtils.getEAIExchangeRate("BND")).thenReturn(eaiExchangeRateBND);
//		when(EAIUtils.getEAIExchangeRate("CAD")).thenReturn(eaiExchangeRateCAD);
//		when(EAIUtils.getEAIExchangeRate("CNY")).thenReturn(eaiExchangeRateCNY);
//		when(EAIUtils.getEAIExchangeRate("DKK")).thenReturn(eaiExchangeRateDKK);
//		when(EAIUtils.getEAIExchangeRate("IDR")).thenReturn(eaiExchangeRateIDR);
	}

	private void insertItemReaderDBData() throws IOException {
		String insert1 = "INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (job_execution_id, currency_rate_config_id ,code ,description ,buy_tt ,unit ,is_processed ,created_time ,updated_time) VALUES (1, 1, 'AUD', 'Australian Dollar', 0.0120000, 1, false, NOW(), NOW())";
		String insert2 = "INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (job_execution_id, currency_rate_config_id ,code ,description ,buy_tt ,unit ,is_processed ,created_time ,updated_time) VALUES (1, 2, 'BDT', 'Bangladesh taka', 0.2410000, 1, false, NOW(), NOW())";
		String insert3 = "INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (job_execution_id, currency_rate_config_id ,code ,description ,buy_tt ,unit ,is_processed ,created_time ,updated_time) VALUES (1, 3, 'BND', 'Bruneian Dollar', 0.0240000, 10, false, NOW(), NOW())";
		String insert4 = "INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (job_execution_id, currency_rate_config_id ,code ,description ,buy_tt ,unit ,is_processed ,created_time ,updated_time) VALUES (1, 4, 'CAD', 'Canadian Dollar', 0.1240000, 10, false, NOW(), NOW())";
		String insert5 = "INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (job_execution_id, currency_rate_config_id ,code ,description ,buy_tt ,unit ,is_processed ,created_time ,updated_time) VALUES (1, 5, 'CNY', 'Chinese Yuan Renminbi', 0.0323000, 100, false, NOW(), NOW())";
		String insert6 = "INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (job_execution_id, currency_rate_config_id ,code ,description ,buy_tt ,unit ,is_processed ,created_time ,updated_time) VALUES (1, 6, 'DKK', 'Danish krone', 0.0633000, 100, false, NOW(), NOW())";
		String insert7 = "INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (job_execution_id, currency_rate_config_id ,code ,description ,buy_tt ,unit ,is_processed ,created_time ,updated_time) VALUES (1, 7, 'IDR', 'Indonesian Rupiah', 0.4240000, 100, false, NOW(), NOW())";
		jdbcTemplate.batchUpdate(insert1, insert2, insert3, insert4, insert5, insert6, insert7);
	}

	private void createItemReaderDBTable() throws IOException {
//		ClassLoader classLoader = getClass().getClassLoader();
//		String createStagingTable = IOUtils.toString(classLoader.getResource("sql/create_exchange_rate_table.sql"),
//				Charset.defaultCharset());
//		jdbcTemplate.execute(createStagingTable);
	}

	private void cleanupItemReaderDB() {
		String deleteStagingTable = "TRUNCATE TABLE TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE";
		jdbcTemplate.execute(deleteStagingTable);
	}

	private void cleanupItemReaderDBFunction() {
		String deleteFunction = "DROP FUNCTION GETDATE";
		jdbcTemplate.execute(deleteFunction);
	}
}
