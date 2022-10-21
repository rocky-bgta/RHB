package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedMergeCISDetailTxn;

@Component
@Lazy
public class BatchStagedMergeCISRepositoryImpl extends BaseRepositoryImpl{
	final static Logger logger = Logger.getLogger(BatchStagedMergeCISRepositoryImpl.class);
	String logMsg = "";

	public List<BatchStagedMergeCISDetailTxn> getUnproccessedStagedCIS(String jobExecutionId) {
		logMsg=String.format("query from Batch Staged Merge CIS jobExecutionId=%s", jobExecutionId);
		logger.info(logMsg);
		String selectSql="select job_execution_id as jobExecutionId , isnull(cis_no,'') cisNo "+
				 ",isnull(new_cis_no,'') newCISNo "+
				 ",isnull(processing_date,'') processingDate " +
				 ",isnull(file_name,'') as fileName " +
				 " from TBL_BATCH_STAGED_MERGE_CIS " +
				 " where is_processed=0 " +
				 " and job_execution_id=? ";
		logMsg=String.format("query from Batch Staged Merge CIS sql=%s,jobExecutionId=%s", selectSql,jobExecutionId);
		logger.info(logMsg);
		return getJdbcTemplate().query(selectSql
				, new Object[]{jobExecutionId}
				, new BeanPropertyRowMapper(BatchStagedMergeCISDetailTxn.class));
	}
	
	public int addMergeCISRecord(BatchStagedMergeCISDetailTxn mergeCISDetail) {
		int rows=0;
		String sql = "INSERT INTO TBL_BATCH_STAGED_MERGE_CIS " + 
				"(job_execution_id,cis_no,new_cis_no, processing_date, is_processed, created_time, updated_time, file_name)" +
				"values (?,?,?,?,?,?,?,?)" ;
		logMsg = String.format("Insert record into TBL_BATCH_STAGED_MERGE_CIS sql=%s, mergeCIS=%s", sql,mergeCISDetail );
		logger.info(logMsg);
		rows = getJdbcTemplate().update(sql,
				new Object[] {mergeCISDetail.getJobExecutionId(), mergeCISDetail.getCisNo(), mergeCISDetail.getNewCISNo(), mergeCISDetail.getProcessingDate(),
						0, new Date(), new Date(), mergeCISDetail.getFileName()}) ;
		return rows;
	}
	
	public int updateProcessStatus(BatchStagedMergeCISDetailTxn mergeCISDetail, int processStatus) {
		int rows = 0;
		logMsg = String.format("Update TBL_BATCH_STAGED_MERGE_CIS processStatus=%s, jobExecutionId=%s, cisNo=%s, newCISNo=%s",
				processStatus, mergeCISDetail.getJobExecutionId(), mergeCISDetail.getCisNo(), mergeCISDetail.getNewCISNo());
		logger.info(logMsg);
		String updateSql = "update TBL_BATCH_STAGED_MERGE_CIS set is_processed=?, updated_time=? "
				+ " where job_execution_id=? and cis_no=? and new_cis_no=? and processing_date=? ";
		logMsg = String.format("Update TBL_BATCH_STAGED_MERGE_CIS sql=%s,mergeCIS=%s", updateSql, mergeCISDetail);
		logger.info(logMsg);
		rows = getJdbcTemplate().update(updateSql,
				new Object[] {processStatus, new Date(), mergeCISDetail.getJobExecutionId(), mergeCISDetail.getCisNo(), mergeCISDetail.getNewCISNo(), mergeCISDetail.getProcessingDate() } ); 
		logMsg = String.format("Update TBL_BATCH_STAGED_MERGE_CIS impacted row=%s", rows);
		logger.info(logMsg);
		return rows;
	}
}
