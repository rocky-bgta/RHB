package com.rhbgroup.dcp.bo.batch.test.repository;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import freemarker.template.Configuration;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.Before;

import static org.mockito.Mockito.when;

import java.util.Date;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateExchangeRate;
import com.rhbgroup.dcp.bo.batch.job.model.CurrencyRateConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUpdateExchangeRateRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class,BatchUpdateExchangeRateRepositoryImpl.class})
@ActiveProfiles("test")
public class BatchUpdateExchangeRateRepositoryImplTest extends BaseJobTest {
	
	@Autowired
	private BatchUpdateExchangeRateRepositoryImpl batchUpdateExchangeRateImpl;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private DataSource dataSourceDCP;
	
	private JdbcTemplate testJdbcTemplate;
	
	@MockBean(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@Test
	public void testAddBatchUpdateExchangeRateStaging() throws BatchException {
		BatchUpdateExchangeRate batchUpdateExchangeRate = createBatchUpdateExchangeRate();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (");
		stringBuilder.append("job_execution_id, currency_rate_config_id, code, description, buy_tt,sell_tt,");
		stringBuilder.append("unit, is_processed, created_time, updated_time) ");
		stringBuilder.append("VALUES (?,?,?,?,?,?,?,?,?,?)");
		String sql = stringBuilder.toString();
		when(jdbcTemplate.update(sql, new Object[] {batchUpdateExchangeRate.getJobExecutionId(),
				batchUpdateExchangeRate.getCurrencyRateConfigId(),
				batchUpdateExchangeRate.getCode(),
				batchUpdateExchangeRate.getDescription(),
				batchUpdateExchangeRate.getBuyTt(),
				batchUpdateExchangeRate.getSellTt(),
				batchUpdateExchangeRate.getUnit(),
				batchUpdateExchangeRate.isProcessed(),
				batchUpdateExchangeRate.getCreatedTime(),
				batchUpdateExchangeRate.getUpdatedTime()})).thenReturn(1);
		Assert.assertEquals(0, batchUpdateExchangeRateImpl.addBatchUpdateExchangeRateStaging(batchUpdateExchangeRate).intValue());
	}
	
	@Test
	public void testNegativeAddBatchUpdateExchangeRateStaging() throws BatchException {
		BatchUpdateExchangeRate batchUpdateExchangeRate = createBatchUpdateExchangeRate();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (");
		stringBuilder.append("job_execution_id, currency_rate_config_id, code, description, buy_tt,sell_tt,");
		stringBuilder.append("unit, is_processed, created_time, updated_time) ");
		stringBuilder.append("VALUES (?,?,?,?,?,?,?,?,?,?)");
		String sql = stringBuilder.toString();
		when(jdbcTemplate.update(sql, new Object[] {batchUpdateExchangeRate.getJobExecutionId(),
				batchUpdateExchangeRate.getCurrencyRateConfigId(),
				batchUpdateExchangeRate.getCode(),
				batchUpdateExchangeRate.getDescription(),
				batchUpdateExchangeRate.getBuyTt(),
				batchUpdateExchangeRate.getSellTt(),
				batchUpdateExchangeRate.getUnit(),
				batchUpdateExchangeRate.isProcessed(),
				batchUpdateExchangeRate.getCreatedTime(),
				batchUpdateExchangeRate.getUpdatedTime()})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0, batchUpdateExchangeRateImpl.addBatchUpdateExchangeRateStaging(batchUpdateExchangeRate).intValue());
	}
	
	@Test
	public void testUpdateStagingBatchUpdateExchangeRate() throws BatchException {
		BatchUpdateExchangeRate batchUpdateExchangeRate = createBatchUpdateExchangeRate();
		when(jdbcTemplate.update(Mockito.anyString(), new Object[] {
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				(Date) Mockito.any(),
				Mockito.any(),
				Mockito.any()})).thenReturn(1);
		Assert.assertEquals(1,batchUpdateExchangeRateImpl.updateStagingBatchUpdateExchangeRate(batchUpdateExchangeRate).intValue());
//				Mockito.anyLong(),
//				Mockito.anyString() })).thenReturn(1);
		Assert.assertEquals(0,batchUpdateExchangeRateImpl.updateStagingBatchUpdateExchangeRate(batchUpdateExchangeRate).intValue());
	}
	
	@Test
	public void testNegativeUpdateStagingBatchUpdateExchangeRate() throws BatchException {
		BatchUpdateExchangeRate batchUpdateExchangeRate = createBatchUpdateExchangeRate();
		batchUpdateExchangeRate.setId(1);
		when(jdbcTemplate.update(Mockito.anyString(), new Object[] {
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				(Date) Mockito.any(),
				Mockito.any(),
				Mockito.any()})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0,batchUpdateExchangeRateImpl.updateStagingBatchUpdateExchangeRate(batchUpdateExchangeRate).intValue());
	}
	
	@Test
	public void testUpdateDCPCurrencyRateConfig() throws BatchException {
		CurrencyRateConfig currencyRateConfig = createCurrencyRate();
		currencyRateConfig.setId(1);
		when(jdbcTemplate.update(Mockito.anyString(), new Object[] {
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				(Date) Mockito.any(),
				Mockito.any(),
				Mockito.any()
		})).thenReturn(1);
		Assert.assertEquals(1,batchUpdateExchangeRateImpl.updateDCPCurrencyRateConfig(currencyRateConfig).intValue());
	}


	@Test
	public void testNegativeUpdateDCPCurrencyRateConfig() throws BatchException {
		CurrencyRateConfig currencyRateConfig = createCurrencyRate();
		currencyRateConfig.setId(1);
		when(jdbcTemplate.update(Mockito.anyString(), new Object[] {
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				(Date) Mockito.any(),
				Mockito.any(),
				Mockito.any()
		})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0,batchUpdateExchangeRateImpl.updateDCPCurrencyRateConfig(currencyRateConfig).intValue());

	}
	
	@Before
	public void startup() {
	}
	
	@After
	public void cleanup() {
	}
	
	private void addDCPCurrencyRate( CurrencyRateConfig currencyRateConfig) {
		testJdbcTemplate.setDataSource(dataSourceDCP);
		String sqlInsert="INSERT INTO TBL_CURRENCY_RATE_CONFIG " +
				"(code,description,buy_tt,unit,created_time,created_by,updated_time,updated_by)" +
				" VALUES " +
				"(?,?,?,?,?,?,?,?)";
		testJdbcTemplate.update(sqlInsert, new Object[]{currencyRateConfig.getCode(),
				currencyRateConfig.getDescription(),
				currencyRateConfig.getBuyTt(),
				currencyRateConfig.getUnit(),
				currencyRateConfig.getCreatedTime(),
				currencyRateConfig.getCreatedBy(),
				currencyRateConfig.getUpdatedTime(),
				currencyRateConfig.getUpdatedBy()});
	}
	
	private CurrencyRateConfig createCurrencyRate() {
		CurrencyRateConfig currencyRateConfig = new CurrencyRateConfig();
		currencyRateConfig.setCode("AUD");
		currencyRateConfig.setDescription ("Australian Dollar");
		currencyRateConfig.setBuyTt(0.0120000);
		currencyRateConfig.setUnit(1);
		currencyRateConfig.setCreatedTime(new Date());
		currencyRateConfig.setCreatedBy("admin");
		currencyRateConfig.setUpdatedTime(new Date());
		currencyRateConfig.setUpdatedBy("admin");
		return currencyRateConfig;
	}
	
	private BatchUpdateExchangeRate createBatchUpdateExchangeRate() {
		BatchUpdateExchangeRate batchUpdateExchangeRate = new BatchUpdateExchangeRate();
		batchUpdateExchangeRate.setJobExecutionId("1");
		batchUpdateExchangeRate.setCurrencyRateConfigId(1);
		batchUpdateExchangeRate.setCode("AUD");
		batchUpdateExchangeRate.setDescription ("Australian Dollar");
		batchUpdateExchangeRate.setBuyTt(0.0120000);
		batchUpdateExchangeRate.setUnit(1);
		batchUpdateExchangeRate.setProcessed(false);
		batchUpdateExchangeRate.setCreatedTime(new Date());
		batchUpdateExchangeRate.setUpdatedTime(new Date());
		batchUpdateExchangeRate.setSellTt(0.2200000);
		return batchUpdateExchangeRate;
		
	}
}
