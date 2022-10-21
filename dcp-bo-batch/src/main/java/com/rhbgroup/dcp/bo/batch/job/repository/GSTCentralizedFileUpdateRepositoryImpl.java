package com.rhbgroup.dcp.bo.batch.job.repository;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchLookup;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.GSTCentralizedFileUpdateDetail;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class GSTCentralizedFileUpdateRepositoryImpl extends BaseRepositoryImpl {

    final static Logger logger = Logger.getLogger(GSTCentralizedFileUpdateRepositoryImpl.class);

    private String stagingTableName = "TBL_BATCH_STAGED_GST_UPDATE";
    private String dcpGSTTableName = "TBL_GST_CONFIG";
    private String batchSuspenseTableName = "TBL_BATCH_SUSPENSE";
    private String selectQuery = "SELECT *";

    @Qualifier("dataSourceDCP")
    @Autowired
    DataSource dataSourceDCP;

    @Autowired
    private BatchSuspenseRepositoryImpl batchSuspenseRepoImpl;

    public Boolean addGSTCentralizedFileUpdateStaging(GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail) {
        try {
            jdbcTemplate.setDataSource(dataSource);
            String insertSQL =
                    "INSERT INTO " + stagingTableName + " (" +
                            "JOB_EXECUTION_ID, IS_PROCESSED, FILE_NAME, HD_DATE, HD_TIME, RECORD_INDICATOR, " +
                            "ENTITY_CODE, ENTITY_INDICATOR, UNIQUE_ID, SOURCE_SYSTEM_ID, TRANSACTION_IDENTIFIER, " +
                            "TRANSACTION_DESCRIPTION, GST_RATE, TREATMENT_TYPE, TAX_CODE, CALCULATION_METHOD, " +
                            "GL_ACCOUNT_CODE_CHARGES, START_DATE, END_DATE, LAST_UPDATE_DATE, LAST_UPDATE_TIME, " +
                            "LAST_UPDATE_BY, HOST_TRAN_OR_GST_GL_CODE, FILLER) " +
                            "Values (?,0,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            jdbcTemplate.update(insertSQL,new Object[] {
                    gstCentralizedFileUpdateDetail.getJobExecutionId(),
                    gstCentralizedFileUpdateDetail.getFileName(),
                    gstCentralizedFileUpdateDetail.getHdDate(),
                    gstCentralizedFileUpdateDetail.getHdTime(),
                    gstCentralizedFileUpdateDetail.getRecordIndicator(),
                    gstCentralizedFileUpdateDetail.getEntityCode(),
                    gstCentralizedFileUpdateDetail.getEntityIndicator(),
                    gstCentralizedFileUpdateDetail.getUniqueId(),
                    gstCentralizedFileUpdateDetail.getSourceSystemId(),
                    gstCentralizedFileUpdateDetail.getTransactionIdentifier(),
                    gstCentralizedFileUpdateDetail.getTransactionDescription(),
                    gstCentralizedFileUpdateDetail.getGstRate(),
                    gstCentralizedFileUpdateDetail.getTreatmentType(),
                    gstCentralizedFileUpdateDetail.getTaxCode(),
                    gstCentralizedFileUpdateDetail.getCalculationMethod(),
                    gstCentralizedFileUpdateDetail.getGlAccountCodeCharges(),
                    gstCentralizedFileUpdateDetail.getStartDate(),
                    gstCentralizedFileUpdateDetail.getEndDate(),
                    gstCentralizedFileUpdateDetail.getLastUpdateDate(),
                    gstCentralizedFileUpdateDetail.getLastUpdateTime(),
                    gstCentralizedFileUpdateDetail.getLastUpdateBy(),
                    gstCentralizedFileUpdateDetail.getHostTranOrGSTGLCode(),
                    gstCentralizedFileUpdateDetail.getFiller().replace("|", "")
            });

            return true;
        } catch (Exception e) {

            logger.error(e.getMessage());

            return false;
        }
    }

    // Get all unprocessed file from staging table
    public List<GSTCentralizedFileUpdateDetail> getUnprocessedGSTCentralizedStatusFromStaging(String jobExecutionId){

        jdbcTemplate.setDataSource(dataSource);
        String logMsg = String.format("Query from Table=%s, status jobExecutionId=%s", stagingTableName, jobExecutionId);
        logger.info(logMsg);

        String selectClause = "SELECT ISNULL(JOB_EXECUTION_ID, '' ) AS jobExecutionId" +
                ",ISNULL(FILE_NAME,'') as fileName" +
                ",ISNULL(HD_DATE,'') as hdDate" +
                ",ISNULL(HD_TIME,'') as hdTime" +
                ",ISNULL(RECORD_INDICATOR,'') as recordIndicator" +
                ",ISNULL(ENTITY_CODE,'') as entityCode" +
                ",ISNULL(ENTITY_INDICATOR,'') as entityIndicator" +
                ",ISNULL(UNIQUE_ID,'') as uniqueId" +
                ",ISNULL(SOURCE_SYSTEM_ID,'') as sourceSystemId" +
                ",ISNULL(TRANSACTION_IDENTIFIER,'') as transactionIdentifier " +
                ",ISNULL(TRANSACTION_DESCRIPTION,'') as transactionDescription" +
                ",ISNULL(GST_RATE,'') as gstRate" +
                ",ISNULL(TREATMENT_TYPE,'') as treatmentType" +
                ",ISNULL(TAX_CODE,'') as taxCode" +
                ",ISNULL(CALCULATION_METHOD,'') as calculationMethod" +
                ",ISNULL(GL_ACCOUNT_CODE_CHARGES,'') as glAccountCodeCharges" +
                ",ISNULL(START_DATE,'') as startDate" +
                ",ISNULL(END_DATE,'') as endDate" +
                ",ISNULL(LAST_UPDATE_DATE,'') as lastUpdateDate" +
                ",ISNULL(LAST_UPDATE_TIME,'') as lastUpdateTime" +
                ",ISNULL(LAST_UPDATE_BY,'') as lastUpdateBy" +
                ",ISNULL(HOST_TRAN_OR_GST_GL_CODE,'') as hostTranOrGSTGLCode" +
                ",ISNULL(FILLER,'') as filler" ;

        String fromClause = " FROM " + stagingTableName;
        String whereClause = " WHERE job_execution_id = ? and is_processed = 0";

        String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();
        logMsg = String.format("getUnprocessedGSTCentralizedStatusFromStaging SQL Statement = %s", sql);
        logger.info(logMsg);

        return jdbcTemplate.query(sql
                , new Object[]{jobExecutionId}
                , new BeanPropertyRowMapper(GSTCentralizedFileUpdateDetail.class));
    }

    // Get all lookup value
    public List<BatchLookup> getBatchLookUpValue (String whereLookupValue){
        jdbcTemplate.setDataSource(dataSource);

        String selectClause = "SELECT \"group\", value ";
        String fromClause = " FROM TBL_BATCH_LOOKUP";
        String whereClause = " WHERE \"group\" = ?";

        String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();

        return jdbcTemplate.query(sql,new Object[] {whereLookupValue} ,new BeanPropertyRowMapper(BatchLookup.class));

    }

    // Get all date from GST Config DCP
    public List<GSTCentralizedFileUpdateDetail> getGSTFromDCP(){

        jdbcTemplate.setDataSource(dataSource);
        String logMsg = "";

        String selectClause = selectQuery ;

        String fromClause = " FROM vw_batch_gst_centralized_file_update";
        String whereClause = "";

        String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();
        logMsg = String.format("getGSTFromDCP SQL Statement = %s", sql);
        logger.info(logMsg);

        return jdbcTemplate.query(sql
                , new BeanPropertyRowMapper(GSTCentralizedFileUpdateDetail.class));

    }

    // This is the method to insert into batch suspense
    public int insertTblBatchSuspense(BatchSuspense batchSuspense ) {
        String logMsg="";
        int inserted = 0;
        try {
            logMsg = String.format("Insert record into =%s,  job exec id=%s, jobname=%s, suspense column=%s, suspense mesg=%s"
                    ,batchSuspenseTableName, batchSuspense.getJobExecutionId(), batchSuspense.getBatchJobName(),
                    batchSuspense.getSuspenseColumn(), batchSuspense.getSuspenseMessage());
            logger.info(logMsg);
            inserted = batchSuspenseRepoImpl.addBatchSuspenseToDB(batchSuspense);
            return inserted;
        }catch(Exception ex) {
            logMsg = String.format("Insert record into TBL_BATCH_SUSPENSE exception=%s", ex.getMessage());
            logger.info(logMsg);
            logger.error(ex);
        }
        return inserted;
    }

    // This is the method to update GST Config in DCP
    public boolean updateGSTDCP (String setSQL, List<String> parameterToUpdate) {

        String logMsg="";
        Object[] parameter = parameterToUpdate.toArray();

        boolean success = false;
        try {
            jdbcTemplate.setDataSource(dataSourceDCP);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE ");
            stringBuilder.append(dcpGSTTableName + " ");
            stringBuilder.append(setSQL);
            stringBuilder.append(", UPDATED_BY = ?");
            stringBuilder.append(", UPDATED_TIME = ?");
            stringBuilder.append(" WHERE UNIQUE_ID = ? AND SOURCE_SYSTEM = ? AND TXN_IDENTIFIER = ? ");

            int rowsAffected = jdbcTemplate.update(stringBuilder.toString(), parameter);

            logMsg = String.format("Update TBL_GST_CONFIG : [%s] Rows affected : %s", stringBuilder, rowsAffected);
            logger.info(logMsg);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return success;
        }
    }

    // This is the method to insert into GST Config in DCP
    public boolean insertNewGSTToDB(GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail) throws BatchException {
        try {
            //Data source for DCP
            jdbcTemplate.setDataSource(dataSourceDCP);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formatDateTime = LocalDateTime.now().format(formatter);
            String createdBy = "LDCPD8923B";

            String insertQuery = "INSERT INTO TBL_GST_CONFIG "
                    + "(TXN_TYPE, MAIN_FUNCTION, SUB_FUNCTION, ENTITY_CODE, ENTITY_INDICATOR, " +
                    "UNIQUE_ID, SOURCE_SYSTEM, TXN_IDENTIFIER, TXN_DESCRIPTION, GST_RATE, " +
                    "TREATMENT_TYPE, TAX_CODE, CALCULATION_METHOD, GL_ACCOUNT_CODE, BEGIN_DATE, " +
                    "END_DATE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY, PAYMENT_METHOD) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            logger.info(String.format("Insert GST Config [%s] to DB", gstCentralizedFileUpdateDetail));

            int rowsAffected = jdbcTemplate.update(insertQuery,
                    gstCentralizedFileUpdateDetail.getTxnType(),
                    gstCentralizedFileUpdateDetail.getMainFunction(),
                    gstCentralizedFileUpdateDetail.getSubFunction(),
                    gstCentralizedFileUpdateDetail.getEntityCode(),
                    gstCentralizedFileUpdateDetail.getEntityIndicator(),
                    gstCentralizedFileUpdateDetail.getUniqueId(),
                    gstCentralizedFileUpdateDetail.getSourceSystemId(),
                    gstCentralizedFileUpdateDetail.getTransactionIdentifier(),
                    gstCentralizedFileUpdateDetail.getTransactionDescription(),
                    gstCentralizedFileUpdateDetail.getGstRate(),
                    gstCentralizedFileUpdateDetail.getTreatmentType(),
                    gstCentralizedFileUpdateDetail.getTaxCode(),
                    gstCentralizedFileUpdateDetail.getCalculationMethod(),
                    gstCentralizedFileUpdateDetail.getGlAccountCodeCharges(),
                    gstCentralizedFileUpdateDetail.getStartDate(),
                    gstCentralizedFileUpdateDetail.getEndDate(),
                    formatDateTime,
                    createdBy,
                    formatDateTime,
                    createdBy,
                    gstCentralizedFileUpdateDetail.getPaymentMethod()
            );

            logger.info(String.format("Insert into TBL_GST_CONFIG rows affected : %s", rowsAffected));
            return true;

        } catch (Exception e) {
            String errorMessage = String.format("INSERT into TBL_GST_CONFIG ERROR [%s]", e.getMessage());
            logger.error(errorMessage);
            throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
        }
    }

    // Get all data necessarily for creating a new update
    public List<GSTCentralizedFileUpdateDetail> getGSTConfigEssentialData(String uniqueId, String systemSource, String txnIdentifier){

        jdbcTemplate.setDataSource(dataSource);
        String logMsg = "";

        String selectClause = selectQuery ;

        String fromClause = " FROM vw_batch_gst_data_extraction";
        String whereClause = " WHERE UNIQUE_ID = ? " +
                " AND SOURCE_SYSTEM = ? " +
                " AND TXN_IDENTIFIER = ? ";

        String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();
        logMsg = String.format("getGSTConfigEssentialData SQL Statement = %s", sql);
        logger.info(logMsg);

        return jdbcTemplate.query(sql, new Object[]{uniqueId, systemSource, txnIdentifier}
                , new BeanPropertyRowMapper(GSTCentralizedFileUpdateDetail.class));

    }

    // Get all data necessarily for creating a new update
    public List<GSTCentralizedFileUpdateDetail> checkGSTConfigNewRecord(String jobExecutionId){

        String logMsg = "";

        JdbcTemplate jdbcTemplate= getJdbcTemplate();

        String selectClause = selectQuery ;

        String fromClause = " FROM vw_batch_gst_config_check_new_record";
        String whereClause = " WHERE JOB_EXECUTION_ID = ? ";

        String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();
        logMsg = String.format("checkGSTConfigNewRecord SQL Statement = %s", sql);
        logger.info(logMsg);

        return jdbcTemplate.query(sql, new Object[] {jobExecutionId}
                , new BeanPropertyRowMapper(GSTCentralizedFileUpdateDetail.class));

    }

    // Get all data necessarily for creating a new update
    public GSTCentralizedFileUpdateDetail getGSTNewValue(String uniqueId, String systemSource, String txnIdentifier, String jobExecutionId){

        jdbcTemplate.setDataSource(dataSource);
        String logMsg = "";

        String selectClause = selectQuery ;

        String fromClause = " FROM " + stagingTableName;
        String whereClause = " WHERE UNIQUE_ID = ? " +
                "AND SOURCE_SYSTEM_ID = ? " +
                "AND TRANSACTION_IDENTIFIER = ? " +
                "AND JOB_EXECUTION_ID = ? ";

        String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();
        logMsg = String.format("getGSTNewValue SQL Statement = %s", sql);
        logger.info(logMsg);

        List<GSTCentralizedFileUpdateDetail> dataList = jdbcTemplate.query(sql
                , new Object[] {uniqueId, systemSource, txnIdentifier, jobExecutionId}
                , new BeanPropertyRowMapper(GSTCentralizedFileUpdateDetail.class));

        return (dataList.size() != 1) ? dataList.get(0) : dataList.get(dataList.size()-1);

    }

    // This is the method to update success batch processed
    public int updateProcessStatus (List<String> parameterToUpdate) {

        String logMsg="";
        Object[] parameter = parameterToUpdate.toArray();

        try {
            jdbcTemplate.setDataSource(dataSource);
            String updateSQL = "UPDATE " + stagingTableName + " ";
            updateSQL += " SET IS_PROCESSED = 1";
            updateSQL += " WHERE UNIQUE_ID = ? AND SOURCE_SYSTEM_ID = ? AND TRANSACTION_IDENTIFIER = ? AND JOB_EXECUTION_ID = ? ";

            int rowsAffected = jdbcTemplate.update(updateSQL, parameter);

            logMsg = String.format("Update %s  : [%s] Rows affected : %s", stagingTableName, updateSQL, rowsAffected);
            logger.info(logMsg);
            return rowsAffected;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 0;
        }
    }
}
