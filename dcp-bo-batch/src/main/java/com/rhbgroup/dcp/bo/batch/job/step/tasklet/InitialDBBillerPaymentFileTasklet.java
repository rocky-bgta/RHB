package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.ReadBatchParameterFromDBTasklet;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerPaymentFileRepositoryImpl;

import java.util.Date;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;

@Component
@Lazy
public class InitialDBBillerPaymentFileTasklet implements Tasklet {
	 private static final Logger logger = Logger.getLogger(ReadBatchParameterFromDBTasklet.class);
	@Autowired
	private BatchBillerPaymentFileRepositoryImpl batchBillerPaymentFileRepositoryimpl;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		 logger.info("Initializing DB Biller PaymentFile ..");
		 try {
			 String batchSystemDate=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

			 Date batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
			 String processingDateStr = DateUtils.formatDateString(batchProcessingDate ,DEFAULT_DATE_FORMAT);
				java.sql.Date sqlDate = new java.sql.Date(batchProcessingDate.getTime());

			 batchBillerPaymentFileRepositoryimpl.getBillerPaymentConfig(sqlDate);
			} catch(Exception ex) {
					String errorMsg = String.format("Exception: exception=%s",ex.getMessage());
					logger.error(errorMsg);
					chunkContext.getStepContext().getStepExecution().getJobExecution().setExitStatus(ExitStatus.FAILED);
					throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
			}
	        return RepeatStatus.FINISHED;
	}
	
}
