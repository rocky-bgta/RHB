package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchUserMaintAutoAging;

public class BatchUserMaintAutoAgingRowMapper implements RowMapper<BatchUserMaintAutoAging> {
	
	@Override
	public BatchUserMaintAutoAging mapRow(ResultSet rs, int rowNum) throws SQLException {
		BatchUserMaintAutoAging batchUserMaintAutoAging = new BatchUserMaintAutoAging();
		
		batchUserMaintAutoAging.setUserId(rs.getInt("USER_ID"));
		batchUserMaintAutoAging.setUserName(rs.getString("USERNAME"));
		batchUserMaintAutoAging.setName(rs.getString("NAME"));
		batchUserMaintAutoAging.setEmail(rs.getString("EMAIL"));
		batchUserMaintAutoAging.setUserDepartmentId(rs.getInt("USER_DEPARTMENT_ID"));
		batchUserMaintAutoAging.setDepartment(rs.getString("DEPARTMENT"));
		batchUserMaintAutoAging.setCurrentUserStatus(rs.getString("USER_STATUS_ID"));
		batchUserMaintAutoAging.setLastLoginTime(rs.getTimestamp("LAST_LOGIN_TIME"));
		batchUserMaintAutoAging.setLastLoginTimeDayDiff(rs.getInt("LAST_LOGIN_TIME_DIFF"));

		return batchUserMaintAutoAging;
	}

}