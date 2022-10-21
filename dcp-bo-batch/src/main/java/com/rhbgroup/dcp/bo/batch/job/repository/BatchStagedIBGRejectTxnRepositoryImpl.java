package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.ext.ParamConverter.Lazy;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectStatusTxn;

@Component
@Lazy
public class BatchStagedIBGRejectTxnRepositoryImpl  extends BaseRepositoryImpl{

	final static Logger logger = Logger.getLogger(BatchStagedIBGRejectTxnRepositoryImpl.class);

	public List<BatchStagedIBGRejectStatusTxn> getUnprocessedIBGRejectStatusFromStaging(String jobExecutionId){
		String logMsg=String.format("query from Batch Staged IBG Reject status jobExecutionId=%s", jobExecutionId);
		logger.info(logMsg);
		JdbcTemplate jdbcTemplate= getJdbcTemplate();
		String selectClause="select isnull(date, '' ) date" + 
						",isnull(teller,'') as teller" + 
						",isnull(trace,'') as trace" + 
						",isnull(ref1,'') as ref1" + 
						",isnull(name,'') as name" + 
						",isnull(amount,'') as amount" + 
						",isnull(reject_code,'') as rejectCode" + 
						",isnull(account_no,'') as accountNo" + 
						",isnull(bene_Name,'') as beneName " + 
						",isnull(bene_Account,'') as beneAccount" + 
						",isnull(job_execution_id,0) as jobExecutionId" +
						",isnull(file_name,'') as fileName" ;
		String fromClause=" from TBL_BATCH_STAGED_IBG_REJECT_TXN " ;
		String whereClause=" where job_execution_id=? and is_processed=0";
		String sql = (new StringBuffer()).append(selectClause).append(fromClause).append(whereClause).toString();
		logMsg=String.format("query from Batch Staged IBG Reject status sql=%s", sql);
		logger.info(logMsg);
		return jdbcTemplate.query(sql
					, new Object[]{jobExecutionId}
					, new BeanPropertyRowMapper(BatchStagedIBGRejectStatusTxn.class));
	}
	
	public int addBatchStagedIBGRejectStatusStaging(BatchStagedIBGRejectStatusTxn ibgRejectStatus) {
		int rows =0;
		String logMsg="";
		try {
			logMsg=String.format("add record into Batch Staged IBG Reject status jobExecutionId=%s, teller=%s, trace=%s, name=%s, bene account=%s, bene name=%s",ibgRejectStatus.getJobExecutionId(),ibgRejectStatus.getTeller(), ibgRejectStatus.getTrace(), ibgRejectStatus.getName(), ibgRejectStatus.getAccountNo(), ibgRejectStatus.getBeneName() );
			logger.info(logMsg);
			rows= getJdbcTemplate().update(
					" INSERT INTO TBL_BATCH_STAGED_IBG_REJECT_TXN (job_execution_id, date,teller,trace,ref1,name,amount,reject_code,account_no,user_id,bene_name,bene_account,created_time, updated_time, file_name ) "
							+  " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					new Object[] {ibgRejectStatus.getJobExecutionId(), ibgRejectStatus.getDate(), ibgRejectStatus.getTeller(), ibgRejectStatus.getTrace(),
							ibgRejectStatus.getRef1(), ibgRejectStatus.getName(), ibgRejectStatus.getAmount(),
							ibgRejectStatus.getRejectCode(), ibgRejectStatus.getAccountNo(), 0,
							ibgRejectStatus.getBeneName(), ibgRejectStatus.getBeneAccount(), new Date(), new Date(), ibgRejectStatus.getFileName() });
			logMsg=String.format("addBatchStagedIBGRejectStatusStaging row affected=%s", rows) ;
			logger.info(logMsg);
		} catch (Exception e) {
			logMsg=String.format("exception while insert Batch Staged IBG Reject Txn jobExecutionId=%s, date=%s, teller=%s, trace=%s, " +
							"ref1=%s, name=%s, amount=%s, rejectCode=%s, accountNo=%s, beneName=%s, accountNo=%s, fileName=%s ",
					ibgRejectStatus.getJobExecutionId(), ibgRejectStatus.getDate(), ibgRejectStatus.getTeller(), ibgRejectStatus.getTrace(),
					ibgRejectStatus.getRef1(), ibgRejectStatus.getName(), ibgRejectStatus.getAmount(),
					ibgRejectStatus.getRejectCode(), ibgRejectStatus.getAccountNo(),
					ibgRejectStatus.getBeneName(), ibgRejectStatus.getBeneAccount(), ibgRejectStatus.getFileName()) ;
			logger.info(logMsg);
			logger.error(e);
		}
		return rows;
	}
	
	public int updateUserId (String userId,BatchStagedIBGRejectStatusTxn ibgRejectStatus) {
		String logMsg="";
		int rows = 0;
		try {
			logMsg = String.format("Update userId into Batch Staged IBG Reject status userId=%s,jobExecutionId=%s,date=%s,teller=%s,trace=%s",userId,ibgRejectStatus.getJobExecutionId(), ibgRejectStatus.getDate(), ibgRejectStatus.getTeller(), ibgRejectStatus.getTeller());
			logger.info(logMsg);
			String sqlUpdate=" update TBL_BATCH_STAGED_IBG_REJECT_TXN set user_id=? " +
					" where job_execution_id=? and trace=? and teller=? and date=? ";
			rows = getJdbcTemplate().update(sqlUpdate, new Object[] {userId, ibgRejectStatus.getJobExecutionId(),ibgRejectStatus.getTrace(),ibgRejectStatus.getTeller(),ibgRejectStatus.getDate()});
			logMsg=String.format("updateUserId row affected=%s", rows) ;
			logger.info(logMsg);
		}catch(Exception ex) {
			logMsg=String.format("exception while updating Batch Staged IBG Reject status user id=%s, jobExecutionId=%s, date=%s, trace=%s, teller=%s",
					userId, ibgRejectStatus.getJobExecutionId(), ibgRejectStatus.getDate(), ibgRejectStatus.getTrace(), ibgRejectStatus.getTeller()) ;
			logger.info(logMsg);
			logger.error(ex);
		}
		return rows;	
	}
	
	public int updateRejectDescription(BatchStagedIBGRejectStatusTxn ibgRejectStatus) {
		String logMsg="";
		String rejectDescription="", rejectCode="";
		int rows = 0;
		try {
			JdbcTemplate jdbcTemplate = getJdbcTemplate();
			jdbcTemplate.setDataSource(dataSource);
			logMsg=String.format("update reject description Batch Staged IBG Reject status jobExecutionId=%s, date=%s, trace=%s, teller=%s", 
					ibgRejectStatus.getJobExecutionId(), ibgRejectStatus.getDate(), ibgRejectStatus.getTrace(), ibgRejectStatus.getTeller()) ;
			logger.info(logMsg);
			rejectCode=ibgRejectStatus.getRejectCode();
			String sqlSelect = " select isnull(reject_description,'') reject_description from tbl_batch_ibg_reject_code " + 
								" where reject_code=?" ;
			logMsg = String.format("query for reject_description- reject_code=%s, sql=%s", rejectCode,sqlSelect );
			logger.info(logMsg);

			rejectDescription = (String)jdbcTemplate.queryForObject(sqlSelect, new Object[] {rejectCode}, String.class);
			if(null ==rejectDescription || rejectDescription.isEmpty()) {
				logMsg = String.format("reject_description not found - reject_code=%s, sql=%s", rejectCode,sqlSelect );
				logger.info(logMsg);
				return rows;				
			}
			String sqlUpdate=" update TBL_BATCH_STAGED_IBG_REJECT_TXN set reject_description=?, is_processed=1 " +
					" where job_execution_id=? and trace=? and teller=? and date=? ";
			logMsg=String.format("updating reject description=%s, sql=%s", rejectDescription, sqlUpdate) ;
			logger.info(logMsg);
			rows = jdbcTemplate.update(sqlUpdate, new Object[] {rejectDescription, ibgRejectStatus.getJobExecutionId(),ibgRejectStatus.getTrace(),ibgRejectStatus.getTeller(),ibgRejectStatus.getDate()});
			logMsg=String.format("updateRejectDescription row affected=%s", rows) ;
			logger.info(logMsg);				
		}catch(Exception ex) {
			logMsg=String.format("exception while updating Batch Staged IBG Reject status rejectDescription=%s, jobExecutionId=%s, date=%s, trace=%s, teller=%s",
					rejectDescription, ibgRejectStatus.getJobExecutionId(), ibgRejectStatus.getDate(), ibgRejectStatus.getTrace(), ibgRejectStatus.getTeller()) ;
			logger.info(logMsg);
			logger.error(ex);
		}
		return rows;
	}
}
