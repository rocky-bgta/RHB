package com.rhbgroup.dcp.bo.batch.job.step;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseJMSStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PushAdhocSystemDowntimeNotificationsProcessorJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDowntimeNotification;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedDowntimeNotificationRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BatchStagedDowntimeNotificationRowMapper;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.GenericCapsule;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.notifications.models.NotificationPayload;

@Component
@Lazy
public class SendPushAdhocSystemDowntimeNotificationsStepBuilder extends BaseJMSStepBuilder{ 
	
	private static final Logger logger = Logger.getLogger(SendPushAdhocSystemDowntimeNotificationsStepBuilder.class);
	
	private static final String STEP_NAME = "SendPushAdhocSystemDowntimeNotificationsStep";
	
	@Autowired
	PushAdhocSystemDowntimeNotificationsProcessorJobConfigProperties jobConfigProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedDowntimeNotification> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedDowntimeNotification, BatchStagedDowntimeNotification> itemProcessor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedDowntimeNotification> itemWriter;
	
	@Autowired
	private BatchStagedDowntimeNotificationRepositoryImpl batchStagedDowntimeNotificationRepository;
	
	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public JdbcPagingItemReader<BatchStagedDowntimeNotification> batchStagedDowntimeNotificationItemReader(@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) throws BatchException {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		// Create the reader to fetch required records from DB
		JdbcPagingItemReader<BatchStagedDowntimeNotification> jdbcPagingItemReader = new JdbcPagingItemReader<>();
		jdbcPagingItemReader.setDataSource(dataSource);
		jdbcPagingItemReader.setPageSize(jobConfigProperties.getJdbcPagingSize());
		
		// Throttle configuration in application.yml
		/**
		 * Throttle size -1: Unlimited, retrieve all not yet process Downtime notifications and push them all to the JBoss queue
		 * Throttle size  0: Fetch nothing from DB, since nothing is getting fetched, at the end no Downtime Notifications is getting push to the JBoss queue
		 * Throttle size >1: Fetch specific amounts of not yet process Downtime notifications from DB, push all fetched only Downtime notifications to JBoss queue
		 */
		if (jobConfigProperties.getThrottleSize() > 1) {
			jdbcPagingItemReader.setFetchSize(jobConfigProperties.getThrottleSize());
		} else if (jobConfigProperties.getThrottleSize() == 0) {
			jdbcPagingItemReader.setFetchSize(0);
		}
		
		String selectClause = "SELECT ID, JOB_EXECUTION_ID, TYPE, ADHOC_TYPE, EVENT_CODE, CONTENT, USER_ID, IS_PROCESSED, START_TIME, END_TIME";
        String fromClause = "FROM TBL_BATCH_STAGED_DOWNTIME_NOTIFICATION";
        String whereClause = "WHERE IS_PROCESSED=0";
        
        // SQL select paging query
        SqlServerPagingQueryProvider selectPagingQueryProvider = new SqlServerPagingQueryProvider();
        selectPagingQueryProvider.setSelectClause(selectClause);
        selectPagingQueryProvider.setFromClause(fromClause);
        selectPagingQueryProvider.setWhereClause(whereClause);
        logger.debug(String.format("SQL select paging query [%s %s %s]", selectClause, fromClause, whereClause));
        
        // Sorting keys using in SQL select paging query
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("ID", Order.ASCENDING);
        selectPagingQueryProvider.setSortKeys(sortKeys);
        
        // Setting the SQL paging select query, parameters used, and row mapper for the reader
        jdbcPagingItemReader.setQueryProvider(selectPagingQueryProvider);
        jdbcPagingItemReader.setRowMapper(new BatchStagedDowntimeNotificationRowMapper());
        
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return jdbcPagingItemReader;
	}
	
	@Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
	public ItemProcessor<BatchStagedDowntimeNotification, BatchStagedDowntimeNotification> batchStagedDowntimeNotificationItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		return new ItemProcessor<BatchStagedDowntimeNotification, BatchStagedDowntimeNotification>() {
			@Override
			public BatchStagedDowntimeNotification process(BatchStagedDowntimeNotification batchStagedDowntimeNotification) throws Exception {
				logger.trace(String.format("BatchStagedDowntimeNotification received by processor [%s]", batchStagedDowntimeNotification));
				return batchStagedDowntimeNotification;
			}
		};
	}
	
	@Bean(STEP_NAME + ".ItemWriter")
    @StepScope
	public ItemWriter<BatchStagedDowntimeNotification> batchStagedDowntimeNotificationItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<BatchStagedDowntimeNotification>() {
			@Override
			public void write(List<? extends BatchStagedDowntimeNotification> batchStagedDowntimeNotificationList) throws Exception {
				logger.info(String.format("Sending total [%d] JSON notification for BatchStagedNotifMass to JMS queue", batchStagedDowntimeNotificationList.size()));
				List<BatchStagedDowntimeNotification> batchStagedDowntimeNotificationProcessedList = new ArrayList<BatchStagedDowntimeNotification>();
				for (BatchStagedDowntimeNotification batchStagedDowntimeNotification : batchStagedDowntimeNotificationList) {
					try {
						batchStagedDowntimeNotification.setJobExecutionId(stepExecution.getJobExecution().getId().intValue());
						NotificationPayload notificationPayload = generateNotificationPayload(batchStagedDowntimeNotification);
						Capsule capsule = new Capsule();
						capsule.setUserId(Integer.parseInt(Long.toString(batchStagedDowntimeNotification.getUserId())));
						capsule.setMessageId(UUID.randomUUID().toString());
						capsule.setQuickLogin(false);
						capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_BACKOFFICE);
						capsule.setProperty(Constants.OPERATION_NAME, "BOBatchPushAdhocDowntimeNotification");
						GenericCapsule<NotificationPayload> genericCapsule = new GenericCapsule<>(notificationPayload, capsule);
						sendCapsuleMessageToSmsJMS(genericCapsule.generateCapsuleWithMetadata());
						logger.trace(String.format("Sending notification [%s] to JMS queue [%s]", notificationPayload, jobConfigProperties.getJmsQueue()));
						batchStagedDowntimeNotificationProcessedList.add(batchStagedDowntimeNotification);
					} catch (Exception e) {
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						String errorMessage = String.format("Error happened while pushing notification to JMS queue for BatchStagedDowntimeNotification [%s]", batchStagedDowntimeNotification);
						logger.error(errorMessage, e);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, errorMessage));
					}
				}
				int updatedCount = batchStagedDowntimeNotificationRepository.addRecordUpdateIsProcessedInBatch(jobConfigProperties.getBatchCode(), batchStagedDowntimeNotificationProcessedList);
				logger.debug(String.format("Updating BatchStagedDowntimeNotification [%s] IS_PROCESSED to 1", updatedCount));
			}
		};
	}
	
	private NotificationPayload generateNotificationPayload(BatchStagedDowntimeNotification batchStagedDowntimeNotification) {
		Integer userId = Integer.parseInt(Long.toString(batchStagedDowntimeNotification.getUserId()));
		String eventCode = batchStagedDowntimeNotification.getEventCode();
		Instant now = Instant.now();
		NotificationPayload notificationPayload = new NotificationPayload(userId, eventCode, now);
		Map<String,String> data =new HashMap<String,String>();
		data.put("msgContent", batchStagedDowntimeNotification.getContent());
		notificationPayload.setData(data);
		return notificationPayload;
	}
	
	@Override
	public Step buildStep() {
		logger.info(String.format("Building step [%s]", STEP_NAME));
		
		Step step = getDefaultStepBuilder(STEP_NAME).<BatchStagedDowntimeNotification, BatchStagedDowntimeNotification>chunk(jobConfigProperties.getChunkSize())
	            .reader(itemReader)
	            .processor(itemProcessor)
	            .writer(itemWriter)
	            .build();
		
		logger.info(String.format("[%s] step build successfully", STEP_NAME));
		return step;
	}	
	
}
