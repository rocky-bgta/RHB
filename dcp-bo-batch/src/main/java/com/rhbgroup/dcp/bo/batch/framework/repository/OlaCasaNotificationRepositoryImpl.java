package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.util.HashMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedOlaCasaNotification;
import com.rhbgroup.dcp.bo.batch.job.model.BoBatchNotificationTemplate;
import com.rhbgroup.dcp.bo.batch.job.model.DepositProduct;
import com.rhbgroup.dcp.bo.batch.job.model.OlaToken;
import com.rhbgroup.dcp.bo.batch.job.model.UserProfile;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.GenericCapsule;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.notifications.models.NotificationPayload;

@Component
public class OlaCasaNotificationRepositoryImpl extends BaseRepositoryImpl {
	
	@Autowired
	@Qualifier("SmsJMSConfig")
	@Lazy
	public JMSConfig smsJMSConfig;

	private static final Logger logger = Logger.getLogger(OlaCasaNotificationRepositoryImpl.class);
	
	public void sendNotification()  {
		List<BatchStagedOlaCasaNotification> batchStagedOlaCasaNotificationList = getBatchStagedOlaCasaNotificationList();
		logger.info(String.format("batchStagedOlaCasaNotificationList = %s", batchStagedOlaCasaNotificationList));
		BoBatchNotificationTemplate boBatchNotificationTemplate = getBoBatchNotificationTemplate();
		logger.info(String.format("boBatchNotificationTemplate = %s", boBatchNotificationTemplate));
		
		for(BatchStagedOlaCasaNotification batchStagedOlaCasaNotification:batchStagedOlaCasaNotificationList) {
			
			UserProfile userProfile = getUserName(batchStagedOlaCasaNotification.getUserId());
			logger.info(String.format("userProfile  = %s", userProfile ));			
			
			List<OlaToken> olaTokenList = getProductCode(userProfile.getUsername());
			
			for(OlaToken olaToken:olaTokenList) {
				Date todayDate = new Date();
				Date secondDate = new Date(olaToken.getCreatedTime().getTime());
				long difference_In_Time = todayDate.getTime() - secondDate.getTime();
				long difference_In_Days = (difference_In_Time/(1000*60*60*24))%365;
				logger.info(String.format("difference_In_Days = %s", difference_In_Days));
				BatchConfig batchConfig = getDays();
				
				if(Long.parseLong(batchConfig.getParameterValue())>=difference_In_Days && olaToken.getId() != null) {					
						logger.info(String.format("product code  = %s", olaToken.getProductCode() ));
						DepositProduct depositProduct = getProductName(olaToken.getProductCode(),olaToken.getProductType(),olaToken.getIsIslamic());
						logger.info(String.format("depositProduct  = %s", depositProduct));
												
						long days = Long.parseLong(batchConfig.getParameterValue()) - difference_In_Days;
						String daysLeft = String.valueOf(days);
						logger.info(String.format("daysLeft  = %s", daysLeft));
						
						String pushData = null;
						String inboxData = null;
						String emailData = null;
						
							String accountName = depositProduct.getProductName();
							logger.info(String.format("accountName   = %s", accountName ));
							String temp = "[%daysLeft]";
							
							pushData = getPushData(boBatchNotificationTemplate,accountName,temp,daysLeft);
							if(boBatchNotificationTemplate.getInboxFlag()) {
								String inputInbox = boBatchNotificationTemplate.getInboxBodyTemplate();
								inboxData = inputInbox.replace("[%olaaccountName]", accountName).replace(temp,daysLeft);

							}
							if(boBatchNotificationTemplate.getEmailFlag()) {
								String inputEmail = boBatchNotificationTemplate.getEmailBodyTemplate();
								emailData = inputEmail.replace("[%olaAccountName]", accountName).replace(temp,daysLeft);
							}
						
							sendNotification(pushData,inboxData,emailData,batchStagedOlaCasaNotification.getUserId());
							logger.info("Notification sent successfully");
					
				}				

			}
	
		}
	}
	
	public String getPushData(BoBatchNotificationTemplate boBatchNotificationTemplate,String accountName,String temp,String daysLeft) {
		String pushData = null;
		if(boBatchNotificationTemplate.getPushFlag()) {
			String inputPush = boBatchNotificationTemplate.getPushBodyTemplate();
			pushData = inputPush.replace("[%olaaccountName]", accountName).replace(temp,daysLeft);
		}
		return pushData;
	}
	
	public void sendNotification(String pushData,String inboxData,String emailData,int userId) {
		NotificationPayload notificationPayload = generateNotificationPayload(pushData,inboxData,emailData,userId);
		Capsule capsule = new Capsule();
		capsule.setUserId(Integer.parseInt(Long.toString(userId)));
		capsule.setMessageId(UUID.randomUUID().toString());
		capsule.setQuickLogin(false);
		capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_BACKOFFICE);
		capsule.setProperty(Constants.OPERATION_NAME, "BOBatchSystemOlaCasaReminderNotification");
		GenericCapsule<NotificationPayload> genericCapsule = new GenericCapsule<>(notificationPayload, capsule);
		sendCapsuleMessageToSmsJMS(genericCapsule.generateCapsuleWithMetadata());
			
	}
	
    protected void sendCapsuleMessageToSmsJMS(Capsule capsule) {
		logger.info(String.format("smsJMSConfig  = %s", smsJMSConfig));
		logger.info(String.format("Capsule  = %s", capsule));

        JMSUtils.sendCapsuleMessageToJMS(capsule, smsJMSConfig);
    }
	
	private NotificationPayload generateNotificationPayload(String pushData,String inboxData,String emailData,int userId) {
		String eventCode = "20037";
		Instant now = Instant.now();
		NotificationPayload notificationPayload = new NotificationPayload(userId, eventCode, now);
		Map<String,String> data =new HashMap<>();
		data.put("msgContentPush", pushData);
		data.put("msgContentInbox", inboxData);
		data.put("msgContentEmail", emailData);
		notificationPayload.setData(data);
		return notificationPayload;
	}
	
	public List<BatchStagedOlaCasaNotification> getBatchStagedOlaCasaNotificationList() {
		JdbcTemplate template = getJdbcTemplate();
		List<BatchStagedOlaCasaNotification> batchStagedOlaCasaNotificationList;
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BATCH_STAGED_OLACASA_NOTIFICATION a";
		logger.info(String.format("get batchStagedOlaCasaNotification list  sql=%s", sql));
		batchStagedOlaCasaNotificationList = template.query(sql,
				new BeanPropertyRowMapper<BatchStagedOlaCasaNotification>(BatchStagedOlaCasaNotification.class));
		return batchStagedOlaCasaNotificationList;
	}
	
	public BoBatchNotificationTemplate getBoBatchNotificationTemplate() {
		JdbcTemplate template = getJdbcTemplate();
		BoBatchNotificationTemplate boBatchNotificationTemplate = new BoBatchNotificationTemplate();
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BO_BATCH_NOTIFICATION_TEMPLATE a WHERE a.event_code='20037'";
		logger.info(String.format("get boBatchNotificationTemplate list  sql=%s", sql));
		try {
			boBatchNotificationTemplate = template.queryForObject(sql,new Object[] {},
					new BeanPropertyRowMapper<BoBatchNotificationTemplate>(BoBatchNotificationTemplate.class));
		}catch(EmptyResultDataAccessException e) {
			 logger.info("boBatchNotificationTemplate is null");
			}

		return boBatchNotificationTemplate;
	}
	
	
	public UserProfile getUserName(long id) {
		JdbcTemplate template = getJdbcTemplate();
		UserProfile userProfile = new UserProfile();
		String sql =  " SELECT * FROM dcp.dbo.TBL_USER_PROFILE a WHERE a.id=?";
		logger.info(String.format("get user profile  sql=%s", sql));
		try {
			userProfile = template.queryForObject(sql,new Object[] {id},
					new BeanPropertyRowMapper<UserProfile>(UserProfile.class));
		}catch(EmptyResultDataAccessException e) {
			logger.info("username is null");
		}

		return userProfile;
	}
	
	public List<OlaToken> getProductCode(String username) {
		JdbcTemplate template = getJdbcTemplate();
		List<OlaToken> olaTokenList;
		String sql =  " SELECT * FROM dcp.dbo.TBL_OLA_TOKEN a WHERE a.username=? AND a.STATUS = 'P'";
		logger.info(String.format("get product code sql=%s", sql));
			olaTokenList = template.query(sql,new Object[] {username},
					new BeanPropertyRowMapper<OlaToken>(OlaToken.class));

		return olaTokenList;
	}
	
	
	public DepositProduct getProductName(String productCode,String productType,Boolean isIslamic) {
		JdbcTemplate template = getJdbcTemplate();
		DepositProduct depositProduct;
		String sql =  " SELECT * FROM dcp.dbo.TBL_DEPOSIT_PRODUCT a WHERE a.product_code=? AND a.deposit_type=? AND a.is_islamic=?";
		logger.info(String.format("get product name list  sql=%s", sql));
		depositProduct = template.queryForObject(sql,new Object[] {productCode,productType,isIslamic},
				new BeanPropertyRowMapper<DepositProduct>(DepositProduct.class));
		return depositProduct;
	}
	
	public BatchConfig getDays() {
		JdbcTemplate template = getJdbcTemplate();
		BatchConfig batchConfig = new BatchConfig();
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BATCH_CONFIG a WHERE a.parameter_key='dcp.olacasa.validity'";
		logger.info(String.format("get parameter value sql=%s", sql));
		try {
			batchConfig = template.queryForObject(sql,new Object[] {},
					new BeanPropertyRowMapper<BatchConfig>(BatchConfig.class));
		}catch(EmptyResultDataAccessException e) {
			logger.info("batch config is null");
		}

		return batchConfig;
	}
	
	
}