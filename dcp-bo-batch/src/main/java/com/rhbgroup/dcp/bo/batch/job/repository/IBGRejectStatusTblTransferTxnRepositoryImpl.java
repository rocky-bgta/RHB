package com.rhbgroup.dcp.bo.batch.job.repository;
import java.util.Date;

import javax.sql.DataSource;
import javax.ws.rs.ext.ParamConverter.Lazy;

import org.apache.log4j.Logger;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.IBGRejectStatusTblTransferTxn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class IBGRejectStatusTblTransferTxnRepositoryImpl extends BaseRepositoryImpl {
	static final Logger logger = Logger.getLogger(IBGRejectStatusTblTransferTxnRepositoryImpl.class);

	@Qualifier("dataSourceDCP")
	@Autowired
	DataSource dataSourceDCP;
	
	public IBGRejectStatusTblTransferTxn getUserId(String date, String tellerId, String traceId) {
		String logMsg="";
		try {
			logMsg=String.format("query user id from vw_batch_tbl_transfer_txn date=%s, tellerId=%s, traceId=%s", date, tellerId, traceId);
			logger.info(logMsg);
			IBGRejectStatusTblTransferTxn tblTransferTxn;
			String selectClause = "select isnull(user_id,0)  as userId "+
					", isnull(teller_id,'') tellerId "+
					", isnull(trace_id,'') traceId" ;
			String fromClause= " from vw_batch_tbl_transfer_txn";
			String whereClause=" where main_function='IBG' and teller_id =?" + 
					" and trace_id =?" + 
					" and convert(varchar(8), updated_time ,112)=?";
			String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();
			logMsg=String.format("query user id from TBL_TRANSFER_TXN sql=%s", sql);
			logger.info(logMsg);
			JdbcTemplate jdbcTemplate =getJdbcTemplate();
			tblTransferTxn = (IBGRejectStatusTblTransferTxn)jdbcTemplate.queryForObject(sql,
								new Object[] {tellerId, traceId, date },
								new BeanPropertyRowMapper(IBGRejectStatusTblTransferTxn.class));
			return tblTransferTxn;			
		}catch(EmptyResultDataAccessException ex) {
			logMsg=String.format("exception EmptyResultDataAccessException when query from TBL_TRANSFER_TXN ex=%s", ex.getMessage() );
			logger.info(logMsg);
			logger.error(ex);
			return null;
		}catch(IncorrectResultSizeDataAccessException ex) {
			logMsg=String.format("exception IncorrectResultSizeDataAccessException when query from TBL_TRANSFER_TXN ex=%s", ex.getMessage() );
			logger.info(logMsg);
			logger.error(ex);
			return null;
		}catch(Exception ex) {
			logMsg=String.format("exception when query from TBL_TRANSFER_TXN ex=%s", ex.getMessage() );
			logger.info(logMsg);
			logger.error(ex);
			return null;
		} 
	}
	
	public int updateTxnStatus(String txnStatus,String date, String tellerId, String traceId) {
		String logMsg="";
		int row = 0;
		logMsg=String.format("update txn_status TBL_TRANSFER_TXN txn_Status=%s, date=%s, tellerId=%s, traceId=%s",
				txnStatus, date, tellerId, traceId);
		logger.info(logMsg);

		try {
			jdbcTemplate.setDataSource(dataSourceDCP);
			String updateSql = " update TBL_TRANSFER_TXN " + "set txn_status=?, updated_time=?"
					+ " where main_function='IBG' and teller_id =? and trace_id =?"
					+ " and convert(varchar(8), updated_time ,112)=?";
			logMsg=String.format("update txn_status TBL_TRANSFER_TXN sql=%s",updateSql);
			logger.info(logMsg);
			row = jdbcTemplate.update(updateSql, txnStatus, new Date(), tellerId, traceId, date);
			logMsg=String.format("update txn_status TBL_TRANSFER_TXN row=%s",row);
			logger.info(logMsg);
		}catch(Exception ex) {
			logMsg=String.format("exception when updating txn_status TBL_TRANSFER_TXN ex=%s", ex.getMessage() );
			logger.info(logMsg);
			logger.error(ex);
		}
		return row;
	}
}
