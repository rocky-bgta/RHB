package com.rhbgroup.dcp.bo.batch.job.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.LoadEMUnitTrustFinalUpdateTasklet;

@Lazy
@Component
public class LoadEMUnitTrustFinalUpdateStepBuilder extends BaseStepBuilder{
	private static final String STEP_NAME = "LoadEMUnitTrustFinalUpdateStep";
	
	@Autowired
	private LoadEMUnitTrustFinalUpdateTasklet finalUpdateTasklet;
	
	@Override
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME).tasklet(finalUpdateTasklet).build();
	}

}
