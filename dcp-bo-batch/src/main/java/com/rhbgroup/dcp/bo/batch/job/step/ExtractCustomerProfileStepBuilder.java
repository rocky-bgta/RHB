package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractCustomerProfileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.ExtractCustomerProfile;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;

import javax.sql.DataSource;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Lazy
public class ExtractCustomerProfileStepBuilder  extends BaseStepBuilder {
    static final Logger logger = Logger.getLogger(ExtractCustomerProfileStepBuilder.class);
    private static final String STEPNAME = "ExtractCustomerProfile";
    @Autowired
    private ExtractCustomerProfileJobConfigProperties configProperties;

    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<ExtractCustomerProfile> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<ExtractCustomerProfile, ExtractCustomerProfile> itemProcessor;
    @Autowired
    @Qualifier(STEPNAME + ".ItemWriter")
    private ItemWriter<ExtractCustomerProfile> itemWriter;

    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<ExtractCustomerProfile, ExtractCustomerProfile>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<ExtractCustomerProfile> extractCustomerProfileJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) {
        JdbcPagingItemReader<ExtractCustomerProfile> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(configProperties.getJdbcpagingpagesize());

        PagingQueryProvider queryProvider = createQueryProvider();
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(ExtractCustomerProfile.class));
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT CIS_NO");
        queryProvider.setFromClause("FROM vw_batch_extract_customer_profile");
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("CIS_NO", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<ExtractCustomerProfile, ExtractCustomerProfile> extractCustomerProfileJobProcessor() {
        return new ItemProcessor<ExtractCustomerProfile, ExtractCustomerProfile>() {
            @Override
            public ExtractCustomerProfile process(ExtractCustomerProfile extractCustomerProfile) {
                return extractCustomerProfile;
            }
        };
    }

    @Bean(STEPNAME + ".ItemWriter")
    @StepScope
    public FlatFileItemWriter<ExtractCustomerProfile> extractCustomerProfileJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
        FlatFileItemWriter<ExtractCustomerProfile> flatFileItemWriter=new FlatFileItemWriter();
        String batchSystemDateStr=(String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
        Date processDate = DateUtils.addDays(batchSystemDate, -1);
        String targetFileNewName=configProperties.getName().replace("{#date}",  DateUtils.formatDateString(processDate, configProperties.getNamedateformat()));
        String jobName=stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);

        File targetFileFullPath=Paths.get(outputFolderFullPath,jobName,targetFileNewName).toFile();

        flatFileItemWriter.setResource(new FileSystemResource(targetFileFullPath.getPath()));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY,
                DateUtils.formatDateString(processDate,DEFAULT_DATE_FORMAT));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,
                targetFileFullPath.getAbsolutePath());

        LineAggregator<ExtractCustomerProfile> lineAggregator = createExtractCustomerProfileLineAggregator();
        flatFileItemWriter.setLineSeparator("\r\n");
        flatFileItemWriter.setLineAggregator(lineAggregator);
        return flatFileItemWriter;
    }

    private LineAggregator<ExtractCustomerProfile> createExtractCustomerProfileLineAggregator() {
        FormatterLineAggregator<ExtractCustomerProfile> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(configProperties.getDetailcolumns());
        FieldExtractor<ExtractCustomerProfile> fieldExtractor = createExtractCustomerProfileFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<ExtractCustomerProfile> createExtractCustomerProfileFieldExtractor() {
        BeanWrapperFieldExtractor<ExtractCustomerProfile> extractor = new BeanWrapperFieldExtractor();
        String[] names=configProperties.getDetailnames().split(",", -1);
        extractor.setNames(names);
        return extractor;
    }
}
