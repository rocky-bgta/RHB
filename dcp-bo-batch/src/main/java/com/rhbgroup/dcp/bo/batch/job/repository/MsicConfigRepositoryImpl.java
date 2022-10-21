package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.DepositProduct;
import com.rhbgroup.dcp.bo.batch.job.model.StagedDepositProducts;
import com.rhbgroup.dcp.bo.batch.job.model.StagedMsicConfigs;

@Component
@Lazy
public class MsicConfigRepositoryImpl extends BaseRepositoryImpl {
	private static final Logger logger = Logger.getLogger(MsicConfigRepositoryImpl.class);
	@Autowired
	private BatchStagedMsicConfigRepositoryImpl batchStagedMsicConfigRepositoryImpl;

	public void updateOrInsertMsicConfigs(Long jobExecutionId) {
		List<StagedMsicConfigs> listOfMsicConfigs = batchStagedMsicConfigRepositoryImpl
				.getStagedMsicConfigs(jobExecutionId);

		logger.info("list of msic configs" + listOfMsicConfigs);
		List<StagedMsicConfigs> listOfNewMsicConfigs = batchStagedMsicConfigRepositoryImpl
				.getNotStagedMsicConfigs(jobExecutionId);
		logger.info("list of new msic configs" + listOfNewMsicConfigs);

		updateMsicConfig(listOfMsicConfigs);
		insertMsicConfig(listOfNewMsicConfigs);


	}

	private int updateMsicConfig(List<StagedMsicConfigs> records) {
		
		String sql = "UPDATE dcp.dbo.TBL_MSIC_CONFIG SET DESCRIPTION=?, IS_ISLAMIC_COMPLIANCE=?, STATUS =?, UPDATED_TIME=?, UPDATED_BY=? WHERE MSIC =? AND ACCOUNT_TYPE =?;";
		logger.debug("updated msic configs" + sql);
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Timestamp updatedTime = new Timestamp(System.currentTimeMillis());
				String updatedBy = "Admin";
				StagedMsicConfigs msicConfigs = records.get(i);
				ps.setString(1, msicConfigs.getDescription());
				ps.setBoolean(2, msicConfigs.isIslamicCompliance());
				ps.setString(3, msicConfigs.getStatus());
				ps.setTimestamp(4, updatedTime);
				ps.setString(5, updatedBy);
				ps.setString(6, msicConfigs.getMsic());
				ps.setString(7, msicConfigs.getAccountType());
			}

			@Override
			public int getBatchSize() {

				return records.size();
			}
		});
		return row.length;
	}
	
	private int insertMsicConfig(List<StagedMsicConfigs> records) {
		
		String sql = "INSERT INTO dcp.dbo.TBL_MSIC_CONFIG"
				+ "(MSIC, DESCRIPTION, ACCOUNT_TYPE, IS_ISLAMIC_COMPLIANCE, STATUS, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY)"
				+ "VALUES(?,?,?,?,?,?,?,?,?)";
		logger.info("updated msic configs" + sql);
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				String createdBy = "Admin";
				StagedMsicConfigs msicConfigs = records.get(i);
				ps.setString(1, msicConfigs.getMsic());
				ps.setString(2, msicConfigs.getDescription());
				ps.setString(3, msicConfigs.getAccountType());
				ps.setBoolean(4, msicConfigs.isIslamicCompliance());
				ps.setString(5, msicConfigs.getStatus());
				ps.setTimestamp(6, timeStamp);
				ps.setString(7, createdBy);
				ps.setTimestamp(8, timeStamp);
				ps.setString(9, createdBy);
			}

			@Override
			public int getBatchSize() {

				return records.size();
			}
		});
		return row.length;
	}

}