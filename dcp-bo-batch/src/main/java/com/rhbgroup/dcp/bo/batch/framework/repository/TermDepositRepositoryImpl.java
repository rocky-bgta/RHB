package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.InvestTxn;
import com.rhbgroup.dcp.bo.batch.job.model.TermDeposit;

@Component
public class TermDepositRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(TermDepositRepositoryImpl.class);

	public void insertStagedData(Date batchProcessingDate)  {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(batchProcessingDate);
		logger.info(String.format("  strDate=%s", strDate));

		int rows = deleteBoInvestTxnData();
		logger.info(String.format("Deleted rows count = %s", rows));

		int borows = insertBoInvestTxn(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_INVEST_TXN = %s", borows));
	}

	public int deleteBoInvestTxnData(){
		String sql = "DELETE FROM dcpbo.dbo.TBL_BO_INVEST_TXN  Where MAIN_FUNCTION = 'TERM_DEPOSIT'";
		logger.info(String.format("deleteBoInvestTxnData sql=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql);

		return rowAffected;
	}

	public int insertBoInvestTxn(String date) {
		int row =0;

		try {

			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_INVEST_TXN"
					+ "(USER_ID, TXN_ID, REF_ID, MAIN_FUNCTION, SUB_FUNCTION, FROM_ACCOUNT_NO, FROM_ACCOUNT_NAME,"
					+ " TO_ACCOUNT_NO, TO_ACCOUNT_NAME, AMOUNT, RECIPIENT_REF, MULTI_FACTOR_AUTH, TXN_STATUS,"
					+ " TXN_TIME, SERVICE_CHARGE, GST_RATE, GST_AMOUNT, GST_TREATMENT_TYPE, GST_CALCULATION_METHOD,"
					+ " GST_TAX_CODE, GST_TXN_ID, GST_REF_NO, IS_QUICK_PAY, FROM_IP_ADDRESS, TXN_STATUS_CODE,"
					+ " FROM_ACCOUNT_CONNECTOR_CODE, TO_FAVOURITE_ID, CHANNEL, PAYMENT_METHOD, ACCESS_METHOD,"
					+ " DEVICE_ID, SUB_CHANNEL, TXN_TOKEN_ID, CURF_ID, REJECT_DESCRIPTION, REJECT_CODE,"
					+ " IS_SETUP_FAVOURITE, IS_SETUP_QUICK_LINK, IS_SETUP_QUICK_PAY, TXN_CCY, UPDATED_TIME,"
					+ " UPDATED_BY, CREATED_TIME, CREATED_BY)"
					+ " SELECT USER_ID, TXN_ID, REF_ID, MAIN_FUNCTION, SUB_FUNCTION, FROM_ACCOUNT_NO,"
					+ " FROM_ACCOUNT_NAME, TO_ACCOUNT_NO, TO_ACCOUNT_NAME, AMOUNT, RECIPIENT_REF,"
					+ " MULTI_FACTOR_AUTH, TXN_STATUS, TXN_TIME, SERVICE_CHARGE, GST_RATE, GST_AMOUNT,"
					+ " GST_TREATMENT_TYPE, GST_CALCULATION_METHOD, GST_TAX_CODE, GST_TXN_ID, GST_REF_NO,"
					+ " IS_QUICK_PAY, FROM_IP_ADDRESS, TXN_STATUS_CODE, FROM_ACCOUNT_CONNECTOR_CODE,"
					+ " ISNULL(TO_FAVOURITE_ID,0) AS TO_FAVOURITE_ID, CHANNEL, PAYMENT_METHOD, ACCESS_METHOD,"
					+ " DEVICE_ID, SUB_CHANNEL, TXN_TOKEN_ID, CURF_ID, REJECT_DESCRIPTION, REJECT_CODE,"
					+ " ISNULL(IS_SETUP_FAVOURITE,0) AS IS_SETUP_FAVOURITE,"
					+ " ISNULL(IS_SETUP_QUICK_LINK,0) AS IS_SETUP_QUICK_LINK, ISNULL(IS_SETUP_QUICK_PAY,0) AS IS_SETUP_QUICK_PAY,"
					+ " TXN_CCY, UPDATED_TIME, UPDATED_BY, CREATED_TIME, CREATED_BY"
					+ " FROM dcp.dbo.TBL_INVEST_TXN WHERE MAIN_FUNCTION = 'TERM_DEPOSIT' AND CREATED_TIME > = ? AND CREATED_TIME < DATEADD(DAY,1,?)";

			logger.info(String.format("insertBoInvestTxn sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
		} catch (Exception e) {
			logger.error(e);
		}

		return row;
	}


	public int deleteBatchStagedTermDeposit(Date date){
		String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_TERM_DEPOSIT where CAST(TXN_DATE as DATE) = ?";
		logger.info(String.format("deleteBatchStagedTermDeposit sql=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql
				, new Object[] {date});

		return rowAffected;
	}

	public void insertSummary(Date date){

		int rows = deleteBatchStagedTermDeposit(date);
		logger.info(String.format("Deleted rows count = %s", rows));

		List<TermDeposit> termDepositList = getTermDepositList(date);
		logger.info(String.format("Term Deposit list=%s", termDepositList));

		String txnType = null;
		for(TermDeposit termDeposit:termDepositList) {
			if(termDeposit.getPaymentMethod() != null && termDeposit.getPaymentMethod().equals("FPX")) {
				txnType = termDeposit.getSubFunction() + "_FPX";
			}else {
				txnType = termDeposit.getSubFunction();
			}
			insertSummaryTermDeposit(termDeposit,txnType,date);
		}


		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_TERM_DEPOSIT");

	}


	public List<TermDeposit> getTermDepositList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<TermDeposit> termDepositList;
		String sql =  "SELECT sum(a.amount) AS 'sum',COUNT(a.amount) AS count, a.channel , a.txn_status,a.payment_method,a.SUB_FUNCTION FROM dcpbo.dbo.TBL_BO_INVEST_TXN a WHERE CAST(a.txn_time as DATE)=? AND a.MAIN_FUNCTION = 'TERM_DEPOSIT' GROUP BY a.channel , a.txn_status,a.payment_method,a.SUB_FUNCTION";
		logger.info(String.format("get invest term deposit list  sql=%s", sql));
		termDepositList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper(TermDeposit.class));
		return termDepositList;
	}


	public Boolean insertSummaryTermDeposit(TermDeposit termDeposit,String txnType,Date date) {
		Date now = new Date();
		try {

			getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_SUMMARY_TERM_DEPOSIT (TXN_DATE, CHANNEL, TXN_TYPE, TXN_STATUS, TXN_COUNT,TXN_AMOUNT,CREATED_TIME) values (?,?,?,?,?,?,?)"
					, new Object[]{
							date
							,termDeposit.getChannel()
							,txnType
							,termDeposit.getTxnStatus()
							,termDeposit.getCount()
							,termDeposit.getSum()
							,now
					});
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

}
