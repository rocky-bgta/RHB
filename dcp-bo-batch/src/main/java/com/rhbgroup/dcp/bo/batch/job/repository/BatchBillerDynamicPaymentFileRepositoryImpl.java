package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerDynamicPaymentConfig;

@Component
public class BatchBillerDynamicPaymentFileRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchBillerDynamicPaymentFileRepositoryImpl.class);

    
	public int updateBatchBillerDynamicPaymentFilePath(BatchBillerDynamicPaymentConfig billerPaymentConfig, int isGenerated,Date batchProcessingDate) {
    	int impacted=0;
    	String updateSQL="UPDATE TBL_BATCH_BILLER_PAYMENT_FILE SET FILE_GENERATED_PATH=?, IS_FILE_GENERATED=?, UPDATED_BY=?, UPDATED_TIME=?"
    			+" WHERE BILLER_CODE=? AND CAST(FILE_DATE AS DATE)=?";
    	impacted=getJdbcTemplate().update(updateSQL, billerPaymentConfig.getFtpFolder(),isGenerated, billerPaymentConfig.getUpdatedBy(), billerPaymentConfig.getUpdatedTime(), billerPaymentConfig.getBillerCode(),batchProcessingDate);
    	logger.debug(String.format("BatchBillerPaymentFiles update path in DB impacted row [%s]", impacted));
    	return impacted;
    }
}