package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;

import java.time.temporal.ChronoUnit;
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
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractCardlinkNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.CardlinkNotification;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchCardlinkNotificationsRepositoryImpl;

@Component
@Lazy
public class ExtractAndStoreCardlinkNotificationStepBuilder extends BaseStepBuilder {

	private static final Logger logger = Logger.getLogger(ExtractAndStoreCardlinkNotificationStepBuilder.class);
	
	private static final String STEP_NAME = "ExtractCardlinkNotificationStep";
	
	Date processedDate = new Date();
		
    @Autowired
    private ExtractCardlinkNotificationsJobConfigProperties configProperties;

    @Autowired
    @Qualifier(STEP_NAME + ".ItemReader")
    private ItemReader<CardlinkNotification> itemReader;
    
    @Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<CardlinkNotification, CardlinkNotification> itemProcessor;
    
    @Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<CardlinkNotification> itemWriter;

    @Autowired
    private BatchCardlinkNotificationsRepositoryImpl batchCardlinkNotificationsRepositoryImpl;
    
    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEP_NAME).<CardlinkNotification, CardlinkNotification>chunk(configProperties.getChunkSize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<CardlinkNotification> extractCardlinkNotificationsJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) throws BatchException {
    	
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));

        JdbcPagingItemReader<CardlinkNotification> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(configProperties.getJdbcPagingPageSize());

		logger.info(String.format("configProperties.getJdbcpagingpagesize() [%s]", configProperties.getJdbcPagingPageSize()));

		Map<String,Object> parameters = new HashMap<>();
		processedDate = getProcessDate(stepExecution,-1,ChronoUnit.DAYS);
		String processedDateStr = DateUtils.formatDateString(processedDate ,DEFAULT_JOB_PARAMETER_DATE_FORMAT);
		parameters.put("processedDate", processedDateStr);
		
        PagingQueryProvider queryProvider = createQueryProvider();
        databaseReader.setQueryProvider(queryProvider);
		databaseReader.setParameterValues( parameters );

        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(CardlinkNotification.class));
        
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));

        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("	SELECT " + 
        		"		FILE_NAME," + 
        		"		EVENT_CODE," + 
        		"		KEY_TYPE," + 
        		"		SYSTEM_DATE," + 
        		"		SYSTEM_TIME," + 
        		"		CARD_NUMBER," + 
        		"		PAYMENT_DUE_DATE," + 
        		"		CARD_TYPE," + 
        		"		MINIMUM_AMOUNT," + 
        		"		OUTSTANDING_AMOUNT," + 
        		"		STATEMENT_AMOUNT," + 
        		"		STATEMENT_DATE," + 
        		"		NOTIFICATION_RAW_ID," + 
        		"		USER_ID");
        queryProvider.setFromClause("FROM vw_batch_cardlink_notification ");
        queryProvider.setWhereClause("WHERE PROCESS_DATE=:processedDate");
        queryProvider.setSortKeys(sortByIdAsc());
        
        logger.debug("Generated query : " + queryProvider.toString());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("USER_ID", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<CardlinkNotification, CardlinkNotification> extractCardlinkNotificationsJobProcessor() {
    	
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
    	
        return extractCardlinkNotifications -> {
            logger.trace(String.format("CardlinkNotification received by processor [%s]", extractCardlinkNotifications));
            return extractCardlinkNotifications;
        };
    }

    @Bean(STEP_NAME + ".ItemWriter")
    @StepScope
    public ItemWriter<CardlinkNotification> addCardlinkNotificationsJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<CardlinkNotification>() {
			@Override
			public void write(List<? extends CardlinkNotification> cardLinkNotificationList) throws Exception {
				logger.info(String.format("Writing total no. of notifications : [%d]", cardLinkNotificationList.size()));
				
	            int failedCount=0;
				String errorMessage = "";

	            long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
				String processedDateStr = DateUtils.formatDateString(processedDate ,DEFAULT_JOB_PARAMETER_DATE_FORMAT);

            	String message = String.format("notifications process_date= %s, jobExecutionId=%s", processedDateStr, jobExecutionId);
            	logger.info(message);

				for(CardlinkNotification cardLinkNotification : cardLinkNotificationList) {
					try {
						batchCardlinkNotificationsRepositoryImpl.addIntoNotificationsStaging(jobExecutionId, processedDateStr, configProperties.getBatchCode(), cardLinkNotification);
						batchCardlinkNotificationsRepositoryImpl.updateIsProcessed(jobExecutionId, configProperties.getBatchCode(), cardLinkNotification);
					} catch (Exception e) {
						++failedCount;
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						errorMessage = String.format("Error happened while writing to DB for CardlinkNotification [%s], id:%s", cardLinkNotification, cardLinkNotification.getNotificationRawId() );
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
