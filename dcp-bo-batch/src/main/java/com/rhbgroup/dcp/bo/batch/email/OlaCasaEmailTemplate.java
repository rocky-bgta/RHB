package com.rhbgroup.dcp.bo.batch.email;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;

import com.rhbgroup.dcp.bo.batch.job.config.properties.OlaCasaEmailConfigProperties;

import freemarker.template.Configuration;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;


@Component
public class OlaCasaEmailTemplate {

	private static final Logger logger = Logger.getLogger(OlaCasaEmailTemplate.class);

	@Autowired
	private JavaMailSender sender;

	@Autowired
	private Configuration config;
	
	@Autowired
	private OlaCasaEmailConfigProperties olaCasaEmailConfigProperties;
	
    private static final String OLAABANDONREPORTID = "DMBUD090";
    private static final String OLAABANDONSUBJECT = "CASA OLA Abandon Report dated DD-MM-YYYY";
    private static final String OLAABANDONBODY = "Enclosed CASA OLA Abandon Report for your reference.";

	public String sendMail(String sourceFileFullPath,Date date,String reportID) {
		logger.info("Send Report File through mail");
		MimeMessage message = sender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			logger.info(String.format("sourceFileFullPath %s", sourceFileFullPath));
			Path path = Paths.get(sourceFileFullPath);
			byte[] content = Files.readAllBytes(path);
			String strDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
			String subject = olaCasaEmailConfigProperties.getSubject();
			String body = olaCasaEmailConfigProperties.getBody();
			if(OLAABANDONREPORTID.equals(reportID)) {
				subject = OLAABANDONSUBJECT;
				body = OLAABANDONBODY;
			}
			
			subject = subject.replace("DD-MM-YYYY", strDate);
			String fileName = sourceFileFullPath.substring(sourceFileFullPath.lastIndexOf('/')+1);
			sendEmail(helper, message,content,fileName,subject,body);

		} catch (Exception e) {
			logger.error(e);
		}
		return "";

	}



	private void sendEmail(MimeMessageHelper helper, MimeMessage message,byte[] content,String fileName,String subject,String body) {

		try {
			logger.info(String.format("from %s", olaCasaEmailConfigProperties.getFrom()));
			helper.setFrom(olaCasaEmailConfigProperties.getFrom());
			logger.info(String.format("to %s", olaCasaEmailConfigProperties.getTo()));
			helper.setTo(olaCasaEmailConfigProperties.getTo());
			helper.setSubject(subject);
			helper.setText(body);
			logger.info(String.format("body %s", body));
	        helper.addAttachment(fileName, new ByteArrayResource(content));
			logger.info(String.format("filename %s", fileName));
		} catch (Exception e) {
			logger.error(e);
		}
		sender.send(message);
		logger.info("Email Sent Successfully");

	}

}
