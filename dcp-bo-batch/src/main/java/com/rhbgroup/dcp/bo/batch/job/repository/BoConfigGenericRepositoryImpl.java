package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BoConfigGeneric;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BoConfigGenericRowMapper;

@Component
public class BoConfigGenericRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BoConfigGenericRepositoryImpl.class);
	
	public List<BoConfigGeneric> getUserStatusDaysConfig() throws BatchException {
		try {
			String selectQuery = String.format("SELECT ID, CONFIG_CODE, CONFIG_DESC FROM TBL_BO_CONFIG_GENERIC WHERE CONFIG_TYPE='user_security' AND CONFIG_CODE IN('%s','%s')", 
				BatchJobParameter.BATCH_JOB_PARAMETER_DB_DAYS_TO_INACTIVE_USER, BatchJobParameter.BATCH_JOB_PARAMETER_DB_DAYS_TO_DELETE_USER);

			logger.debug(String.format("Getting BoConfigGeneric from DB using SQL [%s]", selectQuery));
			JdbcTemplate jdbcTemplate = getJdbcTemplate();
			List<BoConfigGeneric> boConfigGenerics = jdbcTemplate.query(selectQuery, new BoConfigGenericRowMapper());
			
	    	logger.trace(String.format("BoConfigGenerics retrieved successfully from DB [%s]", boConfigGenerics));
	    	return boConfigGenerics;
		} catch (Exception e) {
        	String errorMessage = "Error retrieving days info to update user status from DB";
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }  	
    }
}
