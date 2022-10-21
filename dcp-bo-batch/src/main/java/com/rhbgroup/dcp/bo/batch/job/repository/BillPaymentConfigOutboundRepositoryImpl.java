package com.rhbgroup.dcp.bo.batch.job.repository;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BillerPaymentFileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;


@Component
@Lazy
public class BillPaymentConfigOutboundRepositoryImpl extends BaseRepositoryImpl {
	private static final Logger logger = Logger.getLogger(BillPaymentConfigOutboundRepositoryImpl.class);

	@Autowired
	private BillerPaymentFileJobConfigProperties configProperties;

	public List<BillerPaymentOutboundConfig> getBillerConfigOutbound() {

		JdbcTemplate template = getJdbcTemplate();
		String select ="SELECT payment_conf.id, payment_conf.biller_code as billerCode," +
				" payment_conf.template_name as templateName," +
				" payment_conf.ftp_folder as ftpFolder," +
				" payment_conf.file_name_format as fileNameFormat," +
				" payment_conf.report_unit_uri as reportUnitUri, " +
				" biller.status, " +
				" biller.biller_collection_account_no as billerAccNo," +
				" biller.biller_name  as billerAccName" ;
		String from=" FROM TBL_BATCH_BILLER_PAYMENT_CONFIG payment_conf " +
				" join vw_batch_tbl_biller biller on payment_conf.biller_code=biller.biller_code " ;
		String where=" WHERE payment_conf.is_required_to_execute=1 and payment_conf.template_name like ? and payment_conf.processed_by='LDCPD9001F'" ;
		String templatename  = configProperties.getTemplateNamel();
		String sql = select + from + where;
		logger.info(String.format("get biller config sql=%s", sql));
		if(null == templatename || templatename.isEmpty()) {
			templatename = "%";
		}
		return template.query(sql, new Object[]{templatename} , new BeanPropertyRowMapper(BillerPaymentOutboundConfig.class));

	}

	public List<BillerPaymentOutboundConfig> getActiveOrNonInactiveOrDeleteBillerConfigOutbound(BatchParameter batchSystemDate) {

		JdbcTemplate template = getJdbcTemplate();
		String select ="SELECT payment_conf.id, payment_conf.biller_code as billerCode," +
				" payment_conf.template_name as templateName," +
				" payment_conf.ftp_folder as ftpFolder," +
				" payment_conf.file_name_format as fileNameFormat," +
				" payment_conf.report_unit_uri as reportUnitUri, " +
				" biller.status, " +
				" biller.biller_collection_account_no as billerAccNo," +
				" biller.biller_name  as billerAccName" ;
		String from=" FROM TBL_BATCH_BILLER_PAYMENT_CONFIG payment_conf " +
				" join vw_batch_tbl_biller biller on payment_conf.biller_code=biller.biller_code " ;
		String where=" WHERE biller.status='ACTIVE' OR (biller.status in ('INACTIVE', 'DELETED') AND DATEDIFF(day,CAST(biller.updated_time AS DATE),CAST(? AS DATE)) <= 1)";
		String sql = select + from + where;
		logger.info(String.format("get biller config sql=%s", sql));
		return template.query(sql, new Object[]{batchSystemDate.getValue()} , new BeanPropertyRowMapper(BillerPaymentOutboundConfig.class));
	}




	public List<BillerPaymentOutboundConfig> getActiveBillerConfigOutBound() {
		logger.info("BillerPaymentOutboundConfig for getActiveMethod");
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		String sql ="SELECT payment_conf.id, payment_conf.biller_code as billerCode, payment_conf.ftp_folder as ftpFolder,payment_conf.file_name_format as fileNameFormat, payment_conf.report_unit_uri as reportUnitUri, biller.status,biller.biller_collection_account_no as billerAccNo,biller.biller_name  as billerAccName, biller.category_id as categoryId FROM TBL_BATCH_BILLER_PAYMENT_CONFIG payment_conf  join vw_batch_tbl_biller biller on payment_conf.biller_code=biller.biller_code WHERE biller.status='ACTIVE'";
		logger.info(String.format("lis of getActiveBillerConfigOutBound sql=%s", sql));
		List<BillerPaymentOutboundConfig>	billerConfigOutbound = jdbcTemplate.query(sql,new BeanPropertyRowMapper(BillerPaymentOutboundConfig.class));
		logger.debug("list of paymentconfig"+billerConfigOutbound);
		return billerConfigOutbound;
	}
}
