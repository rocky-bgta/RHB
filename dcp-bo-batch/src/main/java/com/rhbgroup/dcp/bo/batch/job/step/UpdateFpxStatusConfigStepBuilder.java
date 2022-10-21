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
import com.rhbgroup.dcp.bo.batch.framework.utils.FPXUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractandUpdateFpxTransactionStatusJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateFPXStatus;
import com.rhbgroup.dcp.bo.batch.job.model.EAIFpxStatusResponse;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUpdateFpxStatusRepositoryImpl;

@Component
@Lazy
public class UpdateFpxStatusConfigStepBuilder extends BaseStepBuilder{


	static final Logger logger = Logger.getLogger(UpdateFpxStatusConfigStepBuilder.class);
	
	private static final String STEP_NAME = "UpdateFpxStatusConfigStep";
	
	private String sessionToken = "";
	
	@Autowired
	private ExtractandUpdateFpxTransactionStatusJobConfigProperties jobConfigProperties;
	
	@Autowired
	private BoLoginConfigProperties boLoginConfigProperties;
	
    @Autowired
    private RestTemplateConfigProperties restTemplateConfigProperties;

    private RestTemplate restTemplate;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	public ItemReader<BatchUpdateFPXStatus> reader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	public ItemProcessor<BatchUpdateFPXStatus, BatchUpdateFPXStatus> processor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	public ItemWriter<BatchUpdateFPXStatus> writer;

	@Autowired
	private BatchUpdateFpxStatusRepositoryImpl batchUpdateFpxStatusRepositoryImpl;

	@Override
    @Bean(STEP_NAME)
	public Step buildStep() {
		restTemplate = createRestTemplate();
		return getDefaultStepBuilder(STEP_NAME)
				.<BatchUpdateFPXStatus, BatchUpdateFPXStatus>chunk(jobConfigProperties.getChunkSize())
				.reader(reader)
				.processor(processor)
				.writer(writer)
				.build();
	}

	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public JdbcPagingItemReader<BatchUpdateFPXStatus> extractFpxStatusJobReader(@Value("#{stepExecution}") StepExecution stepExecution, 
			DataSource dataSource) throws JSONException {
		// Get session token
		this.sessionToken = EAIUtils.getSessionToken(boLoginConfigProperties.getUsername(), 
				boLoginConfigProperties.getPassword(), boLoginConfigProperties.getApi(), restTemplate).getSessionToken();
		
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
        long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
		JdbcPagingItemReader<BatchUpdateFPXStatus> databaseReader = new JdbcPagingItemReader<>();
		databaseReader.setDataSource(dataSource);
		databaseReader.setPageSize(jobConfigProperties.getJdbcPagingPageSize());

		PagingQueryProvider queryProvider = createQueryProvider();
		databaseReader.setQueryProvider(queryProvider);
		Map<String,Object> parameters = new HashMap<>();
		String jobId = Long.toString(jobExecutionId);
		parameters.put("jobExecutionId", jobId);
		databaseReader.setParameterValues( parameters );
		databaseReader.setRowMapper(new BeanPropertyRowMapper<>(BatchUpdateFPXStatus.class));
		
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));

		return databaseReader;
	}

	private PagingQueryProvider createQueryProvider() {
		SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
		queryProvider.setSelectClause(
				"select id,txn_token_id,txn_status");
		queryProvider.setFromClause("from dcpbo.dbo.tbl_batch_staged_update_fpx_status");
		queryProvider.setWhereClause("where ((main_function='TERM_DEPOSIT' and sub_function='PLACEMENT') OR (main_function='CASA'))"
				+ "AND job_execution_id=:jobExecutionId ");
		queryProvider.setSortKeys(sortByIdAsc());
		
        logger.info("Generated query : " + queryProvider.toString());

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
	public ItemProcessor<BatchUpdateFPXStatus, BatchUpdateFPXStatus> extractFpxStatusJobProcessor() {
		
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));

		return batchUpdateFPXStatus -> {
			int currentRetries = 0;							
			while(currentRetries < jobConfigProperties.getMaxAttempt()) {
				try {
					// Each time we only call EAI with 1 Txn token because if we pass all codes to EAI once, it will treat as failed if either of them found invalid
					EAIFpxStatusResponse eAIFpxStatusResponse = FPXUtils.getEAIFPXStatus(batchUpdateFPXStatus.getTxnTokenId(),batchUpdateFPXStatus.getTxnStatus(),jobConfigProperties.getRestAPI(), sessionToken, restTemplate);

					if(!Objects.isNull(eAIFpxStatusResponse) 
						&& (eAIFpxStatusResponse.getStatusType().equals("success"))) {

							batchUpdateFPXStatus.setProcessed(true);
							break;							
					}
				} catch(Exception e) {
					++currentRetries;
					logger.warn(String.format("Exception %s-Unable to retrieve from eai - fpx [%s]. retries [%d]", e.getMessage(), batchUpdateFPXStatus.getTxnTokenId(),currentRetries));
				}
				
				if(currentRetries == jobConfigProperties.getMaxAttempt()) {
					logger.warn(String.format("Reached max retry attempts to retrieve from eai - fpx [%s]. retries [%d]", batchUpdateFPXStatus.getTxnTokenId(),currentRetries));
				}
			}

			return batchUpdateFPXStatus;
		};
	}

	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	public ItemWriter<BatchUpdateFPXStatus> extractFpxStatusJobWriter(
			@Value("#{stepExecution}") StepExecution stepExecution) {
		
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));

		return new ItemWriter<BatchUpdateFPXStatus>() {
			@Override
			public void write(List<? extends BatchUpdateFPXStatus> batchUpdateFPXStatusList) throws Exception {
				logger.info(String.format("Update total no. of updates for fpx status list  : [%d]", batchUpdateFPXStatusList.size()));
				
	            int failedCount=0;
				String errorMessage = "";

	            long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
            	String message = String.format("%s jobExecutionId=%s", this.getClass().getName(), jobExecutionId);
            	logger.info(message);

				for(BatchUpdateFPXStatus batchUpdateFPXStatus : batchUpdateFPXStatusList) {
					try {
						// Only update DCP if we had successfully get the new term deposit placement from EAI previously
						if(batchUpdateFPXStatus.isProcessed()) {
							batchUpdateFpxStatusRepositoryImpl.updateBatchFpxStatus(batchUpdateFPXStatus);
						}

					} catch (Exception e) {
						++failedCount;
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						errorMessage = String.format("Error happened while updating to DB for batch update fpx status Table [%s], id:%d", batchUpdateFPXStatus, batchUpdateFPXStatus.getId());
						logger.error("Unable to " + e.getLocalizedMessage(), e);
						logger.error(errorMessage, e);
					}
				}
				if(failedCount > 3) {
					
					stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, errorMessage ));
				}
				
			}
		};
	}
	

}
