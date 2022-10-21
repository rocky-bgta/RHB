package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyFTPFileToInputFolderTasklet;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadTermDepositJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.LoadTermDepositProductFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadTermDepositProductValidatorFileStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateDepositProductFromStaginDepositProductStepBuilder;

@Component
@Lazy
public class LoadTermDepositJobConfiguration extends BaseFileToDBJobConfiguration {
	
	private static final Logger logger = Logger.getLogger(LoadTermDepositJobConfiguration.class);
	private static final String JOB_NAME = "LoadTermDepositJob";
	
	@Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
	private String inputFolderFullPath;
	
	@Autowired
	private LoadTermDepositJobConfigProperties loadTermDepositJobConfigProperties;
	
	@Autowired
	private LoadTermDepositProductValidatorFileStepBuilder validatorStep;

	@Autowired
	private LoadTermDepositProductFileToStagingStepBuilder loadFileStep;

	@Autowired
	private FTPConfigProperties ftpConfigProperties;
	
	@Autowired
	private UpdateDepositProductFromStaginDepositProductStepBuilder updateDeositProductStep;

	@Bean(name = JOB_NAME)
	public Job buildJob() {
		logger.info(String.format("Building job %s", JOB_NAME));
		SimpleJobBuilder job = getDefaultFileToDBJobBuilder(JOB_NAME);
		String sourceFileFolder = loadTermDepositJobConfigProperties.getFtpFolder();
		String sourceFileName = loadTermDepositJobConfigProperties.getName();
		String sourceFileDateFormat = loadTermDepositJobConfigProperties.getNameDateFormat();
		logger.info(String.format("sourceFileName %s", sourceFileName));
		logger.info(String.format("sourceFileFolder %s", sourceFileFolder));
		logger.info(String.format("sourceFileDateFormat %s", sourceFileDateFormat));
		Step copyFtpFileStep = downloadLoadTermDepositProductFile(sourceFileFolder, sourceFileName,
				sourceFileDateFormat);
		job.next(copyFtpFileStep);
		job.next(validatorStep.buildStep());
		job.next(loadFileStep.buildStep());
		job.next(updateDeositProductStep.buildStep());
		return job.build();
	}

	private Step downloadLoadTermDepositProductFile(String sourceFileFolder, String sourceFileName,
			String sourceFileDateFormat) {
		CopyFTPFileToInputFolderTasklet downloadLoadTermDepositFileTasklet = new CopyFTPFileToInputFolderTasklet();
		downloadLoadTermDepositFileTasklet.initFTPConfig(ftpConfigProperties);
		downloadLoadTermDepositFileTasklet.initInputFolderPath(inputFolderFullPath);
		downloadLoadTermDepositFileTasklet.init(sourceFileFolder, sourceFileName, sourceFileDateFormat);
		downloadLoadTermDepositFileTasklet.initDayDiff(1);
		return getStepBuilderFactory().get("downloadLoadTermDepositProductFromFTP")
				.tasklet(downloadLoadTermDepositFileTasklet).listener(this.batchJobCommonStepListener).build();
	}

}
