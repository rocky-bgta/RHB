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

import com.rhbgroup.dcp.bo.batch.email.OlaCasaEmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.config.properties.OlaCasaEmailConfigProperties;

@Configuration
public class OlaCasaEmailConfig {
	
	private static final Logger logger = Logger.getLogger(OlaCasaEmailConfig.class);

	private OlaCasaEmailConfigProperties olaCasaEmailConfigProperties;

    @Autowired
    public OlaCasaEmailConfig(OlaCasaEmailConfigProperties olaCasaEmailConfigProperties) {
        this.olaCasaEmailConfigProperties = olaCasaEmailConfigProperties;
    }

    @Bean
	public OlaCasaEmailTemplate getOlaCasaEmailTemplate() 
    {
		return new OlaCasaEmailTemplate();
    }
	
	@Bean
    public JavaMailSender getJavaMailSender() 
    {
        logger.info("getJavaMailSender - mail.smtp.auth : " + olaCasaEmailConfigProperties.getSmtpAuth());
        logger.info("getJavaMailSender - mail.smtp.starttls.enable : " + olaCasaEmailConfigProperties.getTlsEnable());
        logger.info("getJavaMailSender - mail.smtp.starttls.required : " + olaCasaEmailConfigProperties.getTlsRequire());
        logger.info("getJavaMailSender - mail.smtp.ssl.trust : " + olaCasaEmailConfigProperties.getSslTrustAllCert());
        logger.info("getJavaMailSender - mail.debug : " + olaCasaEmailConfigProperties.getEmailDebugMode());

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(olaCasaEmailConfigProperties.getHost());
        mailSender.setPort(Integer.parseInt(olaCasaEmailConfigProperties.getPort()));        

        if (olaCasaEmailConfigProperties.getUsername() != null && !olaCasaEmailConfigProperties.getUsername().equals("")) {
            mailSender.setUsername(olaCasaEmailConfigProperties.getUsername());
            mailSender.setPassword(olaCasaEmailConfigProperties.getPassword());
        }
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", olaCasaEmailConfigProperties.getSmtpAuth());
        props.put("mail.smtp.starttls.enable", olaCasaEmailConfigProperties.getTlsEnable());
        props.put("mail.smtp.starttls.required", olaCasaEmailConfigProperties.getTlsRequire());
        if(olaCasaEmailConfigProperties.getSslTrustAllCert() != null && olaCasaEmailConfigProperties.getSslTrustAllCert().equalsIgnoreCase(IsBoolean.TRUE.toString())) {
            props.put("mail.smtp.ssl.trust", "*");
        }
        props.put("mail.debug", olaCasaEmailConfigProperties.getEmailDebugMode());

        return mailSender;
    }
     
    @Bean
    public SimpleMailMessage olaCasaEmailTemplate()
    {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("");
        message.setFrom("");
        message.setText("");
        return message;
    }
}
