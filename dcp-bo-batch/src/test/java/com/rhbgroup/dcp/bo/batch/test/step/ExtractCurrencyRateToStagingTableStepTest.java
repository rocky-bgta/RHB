package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateExchangeRate;
import com.rhbgroup.dcp.bo.batch.job.model.CurrencyRateConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUpdateExchangeRateRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractCurrencyRateToStagingTableStepBuilder;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class ExtractCurrencyRateToStagingTableStepTest extends BaseJobTest {
	
	private static final Logger logger = Logger.getLogger(ExtractCurrencyRateToStagingTableStepTest.class);
	private static final String STEP_NAME = "ExtractCurrencyRateToStagingTableStep";

	private ArrayList<CurrencyRateConfig> currencyRateConfigList = new ArrayList<CurrencyRateConfig>();
	private ArrayList<BatchUpdateExchangeRate> batchUpdateExchangeRateList = new ArrayList<BatchUpdateExchangeRate>();

	private StepExecution stepExecution;
	@Mock
	protected JobParameters jobParams;
	
	@Autowired
	private ExtractCurrencyRateToStagingTableStepBuilder stepBuilder;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<CurrencyRateConfig> itemReader;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<CurrencyRateConfig, BatchUpdateExchangeRate> itemProcessor;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchUpdateExchangeRate> itemWriter;
	@MockBean
	private BatchUpdateExchangeRateRepositoryImpl batchUpdateExchangeRateRepositoryImpl;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@Before
	public void setUp() throws Exception {
		setupCurrencyRateConfig();
		setupBatchUpdateExchangeRate();
	}

	@After
	public void cleanup() {
		Mockito.reset(batchUpdateExchangeRateRepositoryImpl);
	}
	
    public StepExecution getStepExection() {
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
        return stepExecution;
    }

	/*
	 * Testing end to end for UpdateCurrencyRateConfigStepBuilder.
	 */
	@Test
	public void testBuildStep() throws Exception {
		String myStepName = stepBuilder.buildStep().getName();
		assertEquals(STEP_NAME, myStepName);
	}

	@Test
	public void testPositiveItemReader() throws Exception {
		insertDBData();
		List<CurrencyRateConfig> results = StepScopeTestUtils.doInStepScope(stepExecution,
				new Callable<List<CurrencyRateConfig>>() {

					@Override
					public List<CurrencyRateConfig> call() throws Exception {
						CurrencyRateConfig currencyRateConfig;
						List<CurrencyRateConfig> currencyRateConfigs = new ArrayList<>();
						while ((currencyRateConfig = itemReader.read()) != null) {
							currencyRateConfigs.add(currencyRateConfig);
						}
						return currencyRateConfigs;
					}

				});
		assertEquals(7, results.size());
	}
	
	@Test
	public void testNegativeItemReader() throws Exception {
		cleanupItemReaderDB(); //do cleanup (empty table)
		List<CurrencyRateConfig> results = StepScopeTestUtils.doInStepScope(stepExecution,
				new Callable<List<CurrencyRateConfig>>() {

					@Override
					public List<CurrencyRateConfig> call() throws Exception {
						CurrencyRateConfig currencyRateConfig;
						List<CurrencyRateConfig> currencyRateConfigs = new ArrayList<>();
						while ((currencyRateConfig = itemReader.read()) != null) {
							currencyRateConfigs.add(currencyRateConfig);
						}
						return currencyRateConfigs;
					}

				});
    	assertEquals(0, results.size());    	
	}

	@Test
	
	public void testPositiveItemProcessor() throws Exception {
		for (CurrencyRateConfig currencyRateConfig : currencyRateConfigList) {
			logger.info(String.format("CurrencyRateConfig: %s", currencyRateConfig.toString()));
			BatchUpdateExchangeRate actual = itemProcessor.process(currencyRateConfig);
			assertEquals(currencyRateConfig.getCode(), actual.getCode());
			assertEquals(currencyRateConfig.getBuyTt(), actual.getBuyTt());
			assertEquals(currencyRateConfig.getUnit(), actual.getUnit());
			assertEquals(currencyRateConfig.getId(), actual.getCurrencyRateConfigId());
		}
	}

	@Test
	public void testPositiveItemWriter() throws Exception {
		when(batchUpdateExchangeRateRepositoryImpl.addBatchUpdateExchangeRateStaging(Mockito.any(BatchUpdateExchangeRate.class))).thenReturn(1);
		itemWriter.write(batchUpdateExchangeRateList);

//		String sql = "SELECT currency_rate_config_id, code, buy_tt, unit FROM TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE";
//		List<BatchUpdateExchangeRate> result = jdbcTemplate.query(sql,
//				new BeanPropertyRowMapper<BatchUpdateExchangeRate>(BatchUpdateExchangeRate.class));
//		assertEquals(7, result.size());
		verify(batchUpdateExchangeRateRepositoryImpl , times(7)).addBatchUpdateExchangeRateStaging((BatchUpdateExchangeRate)Mockito.any());
	}
	
//	@Test(expected=BatchException.class)
	@Test
	public void testWriterFail() throws Exception{
		when(batchUpdateExchangeRateRepositoryImpl.addBatchUpdateExchangeRateStaging((BatchUpdateExchangeRate)Mockito.any())).thenThrow(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
//		expectedEx.expect(BatchException.class);
//		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		itemWriter.write(batchUpdateExchangeRateList);
		
		assertTrue(stepExecution.getJobExecution().getFailureExceptions().size() > 0);
	}

	private void insertDBData() {
		
    	logger.info("insert into VW_BATCH_CURRENCY_RATE_CONFIG..");
    	
		String insertSql1 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('AUD', 'Australian Dollar', 0.0120000, 0.0120000, 1, '2018-09-03 07:36:19.207', 'admin', '2018-09-03 07:36:19.207', 'admin')";
		String insertSql2 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('BDT', 'Bangladesh taka', 0.2410000, 0.2410000, 1, '2018-09-03 07:36:19.210', 'admin', '2018-09-06 08:16:06.080', 'admin')";
		String insertSql3 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('BND', 'Bruneian Dollar', 0.0240000, 0.0240000, 10, '2018-09-03 07:36:19.210', 'admin', '2018-09-06 08:16:06.080', 'admin')";
		String insertSql4 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('CAD', 'Canadian Dollar', 0.1240000, 0.1240000, 10, '2018-09-03 07:36:19.210', 'admin', '2018-09-03 07:36:19.210', 'admin')";
		String insertSql5 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('CNY', 'Chinese Yuan Renminbi', 0.0323000, 0.0323000, 100, '2018-09-03 07:36:19.210', 'admin', '2018-09-06 08:16:06.080', 'admin')";
		String insertSql6 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('DKK', 'Danish krone', 0.0633000, 0.0633000, 100, '2018-09-03 07:36:19.210', 'admin', '2018-09-06 08:16:06.083', 'admin')";
		String insertSql7 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('IDR', 'Indonesian Rupiah', 0.4240000, 0.4240000, 100, '2018-09-03 07:36:19.217', 'admin', '2018-09-06 08:16:06.083', 'admin')";
		
		int[] rows = jdbcTemplate.batchUpdate(insertSql1, insertSql2, insertSql3, insertSql4, insertSql5, insertSql6, insertSql7);		
		logger.info("added row="+rows.length);

	}

	private void setupCurrencyRateConfig() {
		CurrencyRateConfig configAUD = new CurrencyRateConfig();
		configAUD.setId(1);
		configAUD.setCode("AUD");
		configAUD.setDescription("Australian Dollar");
		configAUD.setBuyTt(0.0120000);
		configAUD.setUnit(1);

		CurrencyRateConfig configBDT = new CurrencyRateConfig();
		configBDT.setId(2);
		configBDT.setCode("BDT");
		configBDT.setDescription("Bangladesh taka");
		configBDT.setBuyTt(0.2410000);
		configBDT.setUnit(1);

		CurrencyRateConfig configBND = new CurrencyRateConfig();
		configBND.setId(3);
		configBND.setCode("BND");
		configBND.setDescription("Bruneian Dollar");
		configBND.setBuyTt(0.0240000);
		configBND.setUnit(10);

		CurrencyRateConfig configCAD = new CurrencyRateConfig();
		configCAD.setId(4);
		configCAD.setCode("CAD");
		configCAD.setDescription("Canadian Dollar");
		configCAD.setBuyTt(0.1240000);
		configCAD.setUnit(10);

		CurrencyRateConfig configCNY = new CurrencyRateConfig();
		configCNY.setId(5);
		configCNY.setCode("CNY");
		configCNY.setDescription("Chinese Yuan Renminbi");
		configCNY.setBuyTt(0.0323000);
		configCNY.setUnit(100);

		CurrencyRateConfig configDKK = new CurrencyRateConfig();
		configDKK.setId(6);
		configDKK.setCode("DKK");
		configDKK.setDescription("Danish krone");
		configDKK.setBuyTt(0.0633000);
		configDKK.setUnit(100);

		CurrencyRateConfig configIDR = new CurrencyRateConfig();
		configIDR.setId(7);
		configIDR.setCode("IDR");
		configIDR.setDescription("Indonesian Rupiah");
		configIDR.setBuyTt(0.0633000);
		configIDR.setUnit(100);

		currencyRateConfigList.add(configAUD);
		currencyRateConfigList.add(configBDT);
		currencyRateConfigList.add(configBND);
		currencyRateConfigList.add(configCAD);
		currencyRateConfigList.add(configCNY);
		currencyRateConfigList.add(configDKK);
		currencyRateConfigList.add(configIDR);

	}

	private void setupBatchUpdateExchangeRate() {
		BatchUpdateExchangeRate batchUpdateExchangeRate1 = new BatchUpdateExchangeRate();
		batchUpdateExchangeRate1.setCode("AUD");
		batchUpdateExchangeRate1.setDescription("Australian Dollar");
		batchUpdateExchangeRate1.setBuyTt(0.012);
		batchUpdateExchangeRate1.setUnit(1);
		batchUpdateExchangeRate1.setId(1L);
		batchUpdateExchangeRate1.setCreatedTime(new Date());
		batchUpdateExchangeRate1.setUpdatedTime(new Date());	

		BatchUpdateExchangeRate batchUpdateExchangeRate2 = new BatchUpdateExchangeRate();
		batchUpdateExchangeRate2.setCode("BDT");
		batchUpdateExchangeRate2.setDescription("Bangladesh taka");
		batchUpdateExchangeRate2.setBuyTt(0.2410000);
		batchUpdateExchangeRate2.setUnit(1);
		batchUpdateExchangeRate2.setId(2L);
		batchUpdateExchangeRate2.setCreatedTime(new Date());
		batchUpdateExchangeRate2.setUpdatedTime(new Date());	

		BatchUpdateExchangeRate batchUpdateExchangeRate3 = new BatchUpdateExchangeRate();
		batchUpdateExchangeRate3.setCode("BND");
		batchUpdateExchangeRate3.setDescription("Bruneian Dollar");
		batchUpdateExchangeRate3.setBuyTt(0.0240000);
		batchUpdateExchangeRate3.setUnit(10);
		batchUpdateExchangeRate3.setId(3L);
		batchUpdateExchangeRate3.setCreatedTime(new Date());
		batchUpdateExchangeRate3.setUpdatedTime(new Date());	

		BatchUpdateExchangeRate batchUpdateExchangeRate4 = new BatchUpdateExchangeRate();
		batchUpdateExchangeRate4.setCode("CAD");
		batchUpdateExchangeRate4.setDescription("Canadian Dollar");
		batchUpdateExchangeRate4.setBuyTt(0.1240000);
		batchUpdateExchangeRate4.setUnit(10);
		batchUpdateExchangeRate4.setId(4L);
		batchUpdateExchangeRate4.setCreatedTime(new Date());
		batchUpdateExchangeRate4.setUpdatedTime(new Date());	

		BatchUpdateExchangeRate batchUpdateExchangeRate5 = new BatchUpdateExchangeRate();
		batchUpdateExchangeRate5.setCode("CNY");
		batchUpdateExchangeRate5.setDescription("Chinese Yuan Renminbi");
		batchUpdateExchangeRate5.setBuyTt(0.0323000);
		batchUpdateExchangeRate5.setUnit(100);
		batchUpdateExchangeRate5.setId(5L);
		batchUpdateExchangeRate5.setCreatedTime(new Date());
		batchUpdateExchangeRate5.setUpdatedTime(new Date());	

		BatchUpdateExchangeRate batchUpdateExchangeRate6 = new BatchUpdateExchangeRate();
		batchUpdateExchangeRate6.setCode("DKK");
		batchUpdateExchangeRate6.setDescription("Danish krone");
		batchUpdateExchangeRate6.setBuyTt(0.0633000);
		batchUpdateExchangeRate6.setUnit(100);
		batchUpdateExchangeRate6.setId(6L);
		batchUpdateExchangeRate6.setCreatedTime(new Date());
		batchUpdateExchangeRate6.setUpdatedTime(new Date());	

		BatchUpdateExchangeRate batchUpdateExchangeRate7 = new BatchUpdateExchangeRate();
		batchUpdateExchangeRate7.setCode("IDR");
		batchUpdateExchangeRate7.setDescription("Indonesian Rupiah");
		batchUpdateExchangeRate7.setBuyTt(0.4240000);
		batchUpdateExchangeRate7.setUnit(100);
		batchUpdateExchangeRate7.setId(7L);
		batchUpdateExchangeRate7.setCreatedTime(new Date());
		batchUpdateExchangeRate7.setUpdatedTime(new Date());	

		batchUpdateExchangeRateList.add(batchUpdateExchangeRate1);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate2);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate3);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate4);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate5);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate6);
		batchUpdateExchangeRateList.add(batchUpdateExchangeRate7);
	}
	
	private void cleanupItemReaderDB() {
		String deleteStagingTable1 = "TRUNCATE TABLE VW_BATCH_CURRENCY_RATE_CONFIG";
		int deletedCurrencyRateConfigRows = jdbcTemplate.update(deleteStagingTable1);
		logger.info("deleted rows from VW_BATCH_CURRENCY_RATE_CONFIG="+deletedCurrencyRateConfigRows);

		String deleteStagingTable2 = "TRUNCATE TABLE TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE";
		int deletedTblBatchStagedUpdatedExchangeRateRows = jdbcTemplate.update(deleteStagingTable2);
		logger.info("deleted rows from TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE="+deletedTblBatchStagedUpdatedExchangeRateRows);

	}
}
