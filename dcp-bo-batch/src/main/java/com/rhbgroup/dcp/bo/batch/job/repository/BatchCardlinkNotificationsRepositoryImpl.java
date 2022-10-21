package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Date;

import javax.ws.rs.ext.ParamConverter.Lazy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotification;
import com.rhbgroup.dcp.bo.batch.job.model.CardlinkNotification;

@Component
@Lazy
public class BatchCardlinkNotificationsRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchCardlinkNotificationsRepositoryImpl.class);
	
    public int addIntoNotificationsStaging(long jobExecutionId, String processedDate, String batchCode, CardlinkNotification cardLinkNotification) throws BatchException {
    	
		int rows=0;
        try {
			rows = getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_NOTIFICATION (job_execution_id,file_name,process_date,event_code,key_type,user_id,data_1,data_2,data_3,data_4,data_5,data_6,data_7,data_8,data_9,is_processed,created_time,created_by,updated_time,updated_by) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                    ,  		jobExecutionId,
                    		cardLinkNotification.getFileName(),
                    		processedDate,
                            cardLinkNotification.getEventCode(),
                            cardLinkNotification.getKeyType(),
                            cardLinkNotification.getUserId(),
                            cardLinkNotification.getSystemDate(), //data1
                            cardLinkNotification.getSystemTime(),
                            cardLinkNotification.getCardNumber(),
                            cardLinkNotification.getPaymentDueDate(),
                            cardLinkNotification.getCardType(),
                            cardLinkNotification.getMinimumAmount(),
                            cardLinkNotification.getOutstandingAmount(),
                            cardLinkNotification.getStatementAmount(),
                            cardLinkNotification.getStatementDate(),                            
                            false,
                            new Date(),
                            batchCode,
                            new Date(),
                            batchCode);
			logger.debug(String.format("row affected=%s", rows));
			
        } catch (Exception e) {
        	String errorMessage = String.format("Unable to insert new row into TBL_BATCH_STAGED_NOTIFICATION - content : [%s]", cardLinkNotification);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
        return rows;
    }
    
    public int updateIsProcessed(String batchCode, BatchStagedNotification batchStagedNotification) throws BatchException {
        try {        	
        	String updateSql = "UPDATE TBL_BATCH_STAGED_NOTIFICATION SET IS_PROCESSED=1, UPDATED_TIME=?, UPDATED_BY=?, JOB_EXECUTION_ID=? WHERE ID=?";
        	
        	return getJdbcTemplate().update(updateSql, 
        		new Date(),
        		batchCode,
        		batchStagedNotification.getJobExecutionId(),
        		batchStagedNotification.getId());
        	
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while updating record with ID [%s] in TBL_BATCH_STAGED_NOTIFICATION column IS_PROCESSED value as 1 ", batchStagedNotification.getId());
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
    
    public Integer updateIsProcessed(Long jobExecutionId, String batchCode, CardlinkNotification cardLinkNotification) throws BatchException {
        try {        	
        	String updateSql = "UPDATE TBL_BATCH_STAGED_NOTIFICATION_RAW SET IS_PROCESSED=1, UPDATED_TIME=?, UPDATED_BY=?, JOB_EXECUTION_ID=? WHERE ID=?";
        	String currentDate = DateUtils.formatDateString(new Date(), "yyyy-MM-dd HH:mm:ss");
        	
        	return getJdbcTemplate().update(updateSql, 
        		currentDate,
        		batchCode,
        		jobExecutionId,
        		cardLinkNotification.getNotificationRawId());
        	
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while updating record with ID [%s] in TBL_BATCH_STAGED_NOTIFICATION_RAW column IS_PROCESSED value as 1 ", cardLinkNotification.getNotificationRawId());
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
    
    
}
