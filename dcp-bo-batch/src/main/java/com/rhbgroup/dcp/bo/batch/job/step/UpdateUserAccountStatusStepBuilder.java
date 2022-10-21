package com.rhbgroup.dcp.bo.batch.job.step;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseJMSStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.enums.UserStatus;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.UserMaintenanceAutoAgingJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.enums.SuspenseType;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUserMaintAutoAging;
import com.rhbgroup.dcp.bo.batch.job.model.BoConfigGeneric;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUserMaintAutoAgingRespositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BoConfigGenericRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BoUserRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BatchUserMaintAutoAgingRowMapper;

@Component
@Lazy
public class UpdateUserAccountStatusStepBuilder extends BaseJMSStepBuilder {

private static final Logger logger = Logger.getLogger(UpdateUserAccountStatusStepBuilder.class);
	
	private static final String STEP_NAME = "UpdateUserAccountStatusStep";
	
	private static final String NOTIFICATION_JSON_TEMPLATE = "{\"eventCode\":\"%s\",\"userId\":%d,\"status\":\"%s\",\"statusDescription\":\"%s\",\"timestamp\":\"%s\",\"ip\":\"%s\",\"request\":{\"tableBatchAutoAging\":{\"userId\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"department\":\"%s\",\"current_user_status\":\"%s\",\"new_user_status\":\"%s\",\"last_login_time\":\"%s\"}},\"response\":{%s},\"additionalData\":{\"staffId\":\"%s\"}}";
	private static final String JSON_SUCCESS_RESPONSE = "\"status\":\"Status successfully updated\"";
	private static final String JSON_ERROR_RESPONSE = "\"error\":{\"suspense_type\":\"EXCEPTION\",\"suspense_message\":\"%s\"}";
	
	private static final String JOB_ID = "LDCPD5003B";
	
	private static final String BATCH_SUSPENSE_TABLE_NAME = "TBL_BATCH_SUSPENSE";
	
	private static final String AUDIT_MESSAGE_SUCCESS_STATUS_CODE = "10000";
	private static final String AUDIT_MESSAGE_UNSUCCESS_STATUS_CODE = "80000";
	private static final String AUDIT_MESSAGE_SUCCESS_DESCRIPTION = "Success";
	private static final String AUDIT_MESSAGE_UNSUCCESS_DESCRIPTION = "Error";
	
	@Autowired
	private UserMaintenanceAutoAgingJobConfigProperties jobConfigProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchUserMaintAutoAging> itemReader;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<BatchUserMaintAutoAging, BatchUserMaintAutoAging> itemProcessor;
    
	@Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<BatchUserMaintAutoAging> itemWriter;
	
	@Autowired
	private BatchSuspenseRepositoryImpl batchSuspenseRepository;
	
	@Autowired
	private BatchUserMaintAutoAgingRespositoryImpl batchUserMaintAutoAgingRespository;
	
	@Autowired
	private BoUserRepositoryImpl boUserRepository;
	
	@Autowired
	private BoConfigGenericRepositoryImpl boConfigGenericRepository;

	private String auditMessageUserId =null;

	@Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<BatchUserMaintAutoAging> batchUserMaintAutoAgingItemReader(@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) throws BatchException {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		auditMessageUserId =boUserRepository.getUserID(jobConfigProperties.getAuditMessageUsername());

		int dayToInactiveUser = 0;
		int dayToDeleteUser = 0;
		
		List<BoConfigGeneric> boConfigGenerics = boConfigGenericRepository.getUserStatusDaysConfig();
		logger.debug(String.format("BoConfigGenerics retrieved from DB [%s]", boConfigGenerics));
		for(BoConfigGeneric boConfigGeneric : boConfigGenerics) {
			if(boConfigGeneric.getConfigCode().equals(BatchJobParameter.BATCH_JOB_PARAMETER_DB_DAYS_TO_INACTIVE_USER)) {
				dayToInactiveUser = Integer.parseInt(boConfigGeneric.getConfigDesc());
				stepExecution.getJobExecution().getExecutionContext().putInt(BatchJobParameter.BATCH_JOB_PARAMETER_DB_DAYS_TO_INACTIVE_USER, dayToInactiveUser);
			} else if(boConfigGeneric.getConfigCode().equals(BatchJobParameter.BATCH_JOB_PARAMETER_DB_DAYS_TO_DELETE_USER)) {
				dayToDeleteUser = Integer.parseInt(boConfigGeneric.getConfigDesc());
				stepExecution.getJobExecution().getExecutionContext().putInt(BatchJobParameter.BATCH_JOB_PARAMETER_DB_DAYS_TO_DELETE_USER, dayToDeleteUser);
			}
		}
		
		// Create the reader to fetch required records from DB
		JdbcPagingItemReader<BatchUserMaintAutoAging> jdbcPagingItemReader = new JdbcPagingItemReader<>();
        jdbcPagingItemReader.setDataSource(dataSource);
        jdbcPagingItemReader.setPageSize(jobConfigProperties.getJdbcPagingSize());
        
        String selectClause = "SELECT a.ID USER_ID, a.USERNAME, a.NAME, a.EMAIL, a.USER_DEPARTMENT_ID, b.CONFIG_CODE DEPARTMENT, a.USER_STATUS_ID, a.LAST_LOGIN_TIME, DATEDIFF(dd, a.LAST_LOGIN_TIME, :currentDate) LAST_LOGIN_TIME_DIFF";
        String fromClause = "TBL_BO_USER a, TBL_BO_CONFIG_GENERIC b";
        // The inactive breach limit will be used as the minimum filtering here
        String whereClause = String.format("WHERE a.USER_DEPARTMENT_ID = b.id AND b.CONFIG_TYPE = 'user_department' AND a.USER_STATUS_ID IN ('%s','%s') AND DATEDIFF(dd, a.LAST_LOGIN_TIME, :currentDate) > %d", UserStatus.ACTIVE.getStatus(), UserStatus.INACTIVE.getStatus(), dayToInactiveUser);
        
        // SQL select paging query
        SqlServerPagingQueryProvider selectPagingQueryProvider = new SqlServerPagingQueryProvider();
        selectPagingQueryProvider.setSelectClause(selectClause);
        selectPagingQueryProvider.setFromClause(fromClause);
        selectPagingQueryProvider.setWhereClause(whereClause);
        logger.debug(String.format("SQL select paging query [%s %s %s]", selectClause, fromClause, whereClause));
        
        // Sorting keys using in SQL select paging query
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("USER_ID", Order.ASCENDING);
        selectPagingQueryProvider.setSortKeys(sortKeys);
        
        // The parameters used to replace in the SQL select paging query
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("currentDate", new Date());
        
        // Setting the SQL paging select query, parameters used, and row mapper for the reader
        jdbcPagingItemReader.setQueryProvider(selectPagingQueryProvider);
        jdbcPagingItemReader.setParameterValues(parameterValues);
        jdbcPagingItemReader.setRowMapper(new BatchUserMaintAutoAgingRowMapper());
        
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return jdbcPagingItemReader;
    }
	
	@Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchUserMaintAutoAging, BatchUserMaintAutoAging> batchUserMaintAutoAgingItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		return new ItemProcessor<BatchUserMaintAutoAging, BatchUserMaintAutoAging>() {
			@Override
			public BatchUserMaintAutoAging process(BatchUserMaintAutoAging batchUserMaintAutoAging) throws Exception {
				logger.trace(String.format("BatchUserMaintAutoAging received by processor [%s]", batchUserMaintAutoAging));
				// We are assuming the user status breach limit must not be empty in the DB config table
				int daysToDeleteUser = stepExecution.getJobExecution().getExecutionContext().getInt(BatchJobParameter.BATCH_JOB_PARAMETER_DB_DAYS_TO_DELETE_USER);
				
				// Based on ItemReader, the status can only be either Active or Inactive
				String currentUserStatus = batchUserMaintAutoAging.getCurrentUserStatus();
				int lastLoginTimeDiff = batchUserMaintAutoAging.getLastLoginTimeDayDiff();
				
				// Proceed if user current status is Active
				if(currentUserStatus.equals(UserStatus.ACTIVE.getStatus())) {
					if(lastLoginTimeDiff > daysToDeleteUser) {
						// Sometime the user status might not be review and updated accordingly, we need to check the worst scenario in this case
						batchUserMaintAutoAging.setNewUserStatus(UserStatus.DELETED.getStatus());
					} else {
						batchUserMaintAutoAging.setNewUserStatus(UserStatus.INACTIVE.getStatus());
					}
				// Proceed if user current status is Inactive
				} else {
					if(lastLoginTimeDiff > daysToDeleteUser) {
						batchUserMaintAutoAging.setNewUserStatus(UserStatus.DELETED.getStatus());
					} else {
						logger.trace(String.format("BatchUserMaintAutoAging status remain unchange [%s]", batchUserMaintAutoAging));
						return null;
					}
				}
				
				return batchUserMaintAutoAging;
			} 
		};
	}
	
	@Bean(STEP_NAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BatchUserMaintAutoAging> batchUserMaintAutoAgingItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<BatchUserMaintAutoAging>() {
			@Override
			public void write(List<? extends BatchUserMaintAutoAging> batchUserMaintAutoAgings) throws Exception {
				logger.info(String.format("Sending total [%d] JSON notification for BatchUserMaintAutoAgings to JMS queue", batchUserMaintAutoAgings.size()));
				int jobExecutionId = stepExecution.getJobExecution().getId().intValue();
				
				for(BatchUserMaintAutoAging batchUserMaintAutoAging : batchUserMaintAutoAgings) {
					int updatedCount = 0;
					BatchException batchException = null;
					boolean isSuccess = false;
					
					try {
						batchUserMaintAutoAging.setJobExecutionId(jobExecutionId);
						
						// First insert the record to the DB table
						logger.trace(String.format("Inserting BatchUserMaintAutoAging [%s] to DB", batchUserMaintAutoAging));
						batchUserMaintAutoAgingRespository.addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging); 
						
						// Update the user status in DB as well as sending an audit JMS message
						updatedCount = updateUserStatusInDB(batchUserMaintAutoAging);
						isSuccess = updatedCount > 0;
						logger.trace(String.format("BatchUserMaintAutoAging [%s] new user status updated count? [%s]", batchUserMaintAutoAging, updatedCount));
						
						if(isSuccess) {
							// Once the DB record updated successfully, we update the record IsProcessed status
							logger.trace(String.format("Updating BatchUserMaintAutoAging [%s] IS_PROCESSED to 1", batchUserMaintAutoAging));
							batchUserMaintAutoAgingRespository.updateIsProcessed(batchUserMaintAutoAging);
						}
						// Reset set it for next record
						batchException = null;
					} catch (BatchException e) {
						batchException = e;
						String errorMessage = String.format("Error happened during interaction with DB for BatchUserMaintAutoAging [%s]", batchUserMaintAutoAging);
						logger.error(errorMessage, e);
						insertBatchSuspenseToDB(batchUserMaintAutoAging, e.getMessage(), stepExecution);
					}
					
					sendAuditJMSMessage(batchUserMaintAutoAging, isSuccess, batchException, stepExecution);
				}
				
				stepExecution.getJobExecution().getExecutionContext().putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID, Integer.toString(jobExecutionId));				
			}
		};
	}
	
	private int updateUserStatusInDB(BatchUserMaintAutoAging batchUserMaintAutoAging) throws BatchException {
		// Update the user status
		logger.trace(String.format("Updating BatchUserMaintAutoAging [%s] with new user status", batchUserMaintAutoAging));
		return boUserRepository.updateUserStatus(batchUserMaintAutoAging, JOB_ID);
	}
		
	private void sendAuditJMSMessage(BatchUserMaintAutoAging batchUserMaintAutoAging, boolean isSuccess, BatchException be, StepExecution stepExecution) {
		try {
			// We only will send audit JMS if we at least try to update the user status in the DB
			String jsonNotification = generateJSON(batchUserMaintAutoAging, isSuccess, be);
			logger.trace(String.format("Sending JSON notification [%s] to JMS queue [%s]", jsonNotification, jobConfigProperties.getJmsQueue()));
			sendMessageToAuditJMS(jsonNotification);
		} catch(Exception e) {
			// If exception happened, don't stop the batch, just proceed to next record until all complete processed
			String errorMessage = String.format("Error happened while pushing JSON notification to JMS queue for BatchUserMaintAutoAging [%s]", batchUserMaintAutoAging);
			logger.error(errorMessage, e);
			insertBatchSuspenseToDB(batchUserMaintAutoAging, e.getMessage(), stepExecution);
		}
	}
	
	private String generateJSON(BatchUserMaintAutoAging batchUserMaintAutoAging, boolean isSuccess, BatchException e) throws UnknownHostException {
		String currentDate = DateUtils.formatDateString(new Date(), JSON_DATE_FORMAT);
		// Take the original DB exception if possible else fall back to batch exception info
		String exceptionMessage = null;
		if(e != null) {
			exceptionMessage = (e.getCause() != null && e.getCause().getMessage() != null) ? e.getCause().getMessage() : e.getMessage();
		}
		
		return String.format(NOTIFICATION_JSON_TEMPLATE,
			jobConfigProperties.getEventCode(),
			Integer.parseInt(auditMessageUserId),
			(isSuccess) ? AUDIT_MESSAGE_SUCCESS_STATUS_CODE : AUDIT_MESSAGE_UNSUCCESS_STATUS_CODE,
			(isSuccess) ? AUDIT_MESSAGE_SUCCESS_DESCRIPTION : AUDIT_MESSAGE_UNSUCCESS_DESCRIPTION,
			currentDate,
			InetAddress.getLocalHost().getHostAddress(),
			batchUserMaintAutoAging.getUserId(),
			batchUserMaintAutoAging.getUserName(),
			batchUserMaintAutoAging.getEmail(),
			batchUserMaintAutoAging.getUserDepartmentId(),
			batchUserMaintAutoAging.getCurrentUserStatus(),
			batchUserMaintAutoAging.getNewUserStatus(),
			DateUtils.formatDateString(batchUserMaintAutoAging.getLastLoginTime(), JSON_DATE_FORMAT),
			(isSuccess) ? JSON_SUCCESS_RESPONSE : String.format(JSON_ERROR_RESPONSE, exceptionMessage),
			batchUserMaintAutoAging.getUserId()
		);
	}
	
	private void insertBatchSuspenseToDB(BatchUserMaintAutoAging batchUserMaintAutoAging, String suspenseMessage, StepExecution stepExecution) {
		String jobName = stepExecution.getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY);
		int jobExecutionId = stepExecution.getJobExecution().getId().intValue();
		
		BatchSuspense batchSuspense = new BatchSuspense();
		batchSuspense.setBatchJobName(jobName);
		batchSuspense.setJobExecutionId(jobExecutionId);
		batchSuspense.setSuspenseColumn("N/A");
		batchSuspense.setSuspenseType(SuspenseType.EXCEPTION.toString());
		batchSuspense.setSuspenseMessage(suspenseMessage);
		batchSuspense.setCreatedTime(new Date());
		
		String suspenseRecord = String.format("%s|%s", batchUserMaintAutoAging.getUserId(), batchUserMaintAutoAging.getUserName());
		batchSuspense.setSuspenseRecord(suspenseRecord);
		
		try {
			logger.trace(String.format("Invalid BatchUserMaintAutoAging [%s] found, BatchSuspense [%s] created to be insert to DB", batchUserMaintAutoAging, batchSuspense));
			batchSuspenseRepository.addBatchSuspenseToDB(batchSuspense);
		} catch(BatchException e2) {
			// If exception happened, don't stop the batch, just let it proceed until all record get processed
			String errorMessage2 = String.format("Error happened while inserting to record to [%s] for BatchUserMaintAutoAging [%s]", BATCH_SUSPENSE_TABLE_NAME, batchUserMaintAutoAging);
			logger.error(errorMessage2, e2);
		}
	}
	
	@Override
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME).<BatchUserMaintAutoAging, BatchUserMaintAutoAging>chunk(jobConfigProperties.getChunkSize())
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .build();
	}

}
