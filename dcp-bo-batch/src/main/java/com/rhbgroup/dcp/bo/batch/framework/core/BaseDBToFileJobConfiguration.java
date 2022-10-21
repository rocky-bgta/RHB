package com.rhbgroup.dcp.bo.batch.framework.core;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyOutputFolderFileToFTPTasklet;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Configuration
public class BaseDBToFileJobConfiguration extends BaseJobConfiguration {
    static final Logger logger = Logger.getLogger(BaseDBToFileJobConfiguration.class);

    private static final String MOVE_LOCAL_FILE_TO_FTP_STEP_NAME = "MoveLocalFileToFTPStep";
    
    @Autowired
    protected CopyOutputFolderFileToFTPTasklet moveLocalFileToFTPTasklet;

    @Override
    protected Step readBatchParameters() {
        return getStepBuilderFactory().get("readBatchParameters")
                .tasklet(this.readBatchParameterFromDBTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }

    protected Step moveLocalFileToFTP(String targetFileFolder, String  targetFileName, String targetFileDateFormat) {
    	logger.info(String.format("Building step [%s]", MOVE_LOCAL_FILE_TO_FTP_STEP_NAME));
    	
    	logger.debug(String.format("Setup CopyOutputFolderFileToFTPTasklet tasklet using TargetFolder [%s] TargetFileName [%s] TargetDateFormat [%s]", targetFileFolder, targetFileName, targetFileDateFormat));
        this.moveLocalFileToFTPTasklet.init(targetFileFolder, targetFileName, targetFileDateFormat);

        Step step = getStepBuilderFactory().get(MOVE_LOCAL_FILE_TO_FTP_STEP_NAME)
                .tasklet(this.moveLocalFileToFTPTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
        	
        logger.info(String.format("[%s] step build successfully", MOVE_LOCAL_FILE_TO_FTP_STEP_NAME));
        return step;
    }

}
