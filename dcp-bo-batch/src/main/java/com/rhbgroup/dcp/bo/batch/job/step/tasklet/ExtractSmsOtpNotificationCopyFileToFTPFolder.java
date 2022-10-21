package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExtractSmsOtpNotificationParameter.OUTPUT_FILE_LIST;

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
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractSmsOtpNotificationJobConfigProperties;

@Component
@Lazy
public class ExtractSmsOtpNotificationCopyFileToFTPFolder implements Tasklet {
	@Autowired
    private ExtractSmsOtpNotificationJobConfigProperties configProperties;

	@Autowired
	private FTPConfigProperties ftpConfigProperties;
	
    static final Logger logger = Logger.getLogger(ExtractSmsOtpNotificationCopyFileToFTPFolder.class);

	@SuppressWarnings("unchecked")
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		List<File> fileList = (List<File>) chunkContext.getStepContext().getJobExecutionContext().get(OUTPUT_FILE_LIST);
		if (fileList != null) {
			fileList.forEach(file -> {
				try {
					logger.info("Uploading file to FTP");
					logger.info("    FTP host: " + ftpConfigProperties.getHost());
					logger.info("    FTP port: " + ftpConfigProperties.getPort());
					logger.info("    username: " + ftpConfigProperties.getUsername());
					logger.info("    issecure: " + ftpConfigProperties.isIssecureftp());
					logger.info("    absolutePath: " + file.getAbsolutePath());
					logger.info("    ftpfolder: " + configProperties.getFtpfolder());
					FTPUtils.uploadFileToFTP(file.getAbsolutePath(), configProperties.getFtpfolder(), ftpConfigProperties);
				} catch (BatchException ex) {
					logger.error(ex);
				}
			});
		}

		return RepeatStatus.FINISHED;
	}
}
