package com.rhbgroup.dcp.bo.batch.job.config;

import java.util.Properties;

import com.rhbgroup.dcp.bo.batch.job.enums.IsBoolean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.config.properties.EmailConfigProperties;

@Configuration
public class EmailConfig {
	
	private static final Logger logger = Logger.getLogger(EmailConfig.class);

	private EmailConfigProperties emailConfigProperties;

    @Autowired
    public EmailConfig(EmailConfigProperties emailConfigProperties) {
        this.emailConfigProperties = emailConfigProperties;
    }

    @Bean
	public EmailTemplate getEmailTemplate() 
    {
		return new EmailTemplate();
    }
	
	@Bean
    public JavaMailSender getJavaMailSender() 
    {
        logger.info("getJavaMailSender - mail.smtp.auth : " + emailConfigProperties.getSmtpAuth());
        logger.info("getJavaMailSender - mail.smtp.starttls.enable : " + emailConfigProperties.getTlsEnable());
        logger.info("getJavaMailSender - mail.smtp.starttls.required : " + emailConfigProperties.getTlsRequire());
        logger.info("getJavaMailSender - mail.smtp.ssl.trust : " + emailConfigProperties.getSslTrustAllCert());
        logger.info("getJavaMailSender - mail.debug : " + emailConfigProperties.getEmailDebugMode());

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfigProperties.getHost());
        mailSender.setPort(Integer.parseInt(emailConfigProperties.getPort()));
        mailSender.setUsername(emailConfigProperties.getUsername());
        mailSender.setPassword(emailConfigProperties.getPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", emailConfigProperties.getSmtpAuth());
        props.put("mail.smtp.starttls.enable", emailConfigProperties.getTlsEnable());
        props.put("mail.smtp.starttls.required", emailConfigProperties.getTlsRequire());
        if(emailConfigProperties.getSslTrustAllCert() != null && emailConfigProperties.getSslTrustAllCert().equalsIgnoreCase(IsBoolean.TRUE.toString())) {
            props.put("mail.smtp.ssl.trust", "*");
        }
        props.put("mail.debug", emailConfigProperties.getEmailDebugMode());

        return mailSender;
    }
     
    @Bean
    public SimpleMailMessage emailTemplate()
    {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("");
        message.setFrom("");
        message.setText("");
        return message;
    }
}
