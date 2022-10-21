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
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractandUpdateMcaInterestRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateMcaInterestRate;
import com.rhbgroup.dcp.bo.batch.job.model.EAIInterestRateResponse;
import com.rhbgroup.dcp.bo.batch.job.model.InterestRate;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUpdateMcaInterestRateRepositoryImpl;

@Component
@Lazy
public class UpdateMcaCurrencyInterestRateConfigStepBuilder extends BaseStepBuilder{


	static final Logger logger = Logger.getLogger(UpdateMcaCurrencyInterestRateConfigStepBuilder.class);
	
	private static final String STEP_NAME = "UpdateMcaCurrencyInterestRateConfigStep";
	
	private String sessionToken = "";
	
	@Autowired
	@Qualifier("ExtractandUpdateMcaInterestRateJobConfigProperties")
	private ExtractandUpdateMcaInterestRateJobConfigProperties jobConfigProperties;
	
	@Autowired
	private BoLoginConfigProperties boLoginConfigProperties;
	
    @Autowired
    private RestTemplateConfigProperties restTemplateConfigProperties;

    private RestTemplate restTemplate;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	public ItemReader<BatchUpdateMcaInterestRate> reader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	public ItemProcessor<BatchUpdateMcaInterestRate, BatchUpdateMcaInterestRate> processor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	public ItemWriter<BatchUpdateMcaInterestRate> writer;

	@Autowired
	private BatchUpdateMcaInterestRateRepositoryImpl batchUpdateMcaInterestRateRepositoryImpl;

	@Override
    @Bean(STEP_NAME)
	public Step buildStep() {
		restTemplate = createRestTemplate();
		return getDefaultStepBuilder(STEP_NAME)
				.<BatchUpdateMcaInterestRate, BatchUpdateMcaInterestRate>chunk(jobConfigProperties.getChunkSize())
				.reader(reader)
				.processor(processor)
				.writer(writer)
				.build();
	}

	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public JdbcPagingItemReader<BatchUpdateMcaInterestRate> extractExchangeRateJobReader(@Value("#{stepExecution}") StepExecution stepExecution, 
			DataSource dataSource) throws JSONException {
		// Get session token
		this.sessionToken = EAIUtils.getSessionToken(boLoginConfigProperties.getUsername(), 
				boLoginConfigProperties.getPassword(), boLoginConfigProperties.getApi(), restTemplate).getSessionToken();
		
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
        long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
		JdbcPagingItemReader<BatchUpdateMcaInterestRate> databaseReader = new JdbcPagingItemReader<>();
		databaseReader.setDataSource(dataSource);
		databaseReader.setPageSize(jobConfigProperties.getJdbcPagingPageSize());

		PagingQueryProvider queryProvider = createQueryProvider();
		databaseReader.setQueryProvider(queryProvider);
		Map parameters = new HashMap<String, String>();
		parameters.put("jobExecutionId", jobExecutionId);
		databaseReader.setParameterValues( parameters );
		databaseReader.setRowMapper(new BeanPropertyRowMapper<>(BatchUpdateMcaInterestRate.class));
		
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));

		return databaseReader;
	}

	private PagingQueryProvider createQueryProvider() {
		SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
		queryProvider.setSelectClause(
				"SELECT ID, JOB_EXECUTION_ID, MCA_TERM_INTEREST_RATE_ID, CODE, TENURE, INTEREST_RATE, IS_PROCESSED, CREATED_TIME, UPDATED_TIME");
		queryProvider.setFromClause("FROM TBL_BATCH_UPDATE_MCA_INTEREST_RATE");
		queryProvider.setWhereClause("WHERE IS_PROCESSED=0 " 
				+ " and JOB_EXECUTION_ID=:jobExecutionId ");
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
	public ItemProcessor<BatchUpdateMcaInterestRate, BatchUpdateMcaInterestRate> extractCurrencyRateJobProcessor() {
		
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));

		return batchUpdateMcaInterestRate -> {
			int currentRetries = 0;
			logger.info(String.format("RestAPI for eai [%s] ", jobConfigProperties.getRestAPI()));
			while(currentRetries < jobConfigProperties.getMaxAttempt()) {
				try {
					// Each time we only call EAI with 1 code because if we pass all codes to EAI once, it will treat as failed if either of them found invalid
					EAIInterestRateResponse eaiInterestRate = EAIUtils.getEAIInterestRate(batchUpdateMcaInterestRate, jobConfigProperties.getRestAPI(), sessionToken, restTemplate);
					logger.info(String.format("RestAPI for eai eaiInterestRate [%s] ", eaiInterestRate ));

					if(!Objects.isNull(eaiInterestRate) ){
							if( !Objects.isNull(eaiInterestRate.getRate()) && !eaiInterestRate.getRate().isEmpty()){
								InterestRate interestRate = eaiInterestRate.getRate().get(0);
								logger.info(String.format("EAI McaInterest Rate: %s ", eaiInterestRate));
								// Only proceed if both value are valid

								batchUpdateMcaInterestRate = setInterestRate(interestRate,batchUpdateMcaInterestRate);
							}
							//	If response comes from API break the loop
							break;
					}
				} catch(Exception e) {
					++currentRetries;
					logger.warn(String.format("Exception %s-Unable to retrieve from eai - currency [%s]. retries [%d]", e.getMessage(), batchUpdateMcaInterestRate.getCode(),currentRetries));
					logger.error(String.format("Exception -Unable to retrieve from eai [%s] ", e));

				}

				retryMessage(currentRetries,batchUpdateMcaInterestRate);

			}

			return batchUpdateMcaInterestRate;
		};
	}
	
	private BatchUpdateMcaInterestRate setInterestRate(InterestRate interestRate,BatchUpdateMcaInterestRate batchUpdateMcaInterestRate) {
		if (!Objects.isNull(interestRate.getInterestRateOnMca())) {
			// Overwrite with the EAI latest values that need to update to staging in subsequent process
			batchUpdateMcaInterestRate.setInterestRate(interestRate.getInterestRateOnMca());
			batchUpdateMcaInterestRate.setProcessed(true);
		}
		return batchUpdateMcaInterestRate;
	}
	
	private void retryMessage(int currentRetries,BatchUpdateMcaInterestRate batchUpdateMcaInterestRate) {
		if(currentRetries == jobConfigProperties.getMaxAttempt()) {
			logger.warn(String.format("Reached max retry attempts to retrieve from eai - currency [%s]. retries [%d]", batchUpdateMcaInterestRate.getCode(),currentRetries));
		}
	}

	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	public ItemWriter<BatchUpdateMcaInterestRate> extractCurrencyRateJobWriter(
			@Value("#{stepExecution}") StepExecution stepExecution) {
		
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));

		return new ItemWriter<BatchUpdateMcaInterestRate>() {
			@Override
			public void write(List<? extends BatchUpdateMcaInterestRate> batchUpdateMcaInterestRateList) throws Exception {
				logger.info(String.format("Update total no. of updates for Mca interest rate  : [%d]", batchUpdateMcaInterestRateList.size()));
				
	            int failedCount=0;
				String errorMessage = "";

	            long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
            	String message = String.format("%s jobExecutionId=%s", this.getClass().getName(), jobExecutionId);
            	logger.info(message);

				for(BatchUpdateMcaInterestRate batchUpdateMcaInterestRate : batchUpdateMcaInterestRateList) {
					try {
						// Only update DCP if we had successfully get the new mca interest rate from EAI previously
						if(batchUpdateMcaInterestRate.isProcessed()) {
							batchUpdateMcaInterestRateRepositoryImpl.updateBatchUpdateMcaInterestRate(batchUpdateMcaInterestRate);
						}
						// No matter McaInterestRate found or not, we will update the record as processed at last
						batchUpdateMcaInterestRate.setProcessed(true);
						batchUpdateMcaInterestRateRepositoryImpl.updateMcaTermInterestRateConfig(batchUpdateMcaInterestRate);
					} catch (Exception e) {
						++failedCount;
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						errorMessage = String.format("Error happened while updating to DB for batch update mca interest rate Table [%s], id:%d", batchUpdateMcaInterestRate, batchUpdateMcaInterestRate.getId());
						logger.error("Unable to " + e.getLocalizedMessage(), e);
						logger.error(errorMessage, e);
					}
				}
				if(failedCount > 3) {
					
					stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, errorMessage ));
				}
				batchUpdateMcaInterestRateRepositoryImpl.updateCurrencyRateConfigData();
				
			}
		};
	}
	

}
