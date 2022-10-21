package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ASNBReconSettlementJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.AsnbRepository;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.AsnbFtpConfigProperties;

@Component
@Lazy
public class CopyFTPFileToInputFolderTasklet implements Tasklet {
	static final Logger logger = Logger.getLogger(CopyFTPFileToInputFolderTasklet.class);

	@Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
	private String inputFolderFullPath;
	private String sourceFileName;
	private String sourceFileDateFormat;
	private String sourceFileFolderPath;
	private String channelType;
	private int dayDiff = -1;
	private boolean asnbCheck;

	@Autowired
	private FTPConfigProperties ftpConfigProperties;

	@Autowired
	private AsnbFtpConfigProperties asnbFtpConfigProperties;

	@Autowired
	private AsnbRepository asnbRepository;

	private static final String INTERNET = "IB00";

	public void init(String sourceFileFolderPath, String sourceFileName, String sourceFileDateFormat) {
		this.sourceFileDateFormat = sourceFileDateFormat;
		this.sourceFileName = sourceFileName;
		this.sourceFileFolderPath = sourceFileFolderPath;
	}

	public void init(String sourceFileFolderPath, String sourceFileName, String sourceFileDateFormat, boolean asnbCheck,
					 String channelType) {
		this.sourceFileDateFormat = sourceFileDateFormat;
		this.sourceFileName = sourceFileName;
		this.sourceFileFolderPath = sourceFileFolderPath;
		this.asnbCheck = asnbCheck;
		this.channelType = channelType;
	}

	public void initDayDiff(int dayDiff) {
		this.dayDiff = dayDiff;
	}

	public void initFTPConfig(FTPConfigProperties ftpConfigProperties) {
		this.ftpConfigProperties = ftpConfigProperties;
	}

	public void initAsnbFtpConfig(AsnbFtpConfigProperties asnbFtpConfigProperties) {
		this.asnbFtpConfigProperties = asnbFtpConfigProperties;
	}

	public void initInputFolderPath(String inputFolderFullPath) {
		this.inputFolderFullPath = inputFolderFullPath;
	}

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws BatchException {
		logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));
		String jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()
				.getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
		Date processDate = null;

		try {

			processDate = getProcessDate(chunkContext);

			logger.info(String.format("Final process date to be used : [%s]", processDate));

			String sourceFileNewName = this.sourceFileName.replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER,
					DateUtils.formatDateString(processDate, this.sourceFileDateFormat));
			logger.info("SOURCE FILE NAME : " + sourceFileNewName);

			logger.info("source folder path : " + sourceFileFolderPath);
			File sourceFileFullPath = Paths.get(sourceFileFolderPath, sourceFileNewName).toFile();
			File targetFileFullDirectory = null;
			logger.info(String.format("FTP server's input file path: [%s]", sourceFileFullPath));
			/// from here put dynamic code
			if (!asnbCheck) {
				targetFileFullDirectory = getTargetFileFullDirectory(jobName, sourceFileNewName, asnbCheck);

				FTPUtils.downloadFileFromFTP(sourceFileFullPath.toString(), targetFileFullDirectory.toString(),
						ftpConfigProperties);
			} else {
				targetFileFullDirectory = getTargetFileFullDirectory(jobName, sourceFileNewName, asnbCheck);

				String d = DateUtils.getProcessDate(chunkContext);
				logger.info("Date to be process is ::" + d);
				String[] val = channelType.split(",");
				logger.info("val : " + Arrays.toString(val));
				List<String> fileList = FTPUtils.listFilesFromFtpFolder(sourceFileFolderPath, asnbFtpConfigProperties);
				logger.info("fileList : " +  fileList.toString());
				List<String> files = new ArrayList<>();
				for (String v : val) {
					files.addAll(fileList.stream().filter(n->n.contains(v)).collect(Collectors.toList()));
				}

				for (String name : files) {
					logger.info("File name : " + sourceFileFolderPath + "/" + name);
					if (checkFileToProcess(name, d)) {
						FTPUtils.downloadFileFromFTP(sourceFileFolderPath + "/" + name,
								targetFileFullDirectory.toString(), asnbFtpConfigProperties);
					}
				}
			}

			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString(
					BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY,
					Paths.get(inputFolderFullPath, jobName, sourceFileNewName).toAbsolutePath().toString());
		} catch (ParseException ex) {
			throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE,
					ex);
		}

		logger.info(String.format("Tasklet [%s] executed successfully", this.getClass().getSimpleName()));
		return RepeatStatus.FINISHED;
	}

	private File getTargetFileFullDirectory(String jobName, String sourceFileNewName, boolean asnbCheck) {
		File targetFileFullDirectory = null;
		if (!asnbCheck) {
			if (ftpConfigProperties.isIssecureftp()) {
				targetFileFullDirectory = Paths.get(inputFolderFullPath, jobName).toFile();
			} else {
				logger.info("inside else");
				targetFileFullDirectory = Paths.get(inputFolderFullPath, jobName, sourceFileNewName).toFile();
			}
		} else {
			if (asnbFtpConfigProperties.isIssecureftp()) {
				targetFileFullDirectory = Paths.get(inputFolderFullPath, jobName).toFile();
			} else {
				targetFileFullDirectory = Paths.get(inputFolderFullPath, jobName, sourceFileNewName).toFile();
			}
		}
		return targetFileFullDirectory;
	}

	private Date getProcessDate(ChunkContext chunkContext) throws ParseException {
		// Check if custom JobProcessDate is provided
		String jobProcessDateStr = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()
				.getString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
		Date processDate = null;
		if (jobProcessDateStr != null) {
			logger.info(String.format("Job process date found : [%s]", jobProcessDateStr));
			processDate = DateUtils.getDateFromString(jobProcessDateStr, DEFAULT_JOB_PARAMETER_DATE_FORMAT);
			// If not found stick to the BatchSystemDate from DB
		} else {
			String batchSystemDateStr = (String) chunkContext.getStepContext().getStepExecution().getJobExecution()
					.getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			logger.info(String.format("Job process date could not be found, defaulting to Batch System Date : [%s]",
					processDate));
			Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
			processDate = DateUtils.addDays(batchSystemDate, dayDiff);
		}

		return processDate;
	}

	private boolean checkFileToProcess(String name, String asnbDte) {
		String date = name.substring(10, 18);
		logger.info(date + " ::" + asnbDte);
		if (date.equals(asnbDte)) {
			logger.info(date + " :equal: " + asnbDte);
			if (name.substring(6, 10).equals(INTERNET)) {
				return name.contains(ASNBReconSettlementJobParameter.DIB_FILE_NAME_ENDING_PREFIX);
			}
			return true;
		} else {
			return false;
		}

	}
}
