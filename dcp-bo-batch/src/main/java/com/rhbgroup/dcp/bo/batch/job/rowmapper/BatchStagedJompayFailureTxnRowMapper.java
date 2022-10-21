package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedJompayFailureTxn;

public class BatchStagedJompayFailureTxnRowMapper implements RowMapper<BatchStagedJompayFailureTxn>{

	@Override
	public BatchStagedJompayFailureTxn mapRow(ResultSet rs, int rowNum) throws SQLException {
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn = new BatchStagedJompayFailureTxn();
		
		batchStagedJompayFailureTxn.setBillerCode(rs.getString("BILLER_CODE"));
		batchStagedJompayFailureTxn.setPaymentChannel(rs.getString("PAYMENT_CHANNEL"));
		batchStagedJompayFailureTxn.setRequestTime(rs.getTimestamp("REQUEST_TIME"));
		batchStagedJompayFailureTxn.setReasonForFailure(rs.getString("REASON_FOR_FAILURE"));
		
		return batchStagedJompayFailureTxn;
	}

}
