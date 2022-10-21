package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;

@Component
@Lazy
public class UserProfileRepositoryImpl extends BaseRepositoryImpl {
	
	final static Logger logger = Logger.getLogger(UserProfileRepositoryImpl.class);

	@Qualifier("dataSourceDCP")
	@Autowired
	DataSource dataSourceDCP;
	String logMsg="";
	public int updateUserStatusCISNo(String cisNo, String userStatus) {
		int row=0;
		logMsg = String.format("update TBL_USER_PROFILE set USER_STATUS=%s where CIS_NO=%s",userStatus, cisNo);
		logger.info(logMsg);
		String updateSQL="update TBL_USER_PROFILE set USER_STATUS=? where CIS_NO=?";
		getJdbcTemplate().setDataSource(dataSourceDCP);
		row = getJdbcTemplate().update(updateSQL, new Object[] {userStatus, cisNo});
		logMsg = String.format("update TBL_USER_PROFILE impacted row=%s",row);
		logger.info(logMsg);
		return row;
	}
	
	public List<Map<String, Object>> getUserProfiles() throws BatchException {
		String selectSQL = "SELECT TOP 10 ID, CIS_NO, USER_STATUS FROM TBL_USER_PROFILE";
		try {
			getJdbcTemplate().setDataSource(dataSourceDCP);
			return getJdbcTemplate().queryForList(selectSQL);
		} catch(Exception e) {
			String errorMessage = String.format("Error happened while executing [%s]", selectSQL);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}
	
	public List<Map<String, Object>> getActiveUserProfiles() throws BatchException {
		String selectSQL = "SELECT ID, CIS_NO, USER_STATUS FROM TBL_USER_PROFILE WHERE USER_STATUS='A'";
		try {
			getJdbcTemplate().setDataSource(dataSourceDCP);
			return getJdbcTemplate().queryForList(selectSQL);
		} catch(Exception e) {
			String errorMessage = String.format("Error happened while executing [%s]", selectSQL);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}
	
}
