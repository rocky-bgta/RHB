package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;

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
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.SampleIBGRejectFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.SampleIBGReject;
import com.rhbgroup.dcp.bo.batch.job.repository.SampleIBGRejectRepositoryImpl;

@Component
@Lazy
public class SampleJobReadFileToStagingStepBuilder extends BaseStepBuilder {
    @Value("${job.sample.from.ftp.sourcefile.content.names}")
    private String fileContentNames;
    @Value("${job.sample.from.ftp.sourcefile.content.columns}")
    private String fileContentColumns;

    @Autowired
    @Qualifier("SampleJobReadFileToStaging.ItemReader")
    private ItemReader<SampleIBGReject> itemReader;
    @Autowired
    @Qualifier("SampleJobReadFileToStaging.ItemProcessor")
    private ItemProcessor<SampleIBGReject,SampleIBGReject> itemProcessor;
    @Autowired
    @Qualifier("SampleJobReadFileToStaging.ItemWriter")
    private ItemWriter<SampleIBGReject> itemWriter;
    @Autowired
    private SampleIBGRejectRepositoryImpl sampleIBGRejectRepository;

    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder("SampleJobReadFileToStaging").<SampleIBGReject,SampleIBGReject>chunk(100)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean("SampleJobReadFileToStaging.ItemReader")
    @StepScope
    public FlatFileItemReader<SampleIBGReject> sampleJobReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemReader<SampleIBGReject> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));
        reader.setLineMapper(getDefaultFixedLengthLineMapper(SampleIBGReject.class
                ,fileContentNames
                ,fileContentColumns
                ,new SampleIBGRejectFieldSetMapper()));
        return reader;
    }


    @Bean("SampleJobReadFileToStaging.ItemProcessor")
    @StepScope
    public ItemProcessor<SampleIBGReject, SampleIBGReject> sampleJobProcessor() {
        return new ItemProcessor<SampleIBGReject, SampleIBGReject>() {
            @Override
            public SampleIBGReject process(SampleIBGReject sampleIBGReject) throws Exception {
                    return sampleIBGReject;
            }
        };
    }

    @Bean("SampleJobReadFileToStaging.ItemWriter")
    @StepScope
    public ItemWriter<SampleIBGReject> sampleJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        return messages -> {
            String jobExecutionId=stepExecution.getJobExecution().getId().toString();
            for (SampleIBGReject message : messages) {
                message.setJobExecutionId(jobExecutionId);
                sampleIBGRejectRepository.addSampleIBGRejectStaging(message);
            }
        };
    }
}
