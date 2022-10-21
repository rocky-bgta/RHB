package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Arrays;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractandUpdateMcaInterestRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateFPXStatus;

@Component
@Lazy
public class BatchUpdateFpxStatusRepositoryImpl extends BaseRepositoryImpl {

static final Logger logger = Logger.getLogger(BatchUpdateFpxStatusRepositoryImpl.class);
	
	@Autowired
	private ExtractandUpdateMcaInterestRateJobConfigProperties configProperties;
	
	@Qualifier("dataSourceDCP")
	@Autowired
	DataSource dataSourceDCP;
	
	public Integer addBatchUpdateFpxStatusStaging(BatchUpdateFPXStatus batchUpdateFPXStatus) throws BatchException {
		try {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("INSERT INTO dcpbo.dbo.TBL_BATCH_STAGED_UPDATE_FPX_STATUS (");
			stringBuilder.append("job_execution_id, txn_token_id, main_function, sub_function,bank_id,buyer_name,buyer_email,seller_bank_code,");
			stringBuilder.append("seller_ex_id,seller_ex_order_no,seller_id,seller_order_no,seller_txn_time,txn_amount,txn_status,debit_auth_code,debit_auth_no,");
			stringBuilder.append("credit_auth_code,credit_auth_no,txn_description,product_description,txn_id,txn_time,is_processed,created_time,updated_time) ");
			stringBuilder.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

			Object[] parameter = new Object[] { 
					batchUpdateFPXStatus.getJobExecutionId(),
					batchUpdateFPXStatus.getTxnTokenId(),
					batchUpdateFPXStatus.getMainFunction(),
					batchUpdateFPXStatus.getSubFunction(),
					batchUpdateFPXStatus.getBankId(),
					batchUpdateFPXStatus.getBuyerName(),
					batchUpdateFPXStatus.getBuyerEmail(),
					batchUpdateFPXStatus.getSellerBankCode(),
					batchUpdateFPXStatus.getSellerExId(),
					batchUpdateFPXStatus.getSellerExOrderNo(),
					batchUpdateFPXStatus.getSellerId(),
					batchUpdateFPXStatus.getSellerOrderNo(),
					batchUpdateFPXStatus.getSellerTxnTime(),
					batchUpdateFPXStatus.getTxnAmount(),
					batchUpdateFPXStatus.getTxnStatus(),
					batchUpdateFPXStatus.getDebitAuthCode(),
					batchUpdateFPXStatus.getDebitAuthNo(),
					batchUpdateFPXStatus.getCreditAuthCode(),
					batchUpdateFPXStatus.getCreditAuthNo(),
					batchUpdateFPXStatus.getTxnDescription(),
					batchUpdateFPXStatus.getProductDescription(),
					batchUpdateFPXStatus.getTxnId(),
					batchUpdateFPXStatus.getTxnTime(),
					batchUpdateFPXStatus.isProcessed(),
					batchUpdateFPXStatus.getCreatedTime(), 
					batchUpdateFPXStatus.getUpdatedTime()};

			return jdbcTemplate.update(stringBuilder.toString(), parameter);
		} catch (Exception e) {
			logger.error("Exception", e);
			String errorMessage = String.format("Error happened while inserting new record to tbl_batch_staged_fpx_status values [%s] ", batchUpdateFPXStatus);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}

	public Integer updateBatchFpxStatus(BatchUpdateFPXStatus batchUpdateFPXStatus) throws BatchException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("UPDATE TBL_BATCH_STAGED_UPDATE_FPX_STATUS SET ");
		stringBuilder.append("JOB_EXECUTION_ID=?,");
		stringBuilder.append("IS_PROCESSED=?,");
		stringBuilder.append("CREATED_TIME=?,");
		stringBuilder.append("UPDATED_TIME=? ");
		stringBuilder.append(" WHERE ID=? ");

		try {
			String currentDate = DateUtils.formatDateString(new Date(), "yyyy-MM-dd HH:mm:ss");
			
			Object[] parameter = new Object[] { 
					batchUpdateFPXStatus.getJobExecutionId(),
					batchUpdateFPXStatus.isProcessed(),
					currentDate,currentDate,
					batchUpdateFPXStatus.getId() 
			};
			
			logger.info(String.format("Updating TBL_BATCH_STAGED_UPDATE_FPX_STATUS: %s", Arrays.toString(parameter)));

			return jdbcTemplate.update(stringBuilder.toString(), parameter);

		} catch(Exception e) {
			String errorMessage = String.format("Error happened while updating record to TBL_BATCH_STAGED_UPDATE_FPX_STATUS values [%s] ", batchUpdateFPXStatus);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}
	
}
