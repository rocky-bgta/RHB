package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_TARGET_PATH_KEY;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyOutputFolderFileToFTPTasklet;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseRunReportJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.SendEmailOlaCasaTasklet;

@Configuration
@Lazy
public class RunReportJobConfiguration extends BaseRunReportJobConfiguration {

	private static final String JOB_NAME = "RunReportJob";
	
	@Autowired
	FTPConfigProperties ftpConfigProperties;
	
	@Autowired
    private SendEmailOlaCasaTasklet sendEmailOlaCasaTasklet;
	
	@Bean(name = JOB_NAME)
	public Job BuildJob() {
		SimpleJobBuilder jobBuilder = getDefaultRunReportJobBuilder(JOB_NAME)
				.next(runReport())
				.next(sendToFTP())
				.next(sendEmail());
		return jobBuilder.build();
	}
	
	protected Step sendToFTP() {
		CopyOutputFolderFileToFTPTasklet copyOutputFolderFileToFTPTasklet = new CopyOutputFolderFileToFTPTasklet();
		copyOutputFolderFileToFTPTasklet.initFTPConfig(ftpConfigProperties);
		copyOutputFolderFileToFTPTasklet.initByContext(REPORT_JOB_PARAMETER_REPORT_TARGET_PATH_KEY);
		return getStepBuilderFactory().get("sendToFTP")
               .tasklet(copyOutputFolderFileToFTPTasklet)
               .listener(this.batchJobCommonStepListener)
               .build();
    }
	
	protected Step sendEmail() {
	       return getStepBuilderFactory().get("sendEmail")
	               .tasklet(this.sendEmailOlaCasaTasklet)
	               .listener(this.batchJobCommonStepListener)
	               .build();
	    }
}
