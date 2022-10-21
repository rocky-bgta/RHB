package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.DeviceProfile;
import com.rhbgroup.dcp.bo.batch.job.model.UserProfile;

@Component
public class DeviceManagementRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(DeviceManagementRepositoryImpl.class);
	
	public void insertStagedData(Date batchProcessingDate)  {
		List<DeviceProfile> deviceProfileList = getDeviceProfileList(batchProcessingDate);
		logger.info(String.format("device profile list=%s", deviceProfileList));	
		for(DeviceProfile deviceProfile:deviceProfileList) {
			UserProfile userProfile = getUserProfile(deviceProfile.getUserId());
			logger.info(String.format("user profile =%s", userProfile));	
				insertBoDeviceProfile(deviceProfile,userProfile);
			logger.info("Successfully inserted into database TBL_BO_DEVICE_PROFILE ");	
		}

	}
	
	public List<DeviceProfile> getDeviceProfileList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<DeviceProfile> deviceProfileList;
		String sql =  " SELECT * FROM dcp.dbo.TBL_DEVICE_PROFILE a WHERE CAST(a.created_time as DATE)=?";
		logger.info(String.format("get device profile list  sql=%s", sql));
		deviceProfileList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper<DeviceProfile>(DeviceProfile.class));
		return deviceProfileList;
	}
	
	public UserProfile getUserProfile(Integer userId) {
		JdbcTemplate template = getJdbcTemplate();
		UserProfile userProfile;
		String sql =  " SELECT * FROM dcp.dbo.TBL_USER_PROFILE a WHERE a.id = ?";
		logger.info(String.format("get user profile  sql=%s", sql));
		userProfile = template.queryForObject(sql,new Object[] {userId},
				new BeanPropertyRowMapper<UserProfile>(UserProfile.class));
		return userProfile;
	}

	
	public Boolean insertBoDeviceProfile(DeviceProfile deviceProfile,UserProfile userProfile) {
        try {

            getJdbcTemplate().update("INSERT INTO TBL_BO_DEVICE_PROFILE (USER_ID, DEVICE_ID, DEVICE_NAME, SECURE_PLUS_SETUP, SECURE_PLUS_SEQUENCE_NO, SUBSCRIBER_ID, OS, IS_QUICK_LOGIN_BIO_ENABLED,"
            		+ " QUICK_LOGIN_REFRESH_TOKEN, PUSH_NOTIFICATION_SUBSCRIPTION_TOKEN, PUSH_NOTIFICATION_PLATFORM, LAST_LOGIN, CREATED_TIME, RSA_CHALLENGE_STATUS, SESSION_TOKEN_EXPIRY, DEVICE_STATUS,"
            		+ " UPDATED_TIME, UPDATED_BY, DEVICE_TYPE, NAME, CIS_NO, USERNAME, IS_STAFF, IS_PREMIER) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                    , new Object[]{
                    		deviceProfile.getUserId()
                            ,deviceProfile.getDeviceId()
                            ,deviceProfile.getDeviceName()
                            ,deviceProfile.getSecurePlusSetup()
                            ,deviceProfile.getSecurePlusSequenceNo()
                            ,deviceProfile.getSubscriberId()
                            ,deviceProfile.getOs()
                            ,deviceProfile.getIsQuickLoginBioEnabled()
                            ,deviceProfile.getQuickLoginRefreshToken()
                            ,deviceProfile.getPushNotificationSubscriptionToken()
                            ,deviceProfile.getPushNotificationPlatform()
                            ,deviceProfile.getLastLogin()
                            ,deviceProfile.getCreatedTime()
                            ,deviceProfile.getRsaChallengeStatus()
                            ,deviceProfile.getSessionTokenExpiry()
                            ,deviceProfile.getDeviceStatus()
                            ,deviceProfile.getUpdatedTime()
                            ,deviceProfile.getUpdatedBy()
                            ,deviceProfile.getDeviceType()
                            ,userProfile.getName()
                            ,userProfile.getCisNo()
                            ,userProfile.getUsername()
                            ,userProfile.getIsStaff()
                            ,userProfile.getIsPremier()
                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
	
}
