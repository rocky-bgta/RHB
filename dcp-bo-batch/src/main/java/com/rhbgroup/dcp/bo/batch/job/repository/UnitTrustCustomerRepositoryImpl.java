package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomer;

@Lazy
@Component
public class UnitTrustCustomerRepositoryImpl {
	private static final Logger logger = Logger.getLogger(UnitTrustCustomerRepositoryImpl.class);
	private static final String TBL_NAME_PREFIX="TBL_UT_CUSTOMER_";

	@Autowired
	@Qualifier("dataSourceDCP")
	private DataSource dataSourceDCP;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public int deleteAllRecords(int targetDataSet) {
		int row=0;
		String tblName=TBL_NAME_PREFIX.concat(String.valueOf(targetDataSet));
		String sql = "TRUNCATE TABLE " + tblName;
		logger.debug(String.format("Delete record from table %s, sql=%s", tblName,sql));
		jdbcTemplate.setDataSource(dataSourceDCP);
		row = jdbcTemplate.update(sql);
		return row;
	}
	
	public int addRecordBatch(List<UnitTrustCustomer> utCustomers,int targetDataSet) {
		
		String tblName=TBL_NAME_PREFIX.concat(String.valueOf(targetDataSet));
		logger.debug(String.format("Add record into table %s", tblName));

		String sql = "INSERT INTO " + tblName 
				+" (job_execution_id,process_date,batch_extraction_time,"
				+ " cis_no,name,status,"
				+ "created_time,created_by,updated_time,updated_by,file_name) "
				+"VALUES"
				+"(?,?,?,?,?,?,?,?,?,?,?)";
		logger.debug(String.format("sql=%s", sql));
		jdbcTemplate.setDataSource(dataSourceDCP);
		int [] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UnitTrustCustomer customer = utCustomers.get(i);
				ps.setLong(1, customer.getJobExecutionId());
				ps.setString(2, customer.getProcessDate());
				ps.setTimestamp(3, new java.sql.Timestamp(customer.getBatchExtractionTime().getTime()));
				ps.setString(4, customer.getCisNo());
				ps.setString(5, customer.getName() );
				ps.setInt(6, customer.getStatus() );
				ps.setTimestamp(7, new java.sql.Timestamp( customer.getCreatedTime().getTime()));
				ps.setString(8, customer.getCreatedBy() );
				ps.setTimestamp(9, new java.sql.Timestamp( customer.getUpdatedTime().getTime()));
				ps.setString(10, customer.getUpdatedBy() );
				ps.setString(11, customer.getFileName() );
			}
					
			@Override
			public int getBatchSize() {
				return utCustomers.size();
			}
		  });
		return row.length;
	}
}
