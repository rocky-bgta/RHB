package com.rhbgroup.dcp.bo.batch.job.repository;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnDetail;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class BatchStagedIBKPaymentTxnRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchStagedIBKPaymentTxnRepositoryImpl.class);

	private static final String INSERT_STAGED_IBK_PAYMENT_TXN_SQL = "INSERT INTO TBL_BATCH_STAGED_IBK_PAYMENT_TXN "
			+ "(JOB_EXECUTION_ID, PROCESS_DATE, BILLER_ACCOUNT_NO, BILLER_ACCOUNT_NAME, BILLER_CODE, TXN_ID, " +
			"TXN_DATE, TXN_AMOUNT, TXN_TYPE, TXN_DESCRIPTION, BILLER_REF_NO1, BILLER_REF_NO2, BILLER_REF_NO3, " +
			"TXN_TIME, FILE_NAME, CREATED_TIME ," +
			" BILLER_REF_NO4,ID_NO, POLICY_NO, USER_ADDRESS1,USER_ADDRESS2,USER_ADDRESS3,USER_ADDRESS4,USER_STATE,USER_CITY," +
			"USER_POSTCODE , USER_COUNTRY ) "
			+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, " +
			"?,?,?,?,?,?,?,?,?,?,?)";

	private static final String INSERT_STAGED_IBK_PAYMENT_RPT_SQL = "INSERT INTO TBL_BATCH_STAGED_IBK_PAYMENT_RPT "
			+ "(JOB_EXECUTION_ID, PROCESS_DATE, BILLER_CODE, TXN_ID, " +
			"TXN_DATE, TXN_AMOUNT, BILLER_REF_NO1, BILLER_REF_NO2, " +
			"TXN_TIME, FILE_NAME, CREATED_TIME, " +
			"USERNAME, LINE_NO) "
			+ "VALUES(" +
			"?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String DELETE_STAGED_IBK_PAYMENT_TXN_SQL = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE FILE_NAME = ?";

	private static final String DELETE_STAGED_IBK_PAYMENT_RPT_SQL = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_RPT WHERE FILE_NAME = ?";

	private static final String UPDATE_STAGED_IBK_PAYMENT_RPT_SQL = "UPDATE TBL_BATCH_STAGED_IBK_PAYMENT_RPT "
			+ "SET USERNAME = ? " +
			"WHERE JOB_EXECUTION_ID =? AND PROCESS_DATE =? AND FILE_NAME = ? AND LINE_NO = ?";

	public int addBatchStagedIBKPaymentTxnToStaging(BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail) throws BatchException {
		try {

			return getJdbcTemplate().update(INSERT_STAGED_IBK_PAYMENT_TXN_SQL,
					batchStagedIBKPaymentTxnDetail.getJobExecutionId(),
					batchStagedIBKPaymentTxnDetail.getProcessDate(),
					batchStagedIBKPaymentTxnDetail.getBillerAccountNo(),
					batchStagedIBKPaymentTxnDetail.getBillerAccountName(),
					batchStagedIBKPaymentTxnDetail.getBillerCode(),
					batchStagedIBKPaymentTxnDetail.getTxnId(),
					batchStagedIBKPaymentTxnDetail.getTxnDate(),
					batchStagedIBKPaymentTxnDetail.getTxnAmount(),
					batchStagedIBKPaymentTxnDetail.getTxnType(),
					batchStagedIBKPaymentTxnDetail.getTxnDescription(),
					batchStagedIBKPaymentTxnDetail.getBillerRefNo1(),
					batchStagedIBKPaymentTxnDetail.getBillerRefNo2(),
					batchStagedIBKPaymentTxnDetail.getBillerRefNo3(),
					batchStagedIBKPaymentTxnDetail.getTxnTime(),
					batchStagedIBKPaymentTxnDetail.getFileName(),
					batchStagedIBKPaymentTxnDetail.getCreatedTime(),
					batchStagedIBKPaymentTxnDetail.getBillerRefNo4(),
					batchStagedIBKPaymentTxnDetail.getIdNo(),
					batchStagedIBKPaymentTxnDetail.getPolicyNo(),
					batchStagedIBKPaymentTxnDetail.getUserAddress1(),
					batchStagedIBKPaymentTxnDetail.getUserAddress2(),
					batchStagedIBKPaymentTxnDetail.getUserAddress3(),
					batchStagedIBKPaymentTxnDetail.getUserAddress4(),
					batchStagedIBKPaymentTxnDetail.getUserState(),
					batchStagedIBKPaymentTxnDetail.getUserCity(),
					batchStagedIBKPaymentTxnDetail.getUserPostcode(),
					batchStagedIBKPaymentTxnDetail.getUserCountry()
			);
		} catch (Exception e) {
			String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_STAGED_IBK_PAYMENT_TXN table with values [%s] ", batchStagedIBKPaymentTxnDetail.toString());
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}

	public int deleteExistingBatchStagedIBKPaymentTxns(String filename) throws BatchException {
		try {
			return getJdbcTemplate().update(DELETE_STAGED_IBK_PAYMENT_TXN_SQL, filename);
		} catch (Exception e) {
			String errorMessage = String.format("Error happened while deleting existing records from TBL_BATCH_STAGED_IBK_PAYMENT_TXN table where FILE_NAME is [%s] ", filename);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}

	}

	public int addBatchStagedIBKPaymentReportToStaging(BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail) throws BatchException {
		try {

			return getJdbcTemplate().update(INSERT_STAGED_IBK_PAYMENT_RPT_SQL,
					batchStagedIBKPaymentTxnDetail.getJobExecutionId(),
					batchStagedIBKPaymentTxnDetail.getProcessDate(),
					batchStagedIBKPaymentTxnDetail.getBillerCode(),
					batchStagedIBKPaymentTxnDetail.getTxnId(),
					batchStagedIBKPaymentTxnDetail.getTxnDate(),
					batchStagedIBKPaymentTxnDetail.getTxnAmount(),
					batchStagedIBKPaymentTxnDetail.getBillerRefNo1(),
					batchStagedIBKPaymentTxnDetail.getBillerRefNo2(),
					batchStagedIBKPaymentTxnDetail.getTxnTime(),
					batchStagedIBKPaymentTxnDetail.getFileName(),
					batchStagedIBKPaymentTxnDetail.getCreatedTime(),
					batchStagedIBKPaymentTxnDetail.getUsername(),
					batchStagedIBKPaymentTxnDetail.getLineNo()
			);
		} catch (Exception e) {
			String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_STAGED_IBK_PAYMENT_RPT table with values [%s] ", batchStagedIBKPaymentTxnDetail.toString());
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}

	public int updateBatchStagedIBKPaymentReportDetailToStaging(BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail) throws BatchException {
		try {
			return getJdbcTemplate().update(UPDATE_STAGED_IBK_PAYMENT_RPT_SQL,
					batchStagedIBKPaymentTxnDetail.getUsername(),
					batchStagedIBKPaymentTxnDetail.getJobExecutionId(),
					batchStagedIBKPaymentTxnDetail.getProcessDate(),
					batchStagedIBKPaymentTxnDetail.getFileName(),
					batchStagedIBKPaymentTxnDetail.getLineNo() - 1
			);
		} catch (Exception e) {
			String errorMessage = String.format("Error happened while update record to TBL_BATCH_STAGED_IBK_PAYMENT_RPT table with values [%s] ", batchStagedIBKPaymentTxnDetail.toString());
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}

	public int deleteExistingBatchStagedIBKPaymentReport(String filename) throws BatchException {
		try {
			return getJdbcTemplate().update(DELETE_STAGED_IBK_PAYMENT_RPT_SQL, filename);
		} catch (Exception e) {
			String errorMessage = String.format("Error happened while deleting existing records from TBL_BATCH_STAGED_IBK_PAYMENT_RPT table where FILE_NAME is [%s] ", filename);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}

	}
}
