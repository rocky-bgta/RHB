package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.rhbgroup.dcp.bo.batch.job.model.FpxPaymentGateway;

@Component
public class FpxPaymentRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(FpxPaymentRepositoryImpl.class);
	
	public void insertSummary(Date date){
		
		int rows = deleteBatchStagedFpxPaymentGateway(date);
		logger.info(String.format("Deleted rows count = %s", rows));
		
		List<FpxPaymentGateway> fpxPaymentGatewayList = getFpxPaymentGatewayList(date);		
		logger.info(String.format("Fpx Payment Gateway list=%s", fpxPaymentGatewayList));	
		
		for(FpxPaymentGateway fpxPaymentGateway:fpxPaymentGatewayList) {
			insertFpxPaymentGateway(fpxPaymentGateway,date);
		}

		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_FPX_PAYMENT_GATEWAY");

	}
	
	
	public int deleteBatchStagedFpxPaymentGateway(Date date){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_FPX_PAYMENT_GATEWAY where CAST(TXN_DATE as DATE) = ?";
		logger.info(String.format("  sql=%s", sql));
		return getJdbcTemplate().update(sql
                , new Object[] {date});

    }
	
	public List<FpxPaymentGateway> getFpxPaymentGatewayList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<FpxPaymentGateway> fpxPaymentGateway;
		String sql = "SELECT sum(a.amount) AS 'txnAmount',sum(a.SERVICE_CHARGE) AS 'merchantCharge',sum(a.GST_AMOUNT) AS 'tax',COUNT(a.amount) AS txnCount, a.channel , a.txn_status FROM dcpbo.dbo.TBL_BO_TRANSFER_TXN a WHERE CAST(a.txn_time as DATE)=? AND a.MAIN_FUNCTION = 'FPX' AND a.TXN_STATUS='SUCCESS' GROUP BY a.channel,a.TXN_STATUS";
		logger.info(String.format("get fpx payment list sql=%s", sql));
		fpxPaymentGateway = template.query(sql, new Object[] { date },
				new BeanPropertyRowMapper<FpxPaymentGateway>(FpxPaymentGateway.class));
		return fpxPaymentGateway;
	}
	
	public Boolean insertFpxPaymentGateway(FpxPaymentGateway fpxPaymentGateway,Date txnTime) {
		long time = System.currentTimeMillis();
		Date date = new java.sql.Date(time);
		try {
            getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_SUMMARY_FPX_PAYMENT_GATEWAY (TXN_DATE, CHANNEL, tax, merchant_charge, TXN_STATUS, TXN_COUNT, TXN_AMOUNT, CREATED_TIME) values (?,?,?,?,?,?,?,?)"
                    , new Object[]{
                    		txnTime,
        					fpxPaymentGateway.getChannel(),
        					fpxPaymentGateway.getTax(),
        					fpxPaymentGateway.getMerchantCharge(),
        					fpxPaymentGateway.getTxnStatus(),
        					fpxPaymentGateway.getTxnCount(),
        					fpxPaymentGateway.getTxnAmount(),
        					date
                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
	
}
