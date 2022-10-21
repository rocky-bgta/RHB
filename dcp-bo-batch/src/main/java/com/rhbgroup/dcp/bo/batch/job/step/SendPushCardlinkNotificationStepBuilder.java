package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseJMSStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PushCardlinkNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotification;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchCardlinkNotificationsRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BatchStagedNotificationRowMapper;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.GenericCapsule;
import com.rhbgroup.dcp.notifications.models.NotificationPayload;

@Component
@Lazy
public class SendPushCardlinkNotificationStepBuilder extends BaseJMSStepBuilder{
	
	private static final Logger logger = Logger.getLogger(SendPushCardlinkNotificationStepBuilder.class);
	
	private static final String STEP_NAME = "SendPushCardlinkNotificationStep";
	
	@Autowired
	PushCardlinkNotificationsJobConfigProperties jobConfigProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedNotification> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedNotification, BatchStagedNotification> itemProcessor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedNotification> itemWriter;
	
	@Autowired
	private BatchCardlinkNotificationsRepositoryImpl batchCardlinkNotificationsRepository;
	
	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public JdbcPagingItemReader<BatchStagedNotification> batchStagedNotificationItemReader(@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) throws BatchException {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		// Create the reader to fetch required records from DB
		JdbcPagingItemReader<BatchStagedNotification> jdbcPagingItemReader = new JdbcPagingItemReader<>();
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
		
		String selectClause = "SELECT ID, EVENT_CODE, KEY_TYPE, USER_ID, DATA_3, DATA_4, IS_PROCESSED";
        String fromClause = "FROM TBL_BATCH_STAGED_NOTIFICATION";
        String whereClause = "WHERE KEY_TYPE='CC' AND IS_PROCESSED=0 AND PROCESS_DATE=:processDate";
        
        Date processDate = getProcessDate(stepExecution, -1, ChronoUnit.DAYS);
        String batchProcessDate = DateUtils.formatDateString(processDate,DEFAULT_JOB_PARAMETER_DATE_FORMAT); 
        
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
        jdbcPagingItemReader.setRowMapper(new BatchStagedNotificationRowMapper());
        
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return jdbcPagingItemReader;
	}
	
	@Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
	public ItemProcessor<BatchStagedNotification, BatchStagedNotification> batchStagedNotificationItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		return new ItemProcessor<BatchStagedNotification, BatchStagedNotification>() {
			@Override
			public BatchStagedNotification process(BatchStagedNotification batchStagedNotification) throws Exception {
				logger.trace(String.format("BatchStagedNotification received by processor [%s]", batchStagedNotification));
				return batchStagedNotification;
			}
		};
	}
	
	@Bean(STEP_NAME + ".ItemWriter")
    @StepScope
	public ItemWriter<BatchStagedNotification> batchStagedNotificationItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<BatchStagedNotification>() {
			@Override
			public void write(List<? extends BatchStagedNotification> batchStagedNotifications) throws Exception {
				logger.info(String.format("Sending total [%d] JSON notification for BatchStagedNotification to JMS queue", batchStagedNotifications.size()));
				for (BatchStagedNotification batchStagedNotification : batchStagedNotifications) {
					try {
						batchStagedNotification.setJobExecutionId(stepExecution.getJobExecution().getId().intValue());
						NotificationPayload notificationPayload = generateNotificationPayload(batchStagedNotification);
						Capsule capsule = new Capsule();
						capsule.setUserId(Integer.parseInt(Long.toString(batchStagedNotification.getUserId())));
						capsule.setMessageId(UUID.randomUUID().toString());
						capsule.setQuickLogin(false);
						capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_BACKOFFICE);
						capsule.setProperty(Constants.OPERATION_NAME, "BOBatchPushCardlinkNotification");
						GenericCapsule<NotificationPayload> genericCapsule = new GenericCapsule<>(notificationPayload, capsule);
						sendCapsuleMessageToSmsJMS(genericCapsule.generateCapsuleWithMetadata());
						logger.trace(String.format("Sending notification [%s] to JMS queue [%s]", notificationPayload, jobConfigProperties.getJmsQueue()));
						logger.trace(String.format("Updating BatchStagedNotification [%s] IS_PROCESSED to 1", batchStagedNotification));
						batchCardlinkNotificationsRepository.updateIsProcessed(jobConfigProperties.getBatchCode(), batchStagedNotification);
					} catch (BatchException be) {
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						String errorMessage = String.format("Error happened while updating record IsProcessed status in DB for BatchStagedNotification [%s]", batchStagedNotification);
						logger.error(errorMessage, be);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, errorMessage));
					} catch (Exception e) {
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						String errorMessage = String.format("Error happened while pushing notification to JMS queue for BatchStagedNotification [%s]", batchStagedNotification);
						logger.error(errorMessage, e);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, errorMessage));
					}
				}
			}
		};
	}
	
	private NotificationPayload generateNotificationPayload(BatchStagedNotification batchStagedNotification) {
		Integer userId = Integer.parseInt(Long.toString(batchStagedNotification.getUserId()));
		String eventCode = batchStagedNotification.getEventCode();
		Instant now = Instant.now();
		NotificationPayload notificationPayload = new NotificationPayload(userId, eventCode, now);
		String actionableRefData = batchStagedNotification.getData3();
		notificationPayload.setActionableRefData(actionableRefData);
		Map<String,String> data =new HashMap<String,String>();
		data.put("cardNo", batchStagedNotification.getData3());
		String data4 = batchStagedNotification.getData4() + " 00:00";
		LocalDateTime ldt = LocalDateTime.parse(data4, DateTimeFormatter.ofPattern("yyyyMMdd HH:mm"));
		Instant i = ldt.atZone(ZoneId.systemDefault()).toInstant();
		long dueDate = i.toEpochMilli();
		data.put("dueDate", Long.toString(dueDate));
		notificationPayload.setData(data);
		return notificationPayload;
	}
	
	@Override
	public Step buildStep() {
		logger.info(String.format("Building step [%s]", STEP_NAME));
		
		Step step = getDefaultStepBuilder(STEP_NAME).<BatchStagedNotification, BatchStagedNotification>chunk(jobConfigProperties.getChunkSize())
	            .reader(itemReader)
	            .processor(itemProcessor)
	            .writer(itemWriter)
	            .build();
		
		logger.info(String.format("[%s] step build successfully", STEP_NAME));
		return step;
	}	
	
}
