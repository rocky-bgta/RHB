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

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccount;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerRelationship;
@Component
@Lazy
public class UnitTrustAccountRepositoryImpl {
	private static final Logger logger = Logger.getLogger(UnitTrustAccountRepositoryImpl.class);
	private static final String TBL_NAME_PREFIX="TBL_UT_ACCOUNT_";

	@Autowired
	@Qualifier("dataSourceDCP")
	private DataSource dataSourceDCP;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public int deleteAllRecords(int targetDataSet) {
		int row = 0;
		String tblName = TBL_NAME_PREFIX.concat(String.valueOf(targetDataSet));
		logger.info(String.format( "Deleting record from table %s" , tblName));
		String sql = "TRUNCATE TABLE " + tblName;
		jdbcTemplate.setDataSource(dataSourceDCP);
		row = jdbcTemplate.update(sql);
		return row;
	}
	
	public int addRecordBatch(List<UnitTrustAccount> utAccounts, int targetDataSet) {
		String tblName=TBL_NAME_PREFIX.concat(String.valueOf( targetDataSet)) ;
		logger.debug(String.format( "Adding record into table %s" , tblName));
		String sql="INSERT INTO " + tblName 
				+"(job_execution_id,process_date,batch_extraction_time,account_no,signatory_code,signatory_description,account_type,account_status_code,account_status_description,investment_product"
				+ ",last_performed_txn_date,status,created_time,created_by,updated_time,updated_by,file_name)"
				+" VALUES "
				+"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.setDataSource(dataSourceDCP);
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UnitTrustAccount utAccount = utAccounts.get(i);
				ps.setLong(1, utAccount.getJobExecutionId());
				ps.setString(2, utAccount.getProcessDate());
				ps.setTimestamp(3, new java.sql.Timestamp(utAccount.getBatchExtractionTime().getTime()));
				ps.setString(4, utAccount.getAcctNo());
				ps.setString(5, utAccount.getSignatoryCode());
				ps.setString(6, utAccount.getSignatoryDesc());
				ps.setString(7, utAccount.getAcctType());
				ps.setString(8, utAccount.getAcctStatusCode());
				ps.setString(9, utAccount.getAcctStatusDesc());
				ps.setString(10, utAccount.getInvestProd());
				ps.setTimestamp(11, new java.sql.Timestamp(utAccount.getLastPerformedTxnDate().getTime()));
				ps.setInt(12, utAccount.getStatus());
				ps.setTimestamp(13, new java.sql.Timestamp(utAccount.getCreatedTime().getTime()));
				ps.setString(14, utAccount.getCreatedBy());
				ps.setTimestamp(15, new java.sql.Timestamp(utAccount.getUpdatedTime().getTime()));
				ps.setString(16, utAccount.getUpdatedBy());
				ps.setString(17, utAccount.getFileName());
			}

			@Override
			public int getBatchSize() {
				return utAccounts.size();
			}
		} );
		return row.length;
	}
}
