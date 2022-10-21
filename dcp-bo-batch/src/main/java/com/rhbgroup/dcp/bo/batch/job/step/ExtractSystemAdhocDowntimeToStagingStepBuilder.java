package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static java.lang.Long.parseLong;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
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
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractAdhocDowntimeJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.enums.AdhocTypeCategory;
import com.rhbgroup.dcp.bo.batch.job.model.BankDownTime;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDowntimeNotification;
import com.rhbgroup.dcp.bo.batch.job.model.ExtractAdhocDowntime;
import com.rhbgroup.dcp.bo.batch.job.repository.AdhocDowntimeNotificationRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BankDowntimeRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedDowntimeNotificationRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UserProfileRepositoryImpl;

@Component
@Lazy
public class ExtractSystemAdhocDowntimeToStagingStepBuilder extends BaseStepBuilder {
	
	private static final Logger logger = Logger.getLogger(ExtractSystemAdhocDowntimeToStagingStepBuilder.class);
	
	private static final String STEP_NAME = "ExtractSystemAdhocDowntimeToStagingStep";
	
	private static final String DATE_FORMAT = "hh:mm a',' dd MMM yyyy";

	private String eventCode;
	
	private String contentSystemStr;
	private String contentInternalStr;
	private String contentExternalStr;
	private String contentStr;
	
	private static final String SYSTEM_ADHOC_TYPE_CATEGORY = "SYSTEM";        
	private static final String INTERNAL_ADHOC_TYPE_CATEGORY = "INTERNAL";    
	private static final String EXTERNAL_ADHOC_TYPE_CATEGORY = "EXTERNAL"; 
	
	private static final String PUSH_TITLE_TEMPLATE ="PUSH_TITLE_TEMPLATE";
	private static final String PUSH_BODY_TEMPLATE ="PUSH_BODY_TEMPLATE";
	
	private static final String START_TIME ="\\$startTime";
	private static final String END_TIME ="\\$endTime";
	private static final String SERVICE_NAME ="\\$serviceNames";
	private static final String BANK_NAME ="\\$bankName";
	
	@Autowired
	private ExtractAdhocDowntimeJobConfigProperties configProperties;
	
	@Autowired
	private BankDowntimeRepositoryImpl bankDowntimeRepositoryImpl;

	@Autowired
	private AdhocDowntimeNotificationRepositoryImpl adhocDowntimeNotificationRepositoryImpl;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemReader")
    private ItemReader<ExtractAdhocDowntime> itemReader;
    
    @Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<ExtractAdhocDowntime, ExtractAdhocDowntime> itemProcessor;
    
    @Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<ExtractAdhocDowntime> itemWriter;
    
    @Autowired
    private BatchStagedDowntimeNotificationRepositoryImpl batchStagedDowntimeNotificationRepositoryImpl;
    
	@Autowired
	private UserProfileRepositoryImpl userProfileRepositoryImpl;
	
    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEP_NAME).<ExtractAdhocDowntime, ExtractAdhocDowntime>chunk(configProperties.getChunkSize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<ExtractAdhocDowntime> extractSystemAdhocDowntimeJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) throws BatchException {
    	
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
        Date processDate = null;
        
        try {

        	// Check if custom JobProcessDate is provided
        	String jobProcessDateStr = (String) stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
        	if(jobProcessDateStr != null){
        		logger.info(String.format("Job process date found : [%s]", jobProcessDateStr));
        		processDate = DateUtils.getDateFromString(jobProcessDateStr, DEFAULT_JOB_PARAMETER_DATE_FORMAT);
        		// If not found stick to the BatchSystemDate from DB
        		logger.info("processDate if custom JobProcessDate is provided:" + processDate);
        	} else {
        		String batchSystemDateStr = (String) stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        		logger.info(String.format("Job process date could not be found, defaulting to Batch System Date : [%s]", processDate));
        		Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
        		processDate = DateUtils.addDays(batchSystemDate, configProperties.getDayDiff());
        	}
        } catch (ParseException ex) {
        	throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR,BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE,ex);
        }

        logger.info(String.format("Final process date to be used : [%s]", processDate));

        JdbcPagingItemReader<ExtractAdhocDowntime> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(configProperties.getJdbcPagingSize());

		logger.info(String.format("configProperties.getJdbcPagingSize() [%s]", configProperties.getJdbcPagingSize()));

		Map<String,Object> parameters = new HashMap<>();
		String processedDateStr = DateUtils.formatDateString(processDate ,DEFAULT_JOB_PARAMETER_DATE_FORMAT);
		parameters.put("processedDate", processedDateStr);
		
        PagingQueryProvider queryProvider = createQueryProvider();
        databaseReader.setQueryProvider(queryProvider);
		databaseReader.setParameterValues( parameters );

        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(ExtractAdhocDowntime.class));
        
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));

        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("	SELECT " + 
        		"		NAME," +         
        		"		ADHOC_TYPE," + 
        		"		TYPE," + 
        		"		START_TIME," + 
        		"		END_TIME, "+ 
        		"		BANK_ID, " + 
        		"		ADHOC_TYPE_CATEGORY " + " " 
        		);
        queryProvider.setFromClause("FROM VW_BATCH_SYSTEM_DOWNTIME_CONFIG");
        queryProvider.setWhereClause("WHERE IS_ACTIVE = '1' AND IS_PUSH_NOTIFICATION = '1' AND PUSH_DATE=:processedDate");
        queryProvider.setSortKeys(sortByIdAsc());
        
        logger.debug("Generated query : " + queryProvider.toString());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("START_TIME", Order.ASCENDING);
        return sortConfiguration;
    }
    
    @Bean(STEP_NAME + ".ItemProcessor")
	@StepScope
	public ItemProcessor<ExtractAdhocDowntime, ExtractAdhocDowntime> extractSystemAdhocDowntimeProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		return new ItemProcessor<ExtractAdhocDowntime, ExtractAdhocDowntime>() {

			@Override
			public ExtractAdhocDowntime process(ExtractAdhocDowntime item) throws Exception {
				eventCode = configProperties.getEventCode();
				// Getting dynamic content from table in place of configProperties
				// constructing message
				
				if (item.getAdhocTypeCategory() != null
						&& item.getAdhocTypeCategory().equalsIgnoreCase(SYSTEM_ADHOC_TYPE_CATEGORY)) {
					contentSystemStr = constructTemplateBasedOnEventCodeInItemProcessor(item);
				    logger.info(String.format("Template Content for system [%s]", contentSystemStr + contentSystemStr));
				}else if (item.getAdhocTypeCategory() != null
						&& item.getAdhocTypeCategory().equalsIgnoreCase(INTERNAL_ADHOC_TYPE_CATEGORY)) {
					contentInternalStr = constructTemplateBasedOnEventCodeInItemProcessor(item);
					 logger.info(String.format("Template Content for Internal [%s]", contentInternalStr + contentInternalStr));
				} else if (item.getAdhocTypeCategory() != null
						&& item.getAdhocTypeCategory().equalsIgnoreCase(EXTERNAL_ADHOC_TYPE_CATEGORY)) {
					contentExternalStr = constructTemplateBasedOnEventCodeInItemProcessor(item);
					 logger.info(String.format("Template Content for External [%s]", contentExternalStr + contentExternalStr));
				}
				return item;
			}

		};
    }

	// ItemWriter
	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	public ItemWriter<ExtractAdhocDowntime> extractSystemAdhocDowntimeItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<ExtractAdhocDowntime>() {

			@Override
			public void write(List<? extends ExtractAdhocDowntime> extractAdhocDowntimes) throws Exception {
				int inserted = 0;
				
				DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
				List <BatchStagedDowntimeNotification> batchStagedDowntimeNotificationList= new ArrayList<>();
				
				// Retrieve if USER_STATUS='A' from TBL_USER_PROFILE
				List<Map<String, Object>> activeUserProfilesList = userProfileRepositoryImpl.getActiveUserProfiles();
				logger.info("Total no. of active user: " + activeUserProfilesList.size());
				
				//loop thru all system adhoc downtime
				// new method written
				inserted = iterateAllSystemAdhocDowntime(extractAdhocDowntimes, batchStagedDowntimeNotificationList,  activeUserProfilesList, dateFormat, stepExecution, inserted );
				logger.info(String.format("Batch Insert TBL_BATCH_STAGED_DOWNTIME_NOTIFICATION [%s]", inserted)+ inserted);
			}
			
		};
	}
	
	private BatchStagedDowntimeNotification createBatchStagedDowntimeNotification(long jobExecutionId, long userId, ExtractAdhocDowntime extractAdhocDowntime, String formattedTemplateContent) throws BatchException {
		BatchStagedDowntimeNotification batchStagedDowntimeNotification = new BatchStagedDowntimeNotification();
		
		batchStagedDowntimeNotification.setJobExecutionId(jobExecutionId);
		batchStagedDowntimeNotification.setType(extractAdhocDowntime.getType());
		batchStagedDowntimeNotification.setAdhocType(extractAdhocDowntime.getAdhocType());
		batchStagedDowntimeNotification.setEventCode(this.eventCode);
		batchStagedDowntimeNotification.setContent(formattedTemplateContent);
		batchStagedDowntimeNotification.setUserId(userId);
		batchStagedDowntimeNotification.setProcessed(false);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		batchStagedDowntimeNotification.setStartTime(extractAdhocDowntime.getStartTime());
		batchStagedDowntimeNotification.setEndTime(extractAdhocDowntime.getEndTime());
		batchStagedDowntimeNotification.setCreatedTime(now);
		batchStagedDowntimeNotification.setCreatedBy(configProperties.getBatchCode());
		batchStagedDowntimeNotification.setUpdatedTime(now);
		batchStagedDowntimeNotification.setUpdatedBy(configProperties.getBatchCode());
		batchStagedDowntimeNotification.setAdhocTypeCategory(extractAdhocDowntime.getAdhocTypeCategory());
		
		return batchStagedDowntimeNotification;
	}
	
	private int addedRecordBatchStagedDowntimeNotificationInBatch(List <BatchStagedDowntimeNotification> batchStagedDowntimeNotificationList, StepExecution stepExecution, int inserted) throws BatchException{
		try {
			inserted += batchStagedDowntimeNotificationRepositoryImpl.addRecordBatchStagedDowntimeNotificationInBatch(batchStagedDowntimeNotificationList);
		} catch(Exception ex) {
				String errorMsg = String.format("Exception: exception=%s",ex.getMessage());
				logger.error(errorMsg);
				stepExecution.getJobExecution().setExitStatus(ExitStatus.FAILED);
				throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
			}
		return inserted;
	}
	
	private String constructTemplateBasedOnEventCodeInItemProcessor(ExtractAdhocDowntime item) throws BatchException{
		String pushTitleStr= null;
		String tempContentSystemStr =null;
		String tempContentInternalStr =null;
		String tempContentExternalStr =null;
		
		logger.info("AdhocTypecatagory: " + item.getAdhocTypeCategory());
		AdhocTypeCategory typeCat =null;
		Map<String, Object> templatMap = null;
		if (item.getAdhocTypeCategory() != null
				&& item.getAdhocTypeCategory().equalsIgnoreCase(SYSTEM_ADHOC_TYPE_CATEGORY)) {
			typeCat = AdhocTypeCategory.valueOf(SYSTEM_ADHOC_TYPE_CATEGORY);
			templatMap = adhocDowntimeNotificationRepositoryImpl
					.getTemplateFieldFormBatchNotificationTemplateByEventCode(String.valueOf(typeCat.getEventCode()));
			if (templatMap != null) {
				pushTitleStr = templatMap.get(PUSH_TITLE_TEMPLATE).toString();
				tempContentSystemStr = templatMap.get(PUSH_BODY_TEMPLATE).toString();
				tempContentSystemStr = pushTitleStr + " - " + tempContentSystemStr;
				contentStr= tempContentSystemStr;
			}
		} else if (item.getAdhocTypeCategory() != null
				&& item.getAdhocTypeCategory().equalsIgnoreCase(INTERNAL_ADHOC_TYPE_CATEGORY)) {
			typeCat = AdhocTypeCategory.valueOf(INTERNAL_ADHOC_TYPE_CATEGORY);
			templatMap = adhocDowntimeNotificationRepositoryImpl
					.getTemplateFieldFormBatchNotificationTemplateByEventCode(String.valueOf(typeCat.getEventCode()));
			if (templatMap != null) {
				pushTitleStr = templatMap.get(PUSH_TITLE_TEMPLATE).toString();
				tempContentInternalStr = templatMap.get(PUSH_BODY_TEMPLATE).toString();
				tempContentInternalStr = pushTitleStr + " - " + tempContentInternalStr;
				contentStr = tempContentInternalStr;
			}
		} else if (item.getAdhocTypeCategory() != null
				&& item.getAdhocTypeCategory().equalsIgnoreCase(EXTERNAL_ADHOC_TYPE_CATEGORY)) {
			typeCat = AdhocTypeCategory.valueOf(EXTERNAL_ADHOC_TYPE_CATEGORY);
			templatMap = adhocDowntimeNotificationRepositoryImpl
					.getTemplateFieldFormBatchNotificationTemplateByEventCode(String.valueOf(typeCat.getEventCode()));
			if (templatMap != null) {
				pushTitleStr = templatMap.get(PUSH_TITLE_TEMPLATE).toString();
				tempContentExternalStr = templatMap.get(PUSH_BODY_TEMPLATE).toString();
				tempContentExternalStr = pushTitleStr + " - " + tempContentExternalStr;
				contentStr = tempContentExternalStr;
			}
		}
		return contentStr;
	}
	
	// Method to return formated template based on AdhocTypeCategory
	
	private String getFormatedTemplate(ExtractAdhocDowntime extractAdhocDowntime,DateFormat dateFormat,String formattedContent,StepExecution stepExecution) {
		
		if (extractAdhocDowntime.getAdhocTypeCategory().equalsIgnoreCase(SYSTEM_ADHOC_TYPE_CATEGORY)) {
			// construct message
			String formatttedStartTime = dateFormat.format(extractAdhocDowntime.getStartTime());
			String formatttedEndTime = dateFormat.format(extractAdhocDowntime.getEndTime());
			formattedContent = contentSystemStr.replaceAll(START_TIME, formatttedStartTime);
			formattedContent = formattedContent.replaceAll(END_TIME, formatttedEndTime);
			logger.info("Content for Sysetm Adhoc Type: " + formattedContent);
			logger.debug("Content for Sysetm Adhoc Type: " + formattedContent);
		} else if (extractAdhocDowntime.getAdhocTypeCategory()
				.equalsIgnoreCase(INTERNAL_ADHOC_TYPE_CATEGORY)) {
			String adhocType = extractAdhocDowntime.getAdhocType();
			
			String formatttedStartTime = dateFormat.format(extractAdhocDowntime.getStartTime());
			String formatttedEndTime = dateFormat.format(extractAdhocDowntime.getEndTime());
			logger.info("Content for Internal Adhoc Type: " + contentInternalStr);
			formattedContent = contentInternalStr.replaceAll(START_TIME, formatttedStartTime);
			formattedContent = formattedContent.replaceAll(END_TIME, formatttedEndTime);
			formattedContent = formattedContent.replaceAll(SERVICE_NAME, adhocType);
			logger.info("Content for Internal Adhoc Type : " + formattedContent);
			logger.debug("Content for Internal Adhoc Type : " + formattedContent);
		} else if (extractAdhocDowntime.getAdhocTypeCategory()
				.equalsIgnoreCase(EXTERNAL_ADHOC_TYPE_CATEGORY)) {
			String adhocType = extractAdhocDowntime.getAdhocType();
			logger.info("Bank Id : " + extractAdhocDowntime.getBankId());
			logger.debug("Bank Id : " + extractAdhocDowntime.getBankId());
			BankDownTime bankDownTime = null;
			if (extractAdhocDowntime.getBankId() != 0) {
				try {
					bankDownTime = bankDowntimeRepositoryImpl
							.getBankDetails(extractAdhocDowntime.getBankId());
					String formatttedStartTime = dateFormat.format(bankDownTime.getStartDatetime());
					String formatttedEndTime = dateFormat.format(bankDownTime.getEndDatetime());
					formattedContent = contentExternalStr.replaceAll(START_TIME, formatttedStartTime);
					formattedContent = formattedContent.replaceAll(END_TIME, formatttedEndTime);
					formattedContent = formattedContent.replaceAll(SERVICE_NAME, adhocType);
					formattedContent = formattedContent.replaceAll(BANK_NAME,
							bankDownTime.getMainFunction());
					logger.info(String.format("Content for External Adhoc Type [%s] ", formattedContent)+ formattedContent);
					logger.debug(String.format("Content for External Adhoc Type [%s] ", formattedContent)+ formattedContent);
				} catch (Exception e) {
					String errorMessage = String.format(
							"Error happened while getting bank Name from respective repository [%s]",
							STEP_NAME);
					logger.error(errorMessage, e);
					stepExecution.getJobExecution().addFailureException(e);
				}
			} 
			logger.info("Content for External Adhoc Type : " + formattedContent);
		}
		return formattedContent;
	}
	
  private	int iterateAllSystemAdhocDowntime(List<? extends ExtractAdhocDowntime> extractAdhocDowntimes,List <BatchStagedDowntimeNotification> batchStagedDowntimeNotificationList, List<Map<String, Object>> activeUserProfilesList, DateFormat dateFormat,StepExecution stepExecution, int inserted ){
		String action = "inserting";
		String table = "TBL_BATCH_STAGED_DOWNTIME_NOTIFICATION";
		
		for (ExtractAdhocDowntime extractAdhocDowntime:extractAdhocDowntimes) {
			logger.info("System adhoc downtime's name: " + extractAdhocDowntime.getName());
			//initialize counters
			int counter = 0;
			int chunkSizeBlock = 1;
			int chunkSize = configProperties.getChunkSize() * chunkSizeBlock;
			String formattedContent =null;
			String formattedTemplateContent =null;
			//construct formatted message
			formattedTemplateContent = getFormatedTemplate(extractAdhocDowntime, dateFormat, formattedContent, stepExecution);
			logger.info("formattedTemplateContent:: " + formattedTemplateContent);
			try {

				if (!activeUserProfilesList.isEmpty()) {
					//loop thru all active users
					for (Map<String, Object> userProfile : activeUserProfilesList) {
						long jobExecutionId = stepExecution.getJobExecution().getId();
						long userProfileId = parseLong(userProfile.get("ID").toString());
						BatchStagedDowntimeNotification batchStagedDowntimeNotification = createBatchStagedDowntimeNotification(jobExecutionId, userProfileId, extractAdhocDowntime, formattedTemplateContent);
						logger.trace(String.format("Inserting BatchStagedDowntimeNotification object [%s] to DB", batchStagedDowntimeNotification));
						batchStagedDowntimeNotificationList.add(batchStagedDowntimeNotification);
						counter++;
						//batch insert into TBL_BATCH_STAGED_DOWNTIME_NOTIFICATION
						if (counter >= chunkSize || counter >= (activeUserProfilesList.size())) {
							logger.debug("Batch update at counter: " + counter + ", chunksize:" + chunkSize); 
							//perform batch update for batchStagedDowntimeNotification
							
							// Method to insert record in batch
							inserted = addedRecordBatchStagedDowntimeNotificationInBatch(batchStagedDowntimeNotificationList,  stepExecution, inserted);
							logger.debug("added record in batch: " + inserted);
							//increase next chunkSizeBlock
							chunkSizeBlock++;
							chunkSize =  configProperties.getChunkSize() * chunkSizeBlock;
							batchStagedDowntimeNotificationList.clear();
						}
					}
				}
			} catch(BatchException be) {
				// If exception happened, don't stop the batch, just proceed to next record until all complete processed
				String errorMessage = String.format("Error happened while [%s] record in DB [%s]", action, table);
				logger.error(errorMessage, be);
				stepExecution.getJobExecution().addFailureException(be);
			} catch(Exception e) {
				String errorMessage = String.format("Error happened while creating ItemWriter [%s]", STEP_NAME);
				logger.error(errorMessage, e);
				stepExecution.getJobExecution().addFailureException(e);
			}
		}
		return inserted;
	}

}
