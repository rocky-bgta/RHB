package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.UpdateCustomerProfileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.UpdateCustomerProfileFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.UpdateCustomerProfile;
import com.rhbgroup.dcp.bo.batch.job.repository.UpdateCustomerProfileRepositoryImpl;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.util.Set;

@Component
@Lazy
public class UpdateCustomerProfileStagingStepBuilder extends BaseStepBuilder {

    static final Logger logger = Logger.getLogger(UpdateCustomerProfileStagingStepBuilder.class);

    private static final String STEPNAME = "UpdateCustomerProfileStaging";
    @Autowired
    private UpdateCustomerProfileJobConfigProperties configProperties;
    @Autowired
    private UpdateCustomerProfileRepositoryImpl updateCustomerProfileRepository;

    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<UpdateCustomerProfile> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<UpdateCustomerProfile,UpdateCustomerProfile> itemProcessor;
    @Autowired
    @Qualifier(STEPNAME + ".ItemWriter")
    private ItemWriter<UpdateCustomerProfile> itemWriter;

    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<UpdateCustomerProfile,UpdateCustomerProfile>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public FlatFileItemReader<UpdateCustomerProfile> updateCustomerProfileJobReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemReader<UpdateCustomerProfile> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));

        reader.setLineMapper(getDefaultDelimitedLineMapper(UpdateCustomerProfile.class
                ,configProperties.getDetailnames()
                ,configProperties.getDelimiter()
                ,new UpdateCustomerProfileFieldSetMapper()));
        return reader;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<UpdateCustomerProfile, UpdateCustomerProfile> updateCustomerProfileJobProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return new ItemProcessor<UpdateCustomerProfile, UpdateCustomerProfile>() {
            @Override
            public UpdateCustomerProfile process(UpdateCustomerProfile updateCustomerProfile) throws Exception {

                Set<ConstraintViolation<UpdateCustomerProfile>> violations = validator.validate(updateCustomerProfile);
                if(!violations.isEmpty()) {
                    for (ConstraintViolation<UpdateCustomerProfile> violation : violations) {
                        logger.error(violation.getPropertyPath()+": "+violation.getMessage() +", input validation at record: "+ updateCustomerProfile.getCount());
                    }
                    logger.error(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE));
                    stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE));
                    return null;
                }else{
                    return updateCustomerProfile;
                }
            }
        };
    }

    @Bean(STEPNAME + ".ItemWriter")
    @StepScope
    public ItemWriter<UpdateCustomerProfile> updateCustomerProfileJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        updateCustomerProfileRepository.cleanCustomerProfileStagingTable();

        return messages -> {
            int jobExecutionId= stepExecution.getJobExecution().getId().intValue();
            String fileFullPath =stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
            String fileName = new File(fileFullPath).getName();
            for (UpdateCustomerProfile message : messages) {
                message.setJobExecutionId(jobExecutionId);
                message.setProcessed(false);
                message.setFileName(fileName);
                updateCustomerProfileRepository.addUpdateCustomerProfileStaging(message);
            }
        };
    }
}
