package com.rhbgroup.dcp.bo.batch.test.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BoLoginConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.utils.EAIUtils;
import com.rhbgroup.dcp.bo.batch.job.config.ExtractAndUpdateExchangeRateJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractAndUpdateExchangeRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.EAIExchangeRateResponse;
import com.rhbgroup.dcp.bo.batch.job.model.EAISessionTokenResponse;
import com.rhbgroup.dcp.bo.batch.job.model.Rate;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(EAIUtils.class)
@PowerMockIgnore(value = { "javax.net.ssl.*" })
@SpringBootTest(classes = { BatchTestConfigHSQL.class, ExtractAndUpdateExchangeRateJobConfiguration.class })
@ActiveProfiles("test")
public class ExtractAndUpdateExchangeRateJobTest extends BaseJobTest {

	private static final Logger logger = Logger.getLogger(ExtractAndUpdateExchangeRateJobTest.class);

	public static final String JOB_NAME = "ExtractAndUpdateExchangeRateJob";
	public static final String JOB_LAUNCHER_UTILS = "ExtractAndUpdateExchangeRateJobLauncherTestUtils";

	@Autowired
	private ExtractAndUpdateExchangeRateJobConfigProperties jobConfigProperties;
	
	@Autowired
	private BoLoginConfigProperties boLoginConfigProperties;
	
	@Lazy
	@Autowired
	@Qualifier(JOB_LAUNCHER_UTILS)
	private JobLauncherTestUtils jobLauncherTestUtils;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@Before
	public void setUp() throws Exception {
		insertDBData();
	}

	@After
	public void cleanUp() {
		cleanupItemReaderDB();
	}

	@Test
	public void testJob() throws Exception {
		Map<String, EAIExchangeRateResponse> eaiExhangeRateMap = setupEAIExchangeRateResult();
		
		PowerMockito.mockStatic(EAIUtils.class);
		
		EAISessionTokenResponse mockEAISessionTokenResponse = new EAISessionTokenResponse();
		mockEAISessionTokenResponse.setSessionToken("xxxxx");
		PowerMockito.when(EAIUtils.getSessionToken(Mockito.eq(boLoginConfigProperties.getUsername()), Mockito.eq(boLoginConfigProperties.getPassword()), Mockito.eq(boLoginConfigProperties.getApi()), Mockito.any())).thenReturn(mockEAISessionTokenResponse);
		
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("AUD"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("AUD"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("BDT"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("BDT"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("BND"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("BND"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("CAD"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("CAD"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("CNY"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("CNY"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("DKK"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("DKK"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("IDR"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("IDR"));
		
		JobParameters jobParameters = new JobParametersBuilder()
										.addDate("now", new Date())
										.addString("jobname", JOB_NAME)
										.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobEAIException() throws Exception {
		Map<String, EAIExchangeRateResponse> eaiExhangeRateMap = setupEAIExchangeRateResult();

		PowerMockito.mockStatic(EAIUtils.class);
		
		EAISessionTokenResponse mockEAISessionTokenResponse = new EAISessionTokenResponse();
		mockEAISessionTokenResponse.setSessionToken("xxxxx");
		PowerMockito.when(EAIUtils.getSessionToken(Mockito.eq(boLoginConfigProperties.getUsername()), Mockito.eq(boLoginConfigProperties.getPassword()), Mockito.eq(boLoginConfigProperties.getApi()), Mockito.any())).thenReturn(mockEAISessionTokenResponse);

		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("AUD"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenThrow(RestClientException.class);
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("BDT"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("BDT"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("BND"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("BND"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("CAD"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("CAD"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("CNY"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("CNY"));
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("DKK"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenThrow(RestClientException.class);
		PowerMockito.when(EAIUtils.getEAIExchangeRate(Mockito.eq("IDR"), Mockito.eq(jobConfigProperties.getRestAPI()), Mockito.eq("xxxxx"), Mockito.any())).thenReturn(eaiExhangeRateMap.get("IDR"));

		JobParameters jobParameters = new JobParametersBuilder()
										.addDate("now", new Date())
										.addString("jobname", JOB_NAME)
										.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}


	private void insertDBData() {
		String insertSql1 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('AUD', 'Australian Dollar', 0.0120000, 0.0120000, 1, '2018-09-03 07:36:19.207', 'admin', '2018-09-03 07:36:19.207', 'admin')";
		String insertSql2 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('BDT', 'Bangladesh taka', 0.2410000, 0.2410000, 1, '2018-09-03 07:36:19.210', 'admin', '2018-09-06 08:16:06.080', 'admin')";
		String insertSql3 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('BND', 'Bruneian Dollar', 0.0240000, 0.0240000, 10, '2018-09-03 07:36:19.210', 'admin', '2018-09-06 08:16:06.080', 'admin')";
		String insertSql4 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('CAD', 'Canadian Dollar', 0.1240000, 0.1240000, 10, '2018-09-03 07:36:19.210', 'admin', '2018-09-03 07:36:19.210', 'admin')";
		String insertSql5 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('CNY', 'Chinese Yuan Renminbi', 0.0323000, 0.0323000, 100, '2018-09-03 07:36:19.210', 'admin', '2018-09-06 08:16:06.080', 'admin')";
		String insertSql6 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('DKK', 'Danish krone', 0.0633000, 0.0633000, 100, '2018-09-03 07:36:19.210', 'admin', '2018-09-06 08:16:06.083', 'admin')";
		String insertSql7 = "INSERT INTO VW_BATCH_CURRENCY_RATE_CONFIG (code, description, buy_tt, sell_tt, unit, created_time, created_by, updated_time, updated_by) VALUES ('IDR', 'Indonesian Rupiah', 0.4240000, 0.4240000, 100, '2018-09-03 07:36:19.217', 'admin', '2018-09-06 08:16:06.083', 'admin')";
		int[] rows = jdbcTemplate.batchUpdate(insertSql1, insertSql2, insertSql3, insertSql4, insertSql5, insertSql6, insertSql7);
		logger.info("added row into VW_BATCH_CURRENCY_RATE_CONFIG="+rows.length);
	}

	private void cleanupItemReaderDB() {
		String deleteStagingTable1 = "DELETE FROM VW_BATCH_CURRENCY_RATE_CONFIG";
		int deletedCurrencyRateConfigRows = jdbcTemplate.update(deleteStagingTable1);
		logger.info("deleted rows from VW_BATCH_CURRENCY_RATE_CONFIG="+deletedCurrencyRateConfigRows);
		
		String deleteStagingTable2 = "DELETE FROM TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE";
		int deletedStagingTableRows = jdbcTemplate.update(deleteStagingTable2);
		logger.info("deleted rows from TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE="+deletedStagingTableRows);

	}
	
	private Map<String, EAIExchangeRateResponse> setupEAIExchangeRateResult() {
		Map<String, EAIExchangeRateResponse> eaiExhangeRateMap = new HashMap<>();
		Rate rate = new Rate();
		List<Rate> listRate = new ArrayList<>();

		rate.setBuyTT(3.00);
		rate.setSellTT(3.00);
		rate.setUnit(1);
		rate.setCode("AUD");
		listRate.clear();
		listRate.add(rate);
		EAIExchangeRateResponse eaiExchangeRateAUD = new EAIExchangeRateResponse();
		eaiExchangeRateAUD.setRate(listRate);

		rate.setBuyTT(1.00);
		rate.setSellTT(1.00);
		rate.setUnit(1);
		rate.setCode("BDT");
		listRate.clear();
		listRate.add(rate);
		EAIExchangeRateResponse eaiExchangeRateBDT = new EAIExchangeRateResponse();
		eaiExchangeRateBDT.setRate(listRate);

		rate.setBuyTT(0.3454);
		rate.setSellTT(0.3454);
		rate.setUnit(1);
		rate.setCode("BND");
		listRate.clear();
		listRate.add(rate);
		EAIExchangeRateResponse eaiExchangeRateBND = new EAIExchangeRateResponse();
		eaiExchangeRateBND.setRate(listRate);

		rate.setBuyTT(0.00245);
		rate.setSellTT(0.00245);
		rate.setUnit(10);
		rate.setCode("CAD");
		listRate.clear();
		listRate.add(rate);
		EAIExchangeRateResponse eaiExchangeRateCAD = new EAIExchangeRateResponse();
		eaiExchangeRateCAD.setRate(listRate);

		rate.setBuyTT(0.9855);
		rate.setSellTT(0.9855);
		rate.setUnit(100);
		rate.setCode("CNY");
		listRate.clear();
		listRate.add(rate);
		EAIExchangeRateResponse eaiExchangeRateCNY= new EAIExchangeRateResponse();
		eaiExchangeRateCNY.setRate(listRate);

		rate.setBuyTT(0.0016534);
		rate.setSellTT(0.0016534);
		rate.setUnit(100);
		rate.setCode("DKK");
		listRate.clear();
		listRate.add(rate);
		EAIExchangeRateResponse eaiExchangeRateDKK = new EAIExchangeRateResponse();
		eaiExchangeRateDKK.setRate(listRate);

		rate.setBuyTT(0.55763);
		rate.setSellTT(0.55763);
		rate.setUnit(100);
		rate.setCode("IDR");
		listRate.clear();
		listRate.add(rate);
		EAIExchangeRateResponse eaiExchangeRateIDR = new EAIExchangeRateResponse();
		eaiExchangeRateIDR.setRate(listRate);

		eaiExhangeRateMap.put("AUD", eaiExchangeRateAUD);
		eaiExhangeRateMap.put("BDT", eaiExchangeRateBDT);
		eaiExhangeRateMap.put("BND", eaiExchangeRateBND);
		eaiExhangeRateMap.put("CAD", eaiExchangeRateCAD);
		eaiExhangeRateMap.put("CNY", eaiExchangeRateCNY);
		eaiExhangeRateMap.put("DKK", eaiExchangeRateDKK);
		eaiExhangeRateMap.put("IDR", eaiExchangeRateIDR);

		return eaiExhangeRateMap;
	}
}
