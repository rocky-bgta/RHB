package com.rhbgroup.dcp.bo.batch.framework.repository;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CommonStagingImpl extends BaseRepositoryImpl {

    private static final Logger logger = Logger.getLogger(CommonStagingImpl.class);
    private static String TRUNCATE_SQL = "truncate table %s";

    public void executeStagingTable(String truncateTable, String insertIntoStagingTable) throws BatchException {
        try {
            logger.debug("table to truncate: " + truncateTable);
            logger.debug("staging script: " + insertIntoStagingTable);
            getJdbcTemplate().execute(String.format(TRUNCATE_SQL, truncateTable));
            getJdbcTemplate().execute(insertIntoStagingTable);
            logger.info("successfully populated with data");
        } catch (Exception e) {
            logger.error("Unable to populate Stage table", e);
            throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }
}
