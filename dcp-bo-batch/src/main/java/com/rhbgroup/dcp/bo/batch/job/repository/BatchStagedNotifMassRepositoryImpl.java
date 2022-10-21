package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotifMass;

@Component
@Lazy
public class BatchStagedNotifMassRepositoryImpl extends BaseRepositoryImpl {
	private static final Logger logger = Logger.getLogger(BatchStagedNotifMassRepositoryImpl.class);
	
	public int addRecordBatchStagedNotifMass(BatchStagedNotifMass batchStagedNotifMass) throws BatchException {
		int row=0;
		try {
			String sql = "INSERT INTO TBL_BATCH_STAGED_NOTIF_MASS"+
					"(job_execution_id, file_name, event_code, content, user_id, is_processed, created_time, created_by, updated_time, updated_by)" +
					" VALUES " +
					"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			getJdbcTemplate().setDataSource(dataSource);
			row = getJdbcTemplate().update(sql,
					new Object[] { batchStagedNotifMass.getJobExecutionId(), batchStagedNotifMass.getFileName(),
							batchStagedNotifMass.getEventCode(), batchStagedNotifMass.getContent(),
							batchStagedNotifMass.getUserId(), batchStagedNotifMass.isProcessed(),
							batchStagedNotifMass.getCreatedTime(), batchStagedNotifMass.getCreatedBy(),
							batchStagedNotifMass.getUpdatedTime(), batchStagedNotifMass.getUpdatedBy() });
			logger.info( String.format( "Add [%s] record into TBL_BATCH_STAGED_NOTIF_MASS", row));
		}catch(Exception ex) {
			logger.error(String.format("Exception Adding record into TBL_BATCH_STAGED_NOTIF_MASS, [%s] ", ex));
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, ex);
		}
		return row;
	}
	
    public int updateIsProcessed(String batchCode, BatchStagedNotifMass batchStagedNotifMass) throws BatchException {
        try {        	
        	String updateSql = "UPDATE TBL_BATCH_STAGED_NOTIF_MASS SET IS_PROCESSED=1, UPDATED_TIME=?, UPDATED_BY=?, JOB_EXECUTION_ID=? WHERE ID=?";
			getJdbcTemplate().setDataSource(dataSource);
        	return getJdbcTemplate().update(updateSql, 
        		new Date(),
        		batchCode,
        		batchStagedNotifMass.getJobExecutionId(),
        		batchStagedNotifMass.getId());
        	
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while updating record with ID [%s] in TBL_BATCH_STAGED_NOTIF_MASS column IS_PROCESSED value as 1 ", batchStagedNotifMass.getId());
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
    
}
