package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Arrays;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractAndUpdateExchangeRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateExchangeRate;
import com.rhbgroup.dcp.bo.batch.job.model.CurrencyRateConfig;

@Component
@Lazy
public class BatchUpdateExchangeRateRepositoryImpl extends BaseRepositoryImpl {

	static final Logger logger = Logger.getLogger(BatchUpdateExchangeRateRepositoryImpl.class);
	
	@Autowired
	private ExtractAndUpdateExchangeRateJobConfigProperties configProperties;
	
	@Qualifier("dataSourceDCP")
	@Autowired
	DataSource dataSourceDCP;
	
	public Integer addBatchUpdateExchangeRateStaging(BatchUpdateExchangeRate batchUpdateExchangeRate) throws BatchException {
		try {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("INSERT INTO TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE (");
			stringBuilder.append("job_execution_id, currency_rate_config_id, code, description, buy_tt,sell_tt,");
			stringBuilder.append("unit, is_processed, created_time, updated_time) ");
			stringBuilder.append("VALUES (?,?,?,?,?,?,?,?,?,?)");

			Object[] parameter = new Object[] { 
					batchUpdateExchangeRate.getJobExecutionId(),
					batchUpdateExchangeRate.getCurrencyRateConfigId(), 
					batchUpdateExchangeRate.getCode(),
					batchUpdateExchangeRate.getDescription(), 
					batchUpdateExchangeRate.getBuyTt(),
					batchUpdateExchangeRate.getSellTt(),
					batchUpdateExchangeRate.getUnit(), 
					batchUpdateExchangeRate.isProcessed(),
					batchUpdateExchangeRate.getCreatedTime(), 
					batchUpdateExchangeRate.getUpdatedTime()};

			return jdbcTemplate.update(stringBuilder.toString(), parameter);
		} catch (Exception e) {
			logger.error("Exception", e);
			String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE values [%s] ", batchUpdateExchangeRate);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}

	public Integer updateStagingBatchUpdateExchangeRate(BatchUpdateExchangeRate batchUpdateExchangeRate) throws BatchException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("UPDATE TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE SET ");
		stringBuilder.append("buy_tt=?,");
		stringBuilder.append("sell_tt=?,");
		stringBuilder.append("unit=?,");
		stringBuilder.append("is_processed=?,");
		stringBuilder.append("updated_time=? ");
		stringBuilder.append("WHERE id=? ");
		stringBuilder.append("AND code=?");

		try {
			String currentDate = DateUtils.formatDateString(new Date(), "yyyy-MM-dd HH:mm:ss");
			
			Object[] parameter = new Object[] { 
					batchUpdateExchangeRate.getBuyTt(), 
					batchUpdateExchangeRate.getSellTt(),
					batchUpdateExchangeRate.getUnit(),
					batchUpdateExchangeRate.isProcessed(),
					currentDate,
					batchUpdateExchangeRate.getId(), 
					batchUpdateExchangeRate.getCode() 
			};
			
			logger.info(String.format("Updating TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE: %s", Arrays.toString(parameter)));

			return jdbcTemplate.update(stringBuilder.toString(), parameter);

		} catch(Exception e) {
			String errorMessage = String.format("Error happened while updating record to TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE values [%s] ", batchUpdateExchangeRate);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}

	public Integer updateDCPCurrencyRateConfig(CurrencyRateConfig currencyRateConfig) throws BatchException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("UPDATE TBL_CURRENCY_RATE_CONFIG SET ");
		stringBuilder.append("buy_tt=?,");
		stringBuilder.append("sell_tt=?,");
		stringBuilder.append("unit=?,");
		stringBuilder.append("updated_time=?,");
		stringBuilder.append("updated_by=? ");
		stringBuilder.append("WHERE id=? ");
		stringBuilder.append("AND code=?");

		try {
			String currentDate = DateUtils.formatDateString(new Date(), "yyyy-MM-dd HH:mm:ss");
			
			Object[] parameter = new Object[] {
					currencyRateConfig.getBuyTt(),
					currencyRateConfig.getSellTt(),
					currencyRateConfig.getUnit(),
					currentDate,
					configProperties.getBatchCode(),
					currencyRateConfig.getId(),
					currencyRateConfig.getCode()
			};
			logger.info(String.format("Updating TBL_CURRENCY_RATE_CONFIG: %s", Arrays.toString(parameter)));
			
			jdbcTemplate.setDataSource(dataSourceDCP);
			return jdbcTemplate.update(stringBuilder.toString(), parameter);
			
		} catch(Exception e) {
			String errorMessage = String.format("Error happened while updating record to TBL_CURRENCY_RATE_CONFIG values [%s] ", currencyRateConfig);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		} finally {
			jdbcTemplate.setDataSource(dataSource);
		}

	}
}
