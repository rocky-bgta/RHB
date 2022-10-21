package com.rhbgroup.dcp.bo.batch.framework.repository;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;

@Component
public class AuditLogRepositoryImpl extends BaseRepositoryImpl {	
	/* Note:
	 * Fetching System Batch Date directly from TBL_BATCH_CONFIG by SQL instead of passing date as parameter from spring batch system
	 * Because that way was taking long. So we had to do this exceptional way.
	*/
	private static final String INSERT_INTO_STAG_TABLE = "INSERT INTO TBL_BATCH_STAGED_AUDIT_LOG SELECT * FROM vw_report_audit_log "
			+ " WHERE CONVERT(VARCHAR,timestamp, 112) = (SELECT CONVERT(VARCHAR,DATEADD(day, -1, parameter_value),112) FROM TBL_BATCH_CONFIG "
			+ " WHERE PARAMETER_KEY = ?)";
	private static final String TRUNCATE_STAG_TABLE = "TRUNCATE TABLE TBL_BATCH_STAGED_AUDIT_LOG";
	private static final Logger logger = Logger.getLogger(AuditLogRepositoryImpl.class);

	public void runAuditLogQuery() throws BatchException {
    	try {
    		Object[] params = new Object[] { BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY };
    		getJdbcTemplate().execute(TRUNCATE_STAG_TABLE);
    		getJdbcTemplate().update(INSERT_INTO_STAG_TABLE, params);
    		logger.info("TBL_BATCH_STAGED_AUDIT_LOG successfully populated with data from vw_report_audit_log");
        } catch (Exception e) {
        	logger.error("Unable to populate TBL_BATCH_STAGED_AUDIT_LOG ", e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
}
