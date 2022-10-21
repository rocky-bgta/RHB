package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.PremierCustomerInfoandRMCodeTaggingDetail;

@Component
public class PremierCustomerInfoandRMCodeTaggingRepositoryImpl extends BaseRepositoryImpl {

    final static Logger logger = Logger.getLogger(PremierCustomerInfoandRMCodeTaggingRepositoryImpl.class);

    private String stagingTableName = "TBL_BATCH_STAGED_PREMIER_RM_TAG";

    @Qualifier("dataSourceDCP")
    @Autowired
    DataSource dataSourceDCP;

    @Autowired
    private BatchSuspenseRepositoryImpl batchSuspenseRepoImpl;

	public int addRecordBatch(List<? extends PremierCustomerInfoandRMCodeTaggingDetail> records) {
		
		jdbcTemplate.setDataSource(dataSource);
        String sql =
            "INSERT INTO " + stagingTableName + " (" +
            "JOB_EXECUTION_ID, FILE_NAME, HD_PROCESSING_DT, HD_SYSTEM_DT, HD_SYSTEM_TIME, INDICATOR, " + //6
            "CIF_NO, RELATIONSHIP_MANAGER_CODE, CIS_NO2, FULL_NAME, CIS_NO3, ID_NO, " + //6
            "CIS_NO4, STAFF_IND, \"END\", IS_PROCESSED, CREATED_TIME) " + //5
            "Values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,?)";
        
		int [] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				PremierCustomerInfoandRMCodeTaggingDetail customer = records.get(i);
				ps.setString(1, customer.getJobExecutionId());
				ps.setString(2, customer.getFileName());
				ps.setString(3, customer.getProcessingDt());
				ps.setString(4, customer.getSystemDt());
				ps.setString(5, customer.getSystemTime());
				ps.setString(6,	customer.getIndicator());
				ps.setString(7, customer.getCifNo());
				ps.setString(8, customer.getRmCode());
				ps.setString(9, customer.getCisNo2());
				ps.setString(10, customer.getFullNm());
				ps.setString(11, customer.getCisNo3());
				ps.setString(12, customer.getIdNo());
				ps.setString(13, customer.getCisNo4());
				ps.setString(14, customer.getStaffInd());
				ps.setString(15, customer.getEnd());
				ps.setString(16, customer.getCreatedTime());
		
			}
					
			@Override
			public int getBatchSize() {
				return records.size();
			}
		  });
		return row.length;
	}
	
	public int updateUserProfileBatch(List<PremierCustomerInfoandRMCodeTaggingDetail> records) {
		
		jdbcTemplate.setDataSource(dataSourceDCP);
        String sql = "UPDATE TBL_USER_PROFILE SET IS_PREMIER = 1 WHERE ID = ?;";
        
		int [] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				PremierCustomerInfoandRMCodeTaggingDetail customer = records.get(i);
				ps.setString(1, customer.getId());
			}
					
			@Override
			public int getBatchSize() {
				return records.size();
			}
		  });
		return row.length;
	}
	
    // Use to truncate a table being passed down
    public int truncateTable(String tableName){

        jdbcTemplate.setDataSource(dataSource);
        String logMsg = "";

        String sql = "TRUNCATE TABLE %s";
        logMsg = String.format("SQL Statement = %s", sql);
        logger.info(logMsg);

        int rowAffected = jdbcTemplate.update(sql
                , new Object[] {tableName});

        return rowAffected;
    }

    // Get new value or updated value
    public List<PremierCustomerInfoandRMCodeTaggingDetail> getBatchPremierNeworUpdatedValue (String jobexecutionid){
        jdbcTemplate.setDataSource(dataSource);
        List<PremierCustomerInfoandRMCodeTaggingDetail> batchPremierRMTagNewUpdatedValueList;

        String selectClause = "SELECT * ";
        String fromClause = " FROM vw_batch_premier_check_new_or_update_value";
        String whereClause = " WHERE JOB_EXECUTION_ID = ? ";

        String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();

        batchPremierRMTagNewUpdatedValueList = jdbcTemplate.query(sql,
                new Object[] {jobexecutionid},
                new BeanPropertyRowMapper(PremierCustomerInfoandRMCodeTaggingDetail.class));

        return batchPremierRMTagNewUpdatedValueList;
    }

    // Get all value from batch staged premier rm tag
    public List<PremierCustomerInfoandRMCodeTaggingDetail> getCIFNotFoundRecord (String jobexecutionid){
        jdbcTemplate.setDataSource(dataSource);
        List<PremierCustomerInfoandRMCodeTaggingDetail> cifNotFoundList;

        String selectClause = "SELECT * ";
        String fromClause = " FROM vw_batch_premier_check_cif_not_found_record";
        String whereClause = " WHERE JOB_EXECUTION_ID = ? ";

        String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();

        cifNotFoundList = jdbcTemplate.query(sql,
                new Object[] {jobexecutionid},
                new BeanPropertyRowMapper(PremierCustomerInfoandRMCodeTaggingDetail.class));

        return cifNotFoundList;
    }

    // This is the method to update success batch processed
    public int updateProcessStatus (List<String> parameterToUpdate) {

        String logMsg="";
        Object[] parameter = parameterToUpdate.toArray();

        try {
            jdbcTemplate.setDataSource(dataSource);
            String updateSQL = "UPDATE " + stagingTableName + " ";
            updateSQL += " SET IS_PROCESSED = 1, UPDATED_TIME = ? ";
            updateSQL += " WHERE CIF_NO = ? AND JOB_EXECUTION_ID = ? ";

            int rowsAffected = jdbcTemplate.update(updateSQL, parameter);

            logMsg = String.format("Update %s  : [%s] Rows affected : %s", stagingTableName, updateSQL, rowsAffected);
            logger.debug(logMsg);
            return rowsAffected;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 0;
        }
    }

    // This is the method to insert into batch suspense
    public boolean insertTblBatchSuspense(BatchSuspense batchSuspense ) {
        String logMsg="";
        boolean success=false;
        try {
            logMsg = String.format("Job execution id=%s, jobname=%s, suspense column=%s, suspense mesg=%s",
                    batchSuspense.getJobExecutionId(),
                    batchSuspense.getBatchJobName(),
                    batchSuspense.getSuspenseColumn(),
                    batchSuspense.getSuspenseMessage());

            logger.info(logMsg);
            success = (batchSuspenseRepoImpl.addBatchSuspenseToDB(batchSuspense) == 1) ? true : false;
        }catch(Exception ex) {
            logMsg = String.format("Insert record into TBL_BATCH_SUSPENSE exception=%s", ex.getMessage());
            logger.info(logMsg);
            logger.error(ex);
        }
        return success;
    }

    // This is the method to update User Profile in DCP
    public boolean updateTableUserProfile (String setSQL, List<String> parameterToUpdate) {

        String logMsg="";
        Object[] parameter = parameterToUpdate.toArray();

        try {
            jdbcTemplate.setDataSource(dataSourceDCP);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE TBL_USER_PROFILE ");
            stringBuilder.append(setSQL);
            stringBuilder.append(" WHERE ID = ? ");

            int rowsAffected = jdbcTemplate.update(stringBuilder.toString(), parameter);

            logMsg = String.format("Update TBL_USER_PROFILE : [%s] Rows affected : %s", stringBuilder, rowsAffected);
            logger.debug(logMsg);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
