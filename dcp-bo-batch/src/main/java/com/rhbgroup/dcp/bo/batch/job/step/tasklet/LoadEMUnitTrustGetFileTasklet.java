package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
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
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties.UTFile;
import com.rhbgroup.dcp.bo.batch.job.model.AccountBatchInfo;
import com.rhbgroup.dcp.bo.batch.job.repository.AccountBatchInfoRepositoryImpl;


@Component
@Lazy
public class LoadEMUnitTrustGetFileTasklet implements Tasklet{
    static final Logger logger = Logger.getLogger(LoadEMUnitTrustGetFileTasklet.class);
    
    @Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolderFullPath;

    @Autowired
	private LoadEMUnitTrustJobConfigProperties jobConfigProperties;   
    
    @Autowired
    private FTPConfigProperties ftpConfigProperties;

    @Autowired
    AccountBatchInfoRepositoryImpl acctBatchInfoRepoImpl;
    
    @Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub
        String jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
        Date processDate = new Date();
    	for(UTFile utFileConfig : jobConfigProperties.getUtFiles()) {
    		String sourceName = utFileConfig.getName();
    		String sourceFileDateFormat = utFileConfig.getNameDateFormat();
            String jobProcessDateStr = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
            if(jobProcessDateStr != null){
            	logger.debug(String.format("Job process date found : [%s]", jobProcessDateStr));
            	processDate = DateUtils.getDateFromString(jobProcessDateStr, DEFAULT_JOB_PARAMETER_DATE_FORMAT);
            // If not found stick to the BatchSystemDate from DB
            } else {
            	String batchSystemDateStr = (String)chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
            	logger.debug(String.format("Job process date could not be found, defaulting to Batch System Date : [%s]", processDate));
            	Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
            	processDate = DateUtils.addDays(batchSystemDate, utFileConfig.getDayDiff());
            }
            String sourceFileNewName = sourceName.replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, DateUtils.formatDateString(processDate, sourceFileDateFormat));
            File sourceFileFullPath = Paths.get(jobConfigProperties.getFtpFolder(), sourceFileNewName).toFile();
            File targetFileFullDirectory = Paths.get(inputFolderFullPath, jobName).toFile();
            FTPUtils.downloadFileFromFTP(sourceFileFullPath.toString(), targetFileFullDirectory.toString(), ftpConfigProperties);
            utFileConfig.setDownloadFilePath(Paths.get(inputFolderFullPath, jobName,sourceFileNewName).toAbsolutePath().toString());
            if(!validateFile(utFileConfig)) {
            	String msg = String.format("Fail to validate input file=%s", utFileConfig.getDownloadFilePath());
                logger.error(msg);
                throw new BatchException(BatchErrorCode.FILE_VALIDATION_ERROR,msg);
            }
    	}
    	updateStartAccountBatchInfo();
		return RepeatStatus.FINISHED;
	}

	@SneakyThrows
    private void updateStartAccountBatchInfo() {
    	AccountBatchInfo batchInfo = new AccountBatchInfo();
    	batchInfo.setStartTime(new Date());
    	batchInfo.setUpdatedTime(new Date());
    	batchInfo.setUpdatedBy( jobConfigProperties.getBatchCode() );
    	batchInfo.setAccountType(jobConfigProperties.getUtBatchAccountInfoKey());
    	int row= acctBatchInfoRepoImpl.updateUTBatchInfoStart(batchInfo);
        logger.info(String.format("Update Account Batch Info Start row=%s", row));
    }

    @SneakyThrows
    private boolean validateFile(UTFile utFile) {
    	boolean validated=false;
        logger.info(String.format("validating input file=%s", utFile.getDownloadFilePath()));
        File vFile = Paths.get(utFile.getDownloadFilePath()).toFile();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(vFile)))  {
			String header = bufferedReader.readLine();
			String detail="";
			int recCount=0;
			int totalCount=0;
			if( StringUtils.isBlank(header) ||  !header.startsWith(utFile.getHeaderPrefix())) {
				logger.error( String.format("Invalid file header format:%s",header));
		    	return validated;
			}
			while((detail = bufferedReader.readLine())!=null) {
				if (detail.startsWith(utFile.getDetailPrefix())) {
					recCount++;
				}else if(detail.startsWith(utFile.getTrailerPrefix())){
					logger.debug(String.format("trailer %s", detail));
					totalCount= Integer.parseInt( detail.substring(3).replace("|", ""));
				}
			}
			if(totalCount==recCount) {
				logger.debug( String.format("Total count in trailer %s, matches record count=%s",totalCount, recCount));
				validated=true;
			}
		}catch(Exception ex) {
			logger.error( String.format("Exception while validating file %s",utFile.getDownloadFilePath()), ex);
	    	return validated;
		}
    	return validated;
    }

}
