package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectTxn;

@Component
public class BatchStagedIBGRejectTxnRespositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchStagedIBGRejectTxnRespositoryImpl.class);
	
    public int updateIsNotificationSent(BatchStagedIBGRejectTxn batchStagedIBGRejectTxn) throws BatchException {
        try {     
        	String currentDate = DateUtils.formatDateString(new Date(), "yyyy-MM-dd HH:mm:ss");
        	String updateSql = String.format("UPDATE TBL_BATCH_STAGED_IBG_REJECT_TXN SET IS_NOTIFICATION_SENT=1, UPDATED_TIME='%s' WHERE ID=?", currentDate);
        	
        	return getJdbcTemplate().update(updateSql, batchStagedIBGRejectTxn.getId());
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while updating record with ID [%s] in TBL_BATCH_STAGED_IBG_REJECT_TXN column IS_NOTIFICATION_SENT value as 1 ", batchStagedIBGRejectTxn.getId());
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
	
}
