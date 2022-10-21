package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDowntimeNotification;

@Component
@Lazy
public class BatchStagedDowntimeNotificationRepositoryImpl extends BaseRepositoryImpl {
	
	public int addRecordBatchStagedDowntimeNotificationInBatch(List<? extends BatchStagedDowntimeNotification> records) throws Exception {
		
		jdbcTemplate.setDataSource(dataSource);
		String sql = "INSERT INTO TBL_BATCH_STAGED_DOWNTIME_NOTIFICATION"+
				"(job_execution_id, type, adhoc_type, event_code, content, user_id, is_processed, start_time , end_time, created_time, created_by, updated_time, updated_by,adhoc_type_category)" +
				" VALUES " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?)";
     
		int [] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				BatchStagedDowntimeNotification batchStagedDowntimeNotification = records.get(i);
				ps.setLong(1, batchStagedDowntimeNotification.getJobExecutionId());
				ps.setString(2, batchStagedDowntimeNotification.getType());
				ps.setString(3, batchStagedDowntimeNotification.getAdhocType());
				ps.setString(4, batchStagedDowntimeNotification.getEventCode());
				ps.setString(5, batchStagedDowntimeNotification.getContent());
				ps.setLong(6,	batchStagedDowntimeNotification.getUserId());
				ps.setBoolean(7, batchStagedDowntimeNotification.isProcessed());
				ps.setTimestamp(8, batchStagedDowntimeNotification.getStartTime());
				ps.setTimestamp(9, batchStagedDowntimeNotification.getEndTime());
				ps.setTimestamp(10, batchStagedDowntimeNotification.getCreatedTime());
				ps.setString(11, batchStagedDowntimeNotification.getCreatedBy());
				ps.setTimestamp(12, batchStagedDowntimeNotification.getUpdatedTime());
				ps.setString(13, batchStagedDowntimeNotification.getUpdatedBy());
				ps.setString(14, batchStagedDowntimeNotification.getAdhocTypeCategory());
	
			}
					
			@Override
			public int getBatchSize() {
				return records.size();
			}
		  });
		return row.length;
	}
	
	public int addRecordUpdateIsProcessedInBatch(String batchCode, List<? extends BatchStagedDowntimeNotification> records) throws Exception {
		
		jdbcTemplate.setDataSource(dataSource);
		String sql = "UPDATE TBL_BATCH_STAGED_DOWNTIME_NOTIFICATION SET IS_PROCESSED=1, UPDATED_TIME=?, UPDATED_BY=?, JOB_EXECUTION_ID=? WHERE ID=?";
     
		int [] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				BatchStagedDowntimeNotification batchStagedDowntimeNotification = records.get(i);
				Timestamp now = new Timestamp(System.currentTimeMillis());
				ps.setTimestamp(1, now);
				ps.setString(2, batchCode);
				ps.setLong(3, batchStagedDowntimeNotification.getJobExecutionId());
				ps.setLong(4, batchStagedDowntimeNotification.getId());
			}
					
			@Override
			public int getBatchSize() {
				return records.size();
			}
		  });
		return row.length;
	}
    
}
