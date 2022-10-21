
package com.rhbgroup.dcp.bo.batch.framework.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyFTPFileToInputFolderTasklet;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.MoveFiletoSuccessOrFailedFolderTasklet;

@Getter
@NoArgsConstructor
@Configuration
public class BaseFileToDBJobConfiguration extends BaseJobConfiguration {

    @Autowired
    private MoveFiletoSuccessOrFailedFolderTasklet moveFiletoSuccessOrFailedFolderTasklet;
    @Autowired
    private CopyFTPFileToInputFolderTasklet copyFTPFileToLocalTasklet;

    protected Step copyFTPFileToLocal(String sourceFileFolder, String sourceFileName, String sourceFileDateFormat) {
        this.copyFTPFileToLocalTasklet.init(sourceFileFolder,sourceFileName,sourceFileDateFormat);
        return getStepBuilderFactory().get("copyFTPFileToLocal")
                .tasklet(this.copyFTPFileToLocalTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }
    
	protected Step copyFTPFileToLocal(String sourceFileFolder, String sourceFileName, String sourceFileDateFormat, int dayDiff) {
		this.copyFTPFileToLocalTasklet.initDayDiff(dayDiff);
		return copyFTPFileToLocal(sourceFileFolder, sourceFileName, sourceFileDateFormat);
	}

    protected SimpleJobBuilder getDefaultFileToDBJobBuilder(String jobName)
    {
        return getJobBuilderFactory().get(jobName)
                .incrementer(getDefaultIncrementer())
                .start(readBatchParameters())
                .listener(this.commonExecutionListener);
    }

    protected Step moveFiletoSuccessOrFailedFolder() {
        return getStepBuilderFactory().get("moveFiletoSuccessOrFailedFolder")
                .tasklet(this.moveFiletoSuccessOrFailedFolderTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }
}
