package com.rhbgroup.dcp.bo.batch.job.repository;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BillerDynamicPaymentFileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundConfig;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;


@Component
@Lazy
public class BillDynamicPaymentConfigOutboundRepositoryImpl extends BaseRepositoryImpl {
	private static final Logger logger = Logger.getLogger(BillDynamicPaymentConfigOutboundRepositoryImpl.class);
	
	@Autowired
	private BillerDynamicPaymentFileJobConfigProperties configProperties;
	
	public List<BillerDynamicPaymentOutboundConfig> getBillerConfigOutbound() {

		JdbcTemplate template = getJdbcTemplate();
		String select ="SELECT payment_conf.id, payment_conf.biller_code as billerCode," +
				" payment_conf.template_name as templateName," + 
				" payment_conf.ftp_folder as ftpFolder," + 
				" payment_conf.file_name_format as fileNameFormat," + 
				" payment_conf.report_unit_uri as reportUnitUri, " + 
				" payment_conf.template_id as templateId, " + 
				" biller.status, " +
				" biller.biller_collection_account_no as billerAccNo," +
				" biller.biller_name  as billerAccName" ;
		String from=" FROM TBL_BATCH_BILLER_PAYMENT_CONFIG payment_conf " +
				" join vw_batch_tbl_biller biller on payment_conf.biller_code=biller.biller_code " ;
		String where=" WHERE payment_conf.is_required_to_execute=1 and payment_conf.processed_by='Dynamic_Batch'" ;
		String sql = select + from + where;
		logger.info(String.format("get biller config sql=%s", sql));
		return template.query(sql, new BeanPropertyRowMapper<>(BillerDynamicPaymentOutboundConfig.class));

	}

	public Integer getBillerTransactionCount(String billerCode,String txnDate) {

		JdbcTemplate template = getJdbcTemplate();
		String select = "SELECT count(*) ";
		String fromClause = " from vw_batch_biller_payment_txn_template paytxn "
				+ " join vw_batch_tbl_biller biller on biller.biller_code=paytxn.biller_code ";
		String whereClause = " where biller.biller_code=? and paytxn.txn_date =? GROUP BY paytxn.biller_code ";
		String sql = select + fromClause + whereClause;
		logger.info(String.format("get biller config sql=%s", sql));

		Integer transactionCount=0;
		try {
			transactionCount= template.queryForObject(sql, new Object[]{billerCode, txnDate}, (Integer.class));
		} catch (Exception e) {
			logger.info(String.format("template.queryForObject Exception=%s", e));
			transactionCount=  0;
		}
		return transactionCount;
	}

	public BigDecimal getBillerTransactionSum(String billerCode,String txnDate) {

		JdbcTemplate template = getJdbcTemplate();
		String select = "SELECT SUM(CAST(txn_amount AS DECIMAL(10,2))) ";
		String fromClause = " from vw_batch_biller_payment_txn_template paytxn "
				+ " join vw_batch_tbl_biller biller on biller.biller_code=paytxn.biller_code ";
		String whereClause = " where biller.biller_code=? and paytxn.txn_date =? GROUP BY paytxn.biller_code ";
		String sql = select + fromClause + whereClause;
		logger.info(String.format("get biller sum config  sql=%s", sql));

		BigDecimal transactionSum=new BigDecimal(0);
		try {
			transactionSum= template.queryForObject(sql, new Object[]{billerCode, txnDate}, (BigDecimal.class));
		} catch (Exception e) {
			logger.info(String.format("template.queryForObject Exception=%s", e));
		}
		return transactionSum;
	}
}
