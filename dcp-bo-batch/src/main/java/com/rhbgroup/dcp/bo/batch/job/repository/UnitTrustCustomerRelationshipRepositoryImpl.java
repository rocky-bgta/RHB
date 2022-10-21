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

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomer;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerRelationship;

@Lazy
@Component
public class UnitTrustCustomerRelationshipRepositoryImpl {
	private static final Logger logger = Logger.getLogger(UnitTrustCustomerRelationshipRepositoryImpl.class);
	private static final String TBL_NAME_PREFIX="TBL_UT_CUSTOMER_REL_";

	@Autowired
	@Qualifier("dataSourceDCP")
	private DataSource dataSourceDCP;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public int deleteAllRecords(int targetDataSet) {
		int row=0;
		String tblName = TBL_NAME_PREFIX.concat(String.valueOf(targetDataSet));
		String sql="TRUNCATE TABLE "+ tblName;
		logger.info(String.format("clear data from table %s, sql:%s", tblName, sql));
		jdbcTemplate.setDataSource(dataSourceDCP);
		row=jdbcTemplate.update(sql);
		return row;
	}
	
	public int addRecordBatch(List<UnitTrustCustomerRelationship> utCustRelationships, int targetDataSet) {
		String tblName = TBL_NAME_PREFIX.concat(String.valueOf(targetDataSet));
		logger.debug(String.format("Adding record into %s", tblName));
		String sql= "INSERT INTO " + tblName +
			" (job_execution_id,process_date,cis_no,account_no,join_type,batch_extraction_time,status,created_time,created_by,updated_time,updated_by,file_name)"+
			" VALUES" +
			" (?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.setDataSource(dataSourceDCP);
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UnitTrustCustomerRelationship customer = utCustRelationships.get(i);
				ps.setLong(1, customer.getJobExecutionId());
				ps.setString(2, customer.getProcessDate());
				ps.setString(3, customer.getCisNo());
				ps.setString(4, customer.getAcctNo());
				ps.setString(5, customer.getJoinType());
				ps.setTimestamp(6, new java.sql.Timestamp(customer.getBatchExtractionTime().getTime()));
				ps.setInt(7, customer.getStatus());
				ps.setTimestamp(8, new java.sql.Timestamp(customer.getCreatedTime().getTime()));
				ps.setString(9, customer.getCreatedBy());
				ps.setTimestamp(10, new java.sql.Timestamp(customer.getUpdatedTime().getTime()));
				ps.setString(11, customer.getUpdatedBy());
				ps.setString(12, customer.getFileName());
			}
			@Override
			public int getBatchSize() {
				return utCustRelationships.size();
			}
		} );
		return row.length;
	}
}
