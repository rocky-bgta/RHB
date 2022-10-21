package com.rhbgroup.dcp.bo.batch.job.step;

import java.time.Instant;
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

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseJMSStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PushMassNotificationsProcessorJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotifMass;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotifMassRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BatchStagedNotifMassRowMapper;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.GenericCapsule;
import com.rhbgroup.dcp.notifications.models.NotificationPayload;

@Component
@Lazy
public class SendPushMassNotificationsStepBuilder extends BaseJMSStepBuilder{
	
	private static final Logger logger = Logger.getLogger(SendPushMassNotificationsStepBuilder.class);
	
	private static final String STEP_NAME = "SendPushMassNotificationsStep";
	
	@Autowired
	PushMassNotificationsProcessorJobConfigProperties jobConfigProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedNotifMass> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedNotifMass, BatchStagedNotifMass> itemProcessor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedNotifMass> itemWriter;
	
	@Autowired
	private BatchStagedNotifMassRepositoryImpl batchStagedNotifMassRepository;
	
	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public JdbcPagingItemReader<BatchStagedNotifMass> batchStagedNotifMassItemReader(@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) throws BatchException {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		// Create the reader to fetch required records from DB
		JdbcPagingItemReader<BatchStagedNotifMass> jdbcPagingItemReader = new JdbcPagingItemReader<>();
		jdbcPagingItemReader.setDataSource(dataSource);
		jdbcPagingItemReader.setPageSize(jobConfigProperties.getJdbcPagingSize());
		
		// Throttle configuration in application.yml
		/**
		 * Throttle size -1: Unlimited, retrieve all not yet process Cardlink notifications and push them all to the JBoss queue
		 * Throttle size  0: Fetch nothing from DB, since nothing is getting fetched, at the end no Cardlink Notifications is getting push to the JBoss queue
		 * Throttle size >1: Fetch specific amounts of not yet process Cardlink notifications from DB, push all fetched only Cardlink notifications to JBoss queue
		 */
		if (jobConfigProperties.getThrottleSize() > 1) {
			jdbcPagingItemReader.setFetchSize(jobConfigProperties.getThrottleSize());
		} else if (jobConfigProperties.getThrottleSize() == 0) {
			jdbcPagingItemReader.setFetchSize(0);
		}
		
		String selectClause = "SELECT ID, JOB_EXECUTION_ID, FILE_NAME, EVENT_CODE, CONTENT, USER_ID, IS_PROCESSED";
        String fromClause = "FROM TBL_BATCH_STAGED_NOTIF_MASS";
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
        jdbcPagingItemReader.setRowMapper(new BatchStagedNotifMassRowMapper());
        
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return jdbcPagingItemReader;
	}
	
	@Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
	public ItemProcessor<BatchStagedNotifMass, BatchStagedNotifMass> batchStagedNotifMassItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		return new ItemProcessor<BatchStagedNotifMass, BatchStagedNotifMass>() {
			@Override
			public BatchStagedNotifMass process(BatchStagedNotifMass batchStagedNotifMass) throws Exception {
				logger.trace(String.format("BatchStagedNotifMass received by processor [%s]", batchStagedNotifMass));
				return batchStagedNotifMass;
			}
		};
	}
	
	@Bean(STEP_NAME + ".ItemWriter")
    @StepScope
	public ItemWriter<BatchStagedNotifMass> batchStagedNotifMassItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<BatchStagedNotifMass>() {
			@Override
			public void write(List<? extends BatchStagedNotifMass> batchStagedNotifMassList) throws Exception {
				logger.info(String.format("Sending total [%d] JSON notification for BatchStagedNotifMass to JMS queue", batchStagedNotifMassList.size()));
				for (BatchStagedNotifMass batchStagedNotifMass : batchStagedNotifMassList) {
					try {
						batchStagedNotifMass.setJobExecutionId(stepExecution.getJobExecution().getId().intValue());
						NotificationPayload notificationPayload = generateNotificationPayload(batchStagedNotifMass);
						Capsule capsule = new Capsule();
						capsule.setUserId(Integer.parseInt(Long.toString(batchStagedNotifMass.getUserId())));
						capsule.setMessageId(UUID.randomUUID().toString());
						capsule.setQuickLogin(false);
						capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_BACKOFFICE);
						capsule.setProperty(Constants.OPERATION_NAME, "BOBatchPushMassNotification");
						GenericCapsule<NotificationPayload> genericCapsule = new GenericCapsule<>(notificationPayload, capsule);
						sendCapsuleMessageToSmsJMS(genericCapsule.generateCapsuleWithMetadata());
						logger.trace(String.format("Sending notification [%s] to JMS queue [%s]", notificationPayload, jobConfigProperties.getJmsQueue()));
						logger.trace(String.format("Updating BatchStagedNotifMass [%s] IS_PROCESSED to 1", batchStagedNotifMass));
						batchStagedNotifMassRepository.updateIsProcessed(jobConfigProperties.getBatchCode(), batchStagedNotifMass);
					} catch (BatchException be) {
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						String errorMessage = String.format("Error happened while updating record IsProcessed status in DB for BatchStagedNotifMass [%s]", batchStagedNotifMass);
						logger.error(errorMessage, be);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, errorMessage));
					} catch (Exception e) {
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						String errorMessage = String.format("Error happened while pushing notification to JMS queue for BatchStagedNotifMass [%s]", batchStagedNotifMass);
						logger.error(errorMessage, e);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, errorMessage));
					}
				}
			}
		};
	}
	
	private NotificationPayload generateNotificationPayload(BatchStagedNotifMass batchStagedNotifMass) {
		Integer userId = Integer.parseInt(Long.toString(batchStagedNotifMass.getUserId()));
		String eventCode = batchStagedNotifMass.getEventCode();
		Instant now = Instant.now();
		NotificationPayload notificationPayload = new NotificationPayload(userId, eventCode, now);
		Map<String,String> data =new HashMap<String,String>();
		data.put("msgContent", batchStagedNotifMass.getContent());
		notificationPayload.setData(data);
		return notificationPayload;
	}
	
	@Override
	public Step buildStep() {
		logger.info(String.format("Building step [%s]", STEP_NAME));
		
		Step step = getDefaultStepBuilder(STEP_NAME).<BatchStagedNotifMass, BatchStagedNotifMass>chunk(jobConfigProperties.getChunkSize())
	            .reader(itemReader)
	            .processor(itemProcessor)
	            .writer(itemWriter)
	            .build();
		
		logger.info(String.format("[%s] step build successfully", STEP_NAME));
		return step;
	}	
	
}
