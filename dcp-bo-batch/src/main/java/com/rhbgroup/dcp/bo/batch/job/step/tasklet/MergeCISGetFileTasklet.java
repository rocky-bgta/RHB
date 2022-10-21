package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.MergeCISParameter.MERGE_CIS_EXEC_FILE_NAME;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;

@Component
@Lazy
public class MergeCISGetFileTasklet implements Tasklet{
	
	private static final Logger logger = Logger.getLogger(MergeCISGetFileTasklet.class);
	private int dayDiff=-1;
	
	@Autowired
	private FTPConfigProperties ftpConfigProperties;
	
	@Value("${job.mergecisjob.ftpfolder}")
	private String sourceFtpFolder;
	
	@Value("${dcp.bo.batch.inputfolder.path}")
	private String targetFileFolder;
	
	@Value("${job.mergecisjob.namedateformat}")
	private String fileNameDateFormat;
	
	@Value("${job.mergecisjob.name}")
	private String fileNameFormat;
	
	@Autowired
	private DcpBatchApplicationContext dcpBatchApplicationContext;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		String logMsg = "";
		String batchDBSystemDateStr = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		String jobname = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
		logMsg = String.format("Get File from FTP job=%s, batchDBSystemDateStr=%s", jobname, batchDBSystemDateStr);
		logger.info(logMsg);
		Date processDate  = DateUtils.getDateFromString(batchDBSystemDateStr, BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT);
		processDate = DateUtils.addDays(processDate, dayDiff);
		String batchProcessDate=chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
		if(null!=batchProcessDate && !batchProcessDate.isEmpty()) {
			processDate = DateUtils.getDateFromString(batchProcessDate, BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT);
		}
		String fileNameDate = DateUtils.formatDateString(processDate, fileNameDateFormat);
		String targetFileName = fileNameFormat.replace("{#date}", fileNameDate);
		logMsg = String.format("Get File from FTP fileNameFormat=%s, fileNameDate=%s, inboundFileName=%s",fileNameFormat, fileNameDate,targetFileName);
		logger.info(logMsg);
		String inboundLocalFilePath = targetFileFolder.replace("\\", File.separator).concat(File.separator).concat(jobname);
		File localFilePath = new File(inboundLocalFilePath);
		if(!localFilePath.exists()) {
			FileUtils.forceMkdir(localFilePath);
		}
		String ftpFilePath = sourceFtpFolder.concat("/").concat(targetFileName);
		FTPUtils.downloadFileFromFTP(ftpFilePath, inboundLocalFilePath, ftpConfigProperties);
		chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(MERGE_CIS_EXEC_FILE_NAME, targetFileName);
		return RepeatStatus.FINISHED;
	}

}
