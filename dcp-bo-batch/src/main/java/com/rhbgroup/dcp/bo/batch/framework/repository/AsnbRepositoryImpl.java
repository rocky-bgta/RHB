package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.BoInvestTxn;

@Component
public class AsnbRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(AsnbRepositoryImpl.class);
	private static final String VALUES ="  VALUES  ";
	private static final String SUCCESS ="SUCCESS";
	private static final String FAILED ="FAILED";
	private static final String PENDING ="PENDING";

	public void insertStagedData(Date batchProcessingDate)  {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(batchProcessingDate);
		logger.info(String.format("  strDate=%s", strDate));

		int rows = deleteBoInvestTxnData();
		logger.info(String.format("Deleted Bo Invest rows count = %s", rows));
		
		int asnbRows = deleteBoAsnbTxnData();
		logger.info(String.format("Deleted Bo Asnb rows count = %s", asnbRows));
		
		int row = insertBoInvestTxn(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_INVEST_TXN = %s", row));

		int asnbrow = insertBoAsnbTxn(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_ASNB_TXN = %s", asnbrow));
	}
	
	public int deleteBoInvestTxnData(){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BO_INVEST_TXN  Where MAIN_FUNCTION IN ('ASNB_OWN', 'ASNB_THIRDPARTY')";
		logger.info(String.format("  sql delete bo invest=%s", sql));
       return getJdbcTemplate().update(sql);

    }
	
	public int deleteBoAsnbTxnData(){
        String sql = "TRUNCATE TABLE dcpbo.dbo.TBL_BO_ASNB_TXN";
		logger.info(String.format("  sql delete bo asnb=%s", sql));
        return getJdbcTemplate().update(sql);

    }
	
	public int insertBoInvestTxn(String date) {
		int row =0;
		try {
			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_INVEST_TXN"
					+ "(USER_ID, TXN_ID, REF_ID, MAIN_FUNCTION, SUB_FUNCTION, FROM_ACCOUNT_NO,"
					+ " FROM_ACCOUNT_NAME, TO_ACCOUNT_NO, TO_ACCOUNT_NAME, AMOUNT, RECIPIENT_REF,"
					+ " MULTI_FACTOR_AUTH, TXN_STATUS, TXN_TIME, SERVICE_CHARGE, GST_RATE, GST_AMOUNT,"
					+ " GST_TREATMENT_TYPE, GST_CALCULATION_METHOD, GST_TAX_CODE, GST_TXN_ID, GST_REF_NO,"
					+ " IS_QUICK_PAY, FROM_IP_ADDRESS, TXN_STATUS_CODE, FROM_ACCOUNT_CONNECTOR_CODE,"
					+ " TO_FAVOURITE_ID, CHANNEL, PAYMENT_METHOD, ACCESS_METHOD, DEVICE_ID, SUB_CHANNEL,"
					+ " TXN_TOKEN_ID, CURF_ID, REJECT_DESCRIPTION, REJECT_CODE, IS_SETUP_FAVOURITE,"
					+ " IS_SETUP_QUICK_LINK, IS_SETUP_QUICK_PAY, TXN_CCY, UPDATED_TIME, UPDATED_BY,"
					+ " CREATED_TIME, CREATED_BY)"
					+ " SELECT USER_ID, TXN_ID, REF_ID, MAIN_FUNCTION, SUB_FUNCTION, FROM_ACCOUNT_NO,"
					+ " FROM_ACCOUNT_NAME, TO_ACCOUNT_NO, TO_ACCOUNT_NAME, AMOUNT, RECIPIENT_REF,"
					+ " MULTI_FACTOR_AUTH, TXN_STATUS, TXN_TIME, SERVICE_CHARGE, GST_RATE, GST_AMOUNT,"
					+ " GST_TREATMENT_TYPE, GST_CALCULATION_METHOD, GST_TAX_CODE, GST_TXN_ID, GST_REF_NO,"
					+ " IS_QUICK_PAY, FROM_IP_ADDRESS, TXN_STATUS_CODE, FROM_ACCOUNT_CONNECTOR_CODE,"
					+ " ISNULL(TO_FAVOURITE_ID,0) AS TO_FAVOURITE_ID, CHANNEL, PAYMENT_METHOD,"
					+ " ACCESS_METHOD, DEVICE_ID, SUB_CHANNEL, TXN_TOKEN_ID, CURF_ID, REJECT_DESCRIPTION,"
					+ " REJECT_CODE, ISNULL(IS_SETUP_FAVOURITE,0) AS IS_SETUP_FAVOURITE, "
					+ "ISNULL(IS_SETUP_QUICK_LINK,0) AS IS_SETUP_QUICK_LINK , ISNULL(IS_SETUP_QUICK_PAY,0) AS IS_SETUP_QUICK_PAY ,"
					+ " TXN_CCY, UPDATED_TIME, UPDATED_BY, CREATED_TIME, CREATED_BY"
					+ " FROM dcp.dbo.TBL_INVEST_TXN WHERE (MAIN_FUNCTION = 'ASNB_OWN' OR MAIN_FUNCTION = 'ASNB_THIRDPARTY') AND CREATED_TIME > = ? AND CREATED_TIME < DATEADD(DAY,1,?)";

			logger.info(String.format("  sql for TBL_INVEST_TXN=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
        } catch (Exception e) {
            logger.error(e);
        }
		return row;
    }
	
	public int insertBoAsnbTxn(String date) {
		int row =0;
		try {
			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_ASNB_TXN"
					+ "(FUND_ID, FUND_LONG_NAME, ACCOUNT_HOLDER_ID_TYPE, ACCOUNT_HOLDER_ID_NO,"
					+ " SEQ_NUMBER, MEMBERSHIP_NUMBER, GUARDIAN_ID_TYPE, GUARDIAN_ID_NO,"
					+ " GUARDIAN_MEMBERSHIP_NO, NAV, SALES_CHARGE, SALES_TAX, TAX_INVOICE_NO,"
					+ " TXN_TOKEN_ID, FUND_PRICE, FEE_PERCENTAGE, UNITS_ALLOTED, EST_NUM_OF_UNITS,"
					+ " TXN_NO, PRICE_DATE, CREATED_BY, CREATED_TIME, UPDATED_TIME, UPDATED_BY,"
					+ " IS_RECONCILED, PNB_ERROR_CODE)"
					+ " SELECT a.FUND_ID, a.FUND_LONG_NAME, a.ACCOUNT_HOLDER_ID_TYPE,"
					+ " a.ACCOUNT_HOLDER_ID_NO, a.SEQ_NUMBER, a.MEMBERSHIP_NUMBER, a.GUARDIAN_ID_TYPE,"
					+ " a.GUARDIAN_ID_NO, a.GUARDIAN_MEMBERSHIP_NO, a.NAV, a.SALES_CHARGE, a.SALES_TAX,"
					+ " a.TAX_INVOICE_NO, a.TXN_TOKEN_ID, a.FUND_PRICE, a.FEE_PERCENTAGE, a.UNITS_ALLOTED,"
					+ " a.EST_NUM_OF_UNITS, a.TXN_NO, a.PRICE_DATE, a.CREATED_BY, a.CREATED_TIME,"
					+ " a.UPDATED_TIME, a.UPDATED_BY, ISNULL(a.IS_RECONCILED,0) AS IS_RECONCILED ,"
					+ " a.PNB_ERROR_CODE"
					+ " FROM dcp.dbo.TBL_ASNB_TXN a JOIN dcp.dbo.TBL_INVEST_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID"
					+ " WHERE (b.MAIN_FUNCTION = 'ASNB_OWN' OR b.MAIN_FUNCTION = 'ASNB_THIRDPARTY') AND b.CREATED_TIME > = ? AND b.CREATED_TIME < DATEADD(DAY,1,?)";

			logger.info(String.format("  sql for TBL_ASNB_TXN=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
        } catch (Exception e) {
            logger.error(e);
        }
		return row;
    }
	
	public void insertSummary(Date batchProcessingDate)  {
		
		int rows = deleteBatchStagedSummaryAsnb(batchProcessingDate);
		logger.info(String.format("Deleted rows count = %s", rows));
		
		List<BoInvestTxn> boInvestTxnList = getBoInvestTxnList(batchProcessingDate);
		logger.info(String.format("bo invest txn list = %s", boInvestTxnList));
		
		List<BoInvestTxn> boInvestTxnDMBPendingList = boInvestTxnList.stream().filter(e -> e.getTxnStatus().equals(PENDING) && e.getChannel().equals("DMB")).collect(Collectors.toList());
		List<BoInvestTxn> boInvestTxnDIBPendingList = boInvestTxnList.stream().filter(e -> e.getTxnStatus().equals(PENDING) && e.getChannel().equals("DIB")).collect(Collectors.toList());

		logger.info(String.format("bo invest txn DMB pending list = %s", boInvestTxnDMBPendingList));
		logger.info(String.format("bo invest txn DIB pending list = %s", boInvestTxnDIBPendingList));

		List<BoInvestTxn> boInvestTxnDMBFailedList = boInvestTxnList.stream().filter(e -> e.getTxnStatus().equals(FAILED) && e.getChannel().equals("DMB")).collect(Collectors.toList());
		List<BoInvestTxn> boInvestTxnDIBFailedList = boInvestTxnList.stream().filter(e -> e.getTxnStatus().equals(FAILED) && e.getChannel().equals("DIB")).collect(Collectors.toList());

		logger.info(String.format("bo invest txn failed list = %s", boInvestTxnDMBFailedList));
		logger.info(String.format("bo invest txn failed list = %s", boInvestTxnDIBFailedList));

		List<BoInvestTxn> boInvestTxnDMBSuccessList = boInvestTxnList.stream().filter(e -> e.getTxnStatus().equals(SUCCESS) && e.getChannel().equals("DMB")).collect(Collectors.toList());
		List<BoInvestTxn> boInvestTxnDIBSuccessList = boInvestTxnList.stream().filter(e -> e.getTxnStatus().equals(SUCCESS) && e.getChannel().equals("DIB")).collect(Collectors.toList());
		logger.info(String.format("bo invest txn success list = %s", boInvestTxnDMBSuccessList));
		logger.info(String.format("bo invest txn success list = %s", boInvestTxnDIBSuccessList));
		
		if(boInvestTxnDMBPendingList != null) {
			processStagedAsnb(boInvestTxnDMBPendingList,batchProcessingDate,"DMB",PENDING);
		}
		
		if(boInvestTxnDIBPendingList != null) {
			processStagedAsnb(boInvestTxnDIBPendingList,batchProcessingDate,"DIB",PENDING);
		}

		if(boInvestTxnDMBFailedList != null) {
			processStagedAsnb(boInvestTxnDMBFailedList,batchProcessingDate,"DMB",FAILED);
		}
		
		if(boInvestTxnDIBFailedList != null) {
			processStagedAsnb(boInvestTxnDIBFailedList,batchProcessingDate,"DIB",FAILED);
		}
		
		if(boInvestTxnDMBSuccessList != null) {
			processStagedAsnb(boInvestTxnDMBSuccessList,batchProcessingDate,"DMB",SUCCESS);
		}
		
		if(boInvestTxnDIBSuccessList != null) {
			processStagedAsnb(boInvestTxnDIBSuccessList,batchProcessingDate,"DIB",SUCCESS);
		}
		
		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_ASNB ");
	}
	

	public List<BoInvestTxn> getBoInvestTxnList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoInvestTxn> investBoTxnList;
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BO_INVEST_TXN a WHERE CAST(a.created_time as DATE)=? AND (a.main_function='ASNB_THIRDPARTY' OR a.main_function='ASNB_OWN')";
		logger.info(String.format("get Bo invest txn list  sql=%s", sql));
		investBoTxnList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper<BoInvestTxn>(BoInvestTxn.class));
		return investBoTxnList;
	}
	
	public int deleteBatchStagedSummaryAsnb(Date date){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_ASNB where CAST(TXN_DATE as DATE) = ?";
		logger.info(String.format("  sql=%s", sql));
        return getJdbcTemplate().update(sql
                , new Object[] {date});

    }
	
	public void processStagedAsnb(List<BoInvestTxn> boInvestTxnList,Date batchProcessingDate,String channel,String status)  {
		BigDecimal amount = new BigDecimal(0);
		BigDecimal serviceCharge = new BigDecimal(0);
		BigDecimal salesCharge = new BigDecimal(0);
        int count = boInvestTxnList.size();
		for(BoInvestTxn boInvestTxn:boInvestTxnList) {
			BigDecimal charge = retriveSalesCharge(batchProcessingDate,boInvestTxn.getTxnTokenId());
			amount= amount.add(boInvestTxn.getAmount());
			serviceCharge= serviceCharge.add(boInvestTxn.getServiceCharge());
			if(charge!=null) {
				salesCharge = salesCharge.add(charge);
			}
			logger.info(String.format("amount = %s serviceCharge = %s salesCharge = %s count = %s channel = %s status = %s ", amount,serviceCharge,salesCharge,count,channel,status));
		}
		
		if(count!=0) {
			insertStagedAsnbTxn(amount,serviceCharge,salesCharge,batchProcessingDate,count,channel,status);
		}
	}
	
	public BigDecimal retriveSalesCharge(Date date,Integer id)  {
		JdbcTemplate template = getJdbcTemplate();
		BigDecimal charge;
		String sql =  " SELECT a.sales_charge FROM dcpbo.dbo.TBL_BO_ASNB_TXN a WHERE CAST(a.created_time as DATE)=? AND a.txn_token_id=?";
		logger.info(String.format("get Bo asnb list  sql=%s", sql));
		charge = template.queryForObject(sql,new Object[] {date,id},(BigDecimal.class));
		return charge;
	}
	
	public Boolean insertStagedAsnbTxn(BigDecimal amount,BigDecimal serviceCharge,BigDecimal salesCharge,Date txnDate, int count, String channel,String status) {
        Date date = new Date();
		try {

            getJdbcTemplate().update("INSERT INTO dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_ASNB"
    				+ "(TXN_DATE,CHANNEL,TXN_COUNT,TXN_AMOUNT,SALES_CHARGE,BANK_CHARGE,TXN_STATUS,CREATED_TIME)" + VALUES+ "(?,?,?,?,?,?,?,?)"
                    , new Object[]{
                    		txnDate
                            ,channel
                            ,count
                            ,amount
                            ,salesCharge
                            ,serviceCharge
                            ,status
                            ,date
                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
}
