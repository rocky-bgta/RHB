package com.rhbgroup.dcp.bo.batch.job.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.MoveFiletoSuccessOrFailedFolderTasklet;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;

@Component
@Lazy
public class MoveIBKBillerPaymentFileToSuccessFolderStepBuilder extends BaseStepBuilder {
	
	private static final Logger logger = Logger.getLogger(MoveIBKBillerPaymentFileToSuccessFolderStepBuilder.class);
	
	private static final String STEP_NAME = "MoveIBKBillerPaymentFileToSuccessFolderStep";
	
	@Autowired
	private MoveFiletoSuccessOrFailedFolderTasklet moveFiletoSuccessOrFailedFolderTasklet;
    
	@Override
    public Step buildStep() {
    	logger.info(String.format("Building step [%s]", STEP_NAME));
    	
    	Step step = getDefaultStepBuilder(STEP_NAME)
                .tasklet(moveFiletoSuccessOrFailedFolderTasklet)
                .build();
    	
    	logger.info(String.format("[%s] step build successfully", STEP_NAME));
    	return step;
    }
    
}
