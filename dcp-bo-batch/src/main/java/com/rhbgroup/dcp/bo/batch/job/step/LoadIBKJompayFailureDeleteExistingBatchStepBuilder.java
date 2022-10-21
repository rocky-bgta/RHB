package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.LoadIBKJompayFailureDeleteExistingBatchTasklet;

@Component
@Lazy
public class LoadIBKJompayFailureDeleteExistingBatchStepBuilder extends BaseStepBuilder{

	@Autowired
	private LoadIBKJompayFailureDeleteExistingBatchTasklet deleteExistingBatchTasklet;

	@Override
	public Step buildStep() {
		return getDefaultStepBuilder("deleteJompayFailureExistingBatchStep")
                .listener(this.batchJobCommonStepListener)
				.tasklet(deleteExistingBatchTasklet)
				.build();
	}
	
}
