package com.rhbgroup.dcp.bo.batch.job.repository;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.AccountBatchInfo;

@Lazy
@Component
public class AccountBatchInfoRepositoryImpl {
	
	@Autowired
	@Qualifier("dataSourceDCP")
	private DataSource dataSourceDCP;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public int updateUTBatchInfoStart(AccountBatchInfo batchInfo) {
		int row=0;
		jdbcTemplate.setDataSource(dataSourceDCP);
		String sql="UPDATE TBL_ACCOUNT_BATCH_INFO "
				+"SET START_TIME=?, UPDATED_TIME=?, UPDATED_BY=?"
				+" WHERE ACCOUNT_TYPE=?";
		row = jdbcTemplate.update(sql, new Object[] { 
				batchInfo.getStartTime(), batchInfo.getUpdatedTime(),batchInfo.getUpdatedBy(), 
				batchInfo.getAccountType() });
		return row;
	}
	
	public int updateUTBatchInfoEnd(AccountBatchInfo batchInfo) {
		int row=0;
		jdbcTemplate.setDataSource(dataSourceDCP);
		String sql="UPDATE TBL_ACCOUNT_BATCH_INFO "
				+"SET END_TIME=?, TARGET_DATASET=?,UPDATED_TIME=?, UPDATED_BY=?"
				+" WHERE ACCOUNT_TYPE=?";
		row = jdbcTemplate.update(sql, new Object[] { 
				batchInfo.getEndTime(),batchInfo.getTargetDataset(),
				batchInfo.getUpdatedTime(),batchInfo.getUpdatedBy(), batchInfo.getAccountType() });
		return row;
	}
}
