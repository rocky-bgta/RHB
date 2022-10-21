package com.rhbgroup.dcp.bo.batch.framework.repository;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class FinancialTransactionRepositoryImpl extends BaseRepositoryImpl {

    private static final Logger logger = Logger.getLogger(FinancialTransactionRepositoryImpl.class);

    public void insertSummary() {

        int rows = insertBatchStagedFinTxnCount();
        logger.info(String.format("Successfully inserted into database TBL_BATCH_STAGED_FINANCIAL_TXN_COUNT = %s", rows));
    }

    public int totalRec() {
        String countSQL = "SELECT COUNT DISTINCT (a.TXN_TIME) FROM (SELECT DATEADD (dd, 0, DATEDIFF (dd, 0, TXN_TIME)) AS TXN_TIME FROM DCPBO.DBO.TBL_BO_TRANSFER_TXN UNION ALL SELECT DATEADD (dd, 0, DATEDIFF (dd, 0, TXN_TIME)) AS TXN_TIME FROM DCPBO.DBO.TBL_BO_PAYMENT_TXN UNION ALL SELECT DATEADD (dd, 0, DATEDIFF (dd, 0, TXN_TIME)) AS TXN_TIME FROM DCPBO.DBO.TBL_BO_INVEST_TXN UNION ALL SELECT DATEADD (dd, 0, DATEDIFF (dd, 0, TXN_TIME)) AS TXN_TIME FROM DCPBO.DBO.TBL_BO_TOPUP_TXN) a";
        return getJdbcTemplate().queryForObject(countSQL, Integer.class);
    }

    public int insertBatchStagedFinTxnCount() {
        int row = 0;

        try {

            String sql = "INSERT INTO dcpbo.dbo.TBL_BATCH_STAGED_FINANCIAL_TXN_COUNT\r\n"
                    + "(USER_ID, TXN_DATE, OWN_ACCT_VOLUME, OWN_ACCT_AMT, OWN_ACCT_MORTGAGE_VOLUME, OWN_ACCT_MORTGAGE_AMT,\r\n"
                    + "OWN_ACCT_CREDIT_CARD_VOLUME, OWN_ACCT_CREDIT_CARD_AMT,\r\n"
                    + "OWN_ACCT_ASB_VOLUME, OWN_ACCT_ASB_AMT, OWN_ACCT_PERSONAL_FIN_VOLUME, OWN_ACCT_PERSONAL_FIN_AMT,\r\n"
                    + "OWN_ACCT_HP_VOLUME, OWN_ACCT_HP_AMT, OWN_ACCT_PREPAID_VOLUME, OWN_ACCT_PREPAID_AMT,\r\n"
                    + "DUITNOW_VOLUME, DUITNOW_AMT, DUITNOW_QR_VOLUME, DUITNOW_QR_AMT,\r\n"
                    + "INSTANT_TRANSFER_VOLUME, INSTANT_TRANSFER_AMT, IBG_VOLUME, IBG_AMT,\r\n"
                    + "INTRABANK_OPEN_VOLUME, INTRABANK_OPEN_AMT, INTRABANK_FAV_VOLUME, INTRABANK_FAV_AMT,\r\n"
                    + "JOMPAY_OPEN_VOLUME, JOMPAY_OPEN_AMT, JOMPAY_FAV_VOLUME, JOMPAY_FAV_AMT,\r\n"
                    + "RHB_BILLER_OPEN_VOLUME, RHB_BILLER_OPEN_AMT, RHB_BILLER_FAV_VOLUME, RHB_BILLER_FAV_AMT,\r\n"
                    + "PREPAID_TOPUP_VOLUME, PREPAID_TOPUP_AMT, PREPAID_TOPUP_FAV_VOLUME, PREPAID_TOPUP_FAV_AMT,\r\n"
                    + "ASNB_VOLUME, ASNB_AMT, MCA_BUY_CURRENCY_VOLUME, MCA_BUY_CURRENCY_AMT, MCA_SELL_CURRENCY_VOLUME,\r\n"
                    + "MCA_SELL_CURRENCY_AMT, MCA_BUY_METAL_VOLUME, MCA_BUY_METAL_AMT, MCA_SELL_METAL_VOLUME, MCA_SELL_METAL_AMT,\r\n"
                    + "MCA_TD_PLACEMENT_VOLUME, MCA_TD_PLACEMENT_AMT, MCA_TD_WITHDRAW_VOLUME, MCA_TD_WITHDRAW_AMT, TD_PLACEMENT_VOLUME,\r\n"
                    + "TD_PLACEMENT_AMT, TD_WITHDRAW_VOLUME, TD_WITHDRAW_AMT, INWARD_FPX_VOLUME, INWARD_FPX_AMT, OUTWARD_FPX_VOLUME,\r\n"
                    + "OUTWARD_FPX_AMT, CREATED_TIME)\r\n"
                    + "select USER_ID, TXN_DATE, OWN_ACCT_VOLUME, OWN_ACCT_AMT, OWN_ACCT_MORTGAGE_VOLUME, OWN_ACCT_MORTGAGE_AMT,\r\n"
                    + "OWN_ACCT_CREDIT_CARD_VOLUME, OWN_ACCT_CREDIT_CARD_AMT,\r\n"
                    + "OWN_ACCT_ASB_VOLUME, OWN_ACCT_ASB_AMT, OWN_ACCT_PERSONAL_FIN_VOLUME, OWN_ACCT_PERSONAL_FIN_AMT,\r\n"
                    + "OWN_ACCT_HP_VOLUME, OWN_ACCT_HP_AMT, OWN_ACCT_PREPAID_VOLUME, OWN_ACCT_PREPAID_AMT,\r\n"
                    + "DUITNOW_VOLUME, DUITNOW_AMT, DUITNOW_QR_VOLUME, DUITNOW_QR_AMT,\r\n"
                    + "INSTANT_TRANSFER_VOLUME, INSTANT_TRANSFER_AMT, IBG_VOLUME, IBG_AMT,\r\n"
                    + "INTRABANK_OPEN_VOLUME, INTRABANK_OPEN_AMT, INTRABANK_FAV_VOLUME, INTRABANK_FAV_AMT,\r\n"
                    + "JOMPAY_OPEN_VOLUME, JOMPAY_OPEN_AMT, JOMPAY_FAV_VOLUME, JOMPAY_FAV_AMT,\r\n"
                    + "RHB_BILLER_OPEN_VOLUME, RHB_BILLER_OPEN_AMT, RHB_BILLER_FAV_VOLUME, RHB_BILLER_FAV_AMT,\r\n"
                    + "PREPAID_TOPUP_VOLUME, PREPAID_TOPUP_AMT, PREPAID_TOPUP_FAV_VOLUME, PREPAID_TOPUP_FAV_AMT,\r\n"
                    + "ASNB_VOLUME, ASNB_AMT, MCA_BUY_CURRENCY_VOLUME, MCA_BUY_CURRENCY_AMT, MCA_SELL_CURRENCY_VOLUME, MCA_SELL_CURRENCY_AMT,\r\n"
                    + "MCA_BUY_METAL_VOLUME, MCA_BUY_METAL_AMT, MCA_SELL_METAL_VOLUME, MCA_SELL_METAL_AMT, MCA_TD_PLACEMENT_VOLUME,\r\n"
                    + "MCA_TD_PLACEMENT_AMT, MCA_TD_WITHDRAW_VOLUME, MCA_TD_WITHDRAW_AMT, TD_PLACEMENT_VOLUME, TD_PLACEMENT_AMT,\r\n"
                    + "TD_WITHDRAW_VOLUME, TD_WITHDRAW_AMT, INWARD_FPX_VOLUME, INWARD_FPX_AMT, OUTWARD_FPX_VOLUME, OUTWARD_FPX_AMT,\r\n"
                    + "CREATED_TIME from dcpbo.dbo.vw_batch_summarization_fin_transaction;";

            logger.info(String.format("  sql=%s", sql));
            row = getJdbcTemplate().update(sql);
        } catch (Exception e) {
            logger.error(e);
        }
        return row;
    }

}
