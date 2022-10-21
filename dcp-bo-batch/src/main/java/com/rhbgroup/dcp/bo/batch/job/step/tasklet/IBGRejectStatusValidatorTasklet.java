package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExitCode.FAILED;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import java.util.Date;
import java.util.List;

import com.rhbgroup.dcp.bo.batch.job.model.*;
import com.rhbgroup.dcp.bo.batch.job.repository.IBGRejectStatusTblPaymentTxnRepositoryImpl;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

import com.rhbgroup.dcp.bo.batch.job.enums.SuspenseType;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBGRejectTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.IBGRejectStatusTblTransferTxnRepositoryImpl;

@Component
@Lazy
public class IBGRejectStatusValidatorTasklet implements Tasklet, InitializingBean {

	private static final Logger logger = Logger.getLogger(IBGRejectStatusValidatorTasklet.class);
	
	@Autowired
	private BatchStagedIBGRejectTxnRepositoryImpl ibgRejectStatusStagingRepositoryImpl;
	
	@Autowired
	private BatchSuspenseRepositoryImpl batchSuspenseRepoImpl;
	
	@Autowired
	private IBGRejectStatusTblTransferTxnRepositoryImpl tblTransferTxnRepoImpl;

	@Autowired
	private IBGRejectStatusTblPaymentTxnRepositoryImpl tblPaymentTxnRepoImpl;

	String jobExecutionId="";
	String jobname="";

	private static String SUSPENSE_TYPE_ERR=SuspenseType.ERROR.name();
	private static String SUSPENSE_TYPE_WARN=SuspenseType.WARNING.name();
	private static final String TBL_PAYMENT_TXN = "TBL_PAYMENT_TXN";
	private static final String TBL_TRANSFER_TXN = "TBL_TRANSFER_TXN";
	private static final String INSERT_MESSAGE = "insert record into TBL_BATCH_SUSPENSE %s";

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
		String logMsg;
		jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
		checkValidJobExecutionId(chunkContext);
		jobname = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
		logMsg = String.format("validating IBGRejectStatus job name=%s, job execution id=%s",jobname, jobExecutionId);
		logger.info(logMsg);
		List<BatchStagedIBGRejectStatusTxn> ibgRejectStatusList = ibgRejectStatusStagingRepositoryImpl.getUnprocessedIBGRejectStatusFromStaging(jobExecutionId);
		for (BatchStagedIBGRejectStatusTxn ibgRejectStatus : ibgRejectStatusList) {
			BatchSuspense batchSuspense = new BatchSuspense();

			try {
				batchSuspense.setBatchJobName(jobname);
				batchSuspense.setJobExecutionId(Long.parseLong(jobExecutionId));
				batchSuspense.setCreatedTime(new Date());
				String suspenseRecord = (new StringBuilder()
									.append(ibgRejectStatus.getDate()).append("|")
									.append(ibgRejectStatus.getTeller()).append("|")
									.append(ibgRejectStatus.getTrace())).toString();
				batchSuspense.setSuspenseRecord(suspenseRecord);

				if (checkSuspenseStatusAndUpdate(chunkContext, ibgRejectStatus, batchSuspense)) continue;
				//step 6: check date, teller and trace

				//step 7: get user id from [dcp].[dbo].[TBL_TRANSFER_TXN] & [dcp].[dbo].[TBL_PAYMENT_TXN]
				IBGRejectStatusUserDto userDto = getUserId(ibgRejectStatus.getDate(), ibgRejectStatus.getTeller(), ibgRejectStatus.getTrace());
				if ( null == userDto.getUserId() || userDto.getUserId().isEmpty()) {
					//user id not found; insert to suspense
					logMsg = String.format("IBGRejectStatus job execution id=%s,user id is null or empty.",jobExecutionId);					
					logger.warn(logMsg);
					chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS,FAILED);
					batchSuspense.setSuspenseColumn("user_id");
					batchSuspense.setSuspenseType(SUSPENSE_TYPE_ERR);
					batchSuspense.setSuspenseMessage("Column value \"user_id\" should not be null or empty");
				 	logger.info(String.format(INSERT_MESSAGE,insertTblBatchSuspense(batchSuspense)));
					continue;
				}
				if(!updateUserId(userDto.getUserId(),ibgRejectStatus ) ){
					//fail to update user id insert to tbl suspense
					logMsg = String.format("IBGRejectStatus job execution id=%s,fail to update user.",jobExecutionId);					
					logger.warn(logMsg);
					chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS,FAILED);
					batchSuspense.setSuspenseColumn("user_id");
					batchSuspense.setSuspenseType(SUSPENSE_TYPE_ERR);
					batchSuspense.setSuspenseMessage("Column value \"user_id\" should not be null or empty");
				 	logger.info(String.format(INSERT_MESSAGE,insertTblBatchSuspense(batchSuspense)));
					continue;
				}
				else{
					updateFailedPaymentOrTxnStatus(ibgRejectStatus, userDto);
				}
				//step 7: get user id from [dcp].[dbo].[TBL_TRANSFER_TXN]

				//step 8: update reject desc
				if(!updateRejectDesc(ibgRejectStatus)){
					//fail to update reject desc insert to tbl suspense
					logMsg = String.format("IBGRejectStatus job execution id=%s, fail to update reject description.",jobExecutionId);					
					logger.warn(logMsg);
					chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS,FAILED);
					batchSuspense.setSuspenseColumn("reject_description");
					batchSuspense.setSuspenseType(SUSPENSE_TYPE_ERR);
					batchSuspense.setSuspenseMessage("Column value \"reject_description\" should not be null or empty");
				 	logger.info(String.format(INSERT_MESSAGE,insertTblBatchSuspense(batchSuspense)));
					continue;
				}
				//step 8: update reject desc

			} catch (Exception ex) {
				logMsg = String.format("validating IBGRejectStatus exception=%s", ex.getMessage());
				logger.info(logMsg);
				logger.error(ex);
				continue;
			}
		}
		return RepeatStatus.FINISHED;
	}

	private void updateFailedPaymentOrTxnStatus(BatchStagedIBGRejectStatusTxn ibgRejectStatus, IBGRejectStatusUserDto userDto) {
		String logMsg;
		logMsg = String.format("IBGRejectStatus job execution id=%s,update user success.",jobExecutionId);
		logger.info(logMsg);
		if(userDto.getTableName().equalsIgnoreCase(TBL_TRANSFER_TXN)) {
			updateTransferTxnStatus("FAILED", ibgRejectStatus.getDate(), ibgRejectStatus.getTeller(), ibgRejectStatus.getTrace());
			logger.info("update txn_status in TBL_TRANSFER_TXN to FAILED");
		} else {
			updatePaymentTxnStatus("FAILED", ibgRejectStatus.getDate(), ibgRejectStatus.getTeller(), ibgRejectStatus.getTrace());
			logger.info("update txn_status in TBL_PAYMENT_TXN to FAILED");
		}
	}

	private boolean checkSuspenseStatusAndUpdate(ChunkContext chunkContext, BatchStagedIBGRejectStatusTxn ibgRejectStatus, BatchSuspense batchSuspense) {
		String logMsg;
		//step 4: check amount
		if (null == ibgRejectStatus.getAmount() || ibgRejectStatus.getAmount().isEmpty()) {
			logMsg = String.format("IBGRejectStatus job execution id=%s, amount is null or empty.", jobExecutionId);
			logger.warn(logMsg);
			// mark record as fail,
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS,FAILED);
			//insert into batch suspense
			batchSuspense.setSuspenseColumn("amount");
			batchSuspense.setSuspenseType(SUSPENSE_TYPE_ERR);
			batchSuspense.setSuspenseMessage("Column value \"amount\" should not be null or empty");
			 logger.info(String.format("Inserted %d record(s) into TBL_BATCH_SUSPENSE", insertTblBatchSuspense(batchSuspense)));
			return true;
		}
		//step 5: check beneficiary name
		if (null == ibgRejectStatus.getBeneName() || ibgRejectStatus.getBeneName().isEmpty()  ) {
			logMsg = String.format("IBGRejectStatus job execution id=%s, beneficiary name is null or empty.",jobExecutionId);
			logger.warn(logMsg);
			// mark record as fail
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS,FAILED);
			//insert into batch suspense
			batchSuspense.setSuspenseColumn("bene_name");
			batchSuspense.setSuspenseType(SUSPENSE_TYPE_ERR);
			batchSuspense.setSuspenseMessage("Column value \"bene_name\" should not be null or empty");
			 logger.info(String.format(INSERT_MESSAGE,insertTblBatchSuspense(batchSuspense)));
			return true;
		}
		//step 6: check date, teller and trace
		if (null == ibgRejectStatus.getDate() || ibgRejectStatus.getDate().isEmpty() ) {
			logMsg = String.format("IBGRejectStatus job execution id=%s, date is null or empty.",jobExecutionId);
			logger.warn(logMsg);

			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS,FAILED);
			batchSuspense.setSuspenseColumn("date");
			batchSuspense.setSuspenseType(SUSPENSE_TYPE_WARN);
			batchSuspense.setSuspenseMessage("Column value \"date\" should not be null or empty");
			 logger.info(String.format(INSERT_MESSAGE,insertTblBatchSuspense(batchSuspense)));
			return true;
		}
		if (null == ibgRejectStatus.getTeller() || ibgRejectStatus.getTeller().isEmpty()) {
			logMsg = String.format("IBGRejectStatus job execution id=%s,teller is empty.",jobExecutionId);
			logger.warn(logMsg);
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS,FAILED);
			batchSuspense.setSuspenseColumn("teller");
			batchSuspense.setSuspenseType(SUSPENSE_TYPE_WARN);
			batchSuspense.setSuspenseMessage("Column value \"teller\" should not be null or empty");
			 logger.info(String.format(INSERT_MESSAGE,insertTblBatchSuspense(batchSuspense)));
			return true;
		}
		if (null == ibgRejectStatus.getTrace() || ibgRejectStatus.getTrace().isEmpty()) {
			logMsg = String.format("IBGRejectStatus job execution id=%s,trace is empty.",jobExecutionId);
			logger.warn(logMsg);
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS,FAILED);
			batchSuspense.setSuspenseColumn("trace");
			batchSuspense.setSuspenseType(SUSPENSE_TYPE_WARN);
			batchSuspense.setSuspenseMessage("Column value \"trace\" should not be null or empty");
			 logger.info(String.format(INSERT_MESSAGE,insertTblBatchSuspense(batchSuspense)));
			return true;
		}
		return false;
	}

	private void checkValidJobExecutionId(ChunkContext chunkContext) {
		String logMsg;
		if(null==jobExecutionId || jobExecutionId.isEmpty()) {
			Long id= chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
			jobExecutionId=id.toString();
			logMsg  = String.format("Job Execution id not set in job parameter, take from job context jobExecutionId=%s", jobExecutionId);
			logger.info(logMsg);
		}
	}

	/**
	 * this method need to refactor by using enum lookup if there is multiple table to fire.
	 * @param date
	 * @param tellerId
	 * @param trace
	 * @return
	 */
	private IBGRejectStatusUserDto getUserId(String date, String tellerId,String trace ) {
		String logMsg="";
		IBGRejectStatusUserDto userDto = new IBGRejectStatusUserDto();
		userDto.setUserId("");
		userDto.setTableName(TBL_TRANSFER_TXN);
		try {
			String traceId = trace.substring(2);
			logMsg = String.format("Getting user id from TBL_TRANSFER_TXN - date=%s,teller=%s,traceId=%s, trace=%s", 
					date, tellerId, traceId,trace);
			logger.info(logMsg);
			IBGRejectStatusTblTransferTxn transferTxn = tblTransferTxnRepoImpl.getUserId(date, tellerId, traceId);
			if(null==transferTxn || 0==transferTxn.getUserId() ) {
				logMsg = String.format("User id not found in TBL_TRANSFER_TXN- date=%s,teller=%s,traceId=%s", date, tellerId, traceId);
				logger.info(logMsg);

				// User not found, try tbl_payment_txn
				logMsg = String.format("Getting user id from TBL_PAYMENT_TXN - date=%s,teller=%s,traceId=%s, trace=%s",
						date, tellerId, traceId,trace);
				logger.info(logMsg);
				userDto.setTableName(TBL_PAYMENT_TXN);
				IBGRejectStatusTblPaymentTxn paymentTxn = tblPaymentTxnRepoImpl.getUserId(date, tellerId, traceId);
				if(null==paymentTxn || 0==paymentTxn.getUserId() ) {
					logMsg = String.format("User id not found in TBL_PAYMENT_TXN- date=%s,teller=%s,traceId=%s", date, tellerId, traceId);
					logger.info(logMsg);
					return userDto;
				} else {
					logMsg = String.format("Record found in TBL_PAYMENT_TXN - user id=%s,teller=%s,trace=%s",
							paymentTxn.getUserId(), paymentTxn.getTellerId(), paymentTxn.getTraceId());
					logger.info(logMsg);
					userDto.setUserId(String.valueOf(paymentTxn.getUserId()));
				}
			} else {
				logMsg = String.format("Record found in TBL_TRANSFER_TXN - user id=%s,teller=%s,trace=%s",
						transferTxn.getUserId(), transferTxn.getTellerId(), transferTxn.getTraceId());
				logger.info(logMsg);
				userDto.setUserId(String.valueOf(transferTxn.getUserId()));
			}
		}catch(Exception ex) {
			logMsg = String.format("Getting user id from TBL_TRANSFER_TXN - exception=%s", ex.getMessage());
			logger.info(logMsg);
			logger.error(ex);
		}
		return userDto;
	}

	/**
	 * this method need to refactor by using enum lookup if there is multiple table to fire.
	 */
	private void updateTransferTxnStatus(String txnStatus, String date, String tellerId,String trace) {
		String logMsg="";
		String traceId = trace.substring(2);
		logMsg = String.format("Updating TBL_TRANSFER_TXN-set txn_Status=%s, Filter by updated_time=%s, teller_id=%s, trace_id=%s",
				txnStatus, date, tellerId, traceId );
		logger.info(logMsg);
		int row = tblTransferTxnRepoImpl.updateTxnStatus(txnStatus, date, tellerId, traceId);
		logMsg = String.format("Updating txn_status into TBL_TRANSFER_TXN -updated row =%s", row);
		logger.info(logMsg);
	}

	/**
	 * this method need to refactor by using enum lookup if there is multiple table to fire.
	 */
	private void updatePaymentTxnStatus(String txnStatus, String date, String tellerId,String trace) {
		String logMsg="";
		String traceId = trace.substring(2);
		logMsg = String.format("Updating TBL_PAYMENT_TXN-set txn_Status=%s, Filter by updated_time=%s, teller_id=%s, trace_id=%s",
				txnStatus, date, tellerId, traceId );
		logger.info(logMsg);
		int row = tblPaymentTxnRepoImpl.updateTxnStatus(txnStatus, date, tellerId, traceId);
		logMsg = String.format("Updating txn_status into TBL_PAYMENT_TXN -updated row =%s", row);
		logger.info(logMsg);
	}
	
	//[dbo].[TBL_BATCH_SUSPENSE]
	private int insertTblBatchSuspense(BatchSuspense batchSuspense ) {
		String logMsg="";
		int inserted=0;
		try {
			logMsg = String.format("Insert record into TBL_BATCH_SUSPENSE job exec id=%s, jobname=%s, suspense column=%s, suspense mesg=%s"
							, batchSuspense.getJobExecutionId(), batchSuspense.getBatchJobName(), batchSuspense.getSuspenseColumn(), batchSuspense.getSuspenseMessage());
			logger.info(logMsg);
			inserted = batchSuspenseRepoImpl.addBatchSuspenseToDB(batchSuspense);
		}catch(Exception ex) {
			logMsg=String.format("Insert record into TBL_BATCH_SUSPENSE exception=%s", ex.getMessage());
			logger.info(logMsg);
			logger.error(ex);
		}
		return inserted;
	}
	
	private boolean updateUserId(String userId, BatchStagedIBGRejectStatusTxn ibgRejectStatus) {
		String logMsg="";
		boolean updated = false;
		try {
			logMsg = String.format("Updating user id jobExeId=%s, teller=%s, trace=%s",
					ibgRejectStatus.getJobExecutionId(),ibgRejectStatus.getTeller(),ibgRejectStatus.getTrace());
			logger.info(logMsg);
			int row = ibgRejectStatusStagingRepositoryImpl.updateUserId(userId, ibgRejectStatus);
			logMsg = String.format("Updated row=%s", row);
			logger.info(logMsg);
			if(row>0) {
				updated=true;
			}
		}catch(Exception ex) {
			logMsg = String.format("Update user id - exception=%s", ex.getMessage());
			logger.info(logMsg);
			logger.error(ex);
		}
		return updated;
	}
	
	private boolean updateRejectDesc(BatchStagedIBGRejectStatusTxn ibgRejectStatus) {
		boolean updated = false;
		String logMsg="";
		try {
			logMsg = String.format("updating reject description jobExeId=%s, teller=%s, trace=%s",
						ibgRejectStatus.getJobExecutionId(),ibgRejectStatus.getTeller(),ibgRejectStatus.getTrace());
			logger.info(logMsg);
			int row = ibgRejectStatusStagingRepositoryImpl.updateRejectDescription(ibgRejectStatus);
			logMsg = String.format("updated row=%s", row);
			logger.info(logMsg);
			if(row>0) {
				updated=true;
			}
		}catch(Exception ex) {
			logMsg = String.format("update reject description - exception=%s", ex.getMessage());
			logger.info(logMsg);
			logger.error(ex);
		}		
		return updated;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Auto-generated method stub
		
	}

}
