package com.rhbgroup.dcp.bo.batch.job.step;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractAndUpdateExchangeRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateExchangeRate;
import com.rhbgroup.dcp.bo.batch.job.model.CurrencyRateConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUpdateExchangeRateRepositoryImpl;

@Component
@Lazy
public class ExtractCurrencyRateToStagingTableStepBuilder extends BaseStepBuilder {

	static final Logger logger = Logger.getLogger(ExtractCurrencyRateToStagingTableStepBuilder.class);
	private static final String STEP_NAME = "ExtractCurrencyRateToStagingTableStep";
	
	@Autowired
	private ExtractAndUpdateExchangeRateJobConfigProperties jobConfigProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<CurrencyRateConfig> itemReader;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<CurrencyRateConfig, BatchUpdateExchangeRate> itemProcessor;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchUpdateExchangeRate> itemWriter;
	@Autowired
	private BatchUpdateExchangeRateRepositoryImpl batchUpdateExchangeRateRepositoryImpl;

	@Override
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME)
				.<CurrencyRateConfig, BatchUpdateExchangeRate>chunk(jobConfigProperties.getChunkSize())
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
	}

	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	private JdbcPagingItemReader<CurrencyRateConfig> extractCurrencyRateJobReader(
			@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) {
		
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		JdbcPagingItemReader<CurrencyRateConfig> databaseReader = new JdbcPagingItemReader<>();
		databaseReader.setDataSource(dataSource);
		databaseReader.setPageSize(jobConfigProperties.getJdbcPagingPageSize());

		PagingQueryProvider queryProvider = createQueryProvider();
		databaseReader.setQueryProvider(queryProvider);
		databaseReader.setRowMapper(new BeanPropertyRowMapper<>(CurrencyRateConfig.class));
		
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));

		return databaseReader;
	}

	private PagingQueryProvider createQueryProvider() {
		SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
		queryProvider.setSelectClause(
				"SELECT id, code, description, buy_tt, unit, created_time, created_by, updated_time, updated_by");
		queryProvider.setFromClause("FROM VW_BATCH_CURRENCY_RATE_CONFIG"); 
		queryProvider.setSortKeys(sortByIdAsc());
		
        logger.debug("Generated query : " + queryProvider.toString());
        
		return queryProvider;
	}

	private Map<String, Order> sortByIdAsc() {
		Map<String, Order> sortConfiguration = new HashMap<>();
		sortConfiguration.put("id", Order.ASCENDING);
		return sortConfiguration;
	}

	@Bean(STEP_NAME + ".ItemProcessor")
	@StepScope
	private ItemProcessor<CurrencyRateConfig, BatchUpdateExchangeRate> extractCurrencyRateJobProcessor() {
		
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		
		return currencyRateConfig -> {

			BatchUpdateExchangeRate batchUpdateExchangeRate = new BatchUpdateExchangeRate();
			Date currentDate = new Date();
			batchUpdateExchangeRate.setCurrencyRateConfigId(currencyRateConfig.getId());
			batchUpdateExchangeRate.setCode(currencyRateConfig.getCode());
			batchUpdateExchangeRate.setDescription(currencyRateConfig.getDescription());
			batchUpdateExchangeRate.setBuyTt(currencyRateConfig.getBuyTt());
			batchUpdateExchangeRate.setSellTt(currencyRateConfig.getSellTt());
			batchUpdateExchangeRate.setUnit(currencyRateConfig.getUnit());
			batchUpdateExchangeRate.setCreatedTime(currentDate);
			batchUpdateExchangeRate.setUpdatedTime(currentDate);

			logger.info(String.format("[Processing] BatchUpdateExchangeRate: [%s]", batchUpdateExchangeRate.toString()));
			return batchUpdateExchangeRate;
		};
	}

	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	private ItemWriter<BatchUpdateExchangeRate> extractCurrencyRateJobWriter(
			@Value("#{stepExecution}") StepExecution stepExecution) {	
		
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));

		return new ItemWriter<BatchUpdateExchangeRate>() {
			@Override
			public void write(List<? extends BatchUpdateExchangeRate> batchUpdateExchangeRateList) throws Exception {
				logger.info(String.format("Writing total no. of exchange rate : [%d]", batchUpdateExchangeRateList.size()));
				
	            int failedCount=0;
				String errorMessage = "";

	            long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
            	String message = String.format("%s jobExecutionId=%s", this.getClass().getName(), jobExecutionId);
            	logger.info(message);

				for(BatchUpdateExchangeRate batchUpdateExchangeRate : batchUpdateExchangeRateList) {
					try {
						batchUpdateExchangeRate.setJobExecutionId(Long.toString(jobExecutionId));
						batchUpdateExchangeRateRepositoryImpl.addBatchUpdateExchangeRateStaging(batchUpdateExchangeRate);
					} catch (Exception e) {
						++failedCount;
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						errorMessage = String.format("Error happened while writing to DB for Staged Exchange Rate Table [%s], id:%d", batchUpdateExchangeRate, batchUpdateExchangeRate.getId());
						logger.error("Unable to " + e.getLocalizedMessage(), e);
						logger.error(errorMessage, e);

					}
				}
				if(failedCount > 0) {
					stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, errorMessage ));
				}

			}
		};	

	}
}
