package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUserMaintAutoAging;

@Component
public class BoUserRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BoUserRepositoryImpl.class);
	
	public int updateUserStatus(BatchUserMaintAutoAging batchUserMaintAutoAging, String updatedBy) throws BatchException {
        try {        	
        	String currentDate = DateUtils.formatDateString(new Date(), "yyyy-MM-dd HH:mm:ss");
        	String updateSql = String.format("UPDATE TBL_BO_USER SET USER_STATUS_ID='%s', UPDATED_TIME='%s', UPDATED_BY='%s' WHERE ID=?", 
    			batchUserMaintAutoAging.getNewUserStatus(),
    			currentDate,
    			updatedBy);
        	
        	return getJdbcTemplate().update(updateSql, batchUserMaintAutoAging.getUserId());
        } catch (Exception e) {
        	String errorMessage = String.format("Error happened while updating record with ID [%s] in TBL_BO_USER column USER_STATUS_ID value as [%s]", batchUserMaintAutoAging.getUserId(), batchUserMaintAutoAging.getNewUserStatus());
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }

    public String getUserID(String username){
		String query = String.format("Select id from TBL_BO_USER WHERE USERNAME=\'%s\'",username);
		String id = getJdbcTemplate().queryForObject(query, String.class);
		return id;
	}
	
}
