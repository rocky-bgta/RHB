package com.rhbgroup.dcp.bo.batch.framework.common.listener;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_TIME_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils.formatDateString;
import static com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils.getFormattedDateTimeString;

import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Date;

@Component
public class BatchJobCommonExecutionListener implements JobExecutionListener {
	static final Logger logger = Logger.getLogger(BatchJobCommonExecutionListener.class);
	static final Logger jobLogger = Logger.getLogger("com.rhbgroup.dcp.bo.batch.log");
    static final String LOG_RUN_DATE_FORMAT = "yyyyMMdd";
    static final String LOG_DATE_FORMAT = "dd-MMM-yyyy";
    String batchId = null;

    @Override
    public void beforeJob(JobExecution jobExecution){

        String processDateStr=null;
        batchId = jobExecution.getJobParameters().getString(BATCH_JOB_PARAMETER_BATCH_ID_KEY);
        if(StringUtils.isEmpty(batchId))
            batchId = "";
        try{
            if(!StringUtils.isEmpty(jobExecution.getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY))) {
                Date processDate = DateUtils.getDateFromString(jobExecution.getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY), DEFAULT_JOB_PARAMETER_DATE_FORMAT);
                processDateStr = DateUtils.formatDateString(processDate, LOG_RUN_DATE_FORMAT);
            }else{
                processDateStr = formatDateString(jobExecution.getStartTime(), LOG_RUN_DATE_FORMAT);
            }
        }catch(ParseException e){
            logger.error(e);
        }

        logger.info("=============================================================================");
        logger.info(String.format("Job Name:%s",jobExecution.getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY)));
        logger.info(String.format("Job Execution Id:%s",jobExecution.getId()));
        logger.info(String.format("Job Parameters:%s",getJobParametersToString(jobExecution.getJobParameters())));
        logger.info(String.format("Job Start Time:%s", getFormattedDateTimeString(jobExecution.getStartTime())));
        
        jobLogger.info("================================================================================");
        jobLogger.info(String.format("Job Name:\t%s (%s)",batchId, jobExecution.getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY)));
        jobLogger.info("--------------------------------------------------------------------------------");
        jobLogger.info(String.format("Start Date:\t%s", formatDateString(jobExecution.getStartTime(), LOG_DATE_FORMAT)));
        jobLogger.info(String.format("Start Time:\t%s%n", formatDateString(jobExecution.getStartTime(), DEFAULT_TIME_FORMAT)));
        jobLogger.info("-------------------------------- RUN DATE START --------------------------------");
        jobLogger.info("The run date is " + processDateStr);
        jobLogger.info("-------------------------------- RUN DATE   END --------------------------------\n");
        jobLogger.info("PROCESS");
        jobLogger.info("--------------------------------      START     --------------------------------");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        jobLogger.info("--------------------------------       END      --------------------------------\n");
        jobLogger.info("STATUS");
        jobLogger.info("--------------------------------------------------------------------------------");
        if(!jobExecution.getAllFailureExceptions().isEmpty()) {
            jobExecution.setStatus(BatchStatus.FAILED);
            jobExecution.setExitStatus(ExitStatus.FAILED);
            jobLogger.info(String.format("Batch job %s (%s) %s",batchId, jobExecution.getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY),jobExecution.getStatus()));
            for(Throwable exception: jobExecution.getAllFailureExceptions()){
            	StringWriter sw = new StringWriter();
                jobLogger.error(String.format("ERROR:\t%s",exception.getMessage()));
                exception.printStackTrace(new PrintWriter(sw));
                jobLogger.error(String.format("ERROR:\t%s",sw.toString()));
            }
        }else
            jobLogger.info(String.format("Batch job %s (%s) %s",batchId, jobExecution.getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY),jobExecution.getStatus()));
        jobLogger.info(String.format("End Date:\t%s", formatDateString(jobExecution.getEndTime(), LOG_DATE_FORMAT)));
        jobLogger.info(String.format("End Time:\t%s", formatDateString(jobExecution.getEndTime(), DEFAULT_TIME_FORMAT)));
        jobLogger.info(String.format("Duration:\t%s seconds",(jobExecution.getEndTime().getTime()-jobExecution.getStartTime().getTime())/1000));
        jobLogger.info("================================================================================");

        logger.info(String.format("Job End Time:%s", getFormattedDateTimeString(jobExecution.getEndTime())));
        logger.info(String.format("Job Status:%s",jobExecution.getStatus()));
        logger.info(String.format("Job Summary:%s",BatchUtils.getSummaryStatistics(jobExecution)));
    }

    private String getJobParametersToString(JobParameters jobParameters)
    {
        StringBuilder jobParameterStringBuilder=new StringBuilder();
        if(jobParameters!=null && !jobParameters.isEmpty())
        {
            for(String key: jobParameters.getParameters().keySet() )
            {
                jobParameterStringBuilder.append(key+":"+jobParameters.getString(key));
            }
        }

        return  jobParameterStringBuilder.toString();
    }
}
