package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.JMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.RunReportTasklet;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BillerDynamicPaymentFileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BillDynamicPaymentConfigOutboundRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.BillerDynamicPaymentFileJobStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.BillerDynamicPaymentFileWithCommaStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.BillerDynamicPaymentFileOutboundFtpTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.BillerDynamicPaymentFileUpdateFlagTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.BillerDynamicPaymentFileUpdatePathTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.BillerDynamicPaymentInjectReportIdTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.BillerDynamicPaymentRemoveQueueItemTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.BillerDynamicSendPaymentFileTasklet;

import javax.naming.NamingException;


@Configuration
@Lazy
public class BillerDynamicPaymentFileJobConfiguration extends BaseDBToFileJobConfiguration {

	static final Logger logger = Logger.getLogger(BillerDynamicPaymentFileJobConfiguration.class);
	private static final String JOB_NAME = "BillerDynamicPaymentFileJob";

	@Autowired
	private BillerDynamicPaymentFileJobStepBuilder billerDynamicPaymentFileJobStepBuilder;
	
	@Autowired
	private BillerDynamicPaymentFileWithCommaStepBuilder billerDynamicPaymentFileWithCommaStepBuilder;
	
	@Autowired
	private BillerDynamicPaymentInjectReportIdTasklet injectReportId;

	@Autowired
	private RunReportTasklet runReportTasklet;
	
	@Autowired
	private BillerDynamicPaymentRemoveQueueItemTasklet removeItemTasklet;
	
	@Autowired 
	private BillerDynamicPaymentFileUpdateFlagTasklet updateFlagTasklet;
	
	@Autowired 
	private BillerDynamicPaymentFileUpdatePathTasklet updatePathTasklet;

	@Autowired
	private BillerDynamicPaymentFileJobConfigProperties configProperties;
	
	@Autowired
	private FTPConfigProperties ftpConfig;
	
	@Autowired
	private BillerDynamicSendPaymentFileTasklet sendPaymentFileTasklet;
	
    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    
	private List<BillerDynamicPaymentOutboundConfig> listBillerConfig= new ArrayList<>();

	private  String auditQueueName="jms/queue/q_dcpbo_biller";

	@Bean("billerPaymentFileJMSConfig")
	public JMSConfig createJMSConfig(@Qualifier("BillerPaymentJMSConfigProperties") JMSConfigProperties jmsConfigProperties) throws NamingException {
		logger.info("billerPaymentFileJMSConfig" +jmsConfigProperties);
		return JMSUtils.setupJMS(null,auditQueueName , jmsConfigProperties);
	}

	@Bean("BillDynamicPaymentConfigOutboundQueue")
	public Queue<BillerDynamicPaymentOutboundConfig> billerConfig(BillDynamicPaymentConfigOutboundRepositoryImpl billConfigOutboundRepo){
		listBillerConfig = billConfigOutboundRepo.getBillerConfigOutbound();
		return (new ConcurrentLinkedQueue <>(listBillerConfig));
	}

	@Bean(JOB_NAME)
	public Job BuildJob(@Qualifier("BillDynamicPaymentConfigOutboundQueue") Queue<BillerDynamicPaymentOutboundConfig> queue )  {
		SimpleJobBuilder job = getDefaultJobBuilder(JOB_NAME);
		for (BillerDynamicPaymentOutboundConfig config : listBillerConfig) {
			logger.info(String.format("build step config code=%s, biller account=%s, biller name=%s",
					config.getBillerCode(), config.getBillerAccNo(), config.getBillerAccName()));
			String targetFileFolderPath = config.getFtpFolder();
			logger.info(String.format("target File Folder Path %s, ", targetFileFolderPath));
			
			job.next(this.billerDynamicPaymentFileJobStepBuilder.buildStep());
			job.next(copyFileToFTP(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,targetFileFolderPath));
			job.next(copyFileToFTP(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,configProperties.getMasterFtpFolder()));
			
			job.next(this.billerDynamicPaymentFileWithCommaStepBuilder.buildStep());
			job.next(copyFileToFTP(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,targetFileFolderPath));
			job.next(copyFileToFTP(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,configProperties.getMasterFtpFolder()));
			
			job.next(setReportId());
			job.next(runReport());
			job.next(copyFileToFTP(REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY, targetFileFolderPath));
			job.next(copyFileToFTP(REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY, configProperties.getMasterFtpFolder()));
			job.next(updateExecutePath());
			logger.info("Payment File Sending");

			job.next(sendPaymentFile());
			logger.info("Payment File Send");

			logger.info(String.format("target File Folder Path %s, ", targetFileFolderPath));

			job.next(updateExecuteFlag());
			job.next(removeQueueItem());
		}
		return  job.build();
	}

	private Step removeQueueItem() {
		return getStepBuilderFactory()
				.get("removeQueueItem")
				.tasklet(this.removeItemTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}
	
	private Step copyFileToFTP(String contextSourceFile, String targetFileFolderPath)  {
		BillerDynamicPaymentFileOutboundFtpTasklet copyFileFtpTasklet = new  BillerDynamicPaymentFileOutboundFtpTasklet();
		copyFileFtpTasklet.initPath(contextSourceFile, targetFileFolderPath);
		copyFileFtpTasklet.initConnection(ftpConfig);
		logger.info(String.format("Folder Creation Start %s", targetFileFolderPath));

		// Create the target FTP folder
		logger.info(String.format("Folder Creation End %s", targetFileFolderPath));

		return getStepBuilderFactory().get( "copyFileToFTP-".concat(contextSourceFile) )
				.tasklet(copyFileFtpTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}
	

	protected Step setReportId() {
		return getStepBuilderFactory().get("reportId")
				.tasklet(this.injectReportId)
				.listener(this.batchJobCommonStepListener)
				.build();
	}
	
	
	protected Step runReport() {
		return getStepBuilderFactory().get("runReport")
				.tasklet(this.runReportTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}
	
	protected Step updateExecutePath() {
		return getStepBuilderFactory().get("updateExecutePath")
				.tasklet(this.updatePathTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}
	
	protected Step sendPaymentFile() {
		return getStepBuilderFactory().get("sendPaymentFile")
				.tasklet(this.sendPaymentFileTasklet)
				.listener(this.batchJobCommonStepListener).build();
			}
	
	protected Step updateExecuteFlag() {
		return getStepBuilderFactory().get("updateExecuteFlag")
				.tasklet(this.updateFlagTasklet)
				.listener(this.batchJobCommonStepListener).build();
	}

}
