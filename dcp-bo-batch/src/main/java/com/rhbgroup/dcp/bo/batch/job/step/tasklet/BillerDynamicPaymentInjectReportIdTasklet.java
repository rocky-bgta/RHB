package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_REPORT_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_UNIT_URI;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_BILLER_CODE;

import java.util.Queue;

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

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundConfig;



@Component
@Lazy
public class BillerDynamicPaymentInjectReportIdTasklet  implements Tasklet, InitializingBean{
    static final Logger logger = Logger.getLogger(BillerDynamicPaymentInjectReportIdTasklet.class);
    static final String BILLER_PAYMENT_PREFIX_REPORT_ID ="DMBUD999_";
    
	@Autowired
	@Qualifier("BillDynamicPaymentConfigOutboundQueue")
	private Queue<BillerDynamicPaymentOutboundConfig> queue ;
	
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)  {
		try {
			// getExecutionContext
			int stepStatus=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS);
			if(stepStatus == BatchSystemConstant.ExitCode.FAILED) {
				return RepeatStatus.FINISHED;
			}
			logger.info( String.format("prev step exit status:%s", stepStatus ) );
			BillerDynamicPaymentOutboundConfig billerConfig = queue.element();
			String billerCode = billerConfig.getBillerCode();
			String reportid = BILLER_PAYMENT_PREFIX_REPORT_ID.concat(billerCode);
			String reportUri = billerConfig.getReportUnitUri();
			logger.info(String.format("Biller Payment outbound inject report biller code=%s, report Uri=%S", billerCode, reportUri));
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_JOB_PARAMETER_REPORT_ID_KEY, reportid);
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(REPORT_UNIT_URI,reportUri);
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(REPORT_BILLER_CODE, billerCode);
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.SUCCESS);
		} catch (Exception ex) {
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.FAILED);
			String message = String.format("Exception Biller payment outbound file setting report id exception=%s", ex.getMessage());
			logger.info(message);
		}
		return RepeatStatus.FINISHED;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing because this is the implementation of Spring Batch
	}
}
