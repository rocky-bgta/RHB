package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.GetIBKBillerDynamicPaymentReportTasklet;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class GetIBKBillerDynamicPaymentReportStepBuilder extends BaseStepBuilder{

	private static final Logger logger = Logger.getLogger(GetIBKBillerDynamicPaymentReportStepBuilder.class);
	
	private static final String STEP_NAME = "GetIBKBillerDynamicPaymentReportStep";
	
	@Autowired
	private GetIBKBillerDynamicPaymentReportTasklet getIBKBillerDynamicPaymentReportTasklet;

    @Override
	@Bean
    public Step buildStep() {
    	logger.info(String.format("Building step [%s]", STEP_NAME));
    	Step step = getDefaultStepBuilder(STEP_NAME)
            .tasklet(getIBKBillerDynamicPaymentReportTasklet)
            .build();
    	
    	logger.info(String.format("[%s] step build successfully", STEP_NAME));
    	return step;
    }
    


}
