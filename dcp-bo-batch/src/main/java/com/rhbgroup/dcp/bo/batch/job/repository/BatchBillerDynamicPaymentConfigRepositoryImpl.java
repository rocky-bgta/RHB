package com.rhbgroup.dcp.bo.batch.job.repository;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKBillerPaymentJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BatchBillerDynamicPaymentConfigRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchBillerDynamicPaymentConfigRepositoryImpl.class);
	private static final String IBK_FTP_FOLDER = "IBK_FTP_FOLDER";
	private static final String FILE_NAME_FORMAT = "FILE_NAME_FORMAT";
	private static final String REPORT_NAME_FORMAT = "REPORT_NAME_FORMAT";
	private static final String BILLER_CODE = "BILLER_CODE";

	@Autowired
	LoadIBKBillerPaymentJobConfigProperties confProperties;

	public List<BatchBillerDynamicPaymentConfig> getBatchBillerPaymentConfigsForLoadIBKBillerPaymentJob() {
		List<BatchBillerDynamicPaymentConfig> batchBillerPaymentConfigs = new ArrayList<>();
		String selectQuery = "SELECT IBK_FTP_FOLDER, FILE_NAME_FORMAT, BILLER_CODE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE TEMPLATE_NAME=? AND IS_REQUIRED_TO_EXECUTE=1";

		logger.debug(String.format("Retrieving BatchBillerPaymentConfigs from DB using SQL [%s] filter by template name [%s]", selectQuery, confProperties.getTemplateName()));
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(selectQuery, confProperties.getTemplateName());

		for (Map<String, Object> row : rows) {
			BatchBillerDynamicPaymentConfig batchBillerPaymentConfig = new BatchBillerDynamicPaymentConfig();
			batchBillerPaymentConfig.setIbkFtpFolder((String)row.get(IBK_FTP_FOLDER));
			batchBillerPaymentConfig.setFileNameFormat((String)row.get(FILE_NAME_FORMAT));
			batchBillerPaymentConfig.setBillerCode((String)row.get(BILLER_CODE));

			batchBillerPaymentConfigs.add(batchBillerPaymentConfig);
		}

		logger.debug(String.format("BatchBillerPaymentConfigs retrieved successfully from DB [%s]", batchBillerPaymentConfigs));
		return batchBillerPaymentConfigs;
	}

	public int updateBatchBillerPaymentConfigExecuteFlag(BatchBillerDynamicPaymentConfig billerPaymentConfig, int requiredExecute) {
		int impacted=0;
		String updateSQL="UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE ID=?";
		impacted=getJdbcTemplate().update(updateSQL, requiredExecute, billerPaymentConfig.getUpdatedBy(), billerPaymentConfig.getUpdatedTime(), billerPaymentConfig.getId());
		logger.debug(String.format("BatchBillerPaymentConfigs update IS_REQUIRED_TO_EXECUTE in DB impacted row [%s]", impacted));
		return impacted;
	}

	public List<BillerPaymentInboundConfig> getBillerConfigInbound() {
		List<BillerPaymentInboundConfig> billerPaymentInboundConfig = new ArrayList<>();
		String selectQuery = "SELECT IBK_FTP_FOLDER, FILE_NAME_FORMAT, BILLER_CODE, REPORT_NAME_FORMAT FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE IS_REQUIRED_TO_EXECUTE=1 and PROCESSED_BY='Dynamic_Batch'";

		logger.debug(String.format("Retrieving BatchBillerPaymentConfigs from DB using SQL [%s] ", selectQuery));
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(selectQuery);

		for (Map<String, Object> row : rows) {
			BillerPaymentInboundConfig batchBillerPaymentConfig = new BillerPaymentInboundConfig();
			batchBillerPaymentConfig.setIbkFtpFolder((String)row.get(IBK_FTP_FOLDER));
			batchBillerPaymentConfig.setFileNameFormat((String)row.get(FILE_NAME_FORMAT));
			batchBillerPaymentConfig.setBillerCode((String)row.get(BILLER_CODE));
			billerPaymentInboundConfig.add(batchBillerPaymentConfig);
		}

		logger.debug(String.format("BatchBillerPaymentConfigs retrieved successfully from DB [%s]", billerPaymentInboundConfig));
		return billerPaymentInboundConfig;
	}

	public List<BillerPaymentInboundConfig> getBillerConfigInboundReport() {
		List<BillerPaymentInboundConfig> billerPaymentInboundConfig = new ArrayList<>();
		String selectQuery = "SELECT IBK_FTP_FOLDER, FILE_NAME_FORMAT, BILLER_CODE, REPORT_NAME_FORMAT FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE IS_REQUIRED_TO_EXECUTE=1 and REPORT_TEMPLATE_ID !=0 and PROCESSED_BY='Dynamic_Batch'";

		logger.debug(String.format("Retrieving BatchBillerPaymentConfigs from DB using SQL [%s] ", selectQuery));
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(selectQuery);

		for (Map<String, Object> row : rows) {
			BillerPaymentInboundConfig batchBillerPaymentConfig = new BillerPaymentInboundConfig();
			batchBillerPaymentConfig.setIbkFtpFolder((String)row.get(IBK_FTP_FOLDER));
			batchBillerPaymentConfig.setBillerCode((String)row.get(BILLER_CODE));
			batchBillerPaymentConfig.setReportNameFormat((String)row.get(REPORT_NAME_FORMAT));
			billerPaymentInboundConfig.add(batchBillerPaymentConfig);
		}

		logger.debug(String.format("BatchBillerPaymentConfigs retrieved successfully from DB [%s]", billerPaymentInboundConfig));
		return billerPaymentInboundConfig;
	}


	public BillerPaymentInboundTxn getBillerPaymentInboundTxn(String billerCode ,String txnDate) {

		JdbcTemplate template = getJdbcTemplate();
		BillerPaymentInboundTxn billerConfigInbound ;
		String sqlSelect = "select txn_id as txn_id" +
				", isnull(txn_date,'') as txn_date" +
				", isnull(txn_amount,'0.00') as txn_amount" +
				", isnull(txn_Type,'CR') as txn_Type" +
				", isnull(txn_description,'') as txn_description" +
				", isnull(biller_ref_no1,'') as bill_ref_no1" +
				", isnull(biller_ref_no2 ,'') as bill_ref_no2" +
				", isnull(biller_ref_no3 ,'') as bill_ref_no3" +
				", isnull(txn_time,'') as txn_time ";
		String fromClause =" from dcpbo.dbo.vw_batch_biller_payment_txn paytxn "
				+" join dcpbo.dbo.vw_batch_tbl_biller biller on biller.biller_code=paytxn.biller_code ";
		String whereClause= " where biller.biller_code=? and paytxn.txn_date =?" ;
		String sql = sqlSelect + fromClause + whereClause;
		logger.info(String.format("get biller config sql=%s billerCode=%s txnDate=%s ", sql,billerCode,txnDate));

		try {
			billerConfigInbound = template.queryForObject(sql, new Object[]{billerCode,txnDate},
					new RowMapper<BillerPaymentInboundTxn>() {
						public BillerPaymentInboundTxn mapRow(ResultSet rs, int rowNum) throws SQLException {

							BillerPaymentInboundTxn billerConfigInbound = new BillerPaymentInboundTxn();
							billerConfigInbound.setTxnId(rs.getString("txn_id"));
							billerConfigInbound.setTxnDate(rs.getString("txn_date"));
							billerConfigInbound.setTxnAmount(rs.getString("txn_amount"));
							billerConfigInbound.setTxnType(rs.getString("txn_Type"));
							billerConfigInbound.setTxnDesc(rs.getString("txn_description"));
							billerConfigInbound.setBillRefNo1(rs.getString("bill_ref_no1"));
							billerConfigInbound.setBillRefNo2(rs.getString("bill_ref_no2"));
							billerConfigInbound.setBillRefNo3(rs.getString("bill_ref_no3"));
							billerConfigInbound.setTxnTime(rs.getString("txn_time"));
							return billerConfigInbound;
						}
					});
			return billerConfigInbound;
		}catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public BatchBillerPaymentConfig getBillerPaymentConfigDtls(String billerCode)
	{
		JdbcTemplate template = getJdbcTemplate();
		BatchBillerPaymentConfig billerPaymentCnfg;
		String sql = "SELECT ID, BILLER_CODE, IBK_FTP_FOLDER,FTP_FOLDER,FILE_NAME_FORMAT,REPORT_UNIT_URI, IS_REQUIRED_TO_EXECUTE, CREATED_TIME, CREATED_BY, "
				+ "UPDATED_TIME,UPDATED_BY,STATUS,TEMPLATE_ID,BILLER_EMAIL,BILLER_EMAIL_PAYMENT_FILE_SUBJECT,BILLER_EMAIL_PAYMENT_FILE_BODY,BILLER_EMAIL_PAYMENT_FILE_OTP_SUBJECT, "
				+ "BILLER_EMAIL_PAYMENT_FILE_OTP_BODY,DELIVERY_MODE,IS_REQUIRED_TO_GENERATE_PAYMENT_FILE,IS_REQUIRED_TO_GENERATE_REPORT_FILE,PROCESSED_BY,template_name,REPORT_NAME_FORMAT,REPORT_TEMPLATE_ID "
				+ "FROM dcpbo.dbo.TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE=? and IS_REQUIRED_TO_EXECUTE=1 and PROCESSED_BY='Dynamic_Batch'";
		logger.info(String.format("getBillerPaymentConfigDtls sql=%s", sql));
		try {
			billerPaymentCnfg = template.queryForObject(sql, new Object[] {billerCode},

					new RowMapper<BatchBillerPaymentConfig>() {
						public BatchBillerPaymentConfig mapRow(ResultSet rs, int rowNum) throws SQLException {

							BatchBillerPaymentConfig billerPaymentCnfg = new BatchBillerPaymentConfig();
							billerPaymentCnfg.setId(rs.getInt("ID"));
							billerPaymentCnfg.setBillerCode(rs.getString(BILLER_CODE));
							billerPaymentCnfg.setIbkFtpFolder(rs.getString(IBK_FTP_FOLDER));
							billerPaymentCnfg.setFtpFolder(rs.getString("FTP_FOLDER"));
							billerPaymentCnfg.setFileNameFormat(rs.getString(FILE_NAME_FORMAT));
							billerPaymentCnfg.setReportUnitUri(rs.getString("REPORT_UNIT_URI"));
							billerPaymentCnfg.setTemplateId(rs.getInt("TEMPLATE_ID"));
							billerPaymentCnfg.setReportNameFormat(rs.getString("REPORT_NAME_FORMAT"));
							billerPaymentCnfg.setReportTemplateId(rs.getInt("REPORT_TEMPLATE_ID"));
							return billerPaymentCnfg;
						}
					});
			return billerPaymentCnfg;
		}catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public BoBillerTemplateConfig getBillerTemplateConfigDtls(Integer templateId) {

		JdbcTemplate template = getJdbcTemplate();
		BoBillerTemplateConfig boBillerTemplateCnfg;
		String sql = "SELECT TEMPLATE_ID,TEMPLATE_NAME,TEMPLATE_CODE,VIEW_NAME,CREATED_TIME, "
				+ "CREATED_BY,UPDATED_TIME,UPDATED_BY,LINE_SKIP_FROM_TOP,LINE_SKIP_FROM_BOTTOM "
				+ "FROM dcpbo.dbo.TBL_BO_BILLER_TEMPLATE_CONFIG WHERE TEMPLATE_ID=?";

		logger.info(String.format("getBillerTemplateConfigDtls sql=%s", sql));
		try {
			boBillerTemplateCnfg = template.queryForObject(sql, new Object[] {templateId},

					new RowMapper<BoBillerTemplateConfig>() {
						public BoBillerTemplateConfig mapRow(ResultSet rs, int rowNum) throws SQLException {

							BoBillerTemplateConfig boBillerTemplateCnfg = new BoBillerTemplateConfig();
							boBillerTemplateCnfg.setTemplateId(rs.getInt("TEMPLATE_ID"));
							boBillerTemplateCnfg.setTemplateName(rs.getString("TEMPLATE_NAME"));
							boBillerTemplateCnfg.setTemplateCode(rs.getString("TEMPLATE_CODE"));
							boBillerTemplateCnfg.setViewName(rs.getString("VIEW_NAME"));
							boBillerTemplateCnfg.setCreatedTime(rs.getDate("CREATED_TIME"));
							boBillerTemplateCnfg.setCreatedBy(rs.getString("CREATED_BY"));
							boBillerTemplateCnfg.setUpdatedTime(rs.getDate("UPDATED_TIME"));
							boBillerTemplateCnfg.setUpdatedBy(rs.getString("UPDATED_BY"));
							boBillerTemplateCnfg.setLineSkipFromTop(rs.getInt("LINE_SKIP_FROM_TOP"));
							boBillerTemplateCnfg.setLineSkipFromBottom(rs.getInt("LINE_SKIP_FROM_BOTTOM"));

							return boBillerTemplateCnfg;
						}
					});
			return boBillerTemplateCnfg;
		}catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	public List<BoBillerTemplateTagConfig> getBillerTemplateTagConfigDtls(int templateDetailId) {

		JdbcTemplate template = getJdbcTemplate();
		List<BoBillerTemplateTagConfig> boBillerTemplateTagConfiglst;
		String sql = "SELECT TEMPLATE_TAG_ID,TEMPLATE_ID,TAG_NAME,IS_RECURRING,SEQUENCE,CREATED_TIME, "
				+ "CREATED_BY,UPDATED_TIME,UPDATED_BY "
				+ "FROM dcpbo.dbo.TBL_BO_BILLER_TEMPLATE_TAG_CONFIG WHERE TEMPLATE_ID=?";

		logger.info(String.format("getBillerTemplateTagConfigDtls sql=%s", sql));
		boBillerTemplateTagConfiglst = template.query(sql, new Object[] {templateDetailId},
				new BeanPropertyRowMapper(BoBillerTemplateTagConfig.class));
		return boBillerTemplateTagConfiglst;
	}

	public List<BoBillerTemplateTagFieldConfig> getBillerTemplateTagFieldConfigDtls(int templateTagId) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoBillerTemplateTagFieldConfig> boBillerTemplateTagFieldConfiglst;
		String sql = "SELECT TEMPLATE_FIELD_ID,TEMPLATE_TAG_ID,FIELD_NAME,FIELD_TYPE,LENGTH,IS_MANDATORY,VALUE_TYPE,DEFAULT_VALUE,IS_AGGREGATION_REQUIRED,AGGREGATION_TYPE, "
				+ "IS_PADDING_REQUIRED,PADDING_TYPE,PADDING_FILL_VALUE,VIEW_FIELD_NAME,SEQUENCE,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY "
				+ "FROM dcpbo.dbo.TBL_BO_BILLER_TEMPLATE_TAG_FIELD_CONFIG WHERE TEMPLATE_TAG_ID=?";

		logger.info(String.format("getBillerTemplateTagFieldConfigDtls sql=%s", sql));
		boBillerTemplateTagFieldConfiglst = template.query(sql, new Object[] {templateTagId},
				new BeanPropertyRowMapper(BoBillerTemplateTagFieldConfig.class));
		return boBillerTemplateTagFieldConfiglst;
	}


}
