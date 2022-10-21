package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import com.rhbgroup.dcp.bo.batch.job.model.EPullAutoEnrollmentDetails;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EPullAutoEnrollmentRowMapper implements RowMapper<EPullAutoEnrollmentDetails> {
	
	@Override
	public EPullAutoEnrollmentDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		EPullAutoEnrollmentDetails ePullAutoEnrollmentDetails = new EPullAutoEnrollmentDetails();
		ePullAutoEnrollmentDetails.setAccountType(rs.getString("accountType"));
		ePullAutoEnrollmentDetails.setAccountNo(rs.getString("accountNo"));
		ePullAutoEnrollmentDetails.setStatementType(rs.getInt("statementType"));
		return ePullAutoEnrollmentDetails;
	}

}