package com.rhbgroup.dcpbo.user.common;

import java.util.List;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;

import com.rhbgroup.dcpbo.user.LdapConfig;
import com.rhbgroup.dcpbo.user.info.UserInfo;

public class DirectoryServerRepository {
	@Autowired
	private LdapTemplate ldapTemplate;
	
	@Autowired
	private LdapConfig ldapConfig;
	
	private static Logger logger = LogManager.getLogger(DirectoryServerRepository.class);
	
	public UserInfo search(String username) {
		logger.debug("search()");
		logger.debug("    ldapTemplate: " + ldapTemplate);
		logger.debug("    ldapConfig: " + ldapConfig);
		logger.debug("    username: " + username);
		
		List<UserInfo> userInfoList = ldapTemplate.search("", "cn=" + username, new UserContextMapper());
		if (userInfoList == null || userInfoList.isEmpty()) {
			logger.warn("    LdapTemplate.search returns null or empty result for username: " + username);
			return null;
		}
		
		UserInfo userInfo = userInfoList.get(0);
		logger.debug("    userInfo: " + userInfo);

		return userInfo;
	}
	
	private class UserContextMapper implements ContextMapper<UserInfo> {
		@Override
		public UserInfo mapFromContext(Object obj) throws NamingException {
			logger.debug("mapFromContext()");
			logger.debug("    ldapConfig: " + ldapConfig);
			
			DirContextAdapter ctx = (DirContextAdapter) obj;

			String nameField = ldapConfig.getNameField();
			logger.debug("    nameField: " + nameField);
			
			String emailField = ldapConfig.getEmailField();
			logger.debug("    emailField: " + emailField);
			
			UserInfo userInfo = new UserInfo();
			userInfo.setName(ctx.getStringAttribute(nameField));
			userInfo.setEmail(ctx.getStringAttribute(emailField));
		
			return userInfo;
		}
		
	}
}
