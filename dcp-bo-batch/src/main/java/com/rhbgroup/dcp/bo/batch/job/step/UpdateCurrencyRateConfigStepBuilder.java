package com.rhbgroup.dcp.bo.batch.job.step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONException;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BoLoginConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.RestTemplateConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.EAIUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractAndUpdateExchangeRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateExchangeRate;
import com.rhbgroup.dcp.bo.batch.job.model.CurrencyRateConfig;
import com.rhbgroup.dcp.bo.batch.job.model.EAIExchangeRateResponse;
import com.rhbgroup.dcp.bo.batch.job.model.Rate;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUpdateExchangeRateRepositoryImpl;

@Component
@Lazy
public class UpdateCurrencyRateConfigStepBuilder extends BaseStepBuilder {

	static final Logger logger = Logger.getLogger(UpdateCurrencyRateConfigStepBuilder.class);
	
	private static final String STEP_NAME = "UpdateCurrencyRateConfigStep";
	
	private String sessionToken = "";
	
	@Autowired
	private ExtractAndUpdateExchangeRateJobConfigProperties jobConfigProperties;
	
	@Autowired
	private BoLoginConfigProperties boLoginConfigProperties;
	
    @Autowired
    private RestTemplateConfigProperties restTemplateConfigProperties;

    private RestTemplate restTemplate;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	public ItemReader<BatchUpdateExchangeRate> reader;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	public ItemProcessor<BatchUpdateExchangeRate, BatchUpdateExchangeRate> processor;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	public ItemWriter<BatchUpdateExchangeRate> writer;

	@Autowired
	private BatchUpdateExchangeRateRepositoryImpl batchUpdateExchangeRateRepositoryImpl;

	@Override
    @Bean(STEP_NAME)
	public Step buildStep() {
		restTemplate = createRestTemplate();
		return getDefaultStepBuilder(STEP_NAME)
				.<BatchUpdateExchangeRate, BatchUpdateExchangeRate>chunk(jobConfigProperties.getChunkSize())
				.reader(reader)
				.processor(processor)
				.writer(writer)
				.build();
	}

	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public JdbcPagingItemReader<BatchUpdateExchangeRate> extractExchangeRateJobReader(@Value("#{stepExecution}") StepExecution stepExecution, 
			DataSource dataSource) throws JSONException {
		// Get session token
		this.sessionToken = EAIUtils.getSessionToken(boLoginConfigProperties.getUsername(), 
				boLoginConfigProperties.getPassword(), boLoginConfigProperties.getApi(), restTemplate).getSessionToken();
		
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
        long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
		JdbcPagingItemReader<BatchUpdateExchangeRate> databaseReader = new JdbcPagingItemReader<>();
		databaseReader.setDataSource(dataSource);
		databaseReader.setPageSize(jobConfigProperties.getJdbcPagingPageSize());

		PagingQueryProvider queryProvider = createQueryProvider();
		databaseReader.setQueryProvider(queryProvider);
		Map parameters = new HashMap<String, String>();
		parameters.put("jobExecutionId", jobExecutionId);
		databaseReader.setParameterValues( parameters );
		databaseReader.setRowMapper(new BeanPropertyRowMapper<>(BatchUpdateExchangeRate.class));
		
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));

		return databaseReader;
	}

	private PagingQueryProvider createQueryProvider() {
		SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
		queryProvider.setSelectClause(
				"SELECT id, job_execution_id, currency_rate_config_id, code, description, buy_tt,sell_tt, unit, is_processed, created_time, updated_time");
		queryProvider.setFromClause("FROM TBL_BATCH_STAGED_UPDATE_EXCHANGE_RATE");
		queryProvider.setWhereClause("WHERE is_processed=0 " 
				+ " and job_execution_id=:jobExecutionId ");
		queryProvider.setSortKeys(sortByIdAsc());
		
        logger.debug("Generated query : " + queryProvider.toString());

		return queryProvider;
	}

	private Map<String, Order> sortByIdAsc() {
		Map<String, Order> sortConfiguration = new HashMap<>();
		sortConfiguration.put("id", Order.ASCENDING);
		return sortConfiguration;
	}
	
	private RestTemplate createRestTemplate() {
		HttpComponentsClientHttpRequestFactory clientRequestFactory = new HttpComponentsClientHttpRequestFactory();
		// set the timeout
		clientRequestFactory.setConnectTimeout(restTemplateConfigProperties.getConnectTimeout());
		clientRequestFactory.setConnectionRequestTimeout(restTemplateConfigProperties.getConnectionRequestTimeout());
		clientRequestFactory.setReadTimeout(restTemplateConfigProperties.getReadTimeout());
		return new RestTemplate(clientRequestFactory);
	}

	@Bean(STEP_NAME + ".ItemProcessor")
	@StepScope
	public ItemProcessor<BatchUpdateExchangeRate, BatchUpdateExchangeRate> extractCurrencyRateJobProcessor() {
		
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));

		return batchUpdateExchangeRate -> {
			int currentRetries = 0;
			while(currentRetries < jobConfigProperties.getMaxAttempt()) {
				try {
					// Each time we only call EAI with 1 code because if we pass all codes to EAI once, it will treat as failed if either of them found invalid
					EAIExchangeRateResponse eaiExchangeRate = EAIUtils.getEAIExchangeRate(batchUpdateExchangeRate.getCode(), jobConfigProperties.getRestAPI(), sessionToken, restTemplate);
					if(!Objects.isNull(eaiExchangeRate)
						&& !Objects.isNull(eaiExchangeRate.getRate()) && !eaiExchangeRate.getRate().isEmpty()) {
							Rate exchangeRate = eaiExchangeRate.getRate().get(0);
							logger.info(String.format("EAI ExchangeRate: %s ", exchangeRate));
							// Only proceed if both value are valid
							if (!Objects.isNull(exchangeRate.getBuyTT()) && !exchangeRate.getBuyTT().isNaN() && !Objects.isNull(exchangeRate.getUnit())&&!Objects.isNull(exchangeRate.getSellTT())&& !exchangeRate.getSellTT().isNaN()) {
								// Overwrite with the EAI latest values that need to update to staging in subsequent process
								batchUpdateExchangeRate.setBuyTt(exchangeRate.getBuyTT());
								batchUpdateExchangeRate.setSellTt(exchangeRate.getSellTT());
								batchUpdateExchangeRate.setUnit(exchangeRate.getUnit());
								batchUpdateExchangeRate.setProcessed(true);
								break;
							}
					}
				} catch(Exception e) {
					++currentRetries;
					logger.warn(String.format("Exception %s-Unable to retrieve from eai - currency [%s]. retries [%d]", e.getMessage(), batchUpdateExchangeRate.getCode(),currentRetries));
				}

				if(currentRetries == jobConfigProperties.getMaxAttempt()) {
					logger.warn(String.format("Reached max retry attempts to retrieve from eai - currency [%s]. retries [%d]", batchUpdateExchangeRate.getCode(),currentRetries));
				}
			}

			return batchUpdateExchangeRate;
		};
	}

	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	public ItemWriter<BatchUpdateExchangeRate> extractCurrencyRateJobWriter(
			@Value("#{stepExecution}") StepExecution stepExecution) {
		
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));

		return new ItemWriter<BatchUpdateExchangeRate>() {
			@Override
			public void write(List<? extends BatchUpdateExchangeRate> batchUpdateExchangeRateList) throws Exception {
				logger.info(String.format("Update total no. of updates for exchange rate : [%d]", batchUpdateExchangeRateList.size()));
				
	            int failedCount=0;
				String errorMessage = "";

	            long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
            	String message = String.format("%s jobExecutionId=%s", this.getClass().getName(), jobExecutionId);
            	logger.info(message);

				for(BatchUpdateExchangeRate batchUpdateExchangeRate : batchUpdateExchangeRateList) {
					try {
						// Only update DCP if we had successfully get the new exchange rate from EAI previously
						if(batchUpdateExchangeRate.isProcessed()) {
							batchUpdateExchangeRateRepositoryImpl.updateDCPCurrencyRateConfig(convertToCurrencyRateBO(batchUpdateExchangeRate));
						}
						// No matter new exchange rate found or not, we will update the staging record as processed at last
						batchUpdateExchangeRate.setProcessed(true);
						batchUpdateExchangeRateRepositoryImpl.updateStagingBatchUpdateExchangeRate(batchUpdateExchangeRate);
					} catch (Exception e) {
						++failedCount;
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						errorMessage = String.format("Error happened while updating to DB for Staged Exchange Rate Table [%s], id:%d", batchUpdateExchangeRate, batchUpdateExchangeRate.getId());
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
	
	private CurrencyRateConfig convertToCurrencyRateBO(BatchUpdateExchangeRate batchUpdateExchangeRate) {
		CurrencyRateConfig currencyRateConfig = new CurrencyRateConfig();
		currencyRateConfig.setId(batchUpdateExchangeRate.getCurrencyRateConfigId());
		currencyRateConfig.setBuyTt(batchUpdateExchangeRate.getBuyTt());
		currencyRateConfig.setSellTt(batchUpdateExchangeRate.getSellTt());
		currencyRateConfig.setUnit(batchUpdateExchangeRate.getUnit());
		currencyRateConfig.setCode(batchUpdateExchangeRate.getCode());
		
		logger.info(String.format("Processing CurrencyRateConfig: %s", currencyRateConfig.toString()));
		return currencyRateConfig;
	}
}
