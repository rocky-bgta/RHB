package com.rhbgroup.dcp.bo.batch.job.repository;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import org.apache.log4j.Logger;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_SUCCESS;

@Lazy
@Component
public class BatchUnitTrustJobStatusControlRepositoryImpl extends BaseRepositoryImpl{
	private static final Logger logger = Logger.getLogger(BatchUnitTrustJobStatusControlRepositoryImpl.class);

	@SneakyThrows
	public int addRecord(BatchUnitTrustJobStatusControl utJobStatusControl) {
		int row=0;
		try {
			jdbcTemplate.setDataSource(dataSource);
			String sql="INSERT INTO TBL_BATCH_UT_JOB_STATUS_CONTROL  "+ 
				"(JOB_EXECUTION_ID,BATCH_PROCESS_DATE,BATCH_END_DATETIME,TARGET_DATASET,CREATED_BY,CREATED_TIME,UPDATED_BY,UPDATED_TIME)" +
				" VALUES " +
				"(?,?,?,?,?,?,?,?)";
			row = jdbcTemplate.update(sql, new Object[] {utJobStatusControl.getJobExecutionId(),utJobStatusControl.getBatchProcessDate(),utJobStatusControl.getBatchEndDatetime()
					,utJobStatusControl.getTargetDataset(),utJobStatusControl.getCreatedBy(),utJobStatusControl.getCreatedTime()
					,utJobStatusControl.getUpdatedBy(),utJobStatusControl.getUpdatedTime()});
			logger.debug(String.format("Add %s record into TBL_BATCH_UT_JOB_STATUS_CONTROL", row));
		}catch(Exception ex) {
			logger.error("Exception while adding record in TBL_BATCH_UT_JOB_STATUS_CONTROL", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE,ex );
		}
		return row;
	}

	@SneakyThrows
	public int updateJobStatus(BatchUnitTrustJobStatusControl utJobStatusControl) {
		int row=0;
		try {
			jdbcTemplate.setDataSource(dataSource);
			String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set STATUS=?, BATCH_END_DATETIME=?, UPDATED_BY=?, UPDATED_TIME=? WHERE JOB_EXECUTION_ID=?";
			row= jdbcTemplate.update(sql, new Object[] {utJobStatusControl.getStatus(), utJobStatusControl.getBatchEndDatetime()
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() , utJobStatusControl.getJobExecutionId()});
			logger.debug(String.format("updated %s row TBL_BATCH_UT_JOB_STATUS_CONTROL-STATUS=%s", row,utJobStatusControl.getStatus()));
		}catch(Exception ex) {
			logger.error("Exception while updating STATUS in TBL_BATCH_UT_JOB_STATUS_CONTROL", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE,ex );
		}
		return row;
	}

	@SneakyThrows
	public int updateTblCustomerStatus(BatchUnitTrustJobStatusControl utJobStatusControl) {
		int row=0;
		try {
			jdbcTemplate.setDataSource(dataSource);
			String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_CUSTOMER_STATUS=?, UPDATED_BY=?, UPDATED_TIME=? WHERE JOB_EXECUTION_ID=?";
			row= jdbcTemplate.update(sql, new Object[] {utJobStatusControl.getTblUtCustomerStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() , utJobStatusControl.getJobExecutionId()});
			logger.debug(String.format("updated %s row TBL_BATCH_UT_JOB_STATUS_CONTROL-TBL_UT_CUSTOMER_STATUS ", row));
		}catch(Exception ex) {
			logger.error("Exception while updating TBL_UT_CUSTOMER_STATUS in TBL_BATCH_UT_JOB_STATUS_CONTROL", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE,ex );
		}
		return row;
	}

	@SneakyThrows
	public int updateTblCustomerRelStatus(BatchUnitTrustJobStatusControl utJobStatusControl) {
		int row=0;
		try {
			jdbcTemplate.setDataSource(dataSource);
			String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_CUSTOMER_REL_STATUS=?, UPDATED_BY=?, UPDATED_TIME=? WHERE JOB_EXECUTION_ID=?";
			row= jdbcTemplate.update(sql, new Object[] {utJobStatusControl.getTblUtCustomerRelStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() , utJobStatusControl.getJobExecutionId()});
			logger.debug(String.format("updated %s row TBL_BATCH_UT_JOB_STATUS_CONTROL-TBL_UT_CUSTOMER_REL_STATUS ", row));
		}catch(Exception ex) {
			logger.error("Exception while updating TBL_UT_CUSTOMER_REL_STATUS in TBL_BATCH_UT_JOB_STATUS_CONTROL", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE,ex );
		}
		return row;
	}

	@SneakyThrows
	public int updateTblAccountStatus(BatchUnitTrustJobStatusControl utJobStatusControl) {
		int row=0;
		try {
			jdbcTemplate.setDataSource(dataSource);
			String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_ACCOUNT_STATUS=?, UPDATED_BY=?, UPDATED_TIME=? WHERE JOB_EXECUTION_ID=?";
			row= jdbcTemplate.update(sql, new Object[] {utJobStatusControl.getTblUtAccountStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() , utJobStatusControl.getJobExecutionId()});
			logger.debug(String.format("updated %s row TBL_BATCH_UT_JOB_STATUS_CONTROL-TBL_UT_ACCOUNT_STATUS ", row));
		}catch(Exception ex) {
			logger.error("Exception while updating TBL_UT_ACCOUNT_STATUS in TBL_BATCH_UT_JOB_STATUS_CONTROL", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE,ex );
		}
		return row;
	}

	@SneakyThrows
	public int updateTblAccountHldStatus(BatchUnitTrustJobStatusControl utJobStatusControl) {
		int row=0;
		try {
			jdbcTemplate.setDataSource(dataSource);
			String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_ACCOUNT_HOLDING_STATUS=?, UPDATED_BY=?, UPDATED_TIME=?"
					+" WHERE JOB_EXECUTION_ID=?";
			row= jdbcTemplate.update(sql, new Object[] {utJobStatusControl.getTblUtAccountHoldingStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() , utJobStatusControl.getJobExecutionId()});
			logger.debug(String.format("updated %s row TBL_BATCH_UT_JOB_STATUS_CONTROL-TBL_UT_ACCOUNT_HOLDING_STATUS ", row));
		}catch(Exception ex) {
			logger.error("Exception while updating TBL_UT_ACCOUNT_HOLDING_STATUS in TBL_BATCH_UT_JOB_STATUS_CONTROL", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE,ex );
		}
		return row;
	}

	@SneakyThrows
	public int updateTblFundMasterStatus(BatchUnitTrustJobStatusControl utJobStatusControl) {
		int row=0;
		try {
			jdbcTemplate.setDataSource(dataSource);
			String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set tbl_ut_fund_master_status=?, UPDATED_BY=?, UPDATED_TIME=?"
					+" WHERE JOB_EXECUTION_ID=?";
			row= jdbcTemplate.update(sql, new Object[] {utJobStatusControl.getTblUtFundMasterStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() , utJobStatusControl.getJobExecutionId()});
			logger.debug(String.format("updated %s row TBL_BATCH_UT_JOB_STATUS_CONTROL-tbl_ut_fund_master_status ", row));
		}catch(Exception ex) {
			logger.error("Exception while updating tbl_ut_fund_master_status in TBL_BATCH_UT_JOB_STATUS_CONTROL", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE,ex );
		}
		return row;
	}

	@SneakyThrows
	public int getTargetDataSet() {
		int targetSet=0;
		try {
			String sql="SELECT TARGET_DATASET FROM TBL_BATCH_UT_JOB_STATUS_CONTROL "+
					" WHERE ID=(SELECT MAX(ID) FROM TBL_BATCH_UT_JOB_STATUS_CONTROL WHERE STATUS=?)";
			jdbcTemplate.setDataSource(dataSource);
			targetSet= jdbcTemplate.queryForObject(sql, new Object[] {STATUS_SUCCESS}, Integer.class);
		}catch(EmptyResultDataAccessException ex) {
			logger.info("No successful data target set inserted");
			targetSet=0;
		}catch(Exception ex) {
			logger.error("Exception while getting target data set", ex);
			throw ex;
		}
		return targetSet;
	}

	@SneakyThrows
	public int updateCompleteJobStatus(BatchUnitTrustJobStatusControl utJobStatusControl) {
		int row=0;
		try {
			jdbcTemplate.setDataSource(dataSource);
			String sql="update TBL_BATCH_UT_JOB_STATUS_CONTROL" + 
					" set STATUS=?, BATCH_END_DATETIME=?, UPDATED_BY=?, UPDATED_TIME=?" + 
					" where JOB_EXECUTION_ID=?" + 
					" and TBL_UT_CUSTOMER_STATUS=?" + 
					" and TBL_UT_CUSTOMER_REL_STATUS=?" + 
					" and TBL_UT_ACCOUNT_STATUS=?" + 
					" and TBL_UT_ACCOUNT_HOLDING_STATUS=?" + 
					" and TBL_UT_FUND_MASTER_STATUS=?";
			row= jdbcTemplate.update(sql, new Object[] {utJobStatusControl.getStatus(), utJobStatusControl.getBatchEndDatetime()
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() 
				, utJobStatusControl.getJobExecutionId()
				, utJobStatusControl.getTblUtCustomerStatus(), utJobStatusControl.getTblUtCustomerRelStatus()
				, utJobStatusControl.getTblUtAccountStatus(), utJobStatusControl.getTblUtAccountHoldingStatus()
				, utJobStatusControl.getTblUtFundMasterStatus()});
			logger.info(String.format("updated %s row TBL_BATCH_UT_JOB_STATUS_CONTROL-FINAL STATUS=%s", row,utJobStatusControl.getStatus()));
		}catch(Exception ex) {
			logger.error("Exception while updating STATUS in TBL_BATCH_UT_JOB_STATUS_CONTROL", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE,ex );
		}
		return row;
	}
}
