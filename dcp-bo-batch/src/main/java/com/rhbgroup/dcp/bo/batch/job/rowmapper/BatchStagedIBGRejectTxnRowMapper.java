package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectTxn;

public class BatchStagedIBGRejectTxnRowMapper implements RowMapper<BatchStagedIBGRejectTxn> {
	
	@Override
	public BatchStagedIBGRejectTxn mapRow(ResultSet rs, int rowNum) throws SQLException {
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = new BatchStagedIBGRejectTxn();
		
		batchStagedIBGRejectTxn.setId(rs.getInt("ID"));
		batchStagedIBGRejectTxn.setUserId(rs.getInt("USER_ID"));
		batchStagedIBGRejectTxn.setAmount(rs.getString("AMOUNT"));
		batchStagedIBGRejectTxn.setBeneName(rs.getString("BENE_NAME"));
		batchStagedIBGRejectTxn.setDate(rs.getString("DATE"));
		batchStagedIBGRejectTxn.setRejectDescription(rs.getString("REJECT_DESCRIPTION"));

		return batchStagedIBGRejectTxn;
	}

}