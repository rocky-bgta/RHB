package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BatchSuspenseRowMapper;

@Component
public class BatchSuspenseRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchSuspenseRepositoryImpl.class);
	
    public int addBatchSuspenseToDB(BatchSuspense batchSuspense) throws BatchException {
        try {
        	String insertQuery = "INSERT INTO TBL_BATCH_SUSPENSE "
        			+ "(JOB_EXECUTION_ID, BATCH_JOB_NAME, CREATED_TIME, SUSPENSE_COLUMN, SUSPENSE_TYPE, SUSPENSE_MESSAGE, SUSPENSE_RECORD) "
        			+ "VALUES(?,?,?,?,?,?,?)";
        	
        	logger.trace(String.format("Inserting BatchSuspense [%s] to DB", batchSuspense));
        	return getJdbcTemplate().update(insertQuery,
        			batchSuspense.getJobExecutionId(),
        			batchSuspense.getBatchJobName(),
        			batchSuspense.getCreatedTime(),
        			batchSuspense.getSuspenseColumn(),
        			batchSuspense.getSuspenseType(),
        			batchSuspense.getSuspenseMessage(),
        			batchSuspense.getSuspenseRecord());
        	
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_SUSPENSE table with values [%s] ", batchSuspense);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
    
    public List<BatchSuspense> getByJobNameAndJobExecutionId(String jobName, String jobExecutionId, int maxLimit) throws BatchException {
    	try {
			String selectQuery = "SELECT JOB_EXECUTION_ID, BATCH_JOB_NAME, ID, CREATED_TIME, SUSPENSE_COLUMN, SUSPENSE_TYPE, SUSPENSE_MESSAGE, SUSPENSE_RECORD FROM TBL_BATCH_SUSPENSE"
	    		+ " WHERE BATCH_JOB_NAME=? AND JOB_EXECUTION_ID=?";
	
			logger.debug(String.format("Getting BatchSuspenses from DB using SQL [%s] JobName [%s] JobExecutonId [%s] MaxLimit [%d]", selectQuery, jobName, jobExecutionId, maxLimit));
			JdbcTemplate jdbcTemplate = getJdbcTemplate();
			jdbcTemplate.setMaxRows(maxLimit);
			List<BatchSuspense> batchSuspenses = jdbcTemplate.query(selectQuery, new BatchSuspenseRowMapper(), jobName, jobExecutionId);
			
	    	logger.trace(String.format("BatchSuspenses retrieved successfully from DB [%s]", batchSuspenses));
	    	return batchSuspenses;   
    	} catch (Exception e) {
        	String errorMessage = String.format("Error happened while getting BatchSuspenses from DB using JobName [%s] JobExecutonId [%s] MaxLimit [%d]", jobName, jobExecutionId, maxLimit);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
}