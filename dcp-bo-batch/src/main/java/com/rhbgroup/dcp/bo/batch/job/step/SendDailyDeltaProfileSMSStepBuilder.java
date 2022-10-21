package com.rhbgroup.dcp.bo.batch.job.step;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJMSStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.DailyDeltaProfileSMSJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDailyDeltaNewProfile;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedDailyDeltaNewProfileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BatchStagedDailyDeltaNewProfileRowMapper;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.GenericCapsule;
import com.rhbgroup.dcp.notifications.models.NotificationPayload;

@Component
@Lazy
public class SendDailyDeltaProfileSMSStepBuilder extends BaseJMSStepBuilder {

	private static final Logger logger = Logger.getLogger(SendDailyDeltaProfileSMSStepBuilder.class);
	
	private static final String STEP_NAME = "SendDailyDeltaProfileSMSStep";
		
	@Autowired
	private DailyDeltaProfileSMSJobConfigProperties jobConfigProperties;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedDailyDeltaNewProfile> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedDailyDeltaNewProfile, BatchStagedDailyDeltaNewProfile> itemProcessor;
	   
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedDailyDeltaNewProfile> itemWriter;

	@Autowired
	private BatchStagedDailyDeltaNewProfileRepositoryImpl batchStagedDailyDeltaNewProfileRepository;
	
	@Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfileItemReader(@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) throws BatchException {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		// Create the reader to fetch required records from DB
		JdbcPagingItemReader<BatchStagedDailyDeltaNewProfile> jdbcPagingItemReader = new JdbcPagingItemReader<>();
        jdbcPagingItemReader.setDataSource(dataSource);
        jdbcPagingItemReader.setPageSize(jobConfigProperties.getJdbcPagingSize());

        String selectClause = "SELECT ID, PROCESSING_DATE, USER_ID, IS_PROCESSED";
        String fromClause = "FROM TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE";
        String whereClause = "WHERE IS_PROCESSED=0 AND PROCESSING_DATE=:processDate";
        
        Date batchProcessDate = getProcessDate(stepExecution, 0, null);
        
        // SQL select paging query
        SqlServerPagingQueryProvider selectPagingQueryProvider = new SqlServerPagingQueryProvider();
        selectPagingQueryProvider.setSelectClause(selectClause);
        selectPagingQueryProvider.setFromClause(fromClause);
        selectPagingQueryProvider.setWhereClause(whereClause);
        logger.debug(String.format("SQL select paging query [%s %s %s] using parameters ProcessDate [%s]", selectClause, fromClause, whereClause, batchProcessDate));
        
        // Sorting keys using in SQL select paging query
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("ID", Order.ASCENDING);
        selectPagingQueryProvider.setSortKeys(sortKeys);
        
        // The parameters used to replace in the SQL select paging query
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("processDate", batchProcessDate);
        
        // Setting the SQL paging select query, parameters used, and row mapper for the reader
        jdbcPagingItemReader.setQueryProvider(selectPagingQueryProvider);
        jdbcPagingItemReader.setParameterValues(parameterValues);
        jdbcPagingItemReader.setRowMapper(new BatchStagedDailyDeltaNewProfileRowMapper());
        
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return jdbcPagingItemReader;
	}
		
	@Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchStagedDailyDeltaNewProfile, BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfileItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		return new ItemProcessor<BatchStagedDailyDeltaNewProfile, BatchStagedDailyDeltaNewProfile>() {
			@Override
			public BatchStagedDailyDeltaNewProfile process(BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile) throws Exception {
				logger.trace(String.format("BatchStagedDailyDeltaNewProfile received by processor [%s]", batchStagedDailyDeltaNewProfile));
				return batchStagedDailyDeltaNewProfile;
			} 
		};
	}
	
	@Bean(STEP_NAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfileItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<BatchStagedDailyDeltaNewProfile>() {
			@Override
			public void write(List<? extends BatchStagedDailyDeltaNewProfile> batchStagedDailyDeltaNewProfiles) throws Exception {
				logger.info(String.format("Sending total [%d] JSON notification for BatchStagedDailyDeltaNewProfiles to JMS queue", batchStagedDailyDeltaNewProfiles.size()));
				
				for(BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile : batchStagedDailyDeltaNewProfiles) {
					try {
						batchStagedDailyDeltaNewProfile.setJobExecutionId(stepExecution.getJobExecution().getId().intValue());
                        NotificationPayload notificationPayload = generateNotificationPayload(batchStagedDailyDeltaNewProfile);
                        Capsule capsule = new Capsule();
                        capsule.setUserId(batchStagedDailyDeltaNewProfile.getUserId());
                        capsule.setMessageId(UUID.randomUUID().toString());
                        capsule.setQuickLogin(false);
						capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_BACKOFFICE);
						capsule.setProperty(Constants.OPERATION_NAME, "BOBatchDailyDeltaSMS");
                        GenericCapsule<NotificationPayload> genericCapsule = new GenericCapsule<>(notificationPayload, capsule);
                        sendCapsuleMessageToSmsJMS(genericCapsule.generateCapsuleWithMetadata());
                        logger.trace(String.format("Sending notification [%s] to JMS queue [%s]", notificationPayload, jobConfigProperties.getJmsQueue()));
						logger.trace(String.format("Updating BatchStagedIBGRejectTxn [%s] IS_PROCESSED to 1", batchStagedDailyDeltaNewProfile));
						batchStagedDailyDeltaNewProfileRepository.updateIsProcessed(batchStagedDailyDeltaNewProfile);
					} catch (BatchException e) {
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						String errorMessage = String.format("Error happened while updating record IsProcessed status in DB for BatchStagedDailyDeltaNewProfile [%s]", batchStagedDailyDeltaNewProfile);
						logger.error(errorMessage, e);
					} catch (Exception e) {
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
                        String errorMessage = String.format("Error happened while pushing notification to JMS queue for BatchStagedDailyDeltaNewProfile [%s]", batchStagedDailyDeltaNewProfile);
						logger.error(errorMessage, e);
					}
				}
			}
		};
	}
	
	private NotificationPayload generateNotificationPayload(BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile) {
		Integer userId = batchStagedDailyDeltaNewProfile.getUserId();
		String eventCode = jobConfigProperties.getEventCode();
		Instant now = Instant.now();
		NotificationPayload notificationPayload = new NotificationPayload(userId,eventCode,now);
		return notificationPayload;
	}
	
	@Override
	public Step buildStep() {
		logger.info(String.format("Building step [%s]", STEP_NAME));
		
		Step step = getDefaultStepBuilder(STEP_NAME).<BatchStagedDailyDeltaNewProfile, BatchStagedDailyDeltaNewProfile>chunk(jobConfigProperties.getChunkSize())
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .build();
		
		logger.info(String.format("[%s] step build successfully", STEP_NAME));
		return step;
	}
}