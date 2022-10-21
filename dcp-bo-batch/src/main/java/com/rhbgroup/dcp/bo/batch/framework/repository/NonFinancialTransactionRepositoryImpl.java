package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.BoNonFinancialTxnCount;

@Component
public class NonFinancialTransactionRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(NonFinancialTransactionRepositoryImpl.class);
	private static final String FULL_OUTER_JOIN =" FULL OUTER JOIN ";
	private static final String TIMESTAMP_CHECK_QUERY = "AND a.TIMESTAMP > = ? AND a.TIMESTAMP < DATEADD(DAY,1,?) GROUP BY a.USER_ID)\r\n";


	public void insertSummary(Date batchProcessingDate)  {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(batchProcessingDate);
		logger.info(String.format("  strDate=%s", strDate));

		int deleteRows = deleteBoNonFinancialTxnList(strDate);
		logger.info(String.format("Deleted rows count = %s", deleteRows));

		List<BoNonFinancialTxnCount> boNonFinancialTxnDMBList = getBoNonFinancialTxnDMBList(strDate);
		logger.info(String.format("bo Non financial txn DMB list=%s", boNonFinancialTxnDMBList));

		List<BoNonFinancialTxnCount> boNonFinancialFinalTxnDMBList = new ArrayList<BoNonFinancialTxnCount>();
		for(BoNonFinancialTxnCount boNonFinancialTxnCount:boNonFinancialTxnDMBList) {

			if(!(boNonFinancialTxnCount.getCashxcessCount() == 0 && boNonFinancialTxnCount.getOla_casa_count() == 0
					&& boNonFinancialTxnCount.getUpdate_profile() == 0 && boNonFinancialTxnCount.getForgot_username() == 0
					&& boNonFinancialTxnCount.getForgot_password() == 0 && boNonFinancialTxnCount.getChange_password() == 0
					&& boNonFinancialTxnCount.getChange_secure_word() == 0 && boNonFinancialTxnCount.getChange_challenge_que() == 0
					&& boNonFinancialTxnCount.getFavourite_intra_bank() == 0 && boNonFinancialTxnCount.getDel_favourite_intrabank() == 0
					&& boNonFinancialTxnCount.getFavourite_instant() == 0 && boNonFinancialTxnCount.getDel_favourite_instant() == 0
					&& boNonFinancialTxnCount.getFavourite_duitnow() == 0 && boNonFinancialTxnCount.getDel_favourite_duitnow() == 0
					&& boNonFinancialTxnCount.getFavourite_ibg() == 0 && boNonFinancialTxnCount.getDel_favourite_ibg() == 0
					&& boNonFinancialTxnCount.getFavourite_jompay() == 0 && boNonFinancialTxnCount.getDel_favourite_jompay() == 0
					&& boNonFinancialTxnCount.getFavourite_other_biller() == 0 && boNonFinancialTxnCount.getDel_favourite_other_biller() == 0
					&& boNonFinancialTxnCount.getFavourite_top_up() == 0 && boNonFinancialTxnCount.getDel_favourite_top_up() == 0)) {

				boNonFinancialFinalTxnDMBList.add(boNonFinancialTxnCount);
			}

		}

		int rows = insertNonFinancialTxnCount(boNonFinancialFinalTxnDMBList,"DMB",batchProcessingDate);
		logger.info(String.format("Successfully inserted DMB channel list into  = %s", rows));

		List<BoNonFinancialTxnCount> boNonFinancialTxnDIBList = getBoNonFinancialTxnDIBList(strDate);
		logger.info(String.format("bo Non financial txn DIB list=%s", boNonFinancialTxnDIBList));

		List<BoNonFinancialTxnCount> boNonFinancialFinalTxnDIBList = new ArrayList<BoNonFinancialTxnCount>();
		for(BoNonFinancialTxnCount boNonFinancialTxnCount:boNonFinancialTxnDIBList) {

			if(!(boNonFinancialTxnCount.getCashxcessCount() == 0 && boNonFinancialTxnCount.getOla_casa_count() == 0
					&& boNonFinancialTxnCount.getUpdate_profile() == 0 && boNonFinancialTxnCount.getForgot_username() == 0
					&& boNonFinancialTxnCount.getForgot_password() == 0 && boNonFinancialTxnCount.getChange_password() == 0
					&& boNonFinancialTxnCount.getChange_secure_word() == 0 && boNonFinancialTxnCount.getChange_challenge_que() == 0
					&& boNonFinancialTxnCount.getFavourite_intra_bank() == 0 && boNonFinancialTxnCount.getDel_favourite_intrabank() == 0
					&& boNonFinancialTxnCount.getFavourite_instant() == 0 && boNonFinancialTxnCount.getDel_favourite_instant() == 0
					&& boNonFinancialTxnCount.getFavourite_duitnow() == 0 && boNonFinancialTxnCount.getDel_favourite_duitnow() == 0
					&& boNonFinancialTxnCount.getFavourite_ibg() == 0 && boNonFinancialTxnCount.getDel_favourite_ibg() == 0
					&& boNonFinancialTxnCount.getFavourite_jompay() == 0 && boNonFinancialTxnCount.getDel_favourite_jompay() == 0
					&& boNonFinancialTxnCount.getFavourite_other_biller() == 0 && boNonFinancialTxnCount.getDel_favourite_other_biller() == 0
					&& boNonFinancialTxnCount.getFavourite_top_up() == 0 && boNonFinancialTxnCount.getDel_favourite_top_up() == 0)) {

				boNonFinancialFinalTxnDIBList.add(boNonFinancialTxnCount);
			}

		}

		int dibRows = insertNonFinancialTxnCount(boNonFinancialFinalTxnDIBList,"DIB",batchProcessingDate);
		logger.info(String.format("Successfully inserted DIB channel list into  = %s", dibRows));

	}

	public int deleteBoNonFinancialTxnList(String date){
		String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_NON_FINANCIAL_TXN_COUNT where TXN_DATE >= ? AND TXN_DATE < DATEADD(DAY,1,?)";
		logger.info(String.format("  sql=%s", sql));
		return getJdbcTemplate().update(sql
				, new Object[] {date,date});

	}

	public List<BoNonFinancialTxnCount> getBoNonFinancialTxnDMBList(String date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoNonFinancialTxnCount> boNonFinancialTxnCount;
		String sql =  "Select ISNULL(tup.id,0) as USER_ID,sum(table1.cashxcess_amt) as cashxcess_amt,COUNT(table1.cashxcess_count) as cashxcess_count,\r\n"
				+ "COUNT(table2.ola_casa_count) as ola_casa_count,COUNT(table3.update_profile) as update_profile,\r\n"
				+ "COUNT(table4.forgot_username) as forgot_username,\r\n"
				+ "COUNT(table5.forgot_password) as forgot_password,\r\n"
				+ "COUNT(table6.change_password) as change_password,\r\n"
				+ "COUNT(table7.change_secure_word) as change_secure_word,\r\n"
				+ "COUNT(table8.change_challenge_que) as change_challenge_que,\r\n"
				+ "COUNT(table9.favourite_intra_bank) as favourite_intra_bank,\r\n"
				+ "COUNT(table10.del_favourite_intrabank) as del_favourite_intrabank,\r\n"
				+ "COUNT(table11.favourite_instant) as favourite_instant,\r\n"
				+ "COUNT(table12.del_favourite_instant) as del_favourite_instant,\r\n"
				+ "COUNT(table13.favourite_duitnow) as favourite_duitnow,\r\n"
				+ "COUNT(table14.del_favourite_duitnow) as del_favourite_duitnow,\r\n"
				+ "COUNT(table15.favourite_ibg) as favourite_ibg,\r\n"
				+ "COUNT(table16.del_favourite_ibg) as del_favourite_ibg,\r\n"
				+ "COUNT(table17.favourite_jompay) as favourite_jompay,\r\n"
				+ "COUNT(table18.del_favourite_jompay) as del_favourite_jompay,\r\n"
				+ "COUNT(table19.favourite_other_biller) as favourite_other_biller,\r\n"
				+ "COUNT(table20.del_favourite_other_biller) as del_favourite_other_biller,\r\n"
				+ "COUNT(table21.favourite_top_up) as favourite_top_up,\r\n"
				+ "COUNT(table22.del_favourite_top_up) as del_favourite_top_up\r\n"
				+ "\r\n"
				+ "from\r\n"
				+ "\r\n"
				+ "dcp.dbo.TBL_USER_PROFILE tup \r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as cashxcess_id, SUM(b.TOTAL_AMOUNT) as cashxcess_amt,NULLIF(COUNT(*),0) as cashxcess_count\r\n"
				+ "\r\n"
				+ " FROM dcpbo.dbo.TBL_BO_TRANSFER_TXN a JOIN dcpbo.dbo.TBL_BO_CASHXCESS_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID\r\n"
				+ "WHERE a.TXN_STATUS = 'SUCCESS' AND a.TXN_TIME > = ? AND a.TXN_TIME < DATEADD(DAY,1,?) AND b.CHANNEL_CODE ='DMB' GROUP BY a.USER_ID) AS table1 on tup.id = table1.cashxcess_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT b.ID as ola_casa_id, NULLIF(COUNT(*),0) as ola_casa_count\r\n"
				+ "\r\n"
				+ " FROM dcpbo.dbo.TBL_BO_OLA_TOKEN a JOIN dcp.dbo.TBL_USER_PROFILE b ON a.USERNAME = b.USERNAME \r\n"
				+ "WHERE a.CHANNEL = 'DMB' AND a.UPDATED_TIME > = ? AND a.UPDATED_TIME < DATEADD(DAY,1,?) AND (a.STATUS ='I' OR a.STATUS ='C') GROUP BY b.id) AS table2 on tup.id = table2.ola_casa_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as update_profile\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('20002','20028','20029','20030','20031') AND \r\n"
				+ "a.CHANNEL = 'DMB' AND a.TIMESTAMP > = ? AND a.TIMESTAMP < DATEADD(DAY,1,?) GROUP BY a.USER_ID)\r\n"
				+ "AS table3 on tup.id = table3.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as forgot_username\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20033' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table4 on tup.id = table4.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as forgot_password\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20027' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table5 on tup.id = table5.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as change_password\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20001' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table6 on tup.id = table6.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as change_secure_word\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20026' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table7 on tup.id = table7.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as change_challenge_que\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20032' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table8 on tup.id = table8.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_intra_bank\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21000','21001') AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table9 on tup.id = table9.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_intrabank\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE ='21200' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table10 on tup.id = table10.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_instant\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21004','21005') AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table11 on tup.id = table11.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_instant\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21202' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table12 on tup.id = table12.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_duitnow\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21010','21011') AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table13 on tup.id = table13.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_duitnow\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21205' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table14 on tup.id = table14.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_ibg\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21002','21003') AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table15 on tup.id = table15.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_ibg\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21201' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table16 on tup.id = table16.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_jompay\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21008','21009') AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table17 on tup.id = table17.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_jompay\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21204' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table18 on tup.id = table18.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_other_biller\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21006','21007') AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table19 on tup.id = table19.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_other_biller\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21203' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table20 on tup.id = table20.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_top_up\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21012','21013') AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table21 on tup.id = table21.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_top_up\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21206' AND a.CHANNEL = 'DMB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table22 on tup.id = table22.audit_user_id\r\n"
				+ "\r\n"
				+ "GROUP BY tup.id";
		logger.info(String.format("get bo Non financial txn dmb list  sql=%s", sql));
		boNonFinancialTxnCount = template.query(sql,new Object[] {date,date,date,date,date,date,date
						,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date
						,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date
						,date,date,date},
				new BeanPropertyRowMapper<BoNonFinancialTxnCount>(BoNonFinancialTxnCount.class));
		return boNonFinancialTxnCount;
	}

	public List<BoNonFinancialTxnCount> getBoNonFinancialTxnDIBList(String date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoNonFinancialTxnCount> boNonFinancialTxnCount;
		String sql =  "Select ISNULL(tup.id,0) as USER_ID,sum(table1.cashxcess_amt) as cashxcess_amt,COUNT(table1.cashxcess_count) as cashxcess_count,\r\n"
				+ "COUNT(table2.ola_casa_count) as ola_casa_count,COUNT(table3.update_profile) as update_profile,\r\n"
				+ "COUNT(table4.forgot_username) as forgot_username,\r\n"
				+ "COUNT(table5.forgot_password) as forgot_password,\r\n"
				+ "COUNT(table6.change_password) as change_password,\r\n"
				+ "COUNT(table7.change_secure_word) as change_secure_word,\r\n"
				+ "COUNT(table8.change_challenge_que) as change_challenge_que,\r\n"
				+ "COUNT(table9.favourite_intra_bank) as favourite_intra_bank,\r\n"
				+ "COUNT(table10.del_favourite_intrabank) as del_favourite_intrabank,\r\n"
				+ "COUNT(table11.favourite_instant) as favourite_instant,\r\n"
				+ "COUNT(table12.del_favourite_instant) as del_favourite_instant,\r\n"
				+ "COUNT(table13.favourite_duitnow) as favourite_duitnow,\r\n"
				+ "COUNT(table14.del_favourite_duitnow) as del_favourite_duitnow,\r\n"
				+ "COUNT(table15.favourite_ibg) as favourite_ibg,\r\n"
				+ "COUNT(table16.del_favourite_ibg) as del_favourite_ibg,\r\n"
				+ "COUNT(table17.favourite_jompay) as favourite_jompay,\r\n"
				+ "COUNT(table18.del_favourite_jompay) as del_favourite_jompay,\r\n"
				+ "COUNT(table19.favourite_other_biller) as favourite_other_biller,\r\n"
				+ "COUNT(table20.del_favourite_other_biller) as del_favourite_other_biller,\r\n"
				+ "COUNT(table21.favourite_top_up) as favourite_top_up,\r\n"
				+ "COUNT(table22.del_favourite_top_up) as del_favourite_top_up\r\n"
				+ "\r\n"
				+ "from\r\n"
				+ "\r\n"
				+ "dcp.dbo.TBL_USER_PROFILE tup \r\n"
				+ "\r\n"
				+ " FULL OUTER JOIN \r\n"
				+ "(SELECT a.USER_ID as cashxcess_id, SUM(b.TOTAL_AMOUNT) as cashxcess_amt,NULLIF(COUNT(*),0) as cashxcess_count\r\n"
				+ "\r\n"
				+ " FROM dcpbo.dbo.TBL_BO_TRANSFER_TXN a JOIN dcpbo.dbo.TBL_BO_CASHXCESS_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID\r\n"
				+ "WHERE a.TXN_STATUS = 'SUCCESS' AND a.TXN_TIME > = ? AND a.TXN_TIME < DATEADD(DAY,1,?) AND b.CHANNEL_CODE ='DIB' GROUP BY a.USER_ID) AS table1 on tup.id = table1.cashxcess_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT b.ID as ola_casa_id, NULLIF(COUNT(*),0) as ola_casa_count\r\n"
				+ "\r\n"
				+ " FROM dcpbo.dbo.TBL_BO_OLA_TOKEN a JOIN dcp.dbo.TBL_USER_PROFILE b ON a.USERNAME = b.USERNAME \r\n"
				+ "WHERE a.CHANNEL = 'DIB' AND a.UPDATED_TIME > = ? AND a.UPDATED_TIME < DATEADD(DAY,1,?) AND (a.STATUS ='I' OR a.STATUS ='C') GROUP BY b.id) AS table2 on tup.id = table2.ola_casa_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as update_profile\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('20002','20028','20029','20030','20031') AND \r\n"
				+ "a.CHANNEL = 'DIB' AND a.TIMESTAMP > = ? AND a.TIMESTAMP < DATEADD(DAY,1,?) GROUP BY a.USER_ID)\r\n"
				+ "AS table3 on tup.id = table3.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as forgot_username\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20033' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table4 on tup.id = table4.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as forgot_password\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20027' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table5 on tup.id = table5.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as change_password\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20001' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table6 on tup.id = table6.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as change_secure_word\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20026' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table7 on tup.id = table7.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as change_challenge_que\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '20032' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table8 on tup.id = table8.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_intra_bank\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21000','21001') AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table9 on tup.id = table9.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_intrabank\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE ='21200' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table10 on tup.id = table10.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_instant\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21004','21005') AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table11 on tup.id = table11.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_instant\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21202' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table12 on tup.id = table12.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_duitnow\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21010','21011') AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table13 on tup.id = table13.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_duitnow\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21205' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table14 on tup.id = table14.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_ibg\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21002','21003') AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table15 on tup.id = table15.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_ibg\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21201' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table16 on tup.id = table16.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_jompay\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21008','21009') AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table17 on tup.id = table17.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_jompay\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21204' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table18 on tup.id = table18.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_other_biller\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21006','21007') AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table19 on tup.id = table19.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_other_biller\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21203' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table20 on tup.id = table20.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as favourite_top_up\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE IN ('21012','21013') AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table21 on tup.id = table21.audit_user_id\r\n"
				+ "\r\n"
				+ FULL_OUTER_JOIN
				+ "(SELECT a.USER_ID as audit_user_id,NULLIF(COUNT(*),0) as del_favourite_top_up\r\n"
				+ "FROM dcpbo.dbo.DCP_BO_AUDIT a WHERE a.EVENT_CODE = '21206' AND a.CHANNEL = 'DIB'\r\n"
				+ TIMESTAMP_CHECK_QUERY
				+ "AS table22 on tup.id = table22.audit_user_id\r\n"
				+ "\r\n"
				+ "GROUP BY tup.id";
		logger.info(String.format("get bo Non financial txn dib list  sql=%s", sql));
		boNonFinancialTxnCount = template.query(sql, new Object[] {date,date,date,date,date,date,date
						,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date
						,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date,date
						,date,date,date},
				new BeanPropertyRowMapper<BoNonFinancialTxnCount>(BoNonFinancialTxnCount.class));
		return boNonFinancialTxnCount;
	}

	private BigDecimal parsing(String value) {
		String normalized ="";
		if(value == null || value.trim().length() ==0) {
			normalized = "0";
		}else {
			normalized = value.replaceAll("\\s", "").replace(',', '.');
		}
		return new BigDecimal(normalized);
	}

	public int insertNonFinancialTxnCount(List<BoNonFinancialTxnCount> records,String channel,Date batchDate) {
		java.sql.Date date = new java.sql.Date(batchDate.getTime());

		jdbcTemplate.setDataSource(dataSource);
		String sql = "INSERT INTO dcpbo.dbo.TBL_BATCH_STAGED_NON_FINANCIAL_TXN_COUNT\r\n"
				+ "(USER_ID, CHANNEL, TXN_DATE, CASHXESS_COUNT, CASHXESS_AMT, OLA_CASA_COUNT,"
				+ " UPDATE_PROFILE, FORGOT_USERNAME, FORGOT_PASSWORD, CHANGE_PASSWORD, "
				+ "CHANGE_SECURE_WORD, CHANGE_CHALLENGE_QUE, FAVOURITE_INTRABANK, "
				+ "DEL_FAVOURITE_INTRABANK, FAVOURITE_INSTANT, DEL_FAVOURITE_INSTANT, "
				+ "FAVOURITE_DUITNOW, DEL_FAVOURITE_DUITNOW, FAVOURITE_IBG, DEL_FAVOURITE_IBG,"
				+ " FAVOURITE_JOMPAY, DEL_FAVOURITE_JOMPAY, FAVOURITE_OTHER_BILLER,"
				+ " DEL_FAVOURITE_OTHER_BILLER, FAVOURITE_TOP_UP, DEL_FAVOURITE_TOP_UP,"
				+ " CREATED_TIME)" + "VALUES"+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
				+ "?,?,?,?,?)";

		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				BoNonFinancialTxnCount boNonFinancialTxnCount = records.get(i);
				ps.setInt(1, boNonFinancialTxnCount.getUserId());
				ps.setString(2, channel);
				ps.setDate(3, date);
				ps.setInt(4, boNonFinancialTxnCount.getCashxcessCount());
				ps.setBigDecimal(5, parsing(boNonFinancialTxnCount.getCashxcessAmt()));
				ps.setInt(6, boNonFinancialTxnCount.getOla_casa_count());
				ps.setInt(7, boNonFinancialTxnCount.getUpdate_profile());
				ps.setInt(8, boNonFinancialTxnCount.getForgot_username());
				ps.setInt(9, boNonFinancialTxnCount.getForgot_password());
				ps.setInt(10, boNonFinancialTxnCount.getChange_password());
				ps.setInt(11, boNonFinancialTxnCount.getChange_secure_word());
				ps.setInt(12, boNonFinancialTxnCount.getChange_challenge_que());
				ps.setInt(13, boNonFinancialTxnCount.getFavourite_intra_bank());
				ps.setInt(14, boNonFinancialTxnCount.getDel_favourite_intrabank());
				ps.setInt(15, boNonFinancialTxnCount.getFavourite_instant());
				ps.setInt(16, boNonFinancialTxnCount.getDel_favourite_instant());
				ps.setInt(17, boNonFinancialTxnCount.getFavourite_duitnow());
				ps.setInt(18, boNonFinancialTxnCount.getDel_favourite_duitnow());
				ps.setInt(19, boNonFinancialTxnCount.getFavourite_ibg());
				ps.setInt(20, boNonFinancialTxnCount.getDel_favourite_ibg());
				ps.setInt(21, boNonFinancialTxnCount.getFavourite_jompay());
				ps.setInt(22, boNonFinancialTxnCount.getDel_favourite_jompay());
				ps.setInt(23, boNonFinancialTxnCount.getFavourite_other_biller());
				ps.setInt(24, boNonFinancialTxnCount.getDel_favourite_other_biller());
				ps.setInt(25, boNonFinancialTxnCount.getFavourite_top_up());
				ps.setInt(26, boNonFinancialTxnCount.getDel_favourite_top_up());
				ps.setDate(27, date);

			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
		logger.info(String.format("rows inserted length=%s", row.length));
		return row.length;
	}

}
