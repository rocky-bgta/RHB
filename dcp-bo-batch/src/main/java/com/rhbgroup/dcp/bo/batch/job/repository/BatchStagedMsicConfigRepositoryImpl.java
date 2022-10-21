package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedMsicConfig;
import com.rhbgroup.dcp.bo.batch.job.model.StagedMsicConfigs;

@Component
@Lazy
public class BatchStagedMsicConfigRepositoryImpl extends BaseRepositoryImpl {
	private static final Logger logger = Logger.getLogger(BatchStagedMsicConfigRepositoryImpl.class);

	public int addBatchStageMsicConfig(List<BatchStagedMsicConfig> batchStagedMsicConfigs) {
		Date date = new Date();
		Timestamp now = new Timestamp(date.getTime());
		String sql = "INSERT INTO TBL_BATCH_STAGED_MSIC_CONFIG"
				+ "(job_execution_id,msic_id,msic,description,account_type"
				+ ",is_islamic_compliance,status,is_processed,file_name,process_date,created_time)"
				+ " VALUES " + "(?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.setDataSource(dataSource);
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				BatchStagedMsicConfig batchStagedMsicConfig = batchStagedMsicConfigs.get(i);
				ps.setLong(1, batchStagedMsicConfig.getJobExecutionId());
				ps.setLong(2, batchStagedMsicConfig.getJobExecutionId());
				ps.setString(3, batchStagedMsicConfig.getMsic());
				ps.setString(4, batchStagedMsicConfig.getDescription());
				ps.setString(5, batchStagedMsicConfig.getAccountType());
				ps.setBoolean(6, batchStagedMsicConfig.isIslamicCompliance());
				ps.setString(7, batchStagedMsicConfig.getStatus());
				ps.setBoolean(8, batchStagedMsicConfig.isProcessed());
				ps.setString(9, batchStagedMsicConfig.getFileName());
				ps.setTimestamp(10, now);
				ps.setTimestamp(11, now);
			}

			@Override
			public int getBatchSize() {
				return batchStagedMsicConfigs.size();
			}
		});
		return row.length;
	}
	
	public List<StagedMsicConfigs> getStagedMsicConfigs(Long jobExecutionId) {
		String sql = "SELECT stagedconfigs.msic,stagedconfigs.description,stagedconfigs.account_type,CAST(stagedconfigs.is_islamic_compliance AS BIT) AS islamic_compliance,stagedconfigs.status \r\n"
				+ "FROM dcpbo.dbo.TBL_BATCH_STAGED_MSIC_CONFIG stagedconfigs INNER JOIN dcp.dbo.TBL_MSIC_CONFIG configs ON stagedconfigs.msic=configs.msic\r\n"
				+ "AND stagedconfigs.ACCOUNT_TYPE = configs.ACCOUNT_TYPE \r\n"
				+ "WHERE stagedconfigs.JOB_EXECUTION_ID = ? AND stagedconfigs.ACCOUNT_TYPE IN ('S','D','C')";
		logger.info(String.format("get staged configs sql=%s", sql));
		return jdbcTemplate.query(sql, new Object[] {jobExecutionId},new BeanPropertyRowMapper<StagedMsicConfigs>(StagedMsicConfigs.class));
	}
	
	public List<StagedMsicConfigs> getNotStagedMsicConfigs(Long jobExecutionId) {
		String sql = "SELECT stagedconfigs.msic,stagedconfigs.description,stagedconfigs.account_type,CAST(stagedconfigs.is_islamic_compliance AS BIT) AS islamic_compliance,stagedconfigs.status \r\n"
				+ "FROM dcpbo.dbo.TBL_BATCH_STAGED_MSIC_CONFIG stagedconfigs LEFT JOIN dcp.dbo.TBL_MSIC_CONFIG configs ON configs.msic = stagedconfigs.msic\r\n"
				+ "AND configs.ACCOUNT_TYPE = stagedconfigs.ACCOUNT_TYPE \r\n"
				+ "WHERE stagedconfigs.JOB_EXECUTION_ID = ? AND \r\n"
				+ "stagedconfigs.ACCOUNT_TYPE IN ('S','D','C') AND configs.msic is NULL OR stagedconfigs.msic is NULL";
		logger.info(String.format("get staged configs sql=%s", sql));
		return jdbcTemplate.query(sql, new Object[] {jobExecutionId},new BeanPropertyRowMapper<StagedMsicConfigs>(StagedMsicConfigs.class));
	}
	
	public int findBatchStagedMsicConfigFileLoaded(String fileName,Long jobExecutionId) throws BatchException{
		int row = 0;
		try {
			String sql = "SELECT COUNT(ID) FROM TBL_BATCH_STAGED_MSIC_CONFIG WHERE file_name=? AND job_execution_id=?";
			row = getJdbcTemplate().queryForObject(sql, new Object[] { fileName,jobExecutionId }, Integer.class);
		} catch (Exception ex) {
			logger.error(String.format("Exception checking file loaded TBL_BATCH_STAGED_MSIC_CONFIG, %s ", ex));
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, ex);
		}
		return row;
	}

	public int deleteBatchStageMsicConfig(){
		String sql = "TRUNCATE TABLE dcpbo.dbo.TBL_BATCH_STAGED_MSIC_CONFIG";
		logger.info(String.format("  sql delete TBL_BATCH_STAGED_MSIC_CONFIG=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql);

		return rowAffected;
	}

}
