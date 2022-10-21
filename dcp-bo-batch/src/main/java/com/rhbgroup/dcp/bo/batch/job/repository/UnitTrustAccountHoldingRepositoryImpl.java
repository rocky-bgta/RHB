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
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomer;

@Component
@Lazy
public class UnitTrustAccountHoldingRepositoryImpl {
	private static final Logger logger = Logger.getLogger(UnitTrustAccountHoldingRepositoryImpl.class);
	private static final String TBL_NAME_PREFIX="TBL_UT_ACCOUNT_HOLDING_";

	@Autowired
	@Qualifier("dataSourceDCP")
	private DataSource dataSourceDCP;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public int deleteAllRecords(int targetDataSet) {
		int row=0;
		String tblName=TBL_NAME_PREFIX.concat(String.valueOf(targetDataSet));
		String sql="TRUNCATE TABLE " + tblName ;
		jdbcTemplate.setDataSource(dataSourceDCP);
		row=jdbcTemplate.update(sql);
		return row;
	}
	
	public int addRecordBatch(List<UnitTrustAccountHolding> utAccountHoldings, int targetDataSet ) {
		String tblName = TBL_NAME_PREFIX.concat(String.valueOf(targetDataSet));
		logger.debug(String.format("Adding record in tbl %s", tblName));
		String sql="INSERT INTO  " +tblName
				+"(job_execution_id,process_date,account_no,fund_id,holding_unit"
				+ ",fund_currency_market_value,fund_currency_unrealised_gain_loss,fund_currency_unrealised_gain_loss_percentage,fund_currency_investment_amount,fund_currency_average_unit_price"
				+ ",fund_myr_market_value,fund_myr_unrealised_gain_loss,fund_myr_unrealised_gain_loss_percentage,fund_myr_investment_amount,fund_myr_average_unit_price"
				+ ",status,batch_extraction_time,created_time,created_by,updated_time,updated_by,file_name)"
				+" VALUES "
				+"(?,?,?,?,?"
				+ ",?,?,?,?,?"
				+ ",?,?,?,?,?"
				+ ",?,?,?,?,?,?,?)" ;
		jdbcTemplate.setDataSource(dataSourceDCP);
		int [] row=jdbcTemplate.batchUpdate(sql,  new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UnitTrustAccountHolding utAccountHolding = utAccountHoldings.get(i);
				ps.setLong(1, utAccountHolding.getJobExecutionId());
				ps.setString(2, utAccountHolding.getProcessDate());
				ps.setString(3, utAccountHolding.getAcctNo());
				ps.setString(4, utAccountHolding.getFundId());
				ps.setDouble(5, utAccountHolding.getHoldingUnit());
				ps.setDouble(6, utAccountHolding.getFundCurrMarketVal());
				ps.setDouble(7, utAccountHolding.getFundCurrUnrealisedGainLoss());
				ps.setDouble(8, utAccountHolding.getFundCurrUnrealisedGainLossPercent());
				ps.setDouble(9, utAccountHolding.getFundCurrInvestAmnt());
				ps.setDouble(10, utAccountHolding.getFundCurrAvgUnitPrice());
				ps.setDouble(11, utAccountHolding.getFundMyrMarketVal());
				ps.setDouble(12, utAccountHolding.getFundMyrUnrealisedGainLoss());
				ps.setDouble(13, utAccountHolding.getFundMyrUnrealisedGainLossPercent());
				ps.setDouble(14, utAccountHolding.getFundMyrInvestAmnt());
				ps.setDouble(15, utAccountHolding.getFundMyrAvgUnitPrice());
				ps.setInt(16, utAccountHolding.getStatus());
				ps.setTimestamp(17, new java.sql.Timestamp(utAccountHolding.getBatchExtractionTime().getTime()));
				ps.setTimestamp(18, new java.sql.Timestamp(utAccountHolding.getCreatedTime().getTime()));
				ps.setString(19, utAccountHolding.getCreatedBy());
				ps.setTimestamp(20, new java.sql.Timestamp(utAccountHolding.getUpdatedTime().getTime()));
				ps.setString(21, utAccountHolding.getUpdatedBy());
				ps.setString(22, utAccountHolding.getFileName());
			}
					
			@Override
			public int getBatchSize() {
				return utAccountHoldings.size();
			}
		  });
		return row.length;
	}
}

