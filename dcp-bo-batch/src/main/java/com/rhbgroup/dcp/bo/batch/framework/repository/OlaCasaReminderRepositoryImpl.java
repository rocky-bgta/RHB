package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.BatchConfig;
import com.rhbgroup.dcp.bo.batch.job.model.OlaToken;
import com.rhbgroup.dcp.bo.batch.job.model.UserProfile;

@Component
public class OlaCasaReminderRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(OlaCasaReminderRepositoryImpl.class);

	
	public void insertSummary(String jobExecutionId)  {
		
		int rows = truncateTable();
		logger.info(String.format("Deleted rows count = %s", rows));

		BatchConfig first = getBatchConfig("dcp.olacasa.reminder.first");
		logger.info(String.format("first = %s", first.getParameterValue()));
		BatchConfig second = getBatchConfig("dcp.olacasa.reminder.second");
		logger.info(String.format("second = %s", second.getParameterValue()));
		BatchConfig validity = getBatchConfig("dcp.olacasa.validity");
		logger.info(String.format("validity = %s", validity.getParameterValue()));
		Date todayDate = new Date();
		logger.info(String.format("todayDate = %s", todayDate));

		List<OlaToken> olaTokenList = getOlaTokenList();
		
		for(OlaToken olaToken:olaTokenList) {
			Date secondDate = new Date(olaToken.getCreatedTime().getTime());
			logger.info(String.format("secondDate = %s", secondDate));
			long difference_In_Time = todayDate.getTime() - secondDate.getTime();
			logger.info(String.format("difference_In_Time = %s", difference_In_Time));
			long difference_In_Days = (difference_In_Time/(1000*60*60*24))%365;
			logger.info(String.format("difference_In_Days = %s", difference_In_Days));
			
			if(String.valueOf(difference_In_Days).equals(first.getParameterValue()) || String.valueOf(difference_In_Days).equals(second.getParameterValue())) {
				UserProfile userProfile = getUserProfile(olaToken.getUsername());
				logger.info(String.format("UserProfile = %s", userProfile));
				if(userProfile.getId() != null) {
					insertBatchStagedOlaCasaNotification(jobExecutionId,userProfile.getId());
					logger.info("Successfully inserted into database TBL_BATCH_STAGED_OLACASA_NOTIFICATION ");
				}

			}
		}
		
		
	}
	
	public BatchConfig getBatchConfig(String parameterKey) {
		JdbcTemplate template = getJdbcTemplate();
		BatchConfig batchConfig = new BatchConfig();
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BATCH_CONFIG a WHERE a.PARAMETER_KEY=? ";
		logger.info(String.format("get batch config=%s", sql));
		try {
			batchConfig = template.queryForObject(sql,new Object[] {parameterKey},
					new BeanPropertyRowMapper<BatchConfig>(BatchConfig.class));
		}catch(EmptyResultDataAccessException e) {
			 logger.info("batch config is null");
			}

		return batchConfig;
	}
	
	public List<OlaToken> getOlaTokenList() {
		JdbcTemplate template = getJdbcTemplate();
		List<OlaToken> olaTokenList;
		String sql =  " SELECT * FROM dcp.dbo.TBL_OLA_TOKEN a WHERE a.status='I' OR a.status='P'";
		logger.info(String.format("get ola token list  sql=%s", sql));
		olaTokenList = template.query(sql,new Object[] {},
				new BeanPropertyRowMapper<OlaToken>(OlaToken.class));
		return olaTokenList;
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
	
	 public int truncateTable(){
	        String sql = "TRUNCATE TABLE dcpbo.dbo.TBL_BATCH_STAGED_OLACASA_NOTIFICATION";
			logger.info(String.format("  sql=%s", sql));
	        return getJdbcTemplate().update(sql
	                , new Object[] {});

	    }
	
	public Boolean insertBatchStagedOlaCasaNotification(String jobExecutionId, int id) {
		Date now = new Date();
        try {

            getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_OLACASA_NOTIFICATION (JOB_EXECUTION_ID, ACTIVITIES_NAME, USER_ID, EVENT_CODE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) values (?,?,?,?,?,?,?,?)"
                    , new Object[]{
                    		jobExecutionId
                            ,"ExtractOlaCasaReminderNotificationJob"
                            ,id
                            ,"20037"
                            ,now
                            ,"System"
                            ,now
                            ,"System"
                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
	
}
