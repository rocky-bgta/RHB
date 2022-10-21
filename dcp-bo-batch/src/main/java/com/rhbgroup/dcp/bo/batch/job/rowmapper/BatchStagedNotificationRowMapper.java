package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotification;

public class BatchStagedNotificationRowMapper implements RowMapper<BatchStagedNotification>{

	@Override
	public BatchStagedNotification mapRow(ResultSet rs, int rowNum) throws SQLException {
		BatchStagedNotification batchStagedNotification = new BatchStagedNotification();
		
		batchStagedNotification.setId(rs.getLong("ID"));
		batchStagedNotification.setEventCode(rs.getString("EVENT_CODE"));
		batchStagedNotification.setKeyType(rs.getString("KEY_TYPE"));
		batchStagedNotification.setUserId(rs.getLong("USER_ID"));
		batchStagedNotification.setData3(rs.getString("DATA_3"));
		batchStagedNotification.setData4(rs.getString("DATA_4"));
		batchStagedNotification.setProcessed(rs.getBoolean("IS_PROCESSED"));
		
		return batchStagedNotification;
	}

}
