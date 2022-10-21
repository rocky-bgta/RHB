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

import com.rhbgroup.dcp.bo.batch.job.model.PaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.TopupTxn;

@Component
public class PaymentRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(PaymentRepositoryImpl.class);



	public void insertStagedData(Date batchProcessingDate)  {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(batchProcessingDate);
		logger.info(String.format("  strDate=%s", strDate));

		int rows = deleteBoPaymentData();
		logger.info(String.format("Deleted rows count = %s", rows));

		int deleteTopupRows = deleteBoTopupData();
		logger.info(String.format("Deleted rows count = %s", deleteTopupRows));

		int paymentrows = insertBoPaymentTxn(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_PAYMENT_TXN = %s", paymentrows));


		int topuprows = insertBoTopupTxn(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_TOPUP_TXN = %s", topuprows));
	}


	public int deleteBoPaymentData(){
		String sql = "TRUNCATE TABLE dcpbo.dbo.TBL_BO_PAYMENT_TXN";
		logger.info(String.format("deleteBoPaymentData sql=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql);

		return rowAffected;
	}

	public int deleteBoTopupData(){
		String sql = "TRUNCATE TABLE dcpbo.dbo.TBL_BO_TOPUP_TXN";
		logger.info(String.format("deleteBoTopupData sql=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql);

		return rowAffected;
	}

	public int insertBoPaymentTxn(String date) {
		int row =0;

		try {

			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_PAYMENT_TXN"
					+ "(USER_ID, TXN_ID, REF_ID, MULTI_FACTOR_AUTH, MAIN_FUNCTION, SUB_FUNCTION,"
					+ " FROM_ACCOUNT_NO, TO_ACCOUNT_NO, TO_BILLER_ID, TO_BILLER_ACCOUNT_NAME,"
					+ " TO_BILLER_ACCOUNT_CODE_NAME, VALIDATE_SIG, NICKNAME, REF_1, REF_2, REF_3,"
					+ " REF_4, RECIPIENT_REF, OTHER_PAYMENT_DETAIL, AMOUNT, REAL_TIME_NOTIFICATION,"
					+ " BANK_CODE_IBG, TOTAL_SERVICE_CHARGE, TXN_STATUS, TXN_TIME, GST_RATE, GST_AMOUNT,"
					+ " GST_TREATMENT_TYPE, GST_CALCULATION_METHOD, GST_TAX_CODE, GST_TXN_ID, GST_REF_NO,"
					+ " IS_QUICK_PAY, RRN_INFO, TXN_STATUS_CODE, UPDATED_TIME, FROM_CARD_NO, CHANNEL,"
					+ " PAYMENT_METHOD, TO_FAVOURITE_ID, TELLER_ID, TRACE_ID, IS_JOMPAY_QR, EAI_ERROR_CODE,"
					+ " EAI_ERROR_MSG, EAI_ERROR_PARAM, REJECTED_CODE, REJECTED_DESC)"
					+ " SELECT USER_ID, TXN_ID, REF_ID, MULTI_FACTOR_AUTH, MAIN_FUNCTION, SUB_FUNCTION,"
					+ " FROM_ACCOUNT_NO, TO_ACCOUNT_NO, TO_BILLER_ID, TO_BILLER_ACCOUNT_NAME,"
					+ " TO_BILLER_ACCOUNT_CODE_NAME, VALIDATE_SIG, NICKNAME, ISNULL(REF_1,' ') AS REF_1,"
					+ " REF_2, REF_3, REF_4, RECIPIENT_REF, OTHER_PAYMENT_DETAIL, AMOUNT,"
					+ " ISNULL(REAL_TIME_NOTIFICATION,0) AS REAL_TIME_NOTIFICATION, BANK_CODE_IBG,"
					+ " TOTAL_SERVICE_CHARGE, TXN_STATUS, TXN_TIME, GST_RATE, GST_AMOUNT, GST_TREATMENT_TYPE,"
					+ " GST_CALCULATION_METHOD, GST_TAX_CODE, GST_TXN_ID, GST_REF_NO,"
					+ " ISNULL(IS_QUICK_PAY,0) AS IS_QUICK_PAY, RRN_INFO, TXN_STATUS_CODE, UPDATED_TIME,"
					+ " FROM_CARD_NO, CHANNEL, PAYMENT_METHOD, ISNULL(TO_FAVOURITE_ID,0) AS TO_FAVOURITE_ID,"
					+ " TELLER_ID, TRACE_ID, ISNULL(IS_JOMPAY_QR,0) AS IS_JOMPAY_QR, EAI_ERROR_CODE, EAI_ERROR_MSG,"
					+ " EAI_ERROR_PARAM, REJECTED_CODE, REJECTED_DESC"
					+ " FROM dcp.dbo.TBL_PAYMENT_TXN a WHERE (MAIN_FUNCTION ='OTHER_BILLER' OR MAIN_FUNCTION ='JOMPAY_BILLER') AND TXN_TIME > =? AND TXN_TIME < DATEADD(DAY,1,?)";

			logger.info(String.format("insertBoPaymentTxn sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
		} catch (Exception e) {
			logger.error(e);
		}
		return row;
	}

	public int insertBoTopupTxn(String date) {
		int row =0;
		try {

			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_TOPUP_TXN"
					+ "(USER_ID, TXN_ID, REF_ID, TELLER_ID, TRACE_ID, MAIN_FUNCTION, MULTI_FACTOR_AUTH, FROM_ACCOUNT_NO,"
					+ " FROM_CARD_NO, TO_BILLER_ID, NICKNAME, REF_1, AMOUNT, TXN_TIME, TXN_STATUS, TXN_STATUS_CODE,"
					+ " TOTAL_SERVICE_CHARGE, PAYMENT_METHOD, GST_RATE, GST_AMOUNT, GST_TREATMENT_TYPE,"
					+ " GST_CALCULATION_METHOD, GST_TAX_CODE, GST_TXN_ID, GST_REF_NO, TO_FAVOURITE_ID,"
					+ " IS_QUICK_PAY, MOBILITY_ONE_TXN_ID, UPDATED_TIME, CHANNEL, STATUS, EAI_ERROR_CODE,"
					+ " EAI_ERROR_MSG, EAI_ERROR_PARAM)"
					+ " SELECT USER_ID, TXN_ID, REF_ID, TELLER_ID, TRACE_ID, MAIN_FUNCTION, MULTI_FACTOR_AUTH,"
					+ " FROM_ACCOUNT_NO, FROM_CARD_NO, TO_BILLER_ID, NICKNAME, ISNULL(REF_1,' ') AS REF_1, AMOUNT,"
					+ " TXN_TIME, TXN_STATUS, TXN_STATUS_CODE, TOTAL_SERVICE_CHARGE, PAYMENT_METHOD, GST_RATE,"
					+ " GST_AMOUNT, GST_TREATMENT_TYPE, GST_CALCULATION_METHOD, GST_TAX_CODE, GST_TXN_ID, GST_REF_NO,"
					+ " ISNULL(TO_FAVOURITE_ID,0) AS TO_FAVOURITE_ID, ISNULL(IS_QUICK_PAY,0) AS IS_QUICK_PAY,"
					+ " MOBILITY_ONE_TXN_ID, UPDATED_TIME, CHANNEL, TXN_STATUS, EAI_ERROR_CODE, EAI_ERROR_MSG,"
					+ " EAI_ERROR_PARAM"
					+ " FROM dcp.dbo.TBL_TOPUP_TXN WHERE MAIN_FUNCTION = 'TOPUP' AND TXN_TIME > =? AND TXN_TIME < DATEADD(DAY,1,?)";

			logger.info(String.format("insertBoTopupTxn sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
		} catch (Exception e) {
			logger.error(e);
		}
		return row;
	}


}