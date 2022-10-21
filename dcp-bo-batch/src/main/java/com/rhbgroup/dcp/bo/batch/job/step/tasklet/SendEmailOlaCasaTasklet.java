package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_REPORT_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_FILE_SOURCE_PATH_KEY;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.email.OlaCasaEmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;

@Component
@Lazy
public class SendEmailOlaCasaTasklet implements Tasklet, InitializingBean {
    private static final Logger logger = Logger.getLogger(SendEmailOlaCasaTasklet.class);
    private static final String OLAREPORTID = "DMBUD089";
    private static final String OLAABANDONREPORTID = "DMBUD090";
    
	@Autowired
	OlaCasaEmailTemplate emailTemplate;
	
	protected Date batchProcessingDate;
    
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		try {
            String reportID=chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_REPORT_ID_KEY);
			logger.info(String.format("reportID = %s", reportID));
			String batchSystemDate=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;

			if(OLAREPORTID.equals(reportID) || OLAABANDONREPORTID.equals(reportID)) {
            	String sourceFileFullPath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(REPORT_JOB_PARAMETER_REPORT_FILE_SOURCE_PATH_KEY);
    			logger.info("Email Initiated");
            	emailTemplate.sendMail(sourceFileFullPath,batchProcessingDate,reportID);
    			logger.info("Email Sent");
            }
            

		} catch(Exception ex) {
				String errorMsg = String.format("Exception: exception=%s",ex.getMessage());
				logger.error(errorMsg);
				chunkContext.getStepContext().getStepExecution().getJobExecution().setExitStatus(ExitStatus.FAILED);
				throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
		}
        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
		// Do nothing because this is the implementation of Spring Batch
    }
}
