package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDowntimeNotification;

public class BatchStagedDowntimeNotificationRowMapper implements RowMapper<BatchStagedDowntimeNotification>{

	@Override
	public BatchStagedDowntimeNotification mapRow(ResultSet rs, int rowNum) throws SQLException {
		BatchStagedDowntimeNotification batchStagedDowntimeNotification = new BatchStagedDowntimeNotification();
		
		batchStagedDowntimeNotification.setId(rs.getLong("ID"));
		batchStagedDowntimeNotification.setJobExecutionId(rs.getLong("JOB_EXECUTION_ID"));
		batchStagedDowntimeNotification.setType(rs.getString("TYPE"));
		batchStagedDowntimeNotification.setAdhocType(rs.getString("ADHOC_TYPE"));
		batchStagedDowntimeNotification.setEventCode(rs.getString("EVENT_CODE"));
		batchStagedDowntimeNotification.setContent(rs.getString("CONTENT"));
		batchStagedDowntimeNotification.setUserId(rs.getLong("USER_ID"));
		batchStagedDowntimeNotification.setProcessed(rs.getBoolean("IS_PROCESSED"));
		batchStagedDowntimeNotification.setStartTime(rs.getTimestamp("START_TIME"));
		batchStagedDowntimeNotification.setEndTime(rs.getTimestamp("END_TIME"));
		
		return batchStagedDowntimeNotification;
	}

}
