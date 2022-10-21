package com.rhbgroup.dcpbo.user;

import com.rhbgroup.dcpbo.user.config.DbConfigProperties;
import com.rhbgroup.dcpbo.user.config.WorkflowConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import com.rhbgroup.dcpbo.user.common.DirectoryServerRepository;

@Configuration
public class DataBeanConfiguration {

	private static Logger logger = LogManager.getLogger(DataBeanConfiguration.class);
	
	@Bean(name = "directoryServerRepository")
	public DirectoryServerRepository getDirectoryServerRepository() {
		return new DirectoryServerRepository();
	}
	
	@Bean(name = "ldapContextSource")
	public LdapContextSource getLdapContextSource(@Qualifier("ldapConfig") LdapConfig ldapConfig) {
		logger.info("getLdapContextSource()");
		
		String url = ldapConfig.getUrl();
		logger.info("    url: " + url);

		String basedn = ldapConfig.getBasedn();
		logger.info("    basedn: " + basedn);

		String userdn = ldapConfig.getUserdn();
		logger.info("    userdn: " + userdn);

		String password = ldapConfig.getPassword();
		logger.info("    password: " + password);

		String referral = ldapConfig.getReferral();
		logger.info("    referral: " + referral);

		LdapContextSource ldapContextSource = new LdapContextSource();
		ldapContextSource.setUrl(url);
		ldapContextSource.setBase(basedn);
		ldapContextSource.setUserDn(userdn);
		ldapContextSource.setPassword(password);
		ldapContextSource.setReferral(referral);
		logger.info("    ldapContextSource: " + ldapContextSource);
		
		return ldapContextSource;
	}
	
	@Bean(name = "ldapTemplate")
	public LdapTemplate getLdapTemplate(@Qualifier("ldapContextSource") LdapContextSource ldapContextSource) {
		return new LdapTemplate(ldapContextSource);
	}
	
	@Bean(name = "ldapConfig")
	public LdapConfig getLdapConfig() {
		return new LdapConfig();
	}

	@Bean(name = "dbConfigProperties")
    public DbConfigProperties getDbConfigProperties() {
	    return new DbConfigProperties();
    }

    @Bean(name = "workflowConfig")
	public WorkflowConfig getWorkflowConfig() { return new WorkflowConfig(); }
}
