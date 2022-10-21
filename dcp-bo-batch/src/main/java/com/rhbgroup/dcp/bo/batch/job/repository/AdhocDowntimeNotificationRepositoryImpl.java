package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.Map;


import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;

@Component
@Lazy
public class AdhocDowntimeNotificationRepositoryImpl extends BaseRepositoryImpl {
	
	private static final Logger logger = Logger.getLogger(AdhocDowntimeNotificationRepositoryImpl.class);

	String logMsg="";
	
	public Map<String, Object>  getTemplateFieldFormBatchNotificationTemplateByEventCode(String eventCode) throws BatchException {
		String selectSQL =  "SELECT PUSH_TITLE_TEMPLATE, PUSH_BODY_TEMPLATE FROM TBL_BO_BATCH_NOTIFICATION_TEMPLATE WHERE EVENT_CODE=?";
		try {
			getJdbcTemplate().setDataSource(dataSource);
			return getJdbcTemplate().queryForMap(selectSQL, eventCode);
		} catch(Exception e) {
			String errorMessage = String.format("Error happened while executing [%s]", selectSQL);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}
	
}
