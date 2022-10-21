package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKBillerPaymentJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerPaymentConfig;

@Component
public class BatchBillerPaymentConfigRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchBillerPaymentConfigRepositoryImpl.class);
	
	@Autowired
	LoadIBKBillerPaymentJobConfigProperties configProperties;
    
    public List<BatchBillerPaymentConfig> getBatchBillerPaymentConfigsForLoadIBKBillerPaymentJob() {
    	List<BatchBillerPaymentConfig> batchBillerPaymentConfigs = new ArrayList<>();
    	String selectQuery = "SELECT IBK_FTP_FOLDER, FILE_NAME_FORMAT, BILLER_CODE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE TEMPLATE_NAME=? AND IS_REQUIRED_TO_EXECUTE=1";
    	
    	logger.debug(String.format("Retrieving BatchBillerPaymentConfigs from DB using SQL [%s] filter by template name [%s]", selectQuery, configProperties.getTemplateName()));
    	List<Map<String, Object>> rows = getJdbcTemplate().queryForList(selectQuery, configProperties.getTemplateName());
    	
    		for (Map<String, Object> row : rows) {
    			BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    			batchBillerPaymentConfig.setIbkFtpFolder((String)row.get("IBK_FTP_FOLDER"));
    			batchBillerPaymentConfig.setFileNameFormat((String)row.get("FILE_NAME_FORMAT"));
    			batchBillerPaymentConfig.setBillerCode((String)row.get("BILLER_CODE"));
    			
    			batchBillerPaymentConfigs.add(batchBillerPaymentConfig);
    	}
    	
    	logger.debug(String.format("BatchBillerPaymentConfigs retrieved successfully from DB [%s]", batchBillerPaymentConfigs));
    	return batchBillerPaymentConfigs;
    }
    
	public int updateBatchBillerPaymentConfigExecuteFlag(BatchBillerPaymentConfig billerPaymentConfig, int requiredExecute) {
    	int impacted=0;
    	String updateSQL="UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE=?, UPDATED_BY=?, UPDATED_TIME=?"
    			+" WHERE ID=?";
    	impacted=getJdbcTemplate().update(updateSQL, requiredExecute, billerPaymentConfig.getUpdatedBy(), billerPaymentConfig.getUpdatedTime(), billerPaymentConfig.getId());
    	logger.debug(String.format("BatchBillerPaymentConfigs update IS_REQUIRED_TO_EXECUTE in DB impacted row [%s]", impacted));
    	return impacted;
    }
}