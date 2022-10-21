package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKBillerPaymentJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerPaymentConfig;

@Component
@Lazy
public class GetIBKBillerPaymentFileTasklet implements Tasklet {

	private static final Logger logger = Logger.getLogger(GetIBKBillerPaymentFileTasklet.class);
	
	@Value(SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolderPath;
	
	@Autowired
	private LoadIBKBillerPaymentJobConfigProperties jobConfigProperties;
	
	@Autowired
	private FTPIBKConfigProperties ftpConfigProperties;
	
	@Autowired
	@Qualifier("BatchBillerPaymentConfigQueue")
    private Queue<BatchBillerPaymentConfig> batchBillerPaymentConfigQueue;
	
	@Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws BatchException {
		logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));
		
		// Remove one of the biller from the queue to be process
		BatchBillerPaymentConfig batchBillerPaymentConfig = batchBillerPaymentConfigQueue.poll();
		
    	if (batchBillerPaymentConfig != null) {
    		// Get the jobname and batch system date from job parameters
    		String batchJobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY);
    		
    		// Getting the actual batch process date, for batch system date we will need to -1 day to get the process date
    		Date batchProcessDate = getProcessDate(chunkContext.getStepContext().getStepExecution());
			logger.debug(String.format("Processing date to be used [%s]", batchProcessDate));
        
			// Getting the target input folder path appended with job name
			String targetFolderPath = getTargetFolder(Paths.get(inputFolderPath, batchJobName).toFile());
            	        
            // Download  biller file from the FTP
            String sourceFileName = null;
        	try {
        		String fileNameFormat = batchBillerPaymentConfig.getFileNameFormat();
        		sourceFileName = BatchUtils.generateSourceFileName(fileNameFormat, batchProcessDate);
        	} catch (Exception e) {
    			String errorMessage = String.format("Error happened when generating source filename with format [%s] using date [%s]", batchBillerPaymentConfig.getFileNameFormat(), batchProcessDate);
    			logger.error(errorMessage, e);
    			throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR,BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, e);
    		}
        	String sourceFilePath = batchBillerPaymentConfig.getIbkFtpFolder() + sourceFileName;
            File targetFile = Paths.get(targetFolderPath, sourceFileName).toFile();
            String targetFilePath = targetFile.getAbsolutePath();
            try {
            	logger.debug(String.format("FTP get source [%s] and store into destination [%s]", sourceFilePath, targetFilePath));
            	logger.debug(String.format("Storing biller code [%s] to context", batchBillerPaymentConfig.getBillerCode()));
            	chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_CODE, batchBillerPaymentConfig.getBillerCode());
				FTPUtils.downloadFileFromFTP(sourceFilePath, targetFilePath, ftpConfigProperties);
				validateInputFile(targetFilePath);
				// Store the saved filepath to context for later step reference
				logger.debug(String.format("Storing target filepath [%s] to context", targetFilePath));
	    		chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY, targetFilePath);	    		
			} catch (BatchException e) {
				String errorMessage = String.format("Error happened when trying to FTP-Get the source file [%s] from [%s:%s], skip processing for BillerCode [%s]", sourceFilePath, 
					ftpConfigProperties.getHost(), ftpConfigProperties.getPort(),
					batchBillerPaymentConfig.getBillerCode());
				if(e.getDcpStatusCode().equalsIgnoreCase(BatchErrorCode.FILE_NOT_FOUND)){
					errorMessage += ", file is not found in the FTP server";
					logger.warn(errorMessage, e);
					chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().remove(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
				} else {
					logger.error(errorMessage, e);
					throw e;
				}
			}
    	}
    	
    	logger.info(String.format("Tasklet [%s] executed successfully", this.getClass().getSimpleName()));
        return RepeatStatus.FINISHED;
    }
    
	private String getTargetFolder(File targetFolder) {
		if (!targetFolder.exists()) {
        	logger.debug(String.format("Target folder [%s] doesn't exists, create it", targetFolder));
        	targetFolder.mkdirs();
        }
        return targetFolder.getAbsolutePath();
	}
	
	private Date getProcessDate(StepExecution stepExecution) throws BatchException {
		String batchProcessDateStr = stepExecution.getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
		logger.debug(String.format("Batch processing date from external [%s]", batchProcessDateStr));
		
		// Get batch system date from DB if it not found in job parameters
		String batchSystemDateStr = null;
		if(batchProcessDateStr == null) {
			batchSystemDateStr = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			logger.debug(String.format("Batch processing date from DB [%s]", batchSystemDateStr));
		}
		
		// Getting the actual batch process date, for batch system date we will need to -1 day to get the process date
		Date batchProcessDate = null;
		String dateStr = null;
		String dateStrFormat = null;
		try {
			if(batchProcessDateStr != null) {
				// The job data parameter is expected to be in format yyyyMMdd
				dateStr = batchProcessDateStr;
				dateStrFormat = General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
				batchProcessDate = DateUtils.getDateFromString(dateStr, dateStrFormat);
			} else {
				dateStr = batchSystemDateStr;
				dateStrFormat = General.DEFAULT_DATE_FORMAT;
				batchProcessDate = DateUtils.getDateFromString(dateStr, dateStrFormat);
				batchProcessDate = DateUtils.addDays(batchProcessDate, -1);
			}
		} catch (ParseException e) {
			String errorMessage = String.format("Error happened while parsing date [%s] using format [%s]", dateStr, dateStrFormat);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, e);
		}
		
		return batchProcessDate;
	}
	
    private void validateInputFile(String filepath) throws BatchException {
    	logger.debug(String.format("Validating input file [%s]", filepath));
		long detailCounter = 0;
		
		try (BufferedReader in = new BufferedReader(new FileReader(filepath))) {
			// Header line checking
			String headerLine = in.readLine();
			logger.trace(String.format("Header [%s]", headerLine));
			if(headerLine.isEmpty()) {
				throw new IllegalArgumentException(String.format("Header is empty, it should be [%s..] instead", jobConfigProperties.getHeaderPrefix()));
			}
			if(!headerLine.startsWith(jobConfigProperties.getHeaderPrefix())) {
				throw new IllegalArgumentException(String.format("Invalid prefix found in header [%s], it should be [%s..] instead", headerLine, jobConfigProperties.getHeaderPrefix()));
			}
			
			// Counting detail lines
			String detailLine = null;
			int lineNumber = 2;
			boolean isTrailerFound = false;
			while((detailLine = in.readLine()) != null) {
				if(StringUtils.isEmpty(detailLine)) {
					throw new IllegalArgumentException(String.format("Detail is empty on line [%s], it should be [%s..] instead", lineNumber, jobConfigProperties.getTrailerPrefix()));
				}
				lineNumber++;
				
				// Expect detail line start from 2nd line onward
				if(detailLine.startsWith(jobConfigProperties.getDetailPrefix())) {
					logger.trace(String.format("Detail [%s]", detailLine));
					detailCounter++;
				// Only trailer will reach this section
				} else {
					if(!detailLine.startsWith(jobConfigProperties.getTrailerPrefix())) {
						throw new IllegalArgumentException(String.format("Invalid prefix found in trailer %s, it should be [%s..] instead", detailLine, jobConfigProperties.getTrailerPrefix()));
					}
					isTrailerFound = true;
					
					logger.trace(String.format("Trailer [%s]", detailLine));
					validateDetailTotal(detailLine, detailCounter);
				}
			}
			
			if (!isTrailerFound) {
				throw new IllegalArgumentException(String.format("No trailer found in the file [%s]", filepath));
			}
			
			logger.debug(String.format("Validation on input file [%s] completed, total details found [%d]", filepath, detailCounter));
		} catch (BatchException e) {
			throw e;
    	} catch (Exception e) {
			String errorMessage = String.format("Error happened when validating input file [%s]", filepath);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
		}	
	}
    
    private void validateDetailTotal(String detailLine, long expectedDetailTotal) throws BatchException {
    	long batchTotal = getBatchTotalFromTrailer(detailLine);
		
		if (batchTotal != expectedDetailTotal) {
			String errorMessage = String.format("Error happened when comparing DetailTotal [%d] with Trailer BatchTotal [%d]", expectedDetailTotal, batchTotal);
			logger.error(errorMessage);
			throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
		}
    }
    
    private long getBatchTotalFromTrailer(String trailerLine) {
    	LineTokenizer tokenizer = BatchUtils.getFixedLengthTokenizer(jobConfigProperties.getTrailerNames(), jobConfigProperties.getTrailerColumns());
		FieldSet fs = tokenizer.tokenize(trailerLine);
		String batchTotalStr = StringUtils.stripStart(fs.readString("batchTotal"),"0");
		return Long.parseLong((batchTotalStr.isEmpty() ? "0" : batchTotalStr));
    }
	
}