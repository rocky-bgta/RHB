package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.Biller;
import com.rhbgroup.dcp.bo.batch.job.model.StagedOtherBiller;

@Component
public class BillerSettlementRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BillerSettlementRepositoryImpl.class);
	private static final String SELECT_QUERY = "SELECT * ";
	private static final String VALUES = "VALUES";
	private static final String NET_AMOUNT = "NET_AMOUNT";
	private static final String SELECT_VALUES = "SELECT COUNT(*) as TXN_COUNT,SUM(a.AMOUNT) as GROSS_AMOUNT, SUM(a.TOTAL_SERVICE_CHARGE) as MERCHANT_CHARGE,SUM(a.GST_AMOUNT) as TAX,\r\n";
	private static final String OTHER_VALUES = "a.CHANNEL,a.PAYMENT_METHOD,b.PAYMENT_MODE,c.CATEGORY_NAME,b.BILLER_CODE FROM \r\n";
	private static final String GROUP_BY_VALUES = "GROUP BY a.CHANNEL, b.BILLER_CODE, c.CATEGORY_NAME , a.PAYMENT_METHOD , b.BILLER_TYPE ,b.PAYMENT_MODE";
	

	public void insertDailySummary(Date date) throws BatchException{
		
		int rows = deleteBatchStagedSummaryDaily(date);
		logger.info(String.format("Deleted rows count = %s", rows));
		
		List<StagedOtherBiller> stagedOtherBillerDailyList = getStagedOtherBillerDailyList(date);
		logger.info(String.format("Bo staged other biller daily payment list = %s", stagedOtherBillerDailyList));

		insertOtherBillerDaily(stagedOtherBillerDailyList,date);
		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY ");

		List<StagedOtherBiller> stagedOtherTopupBillerDailyList = getStagedOtherTopupBillerDailyList(date);
		logger.info(String.format("Bo staged other topup biller dailypayment list = %s", stagedOtherTopupBillerDailyList));
		
		insertOtherTopupBillerDaily(stagedOtherTopupBillerDailyList,date);
		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY ");

	}
	
	public int deleteBatchStagedSummaryDaily(Date date){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY where CAST(TXN_DATE as DATE) = ?";
		logger.info(String.format("  sql=%s", sql));
        return getJdbcTemplate().update(sql
                , new Object[] {date});

    }
	
	public List<StagedOtherBiller> getStagedOtherBillerDailyList(Date date) {

		JdbcTemplate template = getJdbcTemplate();
		List<StagedOtherBiller> stagedOtherBiller;
		
		String sql = SELECT_VALUES
				+ OTHER_VALUES
				+ "dcpbo.dbo.TBL_BO_PAYMENT_TXN a JOIN dcp.dbo.TBL_BILLER b ON a.TO_BILLER_ID = b.ID JOIN \r\n"
				+ "dcp.dbo.TBL_BILLER_CATEGORY c ON b.CATEGORY_ID = c.ID \r\n"
				+ "WHERE a.main_function='OTHER_BILLER' AND a.txn_status='SUCCESS' AND TXN_TIME > =? AND TXN_TIME < DATEADD(DAY,1,?)\r\n"
				+ GROUP_BY_VALUES;
		logger.info(String.format("get staged other biller payment txn sql=%s", sql));
		stagedOtherBiller = template.query(sql, new Object[] { date,date },
				new BeanPropertyRowMapper<StagedOtherBiller>(StagedOtherBiller.class));
		return stagedOtherBiller;
	}
	
	public List<StagedOtherBiller> getStagedOtherTopupBillerDailyList(Date date) {

		JdbcTemplate template = getJdbcTemplate();
		List<StagedOtherBiller> stagedOtherBiller;
		
		String sql = SELECT_VALUES
				+ OTHER_VALUES
				+ "dcpbo.dbo.TBL_BO_TOPUP_TXN a JOIN dcp.dbo.TBL_TOPUP_BILLER b ON a.TO_BILLER_ID = b.ID JOIN \r\n"
				+ "dcp.dbo.TBL_TOPUP_BILLER_CATEGORY c ON b.CATEGORY_ID = c.ID \r\n"
				+ "WHERE a.main_function='TOPUP' AND a.txn_status='SUCCESS' AND TXN_TIME > =? AND TXN_TIME < DATEADD(DAY,1,?)\r\n"
				+ GROUP_BY_VALUES;
		logger.info(String.format("get staged other topup biller payment txn=%s", sql));
		stagedOtherBiller = template.query(sql, new Object[] { date,date },
				new BeanPropertyRowMapper<StagedOtherBiller>(StagedOtherBiller.class));
		return stagedOtherBiller;
	}
	
	public void insertOtherBillerDaily(List<StagedOtherBiller> stagedOtherBillerList,Date date) throws BatchException{
		String bankName = null;
		for(StagedOtherBiller stagedOtherBiller:stagedOtherBillerList) {
			BigDecimal netAmount;

			if(stagedOtherBiller.getPaymentMode()!=null && stagedOtherBiller.getPaymentMode().equals(NET_AMOUNT)) {
					netAmount = stagedOtherBiller.getGrossAmount().subtract(stagedOtherBiller.getMerchantCharge());
					netAmount = netAmount.subtract(stagedOtherBiller.getTax());
			}else {
				netAmount = stagedOtherBiller.getGrossAmount();

			}
			
			Biller biller = getBillerByCode(stagedOtherBiller.getBillerCode());
			
			if(biller.getPaymentBankId()!=null) {
				bankName = getBankName(biller.getPaymentBankId());
			}
			
			logger.info("Inserting into TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY");
			insertSummary(biller,stagedOtherBiller,netAmount,bankName,date);
		}
		
	}

	public void insertOtherTopupBillerDaily(List<StagedOtherBiller> stagedOtherTopupBillerDailyList,Date date) throws BatchException{
		String bankName = null;
		for(StagedOtherBiller stagedOtherBiller:stagedOtherTopupBillerDailyList) {
			BigDecimal netAmount;

			if(stagedOtherBiller.getPaymentMode()!=null && stagedOtherBiller.getPaymentMode().equals(NET_AMOUNT)) {
					netAmount = stagedOtherBiller.getGrossAmount().subtract(stagedOtherBiller.getMerchantCharge());
					netAmount = netAmount.subtract(stagedOtherBiller.getTax());
			}else {
				netAmount = stagedOtherBiller.getGrossAmount();

			}
			
			Biller biller = getTopupBillerByCode(stagedOtherBiller.getBillerCode());
			
			if(biller.getPaymentBankId()!=null) {
				bankName = getBankName(biller.getPaymentBankId());
			}
			
			logger.info("Inserting into TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY");
			insertTopupSummary(biller,stagedOtherBiller,netAmount,bankName,date);
		}
		
	}
	
	public String getBankName(int bankId) {

		JdbcTemplate template = getJdbcTemplate();
		String bankName;
		
		String sql = "SELECT a.BANK_NAME FROM dcp.dbo.TBL_BANK a WHERE ID =?";
		logger.info(String.format("get bank name sql=%s", sql));
		bankName = template.queryForObject(sql,new Object[] {bankId},String.class);
		
		return bankName;
	}
	
	
	public int insertSummary(Biller biller,StagedOtherBiller stagedOtherBiller,BigDecimal netAmount, String bankName,Date txnDate) throws BatchException{
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		String billerType = "OTHER_BILLER";
		try {
			String sql = "INSERT INTO dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY"
					+ "(BILLER_CODE,BILLER_NAME,SETTLEMENT_MODE,TRANSFER_MODE,BILLER_COLLECTION_ACCOUNT,TO_CREDITED_BANK,TO_CREDITED_ACCOUNT,GROSS_AMOUNT,"
					+ "MERCHANT_CHARGE,TAX,NET_AMOUNT,CREATED_TIME,TXN_DATE,BILLER_TYPE)" + VALUES + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			return getJdbcTemplate().update(sql,
					biller.getBillerCode(),
					biller.getBillerName(),
					biller.getPaymentMode(),
					biller.getPaymentMethod(),
					biller.getBillerCollectionAccountNo(),
					bankName,
					biller.getPaymentAccountNo(),
					stagedOtherBiller.getGrossAmount(),
					stagedOtherBiller.getMerchantCharge(),
					stagedOtherBiller.getTax(),
					netAmount,
					now,
					txnDate,
					billerType);
	        } catch (Exception e) {
	        	String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY table with biller values [%s] ", biller.toString());
	            logger.error(errorMessage, e);
	            throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
	        }

	}
	
	public int insertTopupSummary(Biller biller,StagedOtherBiller stagedOtherBiller,BigDecimal netAmount, String bankName,Date txnDate) throws BatchException{
		Timestamp now = new Timestamp(System.currentTimeMillis());		
		try {
			String sql = "INSERT INTO dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY"
					+ "(BILLER_CODE,BILLER_NAME,SETTLEMENT_MODE,TRANSFER_MODE,BILLER_COLLECTION_ACCOUNT,TO_CREDITED_BANK,TO_CREDITED_ACCOUNT,GROSS_AMOUNT,"
					+ "MERCHANT_CHARGE,TAX,NET_AMOUNT,CREATED_TIME,TXN_DATE,BILLER_TYPE)" + VALUES + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			return getJdbcTemplate().update(sql,
					biller.getBillerCode(),
					biller.getBillerName(),
					biller.getPaymentMode(),
					biller.getPaymentMethod(),
					biller.getBillerCollectionAccountNo(),
					bankName,
					biller.getPaymentAccountNo(),
					stagedOtherBiller.getGrossAmount(),
					stagedOtherBiller.getMerchantCharge(),
					stagedOtherBiller.getTax(),
					netAmount,
					now,
					txnDate,
					biller.getBillerType());
	        } catch (Exception e) {
	        	String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY table for topup values [%s] ", biller.toString());
	            logger.error(errorMessage, e);
	            throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
	        }

	}
	
	public void insertSummary(Date date) throws BatchException{
		
		int rows = deleteBatchStagedSummaryOtherBiller(date);
		logger.info(String.format("Deleted rows count = %s", rows));
		
		List<StagedOtherBiller> stagedOtherBillerList = getStagedOtherBillerList(date);
		logger.info(String.format("Bo staged other biller payment list = %s", stagedOtherBillerList));

		insertOtherBiller(stagedOtherBillerList,date);
		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER ");

		List<StagedOtherBiller> stagedOtherTopupBillerList = getStagedOtherTopupBillerList(date);
		logger.info(String.format("Bo staged other topup biller payment list = %s", stagedOtherTopupBillerList));
		
		insertOtherTopupBiller(stagedOtherTopupBillerList,date);
		logger.info("Successfully inserted into database TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER ");

	}
	
	public int deleteBatchStagedSummaryOtherBiller(Date date){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER where CAST(TXN_DATE as DATE) = ?";
		logger.info(String.format("  sql=%s", sql));
        return getJdbcTemplate().update(sql
                , new Object[] {date});

    }
	
	public List<StagedOtherBiller> getStagedOtherBillerList(Date date) {

		JdbcTemplate template = getJdbcTemplate();
		List<StagedOtherBiller> stagedOtherBiller;
		
		String sql = "select sum(TXN_COUNT) as TXN_COUNT ,sum(GROSS_AMOUNT) as GROSS_AMOUNT,SUM(MERCHANT_CHARGE) as MERCHANT_CHARGE,\r\n"
				+ "SUM(TAX) as TAX,channel,payment_method,payment_mode,category_name,biller_code from\r\n"
				+ "(SELECT COUNT(*) as TXN_COUNT,SUM(a.AMOUNT) as GROSS_AMOUNT,\r\n"
				+ "SUM(a.TOTAL_SERVICE_CHARGE) as MERCHANT_CHARGE,SUM(a.GST_AMOUNT) as TAX,\r\n"
				+ "a.CHANNEL,CASE WHEN a.PAYMENT_METHOD='SAVINGS' or a.PAYMENT_METHOD = 'CURRENT'\r\n"
				+ "THEN 'CASA' ELSE a.PAYMENT_METHOD END AS PAYMENT_METHOD,b.PAYMENT_MODE,\r\n"
				+ "c.CATEGORY_NAME,b.BILLER_CODE FROM\r\n"
				+ "dcpbo.dbo.TBL_BO_PAYMENT_TXN a JOIN dcp.dbo.TBL_BILLER b ON a.TO_BILLER_ID = b.ID JOIN\r\n"
				+ "dcp.dbo.TBL_BILLER_CATEGORY c ON b.CATEGORY_ID = c.ID\r\n"
				+ "WHERE a.txn_status='SUCCESS' AND TXN_TIME > =? AND TXN_TIME < DATEADD(DAY,1,?)\r\n"
				+ "GROUP BY a.CHANNEL, b.BILLER_CODE, c.CATEGORY_NAME , a.PAYMENT_METHOD ,b.BILLER_TYPE ,b.PAYMENT_MODE) mix_queries\r\n"
				+ "group by mix_queries.channel,mix_queries.biller_code,mix_queries.category_name,mix_queries.PAYMENT_METHOD ,mix_queries.payment_mode";
		logger.info(String.format("get staged other biller payment txn sql=%s", sql));
		stagedOtherBiller = template.query(sql, new Object[] { date,date },
				new BeanPropertyRowMapper<StagedOtherBiller>(StagedOtherBiller.class));
		return stagedOtherBiller;
	}
	
	public List<StagedOtherBiller> getStagedOtherTopupBillerList(Date date) {

		JdbcTemplate template = getJdbcTemplate();
		List<StagedOtherBiller> stagedOtherBiller;
		
		String sql = "SELECT COUNT(*) as TXN_COUNT,SUM(a.AMOUNT) as GROSS_AMOUNT, SUM(a.TOTAL_SERVICE_CHARGE) as MERCHANT_CHARGE,"
				+ "SUM(a.GST_AMOUNT) as TAX,\r\n"
				+ "a.CHANNEL,a.PAYMENT_METHOD,"
				+ "b.PAYMENT_MODE,c.CATEGORY_NAME,b.BILLER_CODE FROM \r\n"
				+ "dcpbo.dbo.TBL_BO_TOPUP_TXN a JOIN dcp.dbo.TBL_TOPUP_BILLER b ON a.TO_BILLER_ID = b.ID JOIN \r\n"
				+ "dcp.dbo.TBL_TOPUP_BILLER_CATEGORY c ON b.CATEGORY_ID = c.ID \r\n"
				+ "WHERE a.txn_status='SUCCESS' AND TXN_TIME > =? AND TXN_TIME < DATEADD(DAY,1,?)\r\n"
				+ "GROUP BY a.CHANNEL, b.BILLER_CODE, c.CATEGORY_NAME , a.PAYMENT_METHOD ,b.BILLER_TYPE ,b.PAYMENT_MODE";
		logger.info(String.format("get staged other topup biller payment txn=%s", sql));
		stagedOtherBiller = template.query(sql, new Object[] { date,date },
				new BeanPropertyRowMapper<StagedOtherBiller>(StagedOtherBiller.class));
		return stagedOtherBiller;
	}
	
	public Biller getBillerByCode(String billerCode) {

		JdbcTemplate template = getJdbcTemplate();
		Biller biller;
		
		String sql = SELECT_QUERY + "FROM dcp.dbo.TBL_BILLER a WHERE a.BILLER_CODE = ?";
		logger.info(String.format("get biller sql=%s", sql));		
		biller =  template.queryForObject(sql, new Object[] {billerCode},
				new BeanPropertyRowMapper<Biller>(Biller.class));
		
		return biller;
	}
	
	public Biller getTopupBillerByCode(String billerCode) {

		JdbcTemplate template = getJdbcTemplate();
		Biller biller;
		
		String sql = SELECT_QUERY + "FROM dcp.dbo.TBL_TOPUP_BILLER a WHERE a.BILLER_CODE = ?";
		logger.info(String.format("get biller sql=%s", sql));		
		biller = template.queryForObject(sql, new Object[] {billerCode},
				new BeanPropertyRowMapper<Biller>(Biller.class));
		
		return biller;
	}
	
	public void insertOtherBiller(List<StagedOtherBiller> stagedOtherBillerList,Date date) throws BatchException{
		for(StagedOtherBiller stagedOtherBiller:stagedOtherBillerList) {
			BigDecimal netAmount;

			if(stagedOtherBiller.getPaymentMode()!=null && stagedOtherBiller.getPaymentMode().equals(NET_AMOUNT)) {
					netAmount = stagedOtherBiller.getGrossAmount().subtract(stagedOtherBiller.getMerchantCharge());
					netAmount = netAmount.subtract(stagedOtherBiller.getTax());
			}else {
				netAmount = stagedOtherBiller.getGrossAmount();

			}
			
			Biller biller = getBillerByCode(stagedOtherBiller.getBillerCode());
			
			logger.info("Inserting into TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER");
			insertStagedSummary(biller,stagedOtherBiller,netAmount,date);
		}
		
	}
	
	public void insertOtherTopupBiller(List<StagedOtherBiller> stagedOtherTopupBillerList,Date date) throws BatchException{
		for(StagedOtherBiller stagedOtherBiller:stagedOtherTopupBillerList) {
			BigDecimal netAmount;
			if(stagedOtherBiller.getPaymentMode()!=null && stagedOtherBiller.getPaymentMode().equals(NET_AMOUNT)) {
					netAmount = stagedOtherBiller.getGrossAmount().subtract(stagedOtherBiller.getMerchantCharge());
					netAmount = netAmount.subtract(stagedOtherBiller.getTax());
			}else {
				netAmount = stagedOtherBiller.getGrossAmount();

			}
			
			Biller biller = getTopupBillerByCode(stagedOtherBiller.getBillerCode());
			
			logger.info("Inserting into TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER");
			insertStagedTopupSummary(biller,stagedOtherBiller,netAmount,date);
		}
		
	}
	
	public int insertStagedSummary(Biller biller,StagedOtherBiller stagedOtherBiller,BigDecimal netAmount, Date date) throws BatchException{
		String billerType =" ";

		long time = System.currentTimeMillis();
		Date now = new java.sql.Date(time); 		
		try {

			if(biller.getBillerType() != null) {
				billerType = biller.getBillerType();
			}
			
			String sql = "INSERT INTO dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER"
					+ "(TXN_DATE,CHANNEL,BILLER_CODE,BILLER_NAME,BILLER_CATEGORY,BILLER_TYPE,PAYMENT_METHOD,TXN_COUNT,GROSS_AMOUNT,"
					+ "MERCHANT_CHARGE,TAX,NET_AMOUNT,CREATED_TIME)" + " VALUES " + "(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			return getJdbcTemplate().update(sql,
					date,
					stagedOtherBiller.getChannel(),
					biller.getBillerCode(),
					biller.getBillerName(),
					stagedOtherBiller.getCategoryName(),
					billerType,
					stagedOtherBiller.getPaymentMethod(),
					stagedOtherBiller.getTxnCount(),
					stagedOtherBiller.getGrossAmount(),
					stagedOtherBiller.getMerchantCharge(),
					stagedOtherBiller.getTax(),
					netAmount,
					now);
	        } catch (Exception e) {
	        	String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY table with biller values [%s] ", biller.toString());
	            logger.error(errorMessage, e);
	            throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
	        }

	}
	
	public int insertStagedTopupSummary(Biller biller,StagedOtherBiller stagedOtherBiller,BigDecimal netAmount, Date date) throws BatchException{
		long time = System.currentTimeMillis();
		Date now = new java.sql.Date(time); 
		String billerType =" ";
		try {

			if(biller.getBillerType() != null) {
				billerType = biller.getBillerType();
			}

			
			String sql = "INSERT INTO dcpbo.dbo.TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER"
					+ "(TXN_DATE,CHANNEL,BILLER_CODE,BILLER_NAME,BILLER_CATEGORY,BILLER_TYPE,PAYMENT_METHOD,TXN_COUNT,GROSS_AMOUNT,"
					+ "MERCHANT_CHARGE,TAX,NET_AMOUNT,CREATED_TIME)" + " VALUES " + "(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			return getJdbcTemplate().update(sql,
					date,
					stagedOtherBiller.getChannel(),
					biller.getBillerCode(),
					biller.getBillerName(),
					stagedOtherBiller.getCategoryName(),
					billerType,
					stagedOtherBiller.getPaymentMethod(),
					stagedOtherBiller.getTxnCount(),
					stagedOtherBiller.getGrossAmount(),
					stagedOtherBiller.getMerchantCharge(),
					stagedOtherBiller.getTax(),
					netAmount,
					now);
	        } catch (Exception e) {
	        	String errorMessage = String.format("Error happened while inserting new record to TBL_BATCH_STAGED_SUMMARY_OTHER_BILLER_SETTLEMENT_DAILY table with topup values [%s] ", biller.toString());
	            logger.error(errorMessage, e);
	            throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
	        }

	}

}