package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotifMass;

public class BatchStagedNotifMassRowMapper implements RowMapper<BatchStagedNotifMass>{

	@Override
	public BatchStagedNotifMass mapRow(ResultSet rs, int rowNum) throws SQLException {
		BatchStagedNotifMass batchStagedNotifMass = new BatchStagedNotifMass();
		
		batchStagedNotifMass.setId(rs.getLong("ID"));
		batchStagedNotifMass.setJobExecutionId(rs.getLong("JOB_EXECUTION_ID"));
		batchStagedNotifMass.setFileName(rs.getString("FILE_NAME"));
		batchStagedNotifMass.setEventCode(rs.getString("EVENT_CODE"));
		batchStagedNotifMass.setContent(rs.getString("CONTENT"));
		batchStagedNotifMass.setUserId(rs.getLong("USER_ID"));
		batchStagedNotifMass.setProcessed(rs.getBoolean("IS_PROCESSED"));
		
		return batchStagedNotifMass;
	}

}
