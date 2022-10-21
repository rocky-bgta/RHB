package com.rhbgroup.dcp.bo.batch.job.repository;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedJompayFailureTxn;

@Component
public class BatchStagedJompayFailureTxnRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchStagedJompayFailureTxnRepositoryImpl.class);
	
	private static final String REQUEST_TIME_FORMAT = "yyMMddHHmmss";
	
	public int addBatchStagedIBKPaymentTxnToStaging(BatchStagedJompayFailureTxn batchStagedJompayFailureTxn) throws BatchException {
        try {
        	String insertSql = "INSERT INTO TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN "
        		+ "(BILLER_CODE, PAYMENT_CHANNEL, REQUEST_TIME, REASON_FOR_FAILURE, FILE_NAME, CREATED_TIME) "
        		+ "VALUES(?,?,?,?,?,?)";
        	
        	return getJdbcTemplate().update(insertSql,
    			batchStagedJompayFailureTxn.getBillerCode(),
    			batchStagedJompayFailureTxn.getPaymentChannel(),
    			DateUtils.getDateFromString(batchStagedJompayFailureTxn.getRequestTimeStr(), REQUEST_TIME_FORMAT),
    			batchStagedJompayFailureTxn.getReasonForFailure(),
    			batchStagedJompayFailureTxn.getFileName(),
    			batchStagedJompayFailureTxn.getCreatedTime());
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN table with values [%s] ", batchStagedJompayFailureTxn.toString());
            logger.error(errorMessage, e);
            throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
	
	public int deleteExistingBatchStagedJompayFailureTxns(String filename) throws BatchException {
		try {
			String deleteSql = "DELETE FROM TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN WHERE FILE_NAME = ?";
			return getJdbcTemplate().update(deleteSql, filename);
		} catch (Exception e) {
        	String errorMessage = String.format("Error happened while deleting existing records from TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN table where FILE_NAME is [%s] ", filename);
            logger.error(errorMessage, e);
            throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
		
	}
}
