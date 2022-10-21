package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUserMaintAutoAging;

@Component
public class BatchUserMaintAutoAgingRespositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchUserMaintAutoAgingRespositoryImpl.class);
	
	public int addBatchUserMaintAutoAgingToDB(BatchUserMaintAutoAging batchUserMaintAutoAging) throws BatchException {
        try {
        	String insertQuery = "INSERT INTO TBL_BATCH_USER_MAINT_AUTO_AGING "
        			+ "(JOB_EXECUTION_ID, USER_ID, NAME, EMAIL, DEPARTMENT, CURRENT_USER_STATUS_ID, NEW_USER_STATUS_ID, LAST_LOGIN_TIME, CREATED_TIME, UPDATED_TIME) "
        			+ "VALUES(?,?,?,?,?,?,?,?,?,?)";
        	
        	logger.trace(String.format("Inserting BatchUserMaintAutoAging [%s] to DB", batchUserMaintAutoAging));
        	Date currentDate = new Date();
        	
        	return getJdbcTemplate().update(insertQuery,
        			batchUserMaintAutoAging.getJobExecutionId(),
        			batchUserMaintAutoAging.getUserId(),
        			batchUserMaintAutoAging.getName(),
        			batchUserMaintAutoAging.getEmail(),
        			batchUserMaintAutoAging.getDepartment(),
        			batchUserMaintAutoAging.getCurrentUserStatus(),
        			batchUserMaintAutoAging.getNewUserStatus(),
        			batchUserMaintAutoAging.getLastLoginTime(),
        			currentDate,
        			currentDate);
        	
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_USER_MAINT_AUTO_AGING table with values [%s] ", batchUserMaintAutoAging);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
	
    public int updateIsProcessed(BatchUserMaintAutoAging batchUserMaintAutoAging) throws BatchException {
        try {        	
        	String currentDate = DateUtils.formatDateString(new Date(), "yyyy-MM-dd HH:mm:ss");
        	String updateSql = String.format("UPDATE TBL_BATCH_USER_MAINT_AUTO_AGING SET IS_PROCESSED=1, UPDATED_TIME='%s' WHERE USER_ID=?", currentDate);
        	
        	return getJdbcTemplate().update(updateSql, batchUserMaintAutoAging.getUserId());
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while updating record with ID [%s] in TBL_BATCH_USER_MAINT_AUTO_AGING column IS_PROCESSED value as 1 ", batchUserMaintAutoAging.getUserId());
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
	
}
