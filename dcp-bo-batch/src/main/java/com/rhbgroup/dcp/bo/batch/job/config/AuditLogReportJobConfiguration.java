package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_TARGET_PATH_KEY;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.AuditLogQueryTasklet;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyOutputFolderFileToFTPTasklet;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseRunReportJobConfiguration;

@Configuration
@Lazy
public class AuditLogReportJobConfiguration extends BaseRunReportJobConfiguration {

	private static final String JOB_NAME = "AuditLogReportJob";
	
	@Autowired
	FTPConfigProperties ftpConfigProperties;

    @Autowired
    private AuditLogQueryTasklet auditLogQueryTasklet;
	
	@Bean(name = JOB_NAME)
	public Job BuildJob() {
		SimpleJobBuilder jobBuilder = getDefaultRunReportJobBuilder(JOB_NAME)
				.next(runAuditLogQuery())
				.next(runReport())
				.next(sendToFTP());
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
	
	protected Step runAuditLogQuery() {
       return getStepBuilderFactory().get("runAuditLogQuery")
               .tasklet(this.auditLogQueryTasklet)
               .listener(this.batchJobCommonStepListener)
               .build();
	}
}
