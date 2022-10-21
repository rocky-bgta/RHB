package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_TIME_FORMAT;

import com.rhbgroup.dcp.bo.batch.job.config.properties.PrepaidReloadFileFromIBKJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.PrepaidReloadFileFromIBKFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.PrepaidReloadFileFromIBK;
import com.rhbgroup.dcp.bo.batch.job.repository.PrepaidReloadFileFromIBKRepositoryImpl;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Lazy
public class PrepaidReloadFileFromIBKStepBuilder extends BaseStepBuilder {

    static final Logger logger = Logger.getLogger(PrepaidReloadFileFromIBKStepBuilder.class);

    private static final String STEPNAME = "PrepaidReloadFileFromIBK";
    @Autowired
    private PrepaidReloadFileFromIBKJobConfigProperties configProperties;
    @Autowired
    private PrepaidReloadFileFromIBKRepositoryImpl prepaidReloadFileFromIBKRepository;

    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<PrepaidReloadFileFromIBK> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<PrepaidReloadFileFromIBK,PrepaidReloadFileFromIBK> itemProcessor;
    @Autowired
    @Qualifier(STEPNAME + ".ItemWriter")
    private ItemWriter<PrepaidReloadFileFromIBK> itemWriter;

    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<PrepaidReloadFileFromIBK,PrepaidReloadFileFromIBK>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public FlatFileItemReader<PrepaidReloadFileFromIBK> prepaidReloadFileFromIBKItemReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemReader<PrepaidReloadFileFromIBK> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));

        reader.setLineMapper(getSkipEmptyDelimitedLineMapper(PrepaidReloadFileFromIBK.class
                ,configProperties.getDetailnames()
                ,configProperties.getDelimiter()
                ,new PrepaidReloadFileFromIBKFieldSetMapper()));
        return reader;
    }


    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<PrepaidReloadFileFromIBK, PrepaidReloadFileFromIBK> prepaidReloadFileFromIBKItemProcessor() {
        return new ItemProcessor<PrepaidReloadFileFromIBK, PrepaidReloadFileFromIBK>() {
            @Override
            public PrepaidReloadFileFromIBK process(PrepaidReloadFileFromIBK prepaidReloadFileFromIBK) throws Exception {
                PrepaidReloadFileFromIBK prepaidReloadFileFromIBKOut = new PrepaidReloadFileFromIBK();
                prepaidReloadFileFromIBKOut.setTxnTime(prepaidReloadFileFromIBK.getTxnTime().replaceAll("\"",""));
                prepaidReloadFileFromIBKOut.setRefNo(prepaidReloadFileFromIBK.getRefNo().replaceAll("\"",""));
                prepaidReloadFileFromIBKOut.setHostRefNo(prepaidReloadFileFromIBK.getHostRefNo().replaceAll("\"",""));
                prepaidReloadFileFromIBKOut.setMobileNo(prepaidReloadFileFromIBK.getMobileNo().replaceAll("\"",""));
                prepaidReloadFileFromIBKOut.setPrepaidProductCode(prepaidReloadFileFromIBK.getPrepaidProductCode().replaceAll("\"",""));
                prepaidReloadFileFromIBKOut.setAmount(prepaidReloadFileFromIBK.getAmount().replaceAll("\"",""));
                prepaidReloadFileFromIBKOut.setTxnStatus(prepaidReloadFileFromIBK.getTxnStatus().replaceAll("\"",""));
                if (prepaidReloadFileFromIBKOut.getTxnStatus().equalsIgnoreCase("S"))
                    prepaidReloadFileFromIBKOut.setTxnStatus("SUCCESS");
                return prepaidReloadFileFromIBKOut;
            }
        };
    }

    @Bean(STEPNAME + ".ItemWriter")
    @StepScope
    public ItemWriter<PrepaidReloadFileFromIBK> prepaidReloadFileFromIBKItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        return records -> {
            int jobExecutionId= stepExecution.getJobExecution().getId().intValue();
            String createdTime = new SimpleDateFormat(DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT).format(new Date());
            String fileFullPath =stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
            String fileName = new File(fileFullPath).getName();
            String paymentType=null;

            if(fileName.contains("CASA"))
                paymentType = "CASA";
            if(fileName.contains("CC"))
                paymentType = "CC/DB";

            for (PrepaidReloadFileFromIBK record : records) {
                record.setJobExecutionId(jobExecutionId);
                record.setFileName(fileName);
                record.setCreatedTime(createdTime);
                record.setPaymentType(paymentType);
                prepaidReloadFileFromIBKRepository.addToStaging(record);
            }
        };
    }
}
