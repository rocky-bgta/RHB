package com.rhbgroup.dcp.bo.batch.job.repository;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatchingDetail;

@Component
@Lazy
public class BatchStagedIBKJompayEmatchingRepositoryImpl extends BaseRepositoryImpl {
    static final Logger logger = Logger.getLogger(BatchStagedIBKJompayEmatchingRepositoryImpl.class);
	
	public int addRecord(BatchStagedIBKJompayEmatchingDetail batchStagedJompay) {
		int row = 0;
		String insertSQL = "INSERT INTO TBL_BATCH_STAGED_IBK_JOMPAY_EMATCHING_REPORT "
				+ "(job_execution_id,channel_id,channel_status, application_id,acct_ctrl1, acct_ctrl2, acct_ctrl3, account_no, debit_credit_ind, user_tran_code, amount,txn_branch,txn_date, txn_time, file_name , created_time)"
				+ " VALUES "
				+ "(?,?,?, ?,?, ?, ?, ?, ?, ?, ?,?,?, ?, ?, ?)";
		jdbcTemplate = getJdbcTemplate();
		row = jdbcTemplate.update(insertSQL, 
				new Object[] { batchStagedJompay.getJobExecutionId(),batchStagedJompay.getChannelId(), batchStagedJompay.getChannelStatus(),
						batchStagedJompay.getApplicationId(), batchStagedJompay.getAcctCtrl1(), batchStagedJompay.getAcctCtrl2(), batchStagedJompay.getAcctCtrl3(), 
						batchStagedJompay.getAccountNo(), batchStagedJompay.getDebitCreditInd(),batchStagedJompay.getUserTranCode(), batchStagedJompay.getAmount(),
						batchStagedJompay.getTxnBranch() ,batchStagedJompay.getTxnDate(), batchStagedJompay.getTxnTime(),batchStagedJompay.getFileName(),batchStagedJompay.getCreatedTime()});
		logger.info( String.format("%s inserted into tbl_batch_staged_ibk_jompay_ematching", row));
		return row;
	}
}
