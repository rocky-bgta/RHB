package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExtractDailyDataParameter.OUTPUT_FILE_LIST_FIRST_TIME_LOGIN;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExtractDailyDataParameter.OUTPUT_FILE_LIST_LOGIN;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExtractDailyDataParameter.OUTPUT_FILE_LIST_POD;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractDailyFirstTimeLoginJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractDailyLoginJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractDailyPodJobConfigProperties;

@Component
@Lazy
public class ExtractDailyDataCopyFileToFTPFolder implements Tasklet {
	@Autowired
    private ExtractDailyFirstTimeLoginJobConfigProperties firstTimeLoginConfigProperties;

	@Autowired
    private ExtractDailyLoginJobConfigProperties loginConfigProperties;

	@Autowired
    private ExtractDailyPodJobConfigProperties podConfigProperties;
	
	@Autowired
	private FTPConfigProperties ftpConfigProperties;
	
    static final Logger logger = Logger.getLogger(ExtractDailyDataCopyFileToFTPFolder.class);

	@SuppressWarnings("unchecked")
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		List<File> fileList = (List<File>) chunkContext.getStepContext().getJobExecutionContext().get(OUTPUT_FILE_LIST_FIRST_TIME_LOGIN);
		sendFilesToFtpServer(fileList, firstTimeLoginConfigProperties.getFtpfolder());

		fileList = (List<File>) chunkContext.getStepContext().getJobExecutionContext().get(OUTPUT_FILE_LIST_LOGIN);
		sendFilesToFtpServer(fileList, loginConfigProperties.getFtpfolder());

		fileList = (List<File>) chunkContext.getStepContext().getJobExecutionContext().get(OUTPUT_FILE_LIST_POD);
		sendFilesToFtpServer(fileList, podConfigProperties.getFtpfolder());

		return RepeatStatus.FINISHED;
	}
	
	private void sendFilesToFtpServer(List<File> fileList, String destFolder) {
		if (fileList != null) {
			fileList.forEach(file -> {
				try {
					logger.info("Uploading file to FTP");
					logger.info("    FTP host: " + ftpConfigProperties.getHost());
					logger.info("    FTP port: " + ftpConfigProperties.getPort());
					logger.info("    username: " + ftpConfigProperties.getUsername());
					logger.info("    issecure: " + ftpConfigProperties.isIssecureftp());
					logger.info("    absolutePath: " + file.getAbsolutePath());
					logger.info("    destFolder: " + destFolder);
					FTPUtils.uploadFileToFTP(file.getAbsolutePath(), destFolder, ftpConfigProperties);
				} catch (BatchException ex) {
					logger.error(ex);
				}
			});
		}
	}
}
