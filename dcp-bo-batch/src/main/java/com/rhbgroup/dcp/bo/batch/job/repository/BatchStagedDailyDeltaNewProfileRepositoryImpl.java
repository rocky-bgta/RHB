package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Date;

import javax.ws.rs.ext.ParamConverter.Lazy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDailyDeltaNewProfile;

@Component
@Lazy
public class BatchStagedDailyDeltaNewProfileRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchStagedDailyDeltaNewProfileRepositoryImpl.class);
	
    public int updateIsProcessed(BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile) throws BatchException {
        try {        	
        	String updateSql = "UPDATE TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE SET IS_PROCESSED=1, UPDATED_TIME=?, JOB_EXECUTION_ID=? WHERE ID=?";
        	String currentDate = DateUtils.formatDateString(new Date(), "yyyy-MM-dd HH:mm:ss");
        	
        	return getJdbcTemplate().update(updateSql, 
        		currentDate,
        		batchStagedDailyDeltaNewProfile.getJobExecutionId(),
        		batchStagedDailyDeltaNewProfile.getId());
        	
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while updating record with ID [%s] in TBL_BATCH_STAGED_DAILY_DELTA_NEW_PROFILE coumn IS_PROCESSED value as 1 ", batchStagedDailyDeltaNewProfile.getId());
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
	
}
