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
import com.rhbgroup.dcp.bo.batch.job.config.properties.BlacklistedMsicJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadTermDepositJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.BlacklistedMsicFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.BlacklistedMsicValidatorFileStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadTermDepositProductFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadTermDepositProductValidatorFileStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateDepositProductFromStaginDepositProductStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateMsicConfigFromStaginMsicConfigStepBuilder;

@Component
@Lazy
public class BlacklistedMsicJobConfiguration extends BaseFileToDBJobConfiguration {
	
	private static final Logger logger = Logger.getLogger(BlacklistedMsicJobConfiguration.class);
	private static final String JOB_NAME = "BlacklistedMsicJob";
	
	@Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
	private String inputFolderFullPath;
	
	@Autowired
	private BlacklistedMsicJobConfigProperties blacklistedMsicJobConfigProperties;
	
	@Autowired
	private BlacklistedMsicValidatorFileStepBuilder validatorStep;

	@Autowired
	private BlacklistedMsicFileToStagingStepBuilder loadFileStep;

	@Autowired
	private FTPConfigProperties ftpConfigProperties;
	
	@Autowired
	private UpdateMsicConfigFromStaginMsicConfigStepBuilder updateMsicConfigStep;

	@Bean(name = JOB_NAME)
	public Job buildJob() {
		logger.info(String.format("Building job %s", JOB_NAME));
		SimpleJobBuilder job = getDefaultFileToDBJobBuilder(JOB_NAME);
		String sourceFileFolder = blacklistedMsicJobConfigProperties.getFtpFolder();
		String sourceFileName = blacklistedMsicJobConfigProperties.getName();
		String sourceFileDateFormat = blacklistedMsicJobConfigProperties.getNameDateFormat();
		logger.info(String.format("sourceFileName %s", sourceFileName));
		logger.info(String.format("sourceFileFolder %s", sourceFileFolder));
		logger.info(String.format("sourceFileDateFormat %s", sourceFileDateFormat));
		Step copyFtpFileStep = downloadBlacklistedMsicConfigFile(sourceFileFolder, sourceFileName,
				sourceFileDateFormat);
		job.next(copyFtpFileStep);
		job.next(validatorStep.buildStep());
		job.next(loadFileStep.buildStep());
		job.next(updateMsicConfigStep.buildStep());
		return job.build();
	}

	private Step downloadBlacklistedMsicConfigFile(String sourceFileFolder, String sourceFileName,
			String sourceFileDateFormat) {
		CopyFTPFileToInputFolderTasklet downloadMsicConfigFileTasklet = new CopyFTPFileToInputFolderTasklet();
		downloadMsicConfigFileTasklet.initFTPConfig(ftpConfigProperties);
		downloadMsicConfigFileTasklet.initInputFolderPath(inputFolderFullPath);
		downloadMsicConfigFileTasklet.init(sourceFileFolder, sourceFileName, sourceFileDateFormat);
		downloadMsicConfigFileTasklet.initDayDiff(1);
		return getStepBuilderFactory().get("downloadBlacklistedMsicConfigFromFTP")
				.tasklet(downloadMsicConfigFileTasklet).listener(this.batchJobCommonStepListener).build();
	}

}
