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
import org.springframework.dao.EmptyResultDataAccessException;
import com.rhbgroup.dcp.bo.batch.job.model.BoInvestTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BoMcaTxn;
import com.rhbgroup.dcp.bo.batch.job.model.CurrencyRateConfig;
import com.rhbgroup.dcp.bo.batch.job.model.McaCurrency;
import com.rhbgroup.dcp.bo.batch.job.model.McaPreciousMetal;
import com.rhbgroup.dcp.bo.batch.job.model.McaTermSummary;

@Component
public class McaRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(McaRepositoryImpl.class);	

	public void insertStagedData(Date batchProcessingDate)  {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(batchProcessingDate);
		logger.info(String.format("  strDate=%s", strDate));

		int rows = deleteBoInvestTxnData();
		logger.info(String.format("Deleted Bo Invest rows count = %s", rows));
		
		int mcaRow = deleteBoMcaTxnData();
		logger.info(String.format("Deleted Bo Mca rows count = %s", mcaRow));
			
		int investRows = insertBoInvestTxn(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_INVEST_TXN = %s", investRows));
		
		int mcaRows = insertBoMcaTxn(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_MCA_TXN = %s", mcaRows));
	}
	
	public int deleteBoInvestTxnData(){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BO_INVEST_TXN Where MAIN_FUNCTION = 'MCA'";
		logger.info(String.format("  sql for bo Invest=%s", sql));
        return getJdbcTemplate().update(sql);

    }
	
	public int deleteBoMcaTxnData(){
        String sql = "TRUNCATE TABLE dcpbo.dbo.TBL_BO_MCA_TXN";
		logger.info(String.format("  sql for bo mca=%s", sql));
        return getJdbcTemplate().update(sql);
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
						+ " ISNULL(IS_SETUP_FAVOURITE,0) AS IS_SETUP_FAVOURITE, ISNULL(IS_SETUP_QUICK_LINK,0) AS IS_SETUP_QUICK_LINK,"
						+ " ISNULL(IS_SETUP_QUICK_PAY,0) AS IS_SETUP_QUICK_PAY, TXN_CCY, UPDATED_TIME, UPDATED_BY, CREATED_TIME, CREATED_BY"
						+ " FROM dcp.dbo.TBL_INVEST_TXN WHERE MAIN_FUNCTION = 'MCA' AND CREATED_TIME > = ? AND CREATED_TIME < DATEADD(DAY,1,?)";
		
			logger.info(String.format("  sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
        } catch (Exception e) {
            logger.error(e);
        }

		return row;
	}


	public int insertBoMcaTxn(String date) {
		int row =0;

		try {
		
			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_MCA_TXN"
						+ "(TXN_TOKEN_ID, TO_CCY, TO_AMOUNT, CONVERSION_RATE, RATE_TYPE, PURPOSE_CODE,"
						+ " PURPOSE_TEXT, IS_STAFF, INTEREST_RATE, INTEREST_AMOUNT, FEA_UTILIZATION, TENURE_CODE,"
						+ " TENURE_DAYS, SHORT_NAME, LIMIT_CCY, LIMIT_AMT, RATE_USD, RATE_MYR, AMT_USD, AMT_MYR,"
						+ " VALUE_DATE, MATURITY_DATE, PRODUCT_CODE, TERM_REF_NO, PAYOUT_AMOUNT, CREATED_TIME,"
						+ " CREATED_BY, UPDATED_TIME, UPDATED_BY)"
						+ " SELECT a.TXN_TOKEN_ID, a.TO_CCY, a.TO_AMOUNT, a.CONVERSION_RATE, a.RATE_TYPE,"
						+ " a.PURPOSE_CODE, a.PURPOSE_TEXT, a.IS_STAFF, a.INTEREST_RATE, a.INTEREST_AMOUNT,"
						+ " a.FEA_UTILIZATION, a.TENURE_CODE, ISNULL(a.TENURE_DAYS,0) AS TENURE_DAYS, a.SHORT_NAME,"
						+ " a.LIMIT_CCY, a.LIMIT_AMT, a.RATE_USD, a.RATE_MYR, a.AMT_USD, a.AMT_MYR, a.VALUE_DATE,"
						+ " a.MATURITY_DATE, a.PRODUCT_CODE, a.TERM_REF_NO, a.PAYOUT_AMOUNT, a.CREATED_TIME,"
						+ " a.CREATED_BY, a.UPDATED_TIME, a.UPDATED_BY"
						+ " FROM dcp.dbo.TBL_MCA_TXN a JOIN dcp.dbo.TBL_INVEST_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID "
						+ "WHERE b.MAIN_FUNCTION = 'MCA' AND b.CREATED_TIME > = ? AND b.CREATED_TIME < DATEADD(DAY,1,?)";
		
			logger.info(String.format("  sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
        } catch (Exception e) {
            logger.error(e);
		}

		return row;
	}
	
	public void insertSummary(Date batchProcessingDate)  {
		
		int rows = deleteBatchStagedMcaCurrency(batchProcessingDate);
		logger.info(String.format("Deleted Mca rows count = %s", rows));
		
		List<McaCurrency> mcaBuyCurrencyList = getMcaBuyCurrencyList(batchProcessingDate);
		logger.info(String.format("mca buy currency list=%s", mcaBuyCurrencyList));	
		
		int mcaBuyRows = insertMcaBuyCurrency(mcaBuyCurrencyList,batchProcessingDate);
		logger.info(String.format("Successfully inserted in to db TBL_BATCH_STAGED_MCA_CURRENCY_TXN_SUMMARY =%s",mcaBuyRows));	
		
		List<McaCurrency> mcaSellCurrencyList = getMcaSellCurrencyList(batchProcessingDate);
		logger.info(String.format("mca buy currency list=%s", mcaBuyCurrencyList));	
		
		int mcaSellRows = insertMcaSellCurrency(mcaSellCurrencyList,batchProcessingDate);
		logger.info(String.format("Successfully inserted in to db TBL_BATCH_STAGED_MCA_CURRENCY_TXN_SUMMARY =%s",mcaSellRows));	
	}
	
	public int deleteBatchStagedMcaCurrency(Date date){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_MCA_CURRENCY_TXN_SUMMARY where CAST(TXN_DATE as DATE) = ?";
		logger.info(String.format("  sql delete currency summary=%s", sql));
		return getJdbcTemplate().update(sql
                , new Object[] {date});

    }
	
	public List<McaCurrency> getMcaBuyCurrencyList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<McaCurrency> mcaCurrencyList;
		String sql =  "SELECT SUM(a.AMOUNT) as invest_amt,SUM(b.TO_AMOUNT) as mca_amt,COUNT(*) as count,a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,b.TO_CCY\r\n"
				+ "FROM dcpbo.dbo.TBL_BO_INVEST_TXN a JOIN dcpbo.dbo.TBL_BO_MCA_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID \r\n"
				+ "JOIN dcp.dbo.TBL_CURRENCY_RATE_CONFIG c ON b.TO_CCY = c.CODE WHERE CAST(a.created_time as DATE)=? AND a.SUB_FUNCTION ='BUY'\r\n"
				+ "AND c.CURRENCY_TYPE = 'currency' GROUP BY a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,b.TO_CCY";
		logger.info(String.format("get Mca Buy Currency list  sql=%s", sql));
		mcaCurrencyList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper<McaCurrency>(McaCurrency.class));
		return mcaCurrencyList;
	}
	
	public List<McaCurrency> getMcaSellCurrencyList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<McaCurrency> mcaCurrencyList;
		String sql =  "SELECT SUM(a.AMOUNT) as invest_amt,SUM(b.TO_AMOUNT) as mca_amt,COUNT(*) as count,a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,a.TXN_CCY"
				+ " FROM dcpbo.dbo.TBL_BO_INVEST_TXN a JOIN dcpbo.dbo.TBL_BO_MCA_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID "
				+ " JOIN dcp.dbo.TBL_CURRENCY_RATE_CONFIG c ON a.TXN_CCY = c.CODE WHERE CAST(a.created_time as DATE)=? AND a.SUB_FUNCTION ='SELL'"
				+ " AND c.CURRENCY_TYPE = 'currency' GROUP BY a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,a.TXN_CCY";
		logger.info(String.format("get Mca Sell Currency list  sql=%s", sql));
		mcaCurrencyList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper<McaCurrency>(McaCurrency.class));
		return mcaCurrencyList;
	}
	
	public List<BoInvestTxn> getBoInvestTxnList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoInvestTxn> investBoTxnList;
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BO_INVEST_TXN a WHERE CAST(a.created_time as DATE)=? AND (a.sub_function='BUY' OR a.sub_function='SELL')";
		logger.info(String.format("get Bo invest txn list  sql=%s", sql));
		investBoTxnList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper<BoInvestTxn>(BoInvestTxn.class));
		return investBoTxnList;
	}
	
	public List<CurrencyRateConfig> getCurrencyRateConfigList() {
		JdbcTemplate template = getJdbcTemplate();
		List<CurrencyRateConfig> currencyRateConfigList;
		String sql =  " SELECT * FROM dcp.dbo.TBL_CURRENCY_RATE_CONFIG a WHERE a.CURRENCY_TYPE = 'currency'";
		logger.info(String.format("get currency rate config list  sql=%s", sql));
		currencyRateConfigList = template.query(sql,new Object[] {},
				new BeanPropertyRowMapper<CurrencyRateConfig>(CurrencyRateConfig.class));
		return currencyRateConfigList;
	}
	
	public BoMcaTxn getBoMcaTxn(Date date, String code,Integer txnTokenId) {
		JdbcTemplate template = getJdbcTemplate();
		BoMcaTxn boMcaTxn = new BoMcaTxn();
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BO_MCA_TXN a WHERE CAST(a.created_time as DATE)=? AND a.TO_CCY=? AND a.TXN_TOKEN_ID=? ";
		logger.info(String.format("get bo mca list  sql=%s", sql));
		try {
			boMcaTxn = template.queryForObject(sql,new Object[] {date,code,txnTokenId},
					new BeanPropertyRowMapper<BoMcaTxn>(BoMcaTxn.class));
		}catch(EmptyResultDataAccessException e) {
			 logger.info("bo mca txn is null");
		}

		return boMcaTxn;
	}
	
	public BoMcaTxn getBoMcaSellTxn(Date date, Integer txnTokenId) {
		JdbcTemplate template = getJdbcTemplate();
		BoMcaTxn boMcaTxn = new BoMcaTxn();
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BO_MCA_TXN a WHERE CAST(a.created_time as DATE)=? AND a.TXN_TOKEN_ID=? ";
		logger.info(String.format("get bo mca list  sql=%s", sql));
		try {
			boMcaTxn = template.queryForObject(sql,new Object[] {date,txnTokenId},
					new BeanPropertyRowMapper<BoMcaTxn>(BoMcaTxn.class));
		}catch(EmptyResultDataAccessException e) {
			 logger.info("bo mca Sell txn is null");
		}

		return boMcaTxn;
	}
	
	public BoInvestTxn getBoInvestTxnSell(Date date, String code,Integer txnTokenId) {
		JdbcTemplate template = getJdbcTemplate();
		BoInvestTxn boInvestTxn = new BoInvestTxn();
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BO_INVEST_TXN a WHERE CAST(a.created_time as DATE)=? AND a.TXN_CCY=? AND a.TXN_TOKEN_ID=?";
		logger.info(String.format("get bo invest txn list  sql=%s", sql));
		try {
			boInvestTxn = template.queryForObject(sql,new Object[] {date,code,txnTokenId},
					new BeanPropertyRowMapper<BoInvestTxn>(BoInvestTxn.class));
		}catch(EmptyResultDataAccessException e) {
			 logger.info("bo invest txn is null");
		}

		return boInvestTxn;
	}
	
	
	public int insertMcaBuyCurrency(List<McaCurrency> records,Date date) {
		java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		jdbcTemplate.setDataSource(dataSource);
		String sql = "INSERT INTO TBL_BATCH_STAGED_MCA_CURRENCY_TXN_SUMMARY (PRODUCT_TYPE, TXN_DATE, CURRENCY, CHANNEL,"
				+ " TXN_COUNT, AMOUNT_IN_MYR, AMOUNT_IN_FCY, TXN_STATUS, CREATED_TIME) values (?,?,?,?,?,?,?,?,?)";
		
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				McaCurrency mcaCurrency = records.get(i);
				ps.setString(1,mcaCurrency.getSubFunction());
				ps.setDate(2, sqlDate);
				ps.setString(3, mcaCurrency.getToCcy());
				ps.setString(4, mcaCurrency.getChannel());
				ps.setInt(5, mcaCurrency.getCount());
				ps.setBigDecimal(6, mcaCurrency.getInvestAmt());
				ps.setBigDecimal(7, mcaCurrency.getMcaAmt());
				ps.setString(8, mcaCurrency.getTxnStatus());
				ps.setDate(9, now);
			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
		logger.info(String.format("mca buy rows inserted length=%s", row.length));
		return row.length;
	}
	
	public int insertMcaSellCurrency(List<McaCurrency> records,Date date) {
		java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		jdbcTemplate.setDataSource(dataSource);
		String sql = "INSERT INTO TBL_BATCH_STAGED_MCA_CURRENCY_TXN_SUMMARY (PRODUCT_TYPE, TXN_DATE, CURRENCY, CHANNEL,"
				+ " TXN_COUNT, AMOUNT_IN_MYR, AMOUNT_IN_FCY, TXN_STATUS, CREATED_TIME) values (?,?,?,?,?,?,?,?,?)";
		
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				McaCurrency mcaCurrency = records.get(i);
				ps.setString(1,mcaCurrency.getSubFunction());
				ps.setDate(2, sqlDate);
				ps.setString(3, mcaCurrency.getTxnCcy());
				ps.setString(4, mcaCurrency.getChannel());
				ps.setInt(5, mcaCurrency.getCount());
				ps.setBigDecimal(6, mcaCurrency.getMcaAmt());
				ps.setBigDecimal(7, mcaCurrency.getInvestAmt());
				ps.setString(8, mcaCurrency.getTxnStatus());
				ps.setDate(9, now);
			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
		logger.info(String.format("mca sell rows inserted length=%s", row.length));
		return row.length;
	}
	
	public void insertMetalSummary(Date batchProcessingDate)  {
		
		int rows = deleteBatchStagedMetalSummary(batchProcessingDate);
		logger.info(String.format("Deleted Metal rows count = %s", rows));
		
		List<McaPreciousMetal> mcaBuyPreciousMetalList = getMcaBuyPreciousMetalList(batchProcessingDate);
		logger.info(String.format("mca buy precious metal list=%s", mcaBuyPreciousMetalList));	
		
		int mcaBuyMetalRows = insertMcaBuyPreciousMetal(mcaBuyPreciousMetalList,batchProcessingDate);
		logger.info(String.format("Successfully inserted in to db TBL_BATCH_STAGED_MCA_METAL_TXN_SUMMARY =%s",mcaBuyMetalRows));	
		
		List<McaPreciousMetal> mcaSellPreciousMetalList = getMcaSellPreciousMetalList(batchProcessingDate);
		logger.info(String.format("mca sell precious metal list=%s", mcaSellPreciousMetalList));	
		
		int mcaSellMetalRows = insertMcaSellPreciousMetal(mcaSellPreciousMetalList,batchProcessingDate);
		logger.info(String.format("Successfully inserted in to db TBL_BATCH_STAGED_MCA_METAL_TXN_SUMMARY =%s",mcaSellMetalRows));
	}
	
	public int deleteBatchStagedMetalSummary(Date date){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_MCA_METAL_TXN_SUMMARY where CAST(TXN_DATE as DATE) = ?";
		logger.info(String.format("  sql delete metal summary=%s", sql));
        return getJdbcTemplate().update(sql
                , new Object[] {date});
    }
	
	public List<McaPreciousMetal> getMcaBuyPreciousMetalList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<McaPreciousMetal> mcaPreciousMetalList;
		String sql =  "SELECT SUM(a.AMOUNT) as invest_amt,SUM(b.TO_AMOUNT) as mca_amt,COUNT(*) as count,a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,b.TO_CCY\r\n"
				+ "FROM dcpbo.dbo.TBL_BO_INVEST_TXN a JOIN dcpbo.dbo.TBL_BO_MCA_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID \r\n"
				+ "JOIN dcp.dbo.TBL_CURRENCY_RATE_CONFIG c ON b.TO_CCY = c.CODE WHERE CAST(a.created_time as DATE)=? AND a.SUB_FUNCTION ='BUY'\r\n"
				+ "AND c.CURRENCY_TYPE = 'precious_metal' GROUP BY a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,b.TO_CCY";
		logger.info(String.format("get Mca Buy Precious Metal list  sql=%s", sql));
		mcaPreciousMetalList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper<McaPreciousMetal>(McaPreciousMetal.class));
		return mcaPreciousMetalList;
	}
	
	public List<McaPreciousMetal> getMcaSellPreciousMetalList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<McaPreciousMetal> mcaPreciousMetalList;
		String sql =  "SELECT SUM(a.AMOUNT) as invest_amt,SUM(b.TO_AMOUNT) as mca_amt,COUNT(*) as count,a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,a.TXN_CCY"
				+ " FROM dcpbo.dbo.TBL_BO_INVEST_TXN a JOIN dcpbo.dbo.TBL_BO_MCA_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID "
				+ " JOIN dcp.dbo.TBL_CURRENCY_RATE_CONFIG c ON a.TXN_CCY = c.CODE WHERE CAST(a.created_time as DATE)=? AND a.SUB_FUNCTION ='SELL'"
				+ " AND c.CURRENCY_TYPE = 'precious_metal' GROUP BY a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,a.TXN_CCY";
		logger.info(String.format("get Mca Sell Precious Metal list  sql=%s", sql));
		mcaPreciousMetalList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper<McaPreciousMetal>(McaPreciousMetal.class));
		return mcaPreciousMetalList;
	}
	
	public List<CurrencyRateConfig> getPreciousMetalList() {
		JdbcTemplate template = getJdbcTemplate();
		List<CurrencyRateConfig> currencyRateConfigList;
		String sql =  " SELECT * FROM dcp.dbo.TBL_CURRENCY_RATE_CONFIG a WHERE a.CURRENCY_TYPE = 'precious_metal'";
		logger.info(String.format("get currency rate precious metal config list  sql=%s", sql));
		currencyRateConfigList = template.query(sql,new Object[] {},
				new BeanPropertyRowMapper<CurrencyRateConfig>(CurrencyRateConfig.class));
		return currencyRateConfigList;
	}
	
	public int insertMcaBuyPreciousMetal(List<McaPreciousMetal> records,Date date) {
		java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		jdbcTemplate.setDataSource(dataSource);
		String sql = "INSERT INTO TBL_BATCH_STAGED_MCA_METAL_TXN_SUMMARY (PRODUCT_TYPE, TXN_DATE, METAL, CHANNEL,"
				+ "TXN_COUNT, AMOUNT,TXN_STATUS, CREATED_TIME) values (?,?,?,?,?,?,?,?)";
		
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				McaPreciousMetal mcaPreciousMetal = records.get(i);
				ps.setString(1,mcaPreciousMetal.getSubFunction());
				ps.setDate(2, sqlDate);
				ps.setString(3, mcaPreciousMetal.getToCcy());
				ps.setString(4, mcaPreciousMetal.getChannel());
				ps.setInt(5, mcaPreciousMetal.getCount());
				ps.setBigDecimal(6, mcaPreciousMetal.getInvestAmt());
				ps.setString(7, mcaPreciousMetal.getTxnStatus());
				ps.setDate(8, now);
			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
		logger.info(String.format("mca buy metal rows inserted length=%s", row.length));
		return row.length;
	}
	
	public int insertMcaSellPreciousMetal(List<McaPreciousMetal> records,Date date) {
		java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		jdbcTemplate.setDataSource(dataSource);
		String sql = "INSERT INTO TBL_BATCH_STAGED_MCA_METAL_TXN_SUMMARY (PRODUCT_TYPE, TXN_DATE, METAL, CHANNEL,"
				+ "TXN_COUNT, AMOUNT,TXN_STATUS, CREATED_TIME) values (?,?,?,?,?,?,?,?)";
		
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				McaPreciousMetal mcaPreciousMetal = records.get(i);
				ps.setString(1,mcaPreciousMetal.getSubFunction());
				ps.setDate(2, sqlDate);
				ps.setString(3, mcaPreciousMetal.getTxnCcy());
				ps.setString(4, mcaPreciousMetal.getChannel());
				ps.setInt(5, mcaPreciousMetal.getCount());
				ps.setBigDecimal(6, mcaPreciousMetal.getMcaAmt());
				ps.setString(7, mcaPreciousMetal.getTxnStatus());
				ps.setDate(8, now);
			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
		logger.info(String.format("mca sell metal rows inserted length=%s", row.length));
		return row.length;
	}
	
	public void insertPlacementSummary(Date batchProcessingDate)  {
		
		int rows = deleteBatchStagedMcaPlacement(batchProcessingDate);
		logger.info(String.format("Deleted Placement rows count = %s", rows));
		
		List<McaTermSummary> mcaTermSummaryList = getMcaTermSummaryList(batchProcessingDate);
		logger.info(String.format("mca term summary list=%s", mcaTermSummaryList));
		
		int mcaTermSummaryRows = insertMcaPlacementSummary(mcaTermSummaryList,batchProcessingDate);
		logger.info(String.format("Successfully inserted in to db TBL_BATCH_STAGED_MCA_PLACEMENT_WITHDRAWAL_TXN_SUMMARY =%s",mcaTermSummaryRows));
		}
	
	public int deleteBatchStagedMcaPlacement(Date date){
        String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_MCA_PLACEMENT_WITHDRAWAL_TXN_SUMMARY where CAST(TXN_DATE as DATE) = ?";
		logger.info(String.format("  sql delete mca placement summary=%s", sql));
        return getJdbcTemplate().update(sql
                , new Object[] {date});

    }
	
	public List<McaTermSummary> getMcaTermSummaryList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<McaTermSummary> mcaTermSummaryList;
		String sql = "SELECT SUM(b.TO_AMOUNT) as mca_amt,COUNT(*) as count,a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,b.TO_CCY \r\n"
				+ "FROM dcpbo.dbo.TBL_BO_INVEST_TXN a JOIN dcpbo.dbo.TBL_BO_MCA_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID WHERE CAST(a.created_time as DATE)=? AND \r\n"
				+ "(a.sub_function='PLACEMENT' OR a.sub_function='WITHDRAWAL')\r\n"
				+ "GROUP BY a.CHANNEL,a.TXN_STATUS ,a.SUB_FUNCTION,b.TO_CCY";
		logger.info(String.format("get Mca Term Summary list  sql=%s", sql));
		mcaTermSummaryList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper<McaTermSummary>(McaTermSummary.class));
		return mcaTermSummaryList;
	}

	
	public List<BoInvestTxn> getBoInvestList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoInvestTxn> investBoTxnList;
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BO_INVEST_TXN a WHERE CAST(a.created_time as DATE)=? AND (a.sub_function='PLACEMENT' OR a.sub_function='WITHDRAWAL')";
		logger.info(String.format("get Bo invest list  sql=%s", sql));
		investBoTxnList = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper<BoInvestTxn>(BoInvestTxn.class));
		return investBoTxnList;
	}
	
	public BoMcaTxn getBoMcaPlacementTxn(Date date, Integer txnTokenId) {
		JdbcTemplate template = getJdbcTemplate();
		BoMcaTxn boMcaTxn = new BoMcaTxn();
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BO_MCA_TXN a WHERE CAST(a.created_time as DATE)=? AND a.TXN_TOKEN_ID=? ";
		logger.info(String.format("get currency rate placement config list  sql=%s", sql));
		try {
			boMcaTxn = template.queryForObject(sql,new Object[] {date,txnTokenId},
					new BeanPropertyRowMapper<BoMcaTxn>(BoMcaTxn.class));
		}catch(EmptyResultDataAccessException e) {
			 logger.info("bo mca placement txn is null");
		}

		return boMcaTxn;
	}
	
	public int insertMcaPlacementSummary(List<McaTermSummary> records,Date date) {
		java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		jdbcTemplate.setDataSource(dataSource);
		String sql = "INSERT INTO TBL_BATCH_STAGED_MCA_PLACEMENT_WITHDRAWAL_TXN_SUMMARY (PRODUCT_TYPE, TXN_DATE, CURRENCY, CHANNEL,"
				+ "TXN_COUNT, AMOUNT_IN_FCY,TXN_STATUS, CREATED_TIME) values (?,?,?,?,?,?,?,?)";
		
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				McaTermSummary mcaTermSummary = records.get(i);
				ps.setString(1,mcaTermSummary.getSubFunction());
				ps.setDate(2, sqlDate);
				ps.setString(3, mcaTermSummary.getToCcy());
				ps.setString(4, mcaTermSummary.getChannel());
				ps.setInt(5, mcaTermSummary.getCount());
				ps.setBigDecimal(6, mcaTermSummary.getMcaAmt());
				ps.setString(7, mcaTermSummary.getTxnStatus());
				ps.setDate(8, now);
			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
		logger.info(String.format("rows inserted length for TBL_BATCH_STAGED_MCA_PLACEMENT_WITHDRAWAL_TXN_SUMMARY=%s", row.length));
		return row.length;
	}
}
