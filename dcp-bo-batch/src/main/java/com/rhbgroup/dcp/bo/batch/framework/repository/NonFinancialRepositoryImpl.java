package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.rhbgroup.dcp.bo.batch.job.model.CashxcessTxn;
import com.rhbgroup.dcp.bo.batch.job.model.DcpAudit;

@Component
public class NonFinancialRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(NonFinancialRepositoryImpl.class);

	public void insertStagedData(Date batchProcessingDate)  {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(batchProcessingDate);
		logger.info(String.format("  strDate=%s", strDate));

		int deleteAuditRows = deleteBoAuditData(strDate);
		logger.info(String.format("Deleted rows count = %s", deleteAuditRows));

		int rows = insertBoCashxcessTxn(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_CASHXCESS_TXN = %s", rows));

		int auditrows = insertBoDcpAudit(strDate);
		logger.info(String.format("Successfully inserted into database DCP_BO_AUDIT = %s", auditrows));
	}


	public int deleteBoAuditData(String date){
		String sql = "DELETE FROM dcpbo.dbo.DCP_BO_AUDIT where TIMESTAMP >= ? AND TIMESTAMP < DATEADD(DAY,1,?)";
		logger.info(String.format("deleteBoAuditData sql=%s", sql));
		return getJdbcTemplate().update(sql
				, new Object[] {date,date});

	}

	public int insertBoCashxcessTxn(String date) {
		int row =0;

		try {
			date = date.replace("-", "");
			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_CASHXCESS_TXN"
					+ "(TXN_TOKEN_ID, CHANNEL_CODE, SUB_CHANNEL, DELIVERY_CHANNEL, FUNCTION_CODE, CARD_NUMBER, BENEFICIARY_NAME, REQUEST_DATE, REQUEST_TIME, PLAN_NUMBER, TENURE, TOTAL_AMOUNT, TRX_DESCRIPTION, MERCHANT_ID, CARD_EXPIRY_DATE, ENTRY_STAFF_ID, REGION_CODE, BRANCH_CODE, REFERRAL_ID, SALES_STAFF_ID, EFFECTIVE_RATE, INTEREST_RATE, MONTHLY_INSTALLMENT_AMOUNT, TO_BANK_NAME, OBD1_DISBURSEMENT_METHOD, OBD1_BANK_ID, OBD1_CASA_ACCOUNT, OBD1_AMOUNT, OBD1_CTRL3, OBD2_DISBURSEMENT_METHOD, OBD2_BANK_ID, OBD2_CASA_ACCOUNT, OBD2_AMOUNT, OBD2_CTRL3, OBD3_DISBURSEMENT_METHOD, OBD3_BANK_ID, OBD3_CASA_ACCOUNT, OBD3_AMOUNT, OBD3_CTRL3)"
					+ " SELECT TXN_TOKEN_ID, CHANNEL_CODE, SUB_CHANNEL, DELIVERY_CHANNEL, FUNCTION_CODE, CARD_NUMBER, BENEFICIARY_NAME, REQUEST_DATE, REQUEST_TIME, PLAN_NUMBER, TENURE, TOTAL_AMOUNT, TRX_DESCRIPTION, MERCHANT_ID, CARD_EXPIRY_DATE, ENTRY_STAFF_ID, REGION_CODE, BRANCH_CODE, REFERRAL_ID, SALES_STAFF_ID, EFFECTIVE_RATE, INTEREST_RATE, MONTHLY_INSTALLMENT_AMOUNT, TO_BANK_NAME, OBD1_DISBURSEMENT_METHOD, OBD1_BANK_ID, OBD1_CASA_ACCOUNT, OBD1_AMOUNT, OBD1_CTRL3, OBD2_DISBURSEMENT_METHOD, OBD2_BANK_ID, OBD2_CASA_ACCOUNT, OBD2_AMOUNT, OBD2_CTRL3, OBD3_DISBURSEMENT_METHOD, OBD3_BANK_ID, OBD3_CASA_ACCOUNT, OBD3_AMOUNT, OBD3_CTRL3"
					+ " FROM dcp.dbo.TBL_CASHXCESS_TXN WHERE REQUEST_DATE > =? AND REQUEST_DATE < DATEADD(DAY,1,?)";

			logger.info(String.format("insertBoCashxcessTxn sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
		} catch (Exception e) {
			logger.error(e);
		}
		return row;
	}

	public int insertBoDcpAudit(String date) {
		int row =0;
		try {
			date = date.replace("-", "");
			String sql = "INSERT INTO dcpbo.dbo.DCP_BO_AUDIT"
					+ "(EVENT_CODE, USER_ID, UPDATED_BY, USERNAME, CIS_NO, DEVICE_ID, STATUS_CODE, STATUS_DESCRIPTION, CHANNEL, IP_ADDRESS, [TIMESTAMP])"
					+ " SELECT EVENT_CODE, USER_ID, UPDATED_BY, USERNAME, CIS_NO, DEVICE_ID, STATUS_CODE, STATUS_DESCRIPTION, CHANNEL, IP_ADDRESS, [TIMESTAMP]"
					+ " FROM dcp.dbo.DCP_AUDIT WHERE [TIMESTAMP] > =? AND [TIMESTAMP] < DATEADD(DAY,1,?)";

			logger.info(String.format("insertBoDcpAudit sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
		} catch (Exception e) {
			logger.error(e);
		}
		return row;
	}

}
