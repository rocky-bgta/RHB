package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PremierCustomerInfoandRMCodeTaggingJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.PremierCustomerInfoandRMCodeTaggingDetailFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.PremierCustomerInfoandRMCodeTaggingDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.PremierCustomerInfoandRMCodeTaggingRepositoryImpl;

@Component
@Lazy
public class PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder extends BaseStepBuilder {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder.class);

    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobConfigProperties configProperties;
    
    @Autowired
    @Qualifier("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter.ItemReader")
    private ItemReader<PremierCustomerInfoandRMCodeTaggingDetail> itemReader;
    @Autowired
    @Qualifier("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter.ItemProcessor")
    private ItemProcessor<PremierCustomerInfoandRMCodeTaggingDetail,PremierCustomerInfoandRMCodeTaggingDetail> itemProcessor;
    @Autowired
    @Qualifier("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter.ItemWriter")
    private ItemWriter<PremierCustomerInfoandRMCodeTaggingDetail> itemWriter;

    @Autowired
    private PremierCustomerInfoandRMCodeTaggingRepositoryImpl premierCustomerInfoandRMCodeTaggingRepositoryImpl;
    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;

    private boolean header = false;
    private boolean footer = false;
    private int headerLine;
    private int footerLine;
    private int counterItem;
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter").<PremierCustomerInfoandRMCodeTaggingDetail,PremierCustomerInfoandRMCodeTaggingDetail>chunk(configProperties.getChunkSize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter.ItemReader")
    @StepScope
    public FlatFileItemReader<PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingJobReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemReader<PremierCustomerInfoandRMCodeTaggingDetail> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));

        counterItem = 0;
        headerLine = 1;
        footerLine = 0;
        try{
            File file = new File((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY));
            footerLine = getNoOfLines(file);
        }
        catch(IOException ex){logger.info(ex.getMessage());}
        catch (Exception ex){logger.info(ex.getMessage());}

        // Validate Header & Footer
        reader.setLinesToSkip(headerLine);
        reader.setLinesToSkip(footerLine);

        // Initialize header skipped to split line based on fixed length
        LineCallbackHandler skippedLineCallback = line -> {
            counterItem++;
            String indicator = "";
            indicator = line.substring(0,2).trim();

            if(indicator.equalsIgnoreCase(configProperties.getHeaderIndicator())){
                // Check if header exists
                header = true;
            }
            else if(indicator.equalsIgnoreCase(configProperties.getFooterIndicator())){
                // Check if footer exists
                int trailerTotalRecord = Integer.valueOf(line.substring(3,20).trim());

                footer = true;
                if(trailerTotalRecord != (footerLine-2)){
                    logger.warn("Job Execution is stopped because trailer does not match with total number of record");
                    stepExecution.getJobExecution().stop();
                }
            }

            if(counterItem == footerLine && (!footer || !header)){
                logger.warn("Job Execution is stopped because : No header/footer found");
                stepExecution.getJobExecution().stop();
            }
        };

        reader.setSkippedLinesCallback(skippedLineCallback);

        reader.setLineMapper(getDefaultFixedLengthLineMapper(PremierCustomerInfoandRMCodeTaggingDetail.class
                ,configProperties.getDetailNames()
                ,configProperties.getDetailColumns()
                ,new PremierCustomerInfoandRMCodeTaggingDetailFieldSetMapper()));

        return reader;
    }

    @Bean("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter.ItemProcessor")
    @StepScope
    public ItemProcessor<PremierCustomerInfoandRMCodeTaggingDetail, PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingJobProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return new ItemProcessor<PremierCustomerInfoandRMCodeTaggingDetail, PremierCustomerInfoandRMCodeTaggingDetail>() {
            @Override
            public PremierCustomerInfoandRMCodeTaggingDetail process(PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail) throws Exception {
                return premierCustomerInfoandRMCodeTaggingDetail;
            }
        };
    }

    @Bean("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter.ItemWriter")
    @StepScope
    public ItemWriter<PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        return messages -> {};
    }

    // Get the total number of line in file
    public int getNoOfLines(File fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        int counter = 0;

        try {
            String line;
            while ((line = br.readLine()) != null) {
                counter++;
            }
        }finally {
            br.close();
        }

        return counter;
    }
}
