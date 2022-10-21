package com.rhbgroup.dcp.bo.batch.email;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.EmailConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbHelperDTO;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbSuccessList;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbVarianceDetails;

import freemarker.template.Configuration;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Template;

@Component
public class EmailTemplate {

	private static final Logger logger = Logger.getLogger(EmailTemplate.class);
	private static final String  SUBJECT = "ASNB Summary & Variance Notification";

	@Autowired
	private JavaMailSender sender;

	@Autowired
	private Configuration config;
	
	@Autowired
	private EmailConfigProperties emailConfigProperties;

	public String getNoFileWithBankData(String time, AsnbHelperDTO helperClass) {
		// non_receiveasnb_bankSuccess
		logger.info("Inside File Not Exists But Data is avalible::: 1");
		Map<String, Object> model = getModelMap(time, helperClass);
		model.put("time", time);
		MimeMessage message = sender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Template t = config.getTemplate(emailConfigProperties.getTemplate_02());
			sendEmail(t, model, helper, message);

		} catch (Exception e) {
			logger.error(e);
		}
		return "";

	}

	public void getNoFileNoBankData(String time) {
		logger.info("Inside File Not Exists 3");
		// non_receiveasnb_bankNOnsuccess
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("time", time);
		MimeMessage message = sender.createMimeMessage();

		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Template t = config.getTemplate(emailConfigProperties.getTemplate_01());
			sendEmail(t, model, helper, message);

		} catch (Exception e) {
			logger.error(e);
		}

	}

	private void sendEmail(Template t, Map<String, Object> model, MimeMessageHelper helper, MimeMessage message) {

		try {
			String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
			helper.setTo(emailConfigProperties.getTo());
			helper.setText(html, true);
			helper.setSubject(SUBJECT);
			helper.setFrom(emailConfigProperties.getFrom());
		} catch (Exception e) {
			logger.error(e);
		}
		sender.send(message);
		logger.info("Email Sent Successfully*");

	}

	public void getEmptyFileWithNoBankData(String nowTime) {
		logger.info("Empty File With NO Data in DCP:");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("time", nowTime);
		MimeMessage message = sender.createMimeMessage();
		try {
			// set mediaType
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Template t = config.getTemplate(emailConfigProperties.getTemplate_03());
			sendEmail(t, model, helper, message);

		} catch (Exception e) {
			logger.error(e);
		}

	}

	public Map<String, Object> getModelMap(String time, AsnbHelperDTO helperClass) {
		List<EmailDto> summarymailDtosList = new ArrayList<EmailDto>();
		List<VarianceDto> emailVarianceDtosList = new ArrayList<VarianceDto>();
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("time", time);
		NumberFormat numberFormat = NumberFormat.getInstance();//amount
		numberFormat.setGroupingUsed(true);
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);
		DecimalFormat df = new DecimalFormat("###.#");//#Trans

		for (Map.Entry<String, AsnbSuccessList> row : helperClass.getAsnbMap().entrySet()) {
			EmailDto dto = new EmailDto();
			AsnbSuccessList asnbSuccessList = row.getValue();
			dto.setFund(asnbSuccessList.getName());
			dto.setTran(df.format(asnbSuccessList.getBankNoOfTran()));
			dto.setTotalAmount(numberFormat.format(asnbSuccessList.getBankAmount()));
			dto.setAsnbTran(df.format(asnbSuccessList.getAsnbNoOfTran()));
			dto.setAsnbTotalAmount(numberFormat.format(asnbSuccessList.getAsnbAmount()));
			summarymailDtosList.add(dto);
		}

		for (AsnbVarianceDetails row : helperClass.getAnsbVarDetails()) {
			VarianceDto dto = new VarianceDto();

			dto.setBeneficiaryAhnbId(row.getUhBenificiaryAsnbId());
			dto.setFund(row.getFund());
			dto.setDate(row.getDate());
			dto.setTime(row.getTime());
			dto.setBankRefNo(row.getBnkRefNum());
			dto.setAsnbRefNo(row.getFdsRefNum());
			dto.setAmount(numberFormat.format(Double.parseDouble(row.getAmount())));

			emailVarianceDtosList.add(dto);
		}
		model.put("summary", summarymailDtosList);
		model.put("variance", emailVarianceDtosList);

		return model;

	}

	public void getEmptyFileWithBankData(String time, AsnbHelperDTO helperClass) {
		
		Map<String, Object> model = getModelMap(time, helperClass);
		model.put("time", time);

		MimeMessage message = sender.createMimeMessage();
		try {
			// set mediaType
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Template t = config.getTemplate(emailConfigProperties.getTemplate_04());
			sendEmail(t, model, helper, message);

		} catch (Exception e) {
			logger.error(e);
		}

	}

	public void getAsnbScuccessLessTnx(String time, AsnbHelperDTO helperClass) {

		Map<String, Object> model = getModelMap(time, helperClass);
		model.put("time", time);

		MimeMessage message = sender.createMimeMessage();
		try {
			// set mediaType
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Template t = config.getTemplate(emailConfigProperties.getTemplate_05());
			sendEmail(t, model, helper, message);

		} catch (Exception e) {
			logger.error(e);
		}

	}

	public void getAsnbScuccessBnkTnx(String time, AsnbHelperDTO helperClass) {
		Map<String, Object> model = getModelMap(time, helperClass);
		model.put("time", time);
		MimeMessage message = sender.createMimeMessage();
		try {
			// set mediaType
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Template t = config.getTemplate(emailConfigProperties.getTemplate_07());
			sendEmail(t, model, helper, message);

		} catch (Exception e) {
			logger.error(e);
		}

	}

	public void getAsnbScuccessGreaterTnx(String time, AsnbHelperDTO helperClass) {
		Map<String, Object> model = getModelMap(time, helperClass);
		model.put("time", time);
		MimeMessage message = sender.createMimeMessage();
		try {
			// set mediaType
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Template t = config.getTemplate(emailConfigProperties.getTemplate_06());
			sendEmail(t, model, helper, message);

		} catch (Exception e) {
			logger.error(e);
		}

	}
}
