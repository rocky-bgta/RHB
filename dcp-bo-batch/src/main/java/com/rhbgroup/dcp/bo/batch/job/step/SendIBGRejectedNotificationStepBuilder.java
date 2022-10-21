package com.rhbgroup.dcp.bo.batch.job.step;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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
import org.springframework.util.StringUtils;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseJMSStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.IBGRejectedNotificationJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.enums.SuspenseType;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBGRejectTxnRespositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BatchStagedIBGRejectTxnRowMapper;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.GenericCapsule;
import com.rhbgroup.dcp.notifications.models.NotificationPayload;

import static com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils.getDateFromString;

@Component
@Lazy
public class SendIBGRejectedNotificationStepBuilder extends BaseJMSStepBuilder {

	private static final Logger logger = Logger.getLogger(SendIBGRejectedNotificationStepBuilder.class);
	
	private static final String STEP_NAME = "SendIBGRejectedNotificationStep";
	
	private static final String COLUMN_VALUES_EMPTY_OR_NULL_MESSAGE_TEMPLATE = "Column(s) %s values shall not be empty/null";

	@Autowired
	private IBGRejectedNotificationJobConfigProperties jobConfigProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedIBGRejectTxn> itemReader;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<BatchStagedIBGRejectTxn, BatchStagedIBGRejectTxn> itemProcessor;
    
	@Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<BatchStagedIBGRejectTxn> itemWriter;
	
	@Autowired
	private BatchSuspenseRepositoryImpl batchSuspenseRepository;
	
	@Autowired
	private BatchStagedIBGRejectTxnRespositoryImpl batchStagedIBGRejectTxnRespository;
	
	@Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<BatchStagedIBGRejectTxn> batchStagedIBGRejectTxnItemReader(@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		// Retrieve the IBGRejectLastProcessedSuccessJobExecutionId from job parameters
		String ibgRejectLastProcessedSuccessJobExecutionIdStr = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID);
		logger.info(String.format("IBGRejectLastProcessedSuccessJobExecutionId to be used [%s]", ibgRejectLastProcessedSuccessJobExecutionIdStr));
		
		int ibgRejectLastProcessedSuccessJobExecutionId = -1;
		if(!ibgRejectLastProcessedSuccessJobExecutionIdStr.isEmpty()) {
			ibgRejectLastProcessedSuccessJobExecutionId = Integer.parseInt(ibgRejectLastProcessedSuccessJobExecutionIdStr);
		}
		
		// Create the reader to fetch required records from DB
		JdbcPagingItemReader<BatchStagedIBGRejectTxn> jdbcPagingItemReader = new JdbcPagingItemReader<>();
        jdbcPagingItemReader.setDataSource(dataSource);
        jdbcPagingItemReader.setPageSize(jobConfigProperties.getJdbcPagingSize());

        String selectClause = "SELECT ID, USER_ID, AMOUNT, BENE_NAME, DATE, REJECT_DESCRIPTION";
        String fromClause = "FROM TBL_BATCH_STAGED_IBG_REJECT_TXN";
        String whereClause = "WHERE JOB_EXECUTION_ID=:jobExecutionId AND IS_NOTIFICATION_SENT=0 AND IS_PROCESSED=1";
        
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
        
        // The parameters used to replace in the SQL select paging query
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("jobExecutionId", ibgRejectLastProcessedSuccessJobExecutionId);
        
        // Setting the SQL paging select query, parameters used, and row mapper for the reader
        jdbcPagingItemReader.setQueryProvider(selectPagingQueryProvider);
        jdbcPagingItemReader.setParameterValues(parameterValues);
        jdbcPagingItemReader.setRowMapper(new BatchStagedIBGRejectTxnRowMapper());
        
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return jdbcPagingItemReader;
    }
	
	@Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchStagedIBGRejectTxn, BatchStagedIBGRejectTxn> batchStagedIBGRejectTxnItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		return new ItemProcessor<BatchStagedIBGRejectTxn, BatchStagedIBGRejectTxn>() {
			@Override
			public BatchStagedIBGRejectTxn process(BatchStagedIBGRejectTxn batchStagedIBGRejectTxn) throws Exception {
				logger.trace(String.format("BatchStagedIBGRejectTxn received by processor [%s]", batchStagedIBGRejectTxn));
				
				List<String> invalidColumns = getInvalidColumnNames(batchStagedIBGRejectTxn);
				if(invalidColumns != null) {
					try {
						String suspenseColumns = StringUtils.collectionToCommaDelimitedString(invalidColumns);
						String suspenseMessage = String.format(COLUMN_VALUES_EMPTY_OR_NULL_MESSAGE_TEMPLATE, suspenseColumns);
						String jobName = stepExecution.getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY);
						String ibgRejectLastProcessedSuccessJobExecutionId = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID);
						
						BatchSuspense batchSuspense = new BatchSuspense();
						batchSuspense.setBatchJobName(jobName);
						batchSuspense.setJobExecutionId(Long.parseLong(ibgRejectLastProcessedSuccessJobExecutionId));
						batchSuspense.setSuspenseColumn(suspenseColumns);
						batchSuspense.setSuspenseType(SuspenseType.ERROR.toString());
						batchSuspense.setSuspenseMessage(suspenseMessage);
						batchSuspense.setCreatedTime(new Date());
						
						String suspenseRecord = getSuspenseRecord(batchStagedIBGRejectTxn);
						batchSuspense.setSuspenseRecord(suspenseRecord);
						
						logger.trace(String.format("Invalid BatchStagedIBGRejectTxn [%s] found, BatchSuspense [%s] created to be insert to DB", batchStagedIBGRejectTxn, batchSuspense));
						batchSuspenseRepository.addBatchSuspenseToDB(batchSuspense);
					} catch (Exception e) {
						// If exception happened, don't stop the batch, just let it proceed until all record get processed
						String errorMessage = String.format("Error happened while process invalid BatchStagedIBGRejectTxn [%s]", batchStagedIBGRejectTxn);
						logger.error(errorMessage, e);
					}
					
					return null;
				} else {
					return batchStagedIBGRejectTxn;
				}
			} 
		};
	}
	
	private String getSuspenseRecord(BatchStagedIBGRejectTxn batchStagedIBGRejectTxn) {
		List<String> suspenseRecord = new ArrayList<>();
		suspenseRecord.add(StringUtils.isEmpty(batchStagedIBGRejectTxn.getDate()) ? "" : batchStagedIBGRejectTxn.getDate());
		suspenseRecord.add(StringUtils.isEmpty(batchStagedIBGRejectTxn.getTeller()) ? "" : batchStagedIBGRejectTxn.getTeller());
		suspenseRecord.add(StringUtils.isEmpty(batchStagedIBGRejectTxn.getTrace()) ? "" : batchStagedIBGRejectTxn.getTrace());
		suspenseRecord.add((batchStagedIBGRejectTxn.getUserId() == null) ? "" : batchStagedIBGRejectTxn.getUserId().toString());
		suspenseRecord.add(StringUtils.isEmpty(batchStagedIBGRejectTxn.getBeneName()) ? "" : batchStagedIBGRejectTxn.getBeneName());
		
		return StringUtils.collectionToDelimitedString(suspenseRecord, "|");
	}
	
	private List<String> getInvalidColumnNames(BatchStagedIBGRejectTxn batchStagedIBGRejectTxn) {
		List<String> invalidColumns = null;
		
		// Retrieving required info from the BatchStagedIBGRejectTxn object
		Integer userId = batchStagedIBGRejectTxn.getUserId();
		String amount = batchStagedIBGRejectTxn.getAmount();
		String date = batchStagedIBGRejectTxn.getDate();
		String beneName = batchStagedIBGRejectTxn.getBeneName();
		String rejectDescription = batchStagedIBGRejectTxn.getRejectDescription();
				
		// User ID is either null or have something, it won't be empty
		if(userId == null) {
			invalidColumns = new ArrayList<>();
			invalidColumns.add("USER_ID");
		}
		invalidColumns = addInvalidStringColumnToList(amount, "AMOUNT", invalidColumns);
		invalidColumns = addInvalidStringColumnToList(date, "DATE", invalidColumns);
		invalidColumns = addInvalidStringColumnToList(beneName, "BENE_NAME", invalidColumns);
		invalidColumns = addInvalidStringColumnToList(rejectDescription, "REJECT_DESCRIPTION", invalidColumns);
		
		return invalidColumns;
	}
	
	private List<String> addInvalidStringColumnToList(String source, String columnName, List<String> invalidColumns) {
		if(source == null || source.trim().isEmpty()) {
			if(invalidColumns == null) {
				invalidColumns = new ArrayList<>();
			}
			invalidColumns.add(columnName);
		}
		
		return invalidColumns;
	}
	
	
	
	@Bean(STEP_NAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BatchStagedIBGRejectTxn> batchStagedIBGRejectTxnItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<BatchStagedIBGRejectTxn>() {
			@Override
			public void write(List<? extends BatchStagedIBGRejectTxn> batchStagedIBGRejectTxns) throws Exception {
				logger.info(String.format("Sending total [%d] JSON notification for BatchStagedIBGRejectTxns to JMS queue", batchStagedIBGRejectTxns.size()));
				
				for(BatchStagedIBGRejectTxn batchStagedIBGRejectTxn : batchStagedIBGRejectTxns) {
					try {
                        NotificationPayload notificationPayload = generateNotificationPayload(batchStagedIBGRejectTxn);
                        Capsule capsule = new Capsule();
                        capsule.setUserId(batchStagedIBGRejectTxn.getUserId());
                        capsule.setMessageId(UUID.randomUUID().toString());
                        capsule.setQuickLogin(false);
						capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_BACKOFFICE);
						capsule.setProperty(Constants.OPERATION_NAME, "BOBatchIBGRejectNotification");
                        GenericCapsule<NotificationPayload> genericCapsule = new GenericCapsule<>(notificationPayload, capsule);
                        sendCapsuleMessageToSmsJMS(genericCapsule.generateCapsuleWithMetadata());
                        logger.trace(String.format("Sending notification [%s] to JMS queue [%s]", genericCapsule, jobConfigProperties.getJmsQueue()));
						logger.trace(String.format("Updating BatchStagedIBGRejectTxn [%s] IS_NOTIFICATION_SENT to 1", batchStagedIBGRejectTxn));
						batchStagedIBGRejectTxnRespository.updateIsNotificationSent(batchStagedIBGRejectTxn);
					} catch (BatchException e) {
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						String errorMessage = String.format("Error happened while updating record IsNotificationSent status in DB for BatchStagedIBGRejectTxn [%s]", batchStagedIBGRejectTxn);
						logger.error(errorMessage, e);
					} catch (Exception e) {
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						String errorMessage = String.format("Error happened while pushing notification payload to JMS queue for BatchStagedIBGRejectTxn [%s]", batchStagedIBGRejectTxn);
						logger.error(errorMessage, e);
					}
				}
				
				String ibgRejectLastProcessedSuccessJobExecutionId = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID);
				stepExecution.getJobExecution().getExecutionContext().putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID, ibgRejectLastProcessedSuccessJobExecutionId);				
			}
		};
	}

	private NotificationPayload generateNotificationPayload(BatchStagedIBGRejectTxn batchStagedIBGRejectTxn) throws ParseException {
		Integer userId = batchStagedIBGRejectTxn.getUserId();
		String eventCode = jobConfigProperties.getEventCode();
		Instant now = Instant.now();
		String amount = batchStagedIBGRejectTxn.getAmount();
		String beneName = batchStagedIBGRejectTxn.getBeneName();
		String rejectDesc = batchStagedIBGRejectTxn.getRejectDescription();
		Date date = getDateFromString(batchStagedIBGRejectTxn.getDate(), General.COMMON_DATE_DATA_FORMAT);
		String txnTime = Long.toString( date.getTime());
		Map<String,String> data =new HashMap<String,String>();
		
		//need to convert the amount to 2 decimal points
		//example: 1650 --> 16.50
		String pattern = "0.00";
	    DecimalFormat decimalFormat = new DecimalFormat(pattern);
	    double amountInDouble = Double.parseDouble(amount);
	    amount = decimalFormat.format(amountInDouble/100);
	    
		data.put("amount", amount);
		data.put("toAccountName", beneName);
		data.put("txnTime", txnTime);
		data.put("description", rejectDesc);
		NotificationPayload notificationPayload = new NotificationPayload(userId,eventCode,now);
		notificationPayload.setData(data);

		return notificationPayload ;
	}

	@Override
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME).<BatchStagedIBGRejectTxn, BatchStagedIBGRejectTxn>chunk(jobConfigProperties.getChunkSize())
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .build();
	}

}
