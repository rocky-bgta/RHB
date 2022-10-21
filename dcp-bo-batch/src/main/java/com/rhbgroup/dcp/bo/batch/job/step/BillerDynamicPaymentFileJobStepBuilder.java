package com.rhbgroup.dcp.bo.batch.job.step;


import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class BillerDynamicPaymentFileJobStepBuilder extends  BillerDynamicPaymentFileJobBaseStepBuilder {
	static final Logger logger = Logger.getLogger(BillerDynamicPaymentFileJobStepBuilder.class);
    
    @Override
    @Bean
    public Step buildStep() {		
		return super.buildStep();
    }

}
