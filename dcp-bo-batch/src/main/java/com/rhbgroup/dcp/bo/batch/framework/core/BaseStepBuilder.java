package com.rhbgroup.dcp.bo.batch.framework.core;

import java.text.ParseException;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.rhbgroup.dcp.bo.batch.framework.core.mapping.SkipEmptyLineMapper;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;

import com.rhbgroup.dcp.bo.batch.framework.common.listener.BatchJobCommonStepListener;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;

import lombok.Getter;

public abstract class BaseStepBuilder {
	
	private static final Logger logger = Logger.getLogger(BaseStepBuilder.class);
	
    @Autowired
    @Getter
    protected BatchJobCommonStepListener batchJobCommonStepListener;
    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    public abstract Step buildStep();

    protected LineTokenizer getFixedLengthTokenizer(String fileContentNames, String fileContentColumns) {
        List<Range> columns=new ArrayList<>();
        for(String column : fileContentColumns.split(",",-1)){
            String[] rangeValue=column.split("-",-1);
            columns.add(new Range(Integer.parseInt(rangeValue[0]),Integer.parseInt(rangeValue[1])));
        }
        Range[] columnsArr = new Range[columns.size()];
        columnsArr = columns.toArray(columnsArr);

        String[] names=fileContentNames.split(",", -1);
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setColumns(columnsArr);
        tokenizer.setNames(names);
        return tokenizer;
    }

    protected <T>LineMapper getDefaultFixedLengthLineMapper(Class<T> tClass, String fileContentNames, String fileContentColumns, FieldSetMapper<T> fieldSetMapper)  {
        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();

        if(fieldSetMapper==null)
        {
            BeanWrapperFieldSetMapper<T> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<T>();
            beanWrapperFieldSetMapper.setTargetType(tClass);
            lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        }
        else
        {
            lineMapper.setFieldSetMapper(fieldSetMapper);
        }

        lineMapper.setLineTokenizer(getFixedLengthTokenizer(fileContentNames, fileContentColumns));
        return lineMapper;
    }
    
    protected <T>LineMapper getSkipFixedLengthLineMapper(Class<T> tClass, String fileContentNames, String fileContentColumns, FieldSetMapper<T> fieldSetMapper)  {
    	SkipEmptyLineMapper<T> lineMapper = new SkipEmptyLineMapper<>();

        if(fieldSetMapper==null)
        {
            BeanWrapperFieldSetMapper<T> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<T>();
            beanWrapperFieldSetMapper.setTargetType(tClass);
            lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        }
        else
        {
            lineMapper.setFieldSetMapper(fieldSetMapper);
        }

        lineMapper.setLineTokenizer(getFixedLengthTokenizer(fileContentNames, fileContentColumns));
        return lineMapper;
    }

    protected LineTokenizer getDelimitedTokenizer(String fileContentNames, String delimiter) {

        String[] names=fileContentNames.split(",", -1);
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(delimiter);
        tokenizer.setNames(names);
        return tokenizer;
    }

    protected <T>LineMapper getDefaultDelimitedLineMapper(Class<T> tClass, String fileContentNames, String delimiter, FieldSetMapper<T> fieldSetMapper)  {
        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();

        if(fieldSetMapper==null)
        {
            BeanWrapperFieldSetMapper<T> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<T>();
            beanWrapperFieldSetMapper.setTargetType(tClass);
            lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        }
        else
        {
            lineMapper.setFieldSetMapper(fieldSetMapper);
        }

        lineMapper.setLineTokenizer(getDelimitedTokenizer(fileContentNames, delimiter));
        return lineMapper;
    }

    protected <T> SkipEmptyLineMapper getSkipEmptyDelimitedLineMapper(Class<T> tClass, String fileContentNames, String delimiter, FieldSetMapper<T> fieldSetMapper)  {
        SkipEmptyLineMapper<T> lineMapper = new SkipEmptyLineMapper<>();

        if(fieldSetMapper==null)
        {
            BeanWrapperFieldSetMapper<T> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<T>();
            beanWrapperFieldSetMapper.setTargetType(tClass);
            lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        }
        else
        {
            lineMapper.setFieldSetMapper(fieldSetMapper);
        }

        lineMapper.setLineTokenizer(getDelimitedTokenizer(fileContentNames, delimiter));
        return lineMapper;
    }

    protected StepBuilder getDefaultStepBuilder(String stepName)
    {
        return stepBuilderFactory.get(stepName)
                .listener(this.batchJobCommonStepListener);
    }
    
    protected Date getProcessDate(StepExecution stepExecution, int amountToAdd, TemporalUnit unit) throws BatchException {
		String batchProcessDateStr = stepExecution.getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
		logger.debug(String.format("Job parameter processing date from external [%s]", batchProcessDateStr));
		
		// Get batch system date from DB if it not found in job parameters
		String batchSystemDateStr = null;
		if(batchProcessDateStr == null) {
			batchSystemDateStr = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			logger.debug(String.format("Job parameter processing date from DB [%s]", batchSystemDateStr));
		}
		
		// Getting the actual batch process date, for batch system date we will need to -1 day to get the process date
		Date batchProcessDate = null;
		try {
			if(batchProcessDateStr != null) {
				// The job data parameter is expected to be in format yyyyMMdd
				batchProcessDate = DateUtils.getDateFromString(batchProcessDateStr, General.DEFAULT_JOB_PARAMETER_DATE_FORMAT);
			} else {
				batchProcessDate = DateUtils.getDateFromString(batchSystemDateStr, General.DEFAULT_DATE_FORMAT);	
				// amountToAdd and unit should not be altered if batchProcessDateStr is not null
				if(amountToAdd != 0 && unit != null) {
					batchProcessDate = DateUtils.add(batchProcessDate, amountToAdd, unit);
				}
			}
		} catch (ParseException e) {
			String errorMessage = String.format("Error happened while parsing batch system date [%s] using format [%s]", batchSystemDateStr, General.DEFAULT_DATE_FORMAT);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, e);
		}
		
		logger.info(String.format("Final ProcessDate [%s]", batchProcessDate));
		return batchProcessDate;
	}
    
}
