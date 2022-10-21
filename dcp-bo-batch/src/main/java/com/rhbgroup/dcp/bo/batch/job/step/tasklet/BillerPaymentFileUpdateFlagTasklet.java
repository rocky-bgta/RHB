package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;

import java.util.Date;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerPaymentConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerPaymentConfigRepositoryImpl;

@Component
@Lazy
public class BillerPaymentFileUpdateFlagTasklet implements Tasklet {
    static final Logger logger = Logger.getLogger(BillerPaymentFileUpdateFlagTasklet.class);

    String JOB_NAME;
    
    @Autowired
	@Qualifier("BillPaymentConfigOutboundQueue")
	private Queue<BillerPaymentOutboundConfig> queue ;
	
    @Autowired
    BatchBillerPaymentConfigRepositoryImpl batchBillerPaymentConfigRepositoryImpl;
    
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
		try {
			int stepStatus=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS);
			if(stepStatus == BatchSystemConstant.ExitCode.FAILED) {
				return RepeatStatus.FINISHED;
			}
			BillerPaymentOutboundConfig billerConfig = queue.element();
			logger.info( String.format("Tasklet Updating Biller Payment Config IS_REQUIRED_EXCUTE Flag id=%s, biller code=%s",billerConfig.getId(), billerConfig.getBillerCode() ));
			JOB_NAME = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
			BatchBillerPaymentConfig batchBillerPaymentConfig = mapBatchBillerPaymentConfig(billerConfig);
			int requiredExecute = 0;
			batchBillerPaymentConfigRepositoryImpl.updateBatchBillerPaymentConfigExecuteFlag(batchBillerPaymentConfig,requiredExecute);
			logger.info("Update Biller Payment Config IS_REQUIRED_EXCUTE Flag completed");
		} catch (Exception ex) {
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.FAILED);
			logger.info( String.format("Tasklet Updating Biller Payment Config IS_REQUIRED_EXCUTE exception %s",ex.getMessage() ));
			logger.error(ex);
		}
		return RepeatStatus.FINISHED;
	}
	
	private BatchBillerPaymentConfig mapBatchBillerPaymentConfig(BillerPaymentOutboundConfig billerConfig) {
		String updatedBy="BillerPaymentFileJob";
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
		batchBillerPaymentConfig.setId(billerConfig.getId());
		batchBillerPaymentConfig.setBillerCode(billerConfig.getBillerCode());
		batchBillerPaymentConfig.setTemplateName(billerConfig.getTemplateName());
		batchBillerPaymentConfig.setFileNameFormat(billerConfig.getFileNameFormat());
		batchBillerPaymentConfig.setFtpFolder(billerConfig.getFtpFolder());
		batchBillerPaymentConfig.setReportUnitUri(billerConfig.getReportUnitUri());
		batchBillerPaymentConfig.setStatus(billerConfig.getStatus());
		batchBillerPaymentConfig.setUpdatedTime(new Date());
		batchBillerPaymentConfig.setUpdatedBy(updatedBy);
		return batchBillerPaymentConfig;
	}
}
