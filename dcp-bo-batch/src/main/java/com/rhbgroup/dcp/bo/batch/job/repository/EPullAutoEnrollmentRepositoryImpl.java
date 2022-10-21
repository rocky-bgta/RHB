package com.rhbgroup.dcp.bo.batch.job.repository;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.EPullAutoEnrollmentDetails;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.EPullAutoEnrollmentRowMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

@Component
@Lazy
public class EPullAutoEnrollmentRepositoryImpl implements EPullAutoEnrollmentRepository {

	private static final Logger logger = Logger.getLogger(EPullAutoEnrollmentRepositoryImpl.class);

	@Autowired
	@Qualifier("dataSourceDCP")
	private DataSource dataSourceDCP;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<EPullAutoEnrollmentDetails> getEPullAutoEnrollmentAccount(Integer userId, String selectQuery) throws BatchException {
		try {
			logger.debug(String.format("GetEPullAutoEnrollmentAccount from userId[%s] sql: %s", userId, selectQuery));
			jdbcTemplate.setDataSource(dataSourceDCP);
			List<EPullAutoEnrollmentDetails> ePullAutoEnrollmentDetails = jdbcTemplate.query(selectQuery, new EPullAutoEnrollmentRowMapper(), userId);
			logger.trace(String.format("EPull Auto Enrollment account retrieved successfully from DB [%s]", ePullAutoEnrollmentDetails));
			return ePullAutoEnrollmentDetails;
		} catch (Exception e) {
			String errorMessage = String.format("Error happened while getting EPull Auto Enrollment account for user id: %s", userId);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}

	public int updateEPullStatus(Integer userId) {
		int row = 0;
		logger.info(String.format("update epull_status tbl_user_profile user_id=%s", userId));

		try {
			jdbcTemplate.setDataSource(dataSourceDCP);
			String updateSql = " update tbl_user_profile set epull_status = 10000 where id = ?";
			logger.debug(String.format("sql=%s",updateSql));
			row = jdbcTemplate.update(updateSql, userId);
			logger.info(String.format("%s row affected.",row));
		} catch(Exception ex) {
			logger.info(String.format("exception when updating epull_status tbl_user_profile ex=%s", ex.getMessage()));
			logger.error(ex);
		}
		return row;
	}

	public String getCisNoBy(String userId) throws BatchException {

		String selectQuery = "select cis_no from dcp.dbo.tbl_user_profile where id = ?";

		try {
			logger.debug(String.format("GetCisNoBy from userId[%s] sql: %s", userId, selectQuery));
			jdbcTemplate.setDataSource(dataSourceDCP);
			String cisNo = jdbcTemplate.queryForObject(selectQuery, new Object[]{userId}, String.class);
			logger.trace(String.format("Retrieved CIS_NO from user profile table [%s]", cisNo));

			return cisNo;
		} catch (Exception e) {
			String errorMessage = String.format("Error happened while getting CIS_NO for user id: %s", userId);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}
}

