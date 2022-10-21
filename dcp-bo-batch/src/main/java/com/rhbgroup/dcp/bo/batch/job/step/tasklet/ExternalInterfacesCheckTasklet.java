package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_REPORT_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.RunReportTasklet;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.JMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExternalInterfacesCheckTaskletJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKPrepaidConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.repository.UserProfileRepositoryImpl;

@Component
@Lazy
public class ExternalInterfacesCheckTasklet implements Tasklet, InitializingBean {

    private static final Logger logger = Logger.getLogger(ExternalInterfacesCheckTasklet.class);
    
    static final String EXTERNAL_INTERFACE_CHECK_REPORT_ID ="DMBBT001";
    
	@Value("${dcp.bo.batch.inputfolder.path}")
	private String targetFileFolder;

	@Autowired
	private ExternalInterfacesCheckTaskletJobConfigProperties jobConfigProperties;
	
	@Autowired
    private FTPConfigProperties ftpDCPConfigProperties;
	
	@Autowired
    private FTPIBKConfigProperties ftpIBKConfigProperties;
	
	@Autowired
    private FTPIBKPrepaidConfigProperties ftpIBKPrepaidConfigProperties;
	
	@Autowired
	private UserProfileRepositoryImpl userProfileRepositoryImpl;
	
	@Autowired
	private BatchParameterRepositoryImpl batchParameterRepository;
	
	@Autowired
	private RunReportTasklet runReportTasklet;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));
    	
    	String jasperSourceFile = null;
    	String jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY);

		
		// Task 2: Check Jasper Server connectivity
    	jasperSourceFile = checkJasperAccess(contribution, chunkContext);
		
		// Task 3: Upload generated JasperReport file to DCP SFTP folder
    	checkDCPSFTP(jasperSourceFile);
		
		// Task 5: Check connectivity and download from IBK Non-SecureFTP
    	checkIBKFTP(jobName);

		// Task 6: Check connectivity and download from IBK SecureFTP
    	checkIBKSFTP(jobName);
    	
        return RepeatStatus.FINISHED;
    }
    
    private String checkJasperAccess(StepContribution contribution, ChunkContext chunkContext) {
    	String jasperSourceFile = null;
    	logger.info(String.format("Checking Jasper Server connectivity isEnabled::[%b]", jobConfigProperties.isEnableJasperCheckConnectivity()));
		if(jobConfigProperties.isEnableJasperCheckConnectivity()) {
			logger.info("Start checking Jasper Server connectivity");
			try {				
				// We need to add 1 day to current date because RunReportTasklet will -1 day of the batch.system.date
				String originalDate = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
				Date updateDate = DateUtils.addDays(new Date(), 1);
				String batchSystemDate = DateUtils.formatDateString( updateDate, BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT);
				logger.info(String.format("Update [%s] in context with new values [%s]", BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, updateDate));
				chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, batchSystemDate);
				
				String reportId = EXTERNAL_INTERFACE_CHECK_REPORT_ID;
				logger.info(String.format("Setting ReportId [%s] to execute in context", reportId));
				chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_REPORT_ID_KEY, reportId);
				
				logger.info(String.format("Executing RunReportTasklet to generate report with ReportId [%s]", reportId));
				runReportTasklet.execute(contribution, chunkContext);
				jasperSourceFile = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY);
				logger.info(String.format ("Generated Jasper Report filepath [%s]", jasperSourceFile));
				
				logger.info(String.format("Revert [%s] in context with original values [%s]", BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, originalDate));
				chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, originalDate);
			} catch (Exception e) {
				logger.error("Failed to check Jasper Server connectivity", e);
			}
			logger.info("Finished checking Jasper Server connectivity");
		}
		return jasperSourceFile;
    }
    
    private void checkDCPSFTP(String jasperSourceFile) {
    	logger.info(String.format("Checking DCP SFTP connectivity isEnabled::[%b]", jobConfigProperties.isEnableFileSendToFtpCheck()));
		if(jobConfigProperties.isEnableFileSendToFtpCheck()) {
			logger.info("Start checking DCP SFTP connectivity");
			try {
				logger.info(String.format("Uploading file [%s] to target FTP folder [%s] under SFTP server [%s:%d]", 
					jasperSourceFile, jobConfigProperties.getFtpDcpTargetFolderPath(), ftpDCPConfigProperties.getHost(), ftpDCPConfigProperties.getPort()));
				FTPUtils.uploadFileToFTP(jasperSourceFile, jobConfigProperties.getFtpDcpTargetFolderPath(), ftpDCPConfigProperties);
				logger.info(String.format("File [%s] uploaded to target FTP folder [%s] under SFTP server [%s:%d] successfully", 
					jasperSourceFile, jobConfigProperties.getFtpDcpTargetFolderPath(), ftpDCPConfigProperties.getHost(), ftpDCPConfigProperties.getPort()));
			} catch (Exception e) {
				logger.error("Failed to check DCP SFTP connectivity", e);
			}
			logger.info("Finished checking DCP SFTP connectivity");
		}
    }
    
    private void checkIBKFTP(String jobName) {
    	logger.info(String.format("Checking connectivity from IBK Non-SecureFTP isEnabled::[%b]", jobConfigProperties.isEnableIBKFtpCheckConnectivity()));
		if(jobConfigProperties.isEnableIBKFtpCheckConnectivity()) {
			logger.info("Start checking connectivity and download for IBK Non-SecureFTP");
			try {
				// List FTP files
				logger.info(String.format("List file in FTP folder [%s] under IBK Non-SecureFTP [%s:%d]", jobConfigProperties.getFtpIBKFoldertolist(), ftpIBKConfigProperties.getHost(), ftpIBKConfigProperties.getPort()));
				List<String> fileList = FTPUtils.listFilesFromFtpFolder(jobConfigProperties.getFtpIBKFoldertolist(), ftpIBKConfigProperties);
				logger.info(String.format("Total [%d] FTP files found in [%s] under IBK Non-SecureFTP [%s:%d]", fileList.size(), jobConfigProperties.getFtpIBKFoldertolist(), ftpIBKConfigProperties.getHost(), ftpIBKConfigProperties.getPort()));
				// No need for UAT and PROD env (data sensitive)
				logger.info(String.format("Checking download from IBK Non-SecureFTP isEnabled::[%b]", jobConfigProperties.isEnableIBKFtpDownloadCheck()));
				if(jobConfigProperties.isEnableIBKFtpDownloadCheck() && !fileList.isEmpty()) {
					String filename = fileList.get(0) ;
					String ftpTargetFile = Paths.get(jobConfigProperties.getFtpIBKFoldertolist(), filename).toString();
					String localDownloadPath = Paths.get(targetFileFolder, jobName, filename).toString();
					logger.info(String.format ("Download FTP file [%s] from FTP folder [%s] to target folder [%s] in IBK Non-SecureFTP [%s:%d]", filename, ftpTargetFile, localDownloadPath, ftpIBKConfigProperties.getHost(), ftpIBKConfigProperties.getPort()));
					FTPUtils.downloadFileFromFTP(ftpTargetFile, localDownloadPath, ftpIBKConfigProperties);
					logger.info(String.format ("Finished download FTP file [%s] from FTP folder [%s] to target folder IBK Non-SecureFTP [%s] in [%s:%d]", filename, ftpTargetFile, localDownloadPath, ftpIBKConfigProperties.getHost(), ftpIBKConfigProperties.getPort()));
				}
			} catch (Exception e) {
				logger.error("Failed to check connectivity or download from IBK Non-SecureFTP", e);
			}
			logger.info("Finished checking connectivity and download from IBK Non-SecureFTP");
		}
    }
    
    private void checkIBKSFTP(String jobName) {
    	logger.info(String.format("Checking connectivity from IBK SecureFTP isEnabled::[%b]", jobConfigProperties.isEnableIBKSftpCheckConnectivity()));
		if(jobConfigProperties.isEnableIBKSftpCheckConnectivity()) {
			logger.info("Start checking connectivity and download for IBK SecureFTP");
			try {
				// List FTP files
				logger.info(String.format("List file in FTP folder [%s] under IBK SecureFTP [%s:%d]", jobConfigProperties.getFtpIBKPrepaidFoldertolist(), ftpIBKPrepaidConfigProperties.getHost(), ftpIBKPrepaidConfigProperties.getPort()));
				List<String> fileList = FTPUtils.listFilesFromFtpFolder(jobConfigProperties.getFtpIBKPrepaidFoldertolist(), ftpIBKPrepaidConfigProperties );
				logger.info(String.format("Total [%d] FTP files found in [%s] under IBK SecureFTP [%s:%d]", fileList.size(), jobConfigProperties.getFtpIBKPrepaidFoldertolist(), ftpIBKPrepaidConfigProperties.getHost(), ftpIBKPrepaidConfigProperties.getPort()));
				// No need for UAT and PROD env (data sensitive)
				logger.info(String.format("Checking download from IBK SecureFTP isEnabled::[%b]", jobConfigProperties.isEnableIBKSftpDownloadCheck()));
				if(jobConfigProperties.isEnableIBKSftpDownloadCheck() && !fileList.isEmpty()) {
					String filename = fileList.get(0) ;
					String ftpTargetFile = Paths.get(jobConfigProperties.getFtpIBKPrepaidFoldertolist(), filename).toString();
					String localDownloadPath = Paths.get(targetFileFolder, jobName).toString();
					logger.info(String.format ("Download FTP file [%s] from FTP folder [%s] to target folder [%s] in IBK SecureFTP [%s:%d]", filename, ftpTargetFile, localDownloadPath, ftpIBKPrepaidConfigProperties.getHost(), ftpIBKPrepaidConfigProperties.getPort()));
					FTPUtils.downloadFileFromFTP(ftpTargetFile, localDownloadPath, ftpIBKPrepaidConfigProperties);
					logger.info(String.format ("Finished download FTP file [%s] from FTP folder [%s] to target folder IBK SecureFTP [%s] in [%s:%d]", filename, ftpTargetFile, localDownloadPath, ftpIBKPrepaidConfigProperties.getHost(), ftpIBKPrepaidConfigProperties.getPort()));
				}	
			} catch (Exception e) {
				logger.error("Failed to check connectivity or download from IBK SecureFTP", e);
			}
			logger.info("Finished checking connectivity and download from IBK SecureFTP");
		}
    }
    
	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing for now
	}
	
}
