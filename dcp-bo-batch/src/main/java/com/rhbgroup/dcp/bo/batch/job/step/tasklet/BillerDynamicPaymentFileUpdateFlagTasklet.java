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
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerDynamicPaymentConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerDynamicPaymentConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerPaymentConfigRepositoryImpl;

@Component
@Lazy
public class BillerDynamicPaymentFileUpdateFlagTasklet implements Tasklet {
    static final Logger logger = Logger.getLogger(BillerDynamicPaymentFileUpdateFlagTasklet.class);

    String JOB_NAME;
    
    @Autowired
	@Qualifier("BillDynamicPaymentConfigOutboundQueue")
	private Queue<BillerDynamicPaymentOutboundConfig> queue ;
	
    @Autowired
    BatchBillerDynamicPaymentConfigRepositoryImpl batchBillerDynamicPaymentConfigRepositoryImpl;
    
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
		try {
			int stepStatus=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS);
			if(stepStatus == BatchSystemConstant.ExitCode.FAILED) {
				return RepeatStatus.FINISHED;
			}
			BillerDynamicPaymentOutboundConfig billerConfig = queue.element();
			logger.info( String.format("Tasklet Updating Biller Payment Config IS_REQUIRED_EXCUTE Flag id=%s, biller code=%s",billerConfig.getId(), billerConfig.getBillerCode() ));
			JOB_NAME = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
			BatchBillerDynamicPaymentConfig batchBillerDynamicPaymentConfig = mapBatchBillerDynamicPaymentConfig(billerConfig);
			int requiredExecute = 0;
			batchBillerDynamicPaymentConfigRepositoryImpl.updateBatchBillerPaymentConfigExecuteFlag(batchBillerDynamicPaymentConfig,requiredExecute);
			logger.info("Update Biller Payment Config IS_REQUIRED_EXCUTE Flag completed");
		} catch (Exception ex) {
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.FAILED);
			logger.info( String.format("Tasklet Updating Biller Payment Config IS_REQUIRED_EXCUTE exception %s",ex.getMessage() ));
			logger.error(ex);
		}
		return RepeatStatus.FINISHED;
	}
	
	private BatchBillerDynamicPaymentConfig mapBatchBillerDynamicPaymentConfig(BillerDynamicPaymentOutboundConfig billerConfig) {
		String updatedBy="BillerDynamicPaymentFileJob";
		BatchBillerDynamicPaymentConfig batchBillerDynamicPaymentConfig = new BatchBillerDynamicPaymentConfig();
		batchBillerDynamicPaymentConfig.setId(billerConfig.getId());
		batchBillerDynamicPaymentConfig.setBillerCode(billerConfig.getBillerCode());
		batchBillerDynamicPaymentConfig.setTemplateName(billerConfig.getTemplateName());
		batchBillerDynamicPaymentConfig.setFileNameFormat(billerConfig.getFileNameFormat());
		batchBillerDynamicPaymentConfig.setFtpFolder(billerConfig.getFtpFolder());
		batchBillerDynamicPaymentConfig.setReportUnitUri(billerConfig.getReportUnitUri());
		batchBillerDynamicPaymentConfig.setStatus(billerConfig.getStatus());
		batchBillerDynamicPaymentConfig.setUpdatedTime(new Date());
		batchBillerDynamicPaymentConfig.setUpdatedBy(updatedBy);
		return batchBillerDynamicPaymentConfig;
	}
}
