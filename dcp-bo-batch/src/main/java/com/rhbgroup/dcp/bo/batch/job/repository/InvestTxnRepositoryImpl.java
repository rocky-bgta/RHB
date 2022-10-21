package com.rhbgroup.dcp.bo.batch.job.repository;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.InvestTxn;

@Component
public class InvestTxnRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(InvestTxnRepositoryImpl.class);
   
 	public InvestTxn getInvestTxnDetail(long txnTokenId) {

 		InvestTxn investTxn = new InvestTxn();
		String logMsg = "";
 		
 		String sql = "SELECT b.id FROM dcp.dbo.TBL_FPX_TXN a LEFT JOIN dcp.dbo.TBL_INVEST_TXN b ON a.TXN_TOKEN_ID = b.TXN_TOKEN_ID \r\n"
 				      + "WHERE a.TXN_TOKEN_ID = ?";
 		logMsg = String.format("SQL Statement = %s", sql);
 		logger.info(logMsg);
 		try {
 			investTxn = (InvestTxn) jdbcTemplate.queryForObject(sql, new Object[] {txnTokenId},
 					new BeanPropertyRowMapper<InvestTxn>(InvestTxn.class));
 		} catch (DataAccessException e) {
 			logger.error(e.getMessage());
 		}
 		return investTxn;
 	}
}