package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;

public class BatchSuspenseRowMapper implements RowMapper<BatchSuspense> {
	
	@Override
	public BatchSuspense mapRow(ResultSet rs, int rowNum) throws SQLException {
		BatchSuspense batchSuspense = new BatchSuspense();
		batchSuspense.setId(rs.getLong("ID"));
		batchSuspense.setJobExecutionId(rs.getLong("JOB_EXECUTION_ID"));
		batchSuspense.setBatchJobName(rs.getString("BATCH_JOB_NAME"));
		batchSuspense.setSuspenseColumn(rs.getString("SUSPENSE_COLUMN"));
		batchSuspense.setSuspenseMessage(rs.getString("SUSPENSE_MESSAGE"));
		batchSuspense.setSuspenseRecord(rs.getString("SUSPENSE_RECORD"));
		batchSuspense.setSuspenseType(rs.getString("SUSPENSE_TYPE"));
		batchSuspense.setCreatedTime(rs.getTimestamp("CREATED_TIME"));

		return batchSuspense;
	}

}