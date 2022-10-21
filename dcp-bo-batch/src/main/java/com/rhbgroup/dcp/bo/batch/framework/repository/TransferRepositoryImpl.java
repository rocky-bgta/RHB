package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;


@Component
public class TransferRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(TransferRepositoryImpl.class);

	public void insertStagedData(Date batchProcessingDate)  {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(batchProcessingDate);
		logger.info(String.format("  strDate=%s", strDate));

		int rows = deleteBoTransferTxnData();
		logger.info(String.format("Deleted rows count = %s", rows));

		int row = insertTransferTxnList(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_TRANSFER_TXN = %s", row));

	}


	public int deleteBoTransferTxnData(){
		String sql = "TRUNCATE TABLE dcpbo.dbo.TBL_BO_TRANSFER_TXN";
		logger.info(String.format("  sql=%s", sql));
		return getJdbcTemplate().update(sql);

	}

	public int insertTransferTxnList(String date) {
		int row =0;
		try {

			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_TRANSFER_TXN"
					+ "(USER_ID, TXN_TOKEN_ID, TXN_ID, REF_ID, CURF_ID, FIRST_INSTALMENT_DATE, TELLER_ID, TRACE_ID, MAIN_FUNCTION, SUB_FUNCTION, FROM_ACCOUNT_NO, FROM_ACCOUNT_NAME, TO_FAVOURITE_ID, TO_ACCOUNT_NO, TO_ACCOUNT_NAME, TO_BANK_ID, TO_ID_TYPE, TO_ID_NO, TO_RESIDENT_STATUS, AMOUNT, CURRENCY_CODE, RECIPIENT_REF, OTHER_PAYMENT_DETAILS, MULTI_FACTOR_AUTH, TXN_STATUS, TXN_STATUS_CODE, TXN_TIME, SERVICE_CHARGE, GST_RATE, GST_AMOUNT, GST_TREATMENT_TYPE, GST_CALCULATION_METHOD, GST_TAX_CODE, GST_TXN_ID, GST_REF_NO, IS_QUICK_PAY, IS_PRE_LOGIN, DUITNOW_COUNTRY_CODE, FROM_IP_ADDRESS, DUITNOW_TO_REGISTRATION_ID, DUITNOW_TO_BIC, UPDATED_TIME, FROM_ACCOUNT_CONNECTOR_CODE, CHANNEL, PAYMENT_METHOD, FROM_CARD_NO, FROM_CARD_HOLDER, ACCESS_METHOD, DEVICE_ID, SUB_CHANNEL, MOBILE_NO, EAI_ERROR_CODE, EAI_ERROR_MSG, EAI_ERROR_PARAM)"
					+ " SELECT USER_ID, TXN_TOKEN_ID, TXN_ID, REF_ID, CURF_ID, FIRST_INSTALMENT_DATE, TELLER_ID, TRACE_ID, MAIN_FUNCTION, SUB_FUNCTION, FROM_ACCOUNT_NO, FROM_ACCOUNT_NAME, TO_FAVOURITE_ID, TO_ACCOUNT_NO, TO_ACCOUNT_NAME, TO_BANK_ID, TO_ID_TYPE, TO_ID_NO, TO_RESIDENT_STATUS, AMOUNT, CURRENCY_CODE, RECIPIENT_REF, OTHER_PAYMENT_DETAILS, MULTI_FACTOR_AUTH, TXN_STATUS, TXN_STATUS_CODE, TXN_TIME, SERVICE_CHARGE, GST_RATE, GST_AMOUNT, GST_TREATMENT_TYPE, GST_CALCULATION_METHOD, GST_TAX_CODE, GST_TXN_ID, GST_REF_NO, IS_QUICK_PAY, IS_PRE_LOGIN, DUITNOW_COUNTRY_CODE, FROM_IP_ADDRESS, DUITNOW_TO_REGISTRATION_ID, DUITNOW_TO_BIC, UPDATED_TIME, FROM_ACCOUNT_CONNECTOR_CODE, CHANNEL, PAYMENT_METHOD, FROM_CARD_NO, FROM_CARD_HOLDER, ACCESS_METHOD, DEVICE_ID, SUB_CHANNEL, MOBILE_NO, EAI_ERROR_CODE, EAI_ERROR_MSG, EAI_ERROR_PARAM"
					+ " FROM dcp.dbo.TBL_TRANSFER_TXN WHERE TXN_TIME > = ? AND TXN_TIME < DATEADD(DAY,1,?)";

			logger.info(String.format("  sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
		} catch (Exception e) {
			logger.error(e);
		}

		return row;
	}


}
