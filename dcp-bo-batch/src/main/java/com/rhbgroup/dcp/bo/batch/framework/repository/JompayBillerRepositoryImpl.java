package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.JompayBiller;
import com.rhbgroup.dcp.bo.batch.job.model.StagedJompayBiller;

@Component
public class JompayBillerRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(JompayBillerRepositoryImpl.class);
	private static final String SELECT_QUERY = "SELECT * ";

	public void insertDailySummary(Date date) throws BatchException{
		
		int rows = deleteBatchStagedJompayBillerSummary(date);
		logger.info(String.format("Deleted rows count = %s", rows));
		
		List<StagedJompayBiller> stagedJompayBillerList = getStagedJompayBillerList(date);
		logger.info(String.format("Bo staged jompay biller payment list = %s", stagedJompayBillerList));
		

		insertStagedJompayBiller(stagedJompayBillerList,date);
		logger.info("Successfully inserted into database TBL_BATCH_STAGED_JOMPAY_BILLER_SUMMARY ");


	}
	
	public int deleteBatchStagedJompayBillerSummary(Date date){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_JOMPAY_BILLER_SUMMARY where CAST(TXN_DATE as DATE) = ?";
		logger.info(String.format("  sql=%s", sql));
        return getJdbcTemplate().update(sql
                , new Object[] {date});

    }
	
	public List<StagedJompayBiller> getStagedJompayBillerList(Date date) {

		JdbcTemplate template = getJdbcTemplate();
		List<StagedJompayBiller> stagedJompayBiller;
		
		String sql = "select sum(TXN_COUNT) as TXN_COUNT ,sum(GROSS_AMOUNT) as GROSS_AMOUNT,SUM(MERCHANT_CHARGE) as MERCHANT_CHARGE,\r\n"
				+ "\r\n"
				+ "SUM(TAX) as TAX,channel,payment_method,main_function,bank_code_ibg,biller_code from\r\n"
				+ "\r\n"
				+ "(SELECT COUNT(*) as TXN_COUNT,SUM(a.AMOUNT) as GROSS_AMOUNT, SUM(a.TOTAL_SERVICE_CHARGE) as MERCHANT_CHARGE,\r\n"
				+ "\r\n"
				+ "SUM(a.GST_AMOUNT) as TAX,a.CHANNEL,CASE WHEN a.PAYMENT_METHOD='SAVINGS' or a.PAYMENT_METHOD = 'CURRENT'\r\n"
				+ "\r\n"
				+ "THEN 'CASA' ELSE a.PAYMENT_METHOD END AS PAYMENT_METHOD ,a.MAIN_FUNCTION, a.BANK_CODE_IBG,b.BILLER_CODE\r\n"
				+ "\r\n"
				+ "FROM dcpbo.dbo.TBL_BO_PAYMENT_TXN a JOIN dcp.dbo.TBL_JOMPAY_BILLER b ON\r\n"
				+ "\r\n"
				+ "a.TO_BILLER_ID = CAST(b.BILLER_CODE as int) WHERE a.MAIN_FUNCTION = 'JOMPAY_BILLER' AND\r\n"
				+ "\r\n"
				+ "a.txn_status='SUCCESS' AND TXN_TIME > =? AND TXN_TIME < DATEADD(DAY,1,?)\r\n"
				+ "\r\n"
				+ "GROUP BY a.CHANNEL, b.BILLER_CODE, a.MAIN_FUNCTION ,\r\n"
				+ "\r\n"
				+ "CAST(a.TXN_TIME as DATE), a.BANK_CODE_IBG ,a.PAYMENT_METHOD) mix_queries\r\n"
				+ "\r\n"
				+ "group by mix_queries.channel,mix_queries.biller_code,mix_queries.main_function,mix_queries.PAYMENT_METHOD ,mix_queries.bank_code_ibg";
		logger.info(String.format("get staged other biller payment txn sql=%s", sql));
		stagedJompayBiller = template.query(sql, new Object[] { date,date },
				new BeanPropertyRowMapper<StagedJompayBiller>(StagedJompayBiller.class));
		return stagedJompayBiller;
	}
	
	
	public void insertStagedJompayBiller(List<StagedJompayBiller> stagedJompayBillerList,Date date) throws BatchException{
		for(StagedJompayBiller stagedJompayBiller:stagedJompayBillerList) {

			
			JompayBiller jompayBiller = getBillerByCode(stagedJompayBiller.getBillerCode());
			
			String bankCodeIbg = setBankCode(stagedJompayBiller.getBankCodeIbg());
			
			logger.info("Inserting into TBL_BATCH_STAGED_JOMPAY_BILLER_SUMMARY");
			insertJompaySummary(stagedJompayBiller,jompayBiller,bankCodeIbg,date);
		}
		
	}
	
	public JompayBiller getBillerByCode(String billerCode) {

		JdbcTemplate template = getJdbcTemplate();
		JompayBiller jompayBiller;
		
		String sql = SELECT_QUERY + "FROM dcp.dbo.TBL_JOMPAY_BILLER a WHERE a.BILLER_CODE = ?";
		logger.info(String.format("get jompay biller sql=%s", sql));		
		jompayBiller =  template.queryForObject(sql, new Object[] {billerCode},
				new BeanPropertyRowMapper<JompayBiller>(JompayBiller.class));
		
		return jompayBiller;
	}
	
	
	public String setBankCode(String codeIbg) {
		String bankCodeIbg;
		
		if(codeIbg == null || !(codeIbg.equals("100002186"))) {
			bankCodeIbg = "On Off Us";
		}else{
			bankCodeIbg = "On US";
		}
		return bankCodeIbg;
	}
	
	public int insertJompaySummary(StagedJompayBiller stagedJompayBiller,JompayBiller biller,String bankCodeIbg,Date date) throws BatchException{
		long time = System.currentTimeMillis();
		Date now = new java.sql.Date(time);
		try {
			String sql = "INSERT INTO dcpbo.dbo.TBL_BATCH_STAGED_JOMPAY_BILLER_SUMMARY"
					+ "(CHANNEL,BILLER_CODE,BILLER_NAME,BILLER_CATEGORY,BILLER_TYPE,PAYMENT_METHOD,TXN_DATE,TXN_COUNT,GROSS_AMT_MYR,"
					+ "MERCHANT_CHARGE_MYR,TAX_MYR,NET_AMOUNT_MYR,CREATED_TIME)" + " VALUES " + "(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			return getJdbcTemplate().update(sql,
					stagedJompayBiller.getChannel(),
					biller.getBillerCode(),
					biller.getBillerName(),
					stagedJompayBiller.getMainFunction(),
					bankCodeIbg,
					stagedJompayBiller.getPaymentMethod(),
					date,
					stagedJompayBiller.getTxnCount(),
					stagedJompayBiller.getGrossAmount(),
					stagedJompayBiller.getMerchantCharge(),
					stagedJompayBiller.getTax(),
					stagedJompayBiller.getGrossAmount(),
					now);
	        } catch (Exception e) {
	        	String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_STAGED_JOMPAY_BILLER_SUMMARY table with values [%s] ", biller.toString());
	            logger.error(errorMessage, e);
	            throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
	        }

	}

}
