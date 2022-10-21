package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDailyDeltaNewProfile;

public class BatchStagedDailyDeltaNewProfileRowMapper implements RowMapper<BatchStagedDailyDeltaNewProfile>{

	@Override
	public BatchStagedDailyDeltaNewProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
		BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile = new BatchStagedDailyDeltaNewProfile();
		
		batchStagedDailyDeltaNewProfile.setId(rs.getLong("ID"));
		batchStagedDailyDeltaNewProfile.setProcessingDate(rs.getDate("PROCESSING_DATE"));
		batchStagedDailyDeltaNewProfile.setUserId(rs.getInt("USER_ID"));
		batchStagedDailyDeltaNewProfile.setProcessed(rs.getBoolean("IS_PROCESSED"));
				
		return batchStagedDailyDeltaNewProfile;
	}

}