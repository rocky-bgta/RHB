package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
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
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerPaymentFileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;


@Component
@Lazy
public class BillerPaymentFileUpdatePathTasklet implements Tasklet {
    static final Logger logger = Logger.getLogger(BillerPaymentFileUpdatePathTasklet.class);

    String JOB_NAME;
    
    @Autowired
	@Qualifier("BillPaymentConfigOutboundQueue")
	private Queue<BillerPaymentOutboundConfig> queue ;
	
    @Autowired
    BatchBillerPaymentFileRepositoryImpl batchBillerPaymentFileRepositoryImpl;
    
    protected Date batchProcessingDate;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
		try {
			String batchSystemDate=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
			int stepStatus=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS);
			if(stepStatus == BatchSystemConstant.ExitCode.FAILED) {
				return RepeatStatus.FINISHED;
			}
			BillerPaymentOutboundConfig billerConfig = queue.element();
			logger.info( String.format("Tasklet Updating Biller Payment File Path  biller code=%s",billerConfig.getBillerCode() ));
			JOB_NAME = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
			BatchBillerPaymentConfig batchBillerPaymentConfig = mapBatchBillerPaymentConfig(billerConfig);
			int isGenerated = 1;
			java.sql.Date sqlDate = new java.sql.Date(batchProcessingDate.getTime());
			batchBillerPaymentFileRepositoryImpl.updateBatchBillerPaymentFilePath(batchBillerPaymentConfig,isGenerated,sqlDate);
			logger.info("Update Biller Payment File Path completed");
		} catch (Exception ex) {
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.FAILED);
			logger.info( String.format("Tasklet Updating Biller Payment File Path exception %s",ex.getMessage() ));
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
