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
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractandUpdateMcaInterestRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateMcaInterestRate;
import com.rhbgroup.dcp.bo.batch.job.model.MCATermInterestRateConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUpdateMcaInterestRateRepositoryImpl;

@Component
@Lazy
public class ExtractInterestRateToMcaInterestRateTableStepBuilder extends BaseStepBuilder {

	static final Logger logger = Logger.getLogger(ExtractInterestRateToMcaInterestRateTableStepBuilder.class);
	private static final String STEP_NAME = "ExtractInterestRateToMcaInterestRateTableStep";
	@Autowired
	private ExtractandUpdateMcaInterestRateJobConfigProperties jobConfigProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<MCATermInterestRateConfig> itemReader;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<MCATermInterestRateConfig, BatchUpdateMcaInterestRate> itemProcessor;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchUpdateMcaInterestRate> itemWriter;

	@Autowired
	private BatchUpdateMcaInterestRateRepositoryImpl batchUpdateMcaInterestRateRepositoryImpl;
	
	@Override
	@Bean(STEP_NAME)
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME)
				.<MCATermInterestRateConfig, BatchUpdateMcaInterestRate>chunk(jobConfigProperties.getChunkSize())
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
	}

	
	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	private JdbcPagingItemReader<MCATermInterestRateConfig> extractCurrencyRateJobReader(
			@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) {
		
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		JdbcPagingItemReader<MCATermInterestRateConfig> databaseReader = new JdbcPagingItemReader<>();
		databaseReader.setDataSource(dataSource);
		databaseReader.setPageSize(jobConfigProperties.getJdbcPagingPageSize());

		PagingQueryProvider queryProvider = createQueryProvider();
		databaseReader.setQueryProvider(queryProvider);
		databaseReader.setRowMapper(new BeanPropertyRowMapper<>(MCATermInterestRateConfig.class));
		
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));

		return databaseReader;
	}

	private PagingQueryProvider createQueryProvider() {
		SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
		queryProvider.setSelectClause(
				"SELECT id, currency_code, interest_rate,tenure,tenure_description,created_time, created_by, updated_time, updated_by");
		queryProvider.setFromClause("FROM dcp.dbo.TBL_MCA_TERM_INTEREST_RATE_CONFIG"); 
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
	private ItemProcessor<MCATermInterestRateConfig, BatchUpdateMcaInterestRate> extractCurrencyRateJobProcessor() {
		
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		
		return mcaTermInterestRateConfig -> {

			BatchUpdateMcaInterestRate batchUpdateMcaInterestRate = new BatchUpdateMcaInterestRate();
			Date currentDate = new Date();
			batchUpdateMcaInterestRate.setMcaTermInterestrateId(mcaTermInterestRateConfig.getId());
			batchUpdateMcaInterestRate.setCode(mcaTermInterestRateConfig.getCurrencyCode());
			batchUpdateMcaInterestRate.setTenure(mcaTermInterestRateConfig.getTenure());
			batchUpdateMcaInterestRate.setInterestRate(mcaTermInterestRateConfig.getInterestRate());
			batchUpdateMcaInterestRate.setCreatedTime(currentDate);
			batchUpdateMcaInterestRate.setUpdatedTime(currentDate);

			logger.info(String.format("[Processing] BatchUpdateMcaInterestRate: [%s]", batchUpdateMcaInterestRate.toString()));
			return batchUpdateMcaInterestRate;
		};
	}

	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	private ItemWriter<BatchUpdateMcaInterestRate> extractCurrencyRateJobWriter(
			@Value("#{stepExecution}") StepExecution stepExecution) {	
		
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));

		return new ItemWriter<BatchUpdateMcaInterestRate>() {
			@Override
			public void write(List<? extends BatchUpdateMcaInterestRate> batchUpdateExchangeRateList) throws Exception {
				logger.info(String.format("Writing total no. of McaInterestRate : [%d]", batchUpdateExchangeRateList.size()));
				
	            int failedCount=0;
				String errorMessage = "";

	            long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
            	String message = String.format("%s jobExecutionId=%s", this.getClass().getName(), jobExecutionId);
            	logger.info(message);

				for(BatchUpdateMcaInterestRate batchUpdateMcaInterestRate : batchUpdateExchangeRateList) {
					try {
						batchUpdateMcaInterestRate.setJobExecutionId(Long.toString(jobExecutionId));
						batchUpdateMcaInterestRateRepositoryImpl.addBatchUpdateMCAInterestRateStaging(batchUpdateMcaInterestRate);
					} catch (Exception e) {
						++failedCount;
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						errorMessage = String.format("Error happened while writing to DB for Staged McaInterestRate Table [%s], id:%d", batchUpdateMcaInterestRate, batchUpdateMcaInterestRate.getId());
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
