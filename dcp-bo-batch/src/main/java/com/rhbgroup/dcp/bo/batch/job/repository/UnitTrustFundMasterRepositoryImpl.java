package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccountHolding;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFundMaster;

@Component
@Lazy
public class UnitTrustFundMasterRepositoryImpl {
	private static final String TBL_NAME_PREFIX="TBL_UT_FUND_MASTER_";

	@Autowired
	@Qualifier("dataSourceDCP")
	private DataSource dataSourceDCP;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public int deleteAllRecords(int targetDataSet) {
		int row=0;
		String tblName=TBL_NAME_PREFIX.concat(String.valueOf(targetDataSet));
		String sql="TRUNCATE TABLE " + tblName;
		jdbcTemplate.setDataSource(dataSourceDCP);
		row=jdbcTemplate.update(sql);
		return row;
	}

	public int addRecordBatch(List<UnitTrustFundMaster> utFundMasters, int targetDataSet) {
		jdbcTemplate.setDataSource(dataSourceDCP);
		String tblName=TBL_NAME_PREFIX .concat(String.valueOf(targetDataSet));
		String sql="INSERT INTO "+tblName+
				" (job_execution_id,process_date,fund_id,fund_name,"
				+ "fund_currency,fund_currency_nav_price,nav_date,"
				+ "product_category_code,product_category_description,"
				+ "risk_level_code,risk_level_description,myr_nav_price,status,"
				+ "batch_extraction_time,created_time,created_by,updated_time,updated_by,file_name)"+
				" VALUES " +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UnitTrustFundMaster utFundMaster = utFundMasters.get(i);
				ps.setLong(1, utFundMaster.getJobExecutionId());
				ps.setString(2, utFundMaster.getProcessDate());
				ps.setString(3, utFundMaster.getFundId());
				ps.setString(4, utFundMaster.getFundName());
				ps.setString(5, utFundMaster.getFundCurr());
				ps.setDouble(6, utFundMaster.getFundCurrNavPrice());
				ps.setTimestamp(7, new java.sql.Timestamp(utFundMaster.getNavDate().getTime()));
				ps.setString(8, utFundMaster.getProdCategoryCode());
				ps.setString(9, utFundMaster.getProdCategoryDesc());
				ps.setString(10, utFundMaster.getRiskLevelCode());
				ps.setString(11, utFundMaster.getRiskLevelDesc());
				ps.setDouble(12, utFundMaster.getMyrNavPrice());
				ps.setInt(13, utFundMaster.getStatus());
				ps.setTimestamp(14, new java.sql.Timestamp(utFundMaster.getBatchExtractionTime().getTime()));
				ps.setTimestamp(15, new java.sql.Timestamp(utFundMaster.getCreatedTime().getTime()));
				ps.setString(16, utFundMaster.getCreatedBy());
				ps.setTimestamp(17, new java.sql.Timestamp(utFundMaster.getUpdatedTime().getTime()));
				ps.setString(18, utFundMaster.getUpdatedBy());
				ps.setString(19, utFundMaster.getFileName());
			}

			@Override
			public int getBatchSize() {
				return utFundMasters.size();
			}
		});
		return row.length;
	}
}
