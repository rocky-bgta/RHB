package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyFTPFileToInputFolderTasklet;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.RunAsnbReportTasklet;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseRunReportJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.AsnbFtpConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractASNBReconiliationReportJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.HostFtpConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.ASNBReconFileOutboundFtpTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.ASNBSettlementFileOutboundFtpTasklet;


@Configuration
@Lazy
public class AsnbReportJobConfiguration extends BaseRunReportJobConfiguration {

	private static final String JOB_NAME = "AsnbReportJob";

	@Autowired
	private RunAsnbReportTasklet runAsnbReportTasklet;

	@Autowired
	AsnbFtpConfigProperties asnbFtpConfigProperties;

	@Autowired
	HostFtpConfigProperties hostFtpConfigProperties;

	@Autowired
	FTPConfigProperties ftpConfigProperties;

	@Autowired
	private ExtractASNBReconiliationReportJobConfigProperties jobConfigProperties;

	@Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
	private String inputFolderFullPath;
	
	@Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
	private String localOutputFolderFullPath;

	@Bean(name = JOB_NAME)
	public Job buildJob() {
		SimpleJobBuilder jobBuilder = getDefaultRunReportJobBuilder(JOB_NAME)
				 .next(getFromFTP())
				 .next(runAsnbReportTasklet())
				 .next(sendReconFilesToPNB())
				 .next(sendSettlementToHostFTP())
				 .next(sendReconToEChannelFTP());
		return jobBuilder.build();
	}

	@Override
	protected Step runAsnbReportTasklet() {
		return getStepBuilderFactory().get("runAsnbReport").tasklet(this.runAsnbReportTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}

	protected Step getFromFTP() {
		CopyFTPFileToInputFolderTasklet downloadASNPReconciliationTasklet = new CopyFTPFileToInputFolderTasklet();
		downloadASNPReconciliationTasklet.initAsnbFtpConfig(asnbFtpConfigProperties);
		downloadASNPReconciliationTasklet.initInputFolderPath(inputFolderFullPath);
		downloadASNPReconciliationTasklet.init(jobConfigProperties.getFtpFolder(), jobConfigProperties.getName(),
				jobConfigProperties.getNameDateFormat(), true, jobConfigProperties.getChannelType());
		return getStepBuilderFactory().get("downloadReconciliationReportFromFTP")
				.tasklet(downloadASNPReconciliationTasklet).listener(this.batchJobCommonStepListener).build();
	}
	
	/**
	 * This is for the step for send Settlement file to Host FTP server
	 * @return TaskletStep
	 */

	protected Step sendSettlementToHostFTP() {
		ASNBSettlementFileOutboundFtpTasklet sendASNBSettlementFilesToFTPTasklet = new ASNBSettlementFileOutboundFtpTasklet();
		sendASNBSettlementFilesToFTPTasklet.initFTPConfig(hostFtpConfigProperties);
		sendASNBSettlementFilesToFTPTasklet.init(localOutputFolderFullPath, jobConfigProperties.getHostfolder());
		return getStepBuilderFactory().get("sendSettlementToHostFTP").tasklet(sendASNBSettlementFilesToFTPTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}
	
	/**
	 * This is for the step for send Recon file to Echannel FTP server
	 * @return TaskletStep
	 */

	protected Step sendReconToEChannelFTP() {
		
		
		ASNBReconFileOutboundFtpTasklet sendASNBFilesToFTPTasklet = new ASNBReconFileOutboundFtpTasklet();
		sendASNBFilesToFTPTasklet.initFTPConfig(ftpConfigProperties);
		sendASNBFilesToFTPTasklet.init(jobConfigProperties.getEchannelFtpFolder(), localOutputFolderFullPath);
		return getStepBuilderFactory().get("sendReconToEChannelFTP").tasklet(sendASNBFilesToFTPTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}
	
	/**
	 * This is for the step for send Recon file to PNB FTP server
	 * @return TaskletStep
	 */
	private Step sendReconFilesToPNB() {
	
		ASNBReconFileOutboundFtpTasklet sendASNBFilesToFTPTasklet = new ASNBReconFileOutboundFtpTasklet();
		sendASNBFilesToFTPTasklet.initFTPConfig(asnbFtpConfigProperties);
		sendASNBFilesToFTPTasklet.init(jobConfigProperties.getOutputfolder(), localOutputFolderFullPath);
		return getStepBuilderFactory().get("sendReconFilesToPNB").tasklet(sendASNBFilesToFTPTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}

}
