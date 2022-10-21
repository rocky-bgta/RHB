package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotificationRaw;

@Component
@Lazy
public class BatchStagedNotificationRawRepositoryImpl extends BaseRepositoryImpl {
	private static final Logger logger = Logger.getLogger(BatchStagedNotificationRawRepositoryImpl.class);

	@SneakyThrows
	public int addRecordNotificationRaw(BatchStagedNotificationRaw  notificationRec) {
		int row=0;
		try {
			String sql = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION_RAW"+
					"(job_execution_id,file_name,process_date,event_code,key_type,data_1,data_2,data_3,data_4,data_5,data_6,data_7,data_8,data_9,data_10,is_processed,created_time,created_by,updated_time,updated_by)" +
					" VALUES " +
					"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			row = getJdbcTemplate().update(sql,
					new Object[] { notificationRec.getJobExecutionId(), notificationRec.getFileName(),
							notificationRec.getProcessDate(), notificationRec.getEventCode(),
							notificationRec.getKeyType(), notificationRec.getData1(), notificationRec.getData2(),
							notificationRec.getData3(), notificationRec.getData4(), notificationRec.getData5(),
							notificationRec.getData6(), notificationRec.getData7(), notificationRec.getData8(),
							notificationRec.getData9(), notificationRec.getData10(), notificationRec.isProcessed(),
							notificationRec.getCreatedTime(), notificationRec.getCreatedBy(),
							notificationRec.getUpdatedTime(), notificationRec.getUpdatedBy() });
			logger.info( String.format( "Add %s record into TBL_BATCH_STAGED_NOTIFICATION_RAW",row));
		}catch(Exception ex) {
			logger.error(String.format("Exception Adding record into TBL_BATCH_STAGED_NOTIFICATION_RAW, %s ",ex));
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, ex);
		}
		return row;
	}
	
	public int addBatchNotificationRaw(List<BatchStagedNotificationRaw>  rawNotifications) {
		String sql = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION_RAW"+
				"(job_execution_id,file_name,process_date,event_code,key_type" +
				",data_1,data_2,data_3,data_4,data_5,data_6,data_7,data_8,data_9,data_10"+
				",is_processed,created_time,created_by,updated_time,updated_by)" +
				" VALUES " +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		int[] row = getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				BatchStagedNotificationRaw notificationRec = rawNotifications.get(i);
				ps.setLong(1, notificationRec.getJobExecutionId());
				ps.setString(2, notificationRec.getFileName());
				ps.setString(3, notificationRec.getProcessDate());
				ps.setString(4, notificationRec.getEventCode());
				ps.setString(5, notificationRec.getKeyType());
				ps.setString(6, notificationRec.getData1());
				ps.setString(7, notificationRec.getData2());
				ps.setString(8, notificationRec.getData3());
				ps.setString(9, notificationRec.getData4());
				ps.setString(10, notificationRec.getData5());
				ps.setString(11, notificationRec.getData6());
				ps.setString(12, notificationRec.getData7());
				ps.setString(13, notificationRec.getData8());
				ps.setString(14, notificationRec.getData9());
				ps.setString(15, notificationRec.getData10());
				ps.setBoolean(16, notificationRec.isProcessed());
				ps.setTimestamp(17, new java.sql.Timestamp(notificationRec.getCreatedTime().getTime()));
				ps.setString(18, notificationRec.getCreatedBy());
				ps.setTimestamp(19, new java.sql.Timestamp(notificationRec.getUpdatedTime().getTime()));
				ps.setString(20, notificationRec.getUpdatedBy());
			}

			@Override
			public int getBatchSize() {
				return rawNotifications.size();
			}
		});
		return row.length;
	}

	@SneakyThrows
	public int findNotificationFileLoaded(String fileName){
		int row = 0;
		try {
			String sql = "SELECT COUNT(ID) FROM TBL_BATCH_STAGED_NOTIFICATION_RAW WHERE file_name=?";
			row = getJdbcTemplate().queryForObject(sql, new Object[] { fileName }, Integer.class);
		} catch (Exception ex) {
			logger.error(String.format("Exception checking file loaded TBL_BATCH_STAGED_NOTIFICATION_RAW, %s ",ex));
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, ex);
		}
		return row;
	}
}
