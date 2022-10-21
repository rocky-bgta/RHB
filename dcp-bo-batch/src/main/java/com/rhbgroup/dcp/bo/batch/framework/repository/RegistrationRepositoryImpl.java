package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.BoRegistrationBranch;
import com.rhbgroup.dcp.bo.batch.job.model.BoRegistrationMode;
import com.rhbgroup.dcp.bo.batch.job.model.BoRegistrationState;
import com.rhbgroup.dcp.bo.batch.job.model.RegistrationToken;
import com.rhbgroup.dcp.bo.batch.job.model.UserProfile;

@Component
public class RegistrationRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(RegistrationRepositoryImpl.class);

	public void insertStagedData(Date batchProcessingDate)  {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(batchProcessingDate);
		logger.info(String.format("  strDate=%s", strDate));

		int rows = deleteBoRegistrationData();
		logger.info(String.format("Deleted rows count = %s", rows));

		int regrows = insertBoFailRegistration(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_REGISTRATION = %s", regrows));

		int regSuccessrows = insertBoSuccessRegistration(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_REGISTRATION = %s", regSuccessrows));

	}


	public int insertBoFailRegistration(String date) {
		int row =0;
		try {

			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_REGISTRATION"
					+ "(TOKEN, DEVICE_ID, USERNAME, EMAIL, CIS_NO, ACCOUNT_NUMBER, IS_OTP_VERIFIED, CHANNEL, AUDIT_ADDITIONALDATA, IS_STAFF, IS_ACTIVE, USER_TYPE, IS_PREMIER, NAME, ID_NO, ID_TYPE, MOBILE_NO, IP_ADDRESS, IS_ISLAMIC, IS_OTP_REGISTERED_MOBILE, ACCOUNT_TYPE, TXN_STATUS, RESIDENTIAL_STATE, BRANCH_INCENTIVE_CODE, REGISTRATION_DATE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY)"
					+ " SELECT a.TOKEN , a.DEVICE_ID , a.USERNAME, a.EMAIL , a.CIS_NO , a.ACCOUNT_NUMBER , a.IS_OTP_VERIFIED , a.CHANNEL , a.AUDIT_ADDITIONALDATA , a.IS_STAFF , a.IS_ACTIVE , a.USER_TYPE , a.IS_PREMIER , a.NAME, a.ID_NO, a.ID_TYPE, a.MOBILE_NO, a.IP_ADDRESS, a.IS_ISLAMIC, a.IS_OTP_REGISTERED_MOBILE, a.ACCOUNT_TYPE, 'Fail', b.RESIDENTIAL_STATE, b.BRANCH_INCENTIVE_CODE, a.CREATED_TIME , a.CREATED_TIME, a.CREATED_BY, a.UPDATED_TIME, a.UPDATED_BY"
					+ " FROM dcp.dbo.TBL_REGISTRATION_TOKEN a LEFT JOIN dcp.dbo.TBL_USER_PROFILE b ON a.USERNAME = b.USERNAME WHERE CREATED_TIME > =? AND CREATED_TIME < DATEADD(DAY,1,?) AND b.USERNAME IS NULL ";

			logger.info(String.format("insertBoFailRegistration sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
		} catch (Exception e) {
			logger.error(e);
		}
		return row;
	}

	public int insertBoSuccessRegistration(String date) {
		int row =0;

		try {

			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_REGISTRATION"
					+ "(TOKEN, DEVICE_ID, USERNAME, EMAIL, CIS_NO, ACCOUNT_NUMBER, IS_OTP_VERIFIED, CHANNEL, AUDIT_ADDITIONALDATA, IS_STAFF, IS_ACTIVE, USER_TYPE, IS_PREMIER, NAME, ID_NO, ID_TYPE, MOBILE_NO, IP_ADDRESS, IS_ISLAMIC, IS_OTP_REGISTERED_MOBILE, ACCOUNT_TYPE, TXN_STATUS, RESIDENTIAL_STATE, BRANCH_INCENTIVE_CODE, REGISTRATION_DATE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY)"
					+ " SELECT a.TOKEN , a.DEVICE_ID , a.USERNAME, a.EMAIL , a.CIS_NO , a.ACCOUNT_NUMBER , a.IS_OTP_VERIFIED , a.CHANNEL , a.AUDIT_ADDITIONALDATA , a.IS_STAFF , a.IS_ACTIVE , a.USER_TYPE , a.IS_PREMIER , a.NAME, a.ID_NO, a.ID_TYPE, a.MOBILE_NO, a.IP_ADDRESS, a.IS_ISLAMIC, a.IS_OTP_REGISTERED_MOBILE, a.ACCOUNT_TYPE, 'Success', b.RESIDENTIAL_STATE, b.BRANCH_INCENTIVE_CODE, a.CREATED_TIME , a.CREATED_TIME, a.CREATED_BY, a.UPDATED_TIME, a.UPDATED_BY"
					+ " FROM dcp.dbo.TBL_REGISTRATION_TOKEN a LEFT JOIN dcp.dbo.TBL_USER_PROFILE b ON a.USERNAME = b.USERNAME WHERE CREATED_TIME > =? AND CREATED_TIME < DATEADD(DAY,1,?) AND b.USERNAME IS NOT NULL  ";

			logger.info(String.format("insertBoSuccessRegistration sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
		} catch (Exception e) {
			logger.error(e);
		}
		return row;
	}

	public int deleteBoRegistrationData(){
		String sql = "TRUNCATE TABLE dcpbo.dbo.TBL_BO_REGISTRATION";
		logger.info(String.format("  sql delete bo registration=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql);

		return rowAffected;
	}


	public void insertSummary(Date batchProcessingDate)  {

		Date date = new Date();
		Timestamp now = new Timestamp(date.getTime());

		int rows = deleteBatchRegistrationMode(batchProcessingDate);
		logger.info(String.format("Deleted rows count for mode = %s", rows));

		int branchRows = deleteBatchRegistrationBranch(batchProcessingDate);
		logger.info(String.format("Deleted rows count for branch = %s", branchRows));

		int stateRows = deleteBatchRegistrationState(batchProcessingDate);
		logger.info(String.format("Deleted rows count for state = %s", stateRows));

		List<BoRegistrationMode> boRegistrationModeList = getBoRegistrationModeList(batchProcessingDate);
		logger.info(String.format("bo registration Mode list = %s", boRegistrationModeList));

		for(BoRegistrationMode boRegistrationMode:boRegistrationModeList) {
			String islamic = "Conventional";
			if(boRegistrationMode.getIsIslamic()) {
				islamic = "Islamic";
			}
			insertSummaryRegistrationMode(boRegistrationMode,batchProcessingDate,islamic,now);
		}

		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_REGISTRATION_MODE ");

		List<BoRegistrationBranch> boRegistrationBranchList = getBoRegistrationBranchList(batchProcessingDate);
		logger.info(String.format("bo registration branch list = %s", boRegistrationModeList));

		for(BoRegistrationBranch boRegistrationBranch:boRegistrationBranchList) {
			String first=" ";
			String last=" ";
			if(boRegistrationBranch.getBranchIncentiveCode() != null) {
				first = boRegistrationBranch.getBranchIncentiveCode().substring(0, 3);
				last = boRegistrationBranch.getBranchIncentiveCode().substring(3);
			}

			insertSummaryRegistrationBranch(boRegistrationBranch,first,last,batchProcessingDate,now);
		}

		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_REGISTRATION_BRANCH ");

		List<BoRegistrationState> boRegistrationStateList = getBoRegistrationStateList(batchProcessingDate);
		logger.info(String.format("bo registration State list = %s", boRegistrationStateList));

		for(BoRegistrationState boRegistrationState:boRegistrationStateList) {

			insertSummaryRegistrationState(boRegistrationState,batchProcessingDate,now);
		}

		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_REGISTRATION_STATE");

	}

	public int deleteBatchRegistrationMode(Date date){
		String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_REGISTRATION_MODE where CAST(DATE as DATE) = ?";
		logger.info(String.format("deleteBatchRegistrationMode sql=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql
				, new Object[] {date});

		return rowAffected;
	}

	public UserProfile getUserProfile(String username) {
		JdbcTemplate template = getJdbcTemplate();
		UserProfile userProfile = new UserProfile();
		String sql =  " SELECT * FROM dcp.dbo.TBL_USER_PROFILE a WHERE a.username = ?";
		logger.info(String.format("get user profile  sql=%s", sql));
		try {
			userProfile = template.queryForObject(sql,new Object[] {username},
					new BeanPropertyRowMapper<UserProfile>(UserProfile.class));
		}catch(EmptyResultDataAccessException e) {
			logger.info("username is null");
		}

		return userProfile;
	}

	public int deleteBatchRegistrationBranch(Date date){
		String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_REGISTRATION_BRANCH where CAST(DATE as DATE) = ?";
		logger.info(String.format("  sql delete branch=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql
				, new Object[] {date});

		return rowAffected;
	}

	public int deleteBatchRegistrationState(Date date){
		String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_REGISTRATION_STATE where CAST(DATE as DATE) = ?";
		logger.info(String.format("  sql delete state=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql
				, new Object[] {date});

		return rowAffected;
	}

	public List<BoRegistrationMode> getBoRegistrationModeList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoRegistrationMode> boRegistrationModeList;
		String sql =  " SELECT COUNT(*) AS count, a.CHANNEL ,a.ACCOUNT_TYPE ,a.IS_ISLAMIC ,a.TXN_STATUS FROM dcpbo.dbo.TBL_BO_REGISTRATION a WHERE CAST(a.created_time as DATE)=? GROUP BY a.CHANNEL ,a.ACCOUNT_TYPE ,a.IS_ISLAMIC ,a.TXN_STATUS";
		logger.info(String.format("get registration mode list  sql=%s", sql));
		boRegistrationModeList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper(BoRegistrationMode.class));
		return boRegistrationModeList;
	}

	public List<BoRegistrationBranch> getBoRegistrationBranchList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoRegistrationBranch> boRegistrationBranchList;
		String sql =  "SELECT COUNT(*) AS count, a.BRANCH_INCENTIVE_CODE FROM dcpbo.dbo.TBL_BO_REGISTRATION a WHERE CAST(a.created_time as DATE)=? AND a.TXN_STATUS = 'SUCCESS' AND a.BRANCH_INCENTIVE_CODE!=NULL GROUP BY a.BRANCH_INCENTIVE_CODE ";
		logger.info(String.format("get registration branch list  sql=%s", sql));
		boRegistrationBranchList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper(BoRegistrationBranch.class));
		return boRegistrationBranchList;
	}

	public List<BoRegistrationState> getBoRegistrationStateList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoRegistrationState> boRegistrationStateList;
		String sql =  "SELECT COUNT(*) AS count,a.RESIDENTIAL_STATE,a.CHANNEL FROM dcpbo.dbo.TBL_BO_REGISTRATION a WHERE CAST(a.CREATED_TIME as DATE)=? AND a.TXN_STATUS = 'SUCCESS' GROUP BY a.RESIDENTIAL_STATE,a.CHANNEL  ";
		logger.info(String.format("get registration state list  sql=%s", sql));
		boRegistrationStateList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper(BoRegistrationState.class));
		return boRegistrationStateList;
	}

	public Boolean insertSummaryRegistrationMode(BoRegistrationMode boRegistrationMode,Date date,String islamic,Timestamp now) {
		try {

			getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_SUMMARY_REGISTRATION_MODE (DATE,CHANNEL,MODE,CARD_TYPE,STATUS,SUMMARY_COUNT,CREATED_TIME) values (?,?,?,?,?,?,?)"
					, new Object[]{
							date
							,boRegistrationMode.getChannel()
							,boRegistrationMode.getAccountType()
							,islamic
							,boRegistrationMode.getTxnStatus()
							,boRegistrationMode.getCount()
							,now
					});
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	public Boolean insertSummaryRegistrationBranch(BoRegistrationBranch boRegistrationBranch,String branchCode,String staffId,Date date,Timestamp now) {
		try {

			getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_SUMMARY_REGISTRATION_BRANCH (DATE,BRANCH_CODE,STAFF_ID,SUMMARY_COUNT,CREATED_TIME) values (?,?,?,?,?)"
					, new Object[]{
							date
							,branchCode
							,staffId
							,boRegistrationBranch.getCount()
							,now
					});
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	public Boolean insertSummaryRegistrationState(BoRegistrationState boRegistrationState,Date date,Timestamp now) {
		try {

			getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_SUMMARY_REGISTRATION_STATE (DATE,CHANNEL,STATE,SUMMARY_COUNT,CREATED_TIME) values (?,?,?,?,?)"
					, new Object[]{
							date
							,boRegistrationState.getChannel()
							,boRegistrationState.getResidentialState()
							,boRegistrationState.getCount()
							,now
					});
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}


}
