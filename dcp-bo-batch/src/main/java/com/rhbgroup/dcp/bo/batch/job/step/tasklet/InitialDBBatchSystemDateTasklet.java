package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.ReadBatchParameterFromDBTasklet;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;

@Component
@Lazy
public class InitialDBBatchSystemDateTasklet implements Tasklet, InitializingBean {
	private static final String ENVIRONMENT_VARIABLE_NAME = "BATCH_SYSTEM_DATE";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
    private static final Logger logger = Logger.getLogger(ReadBatchParameterFromDBTasklet.class);
    @Autowired
    private BatchParameterRepositoryImpl batchParameterRepository;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("Initializing DB Batch System Date..");
        batchParameterRepository.updateBatchSystemDate(getDate());
        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Do nothing because this is the implementation of Spring Batch
    }

    private Date getDate() {
    	logger.info("getDate()");
    	
    	Date date = null;
    	
    	/*
    	 * Get date from environment variable, if set
    	 */
    	try {
    		date = DateUtils.getDateFromString(System.getenv(ENVIRONMENT_VARIABLE_NAME), DATE_FORMAT);
    		logger.info("    Using date from " + ENVIRONMENT_VARIABLE_NAME + " environment variable: " + date);
    		return date;
    	} catch (ParseException | NullPointerException e) {
    	    String errorMessage = "InitialDBBatchSystemDateTasklet get Date exception";
            logger.error(errorMessage, e);
        }
    	
    	date = new Date();
    	logger.info("    Using current system date: " + date);

    	return date;
    }
}
