package com.rhbgroup.dcp.bo.batch.job.step;

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

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JsonUtils;
import com.rhbgroup.dcp.bo.batch.job.flatfilecallback.StringFooterWriter;
import com.rhbgroup.dcp.bo.batch.job.flatfilecallback.StringHeaderWriter;
import com.rhbgroup.dcp.bo.batch.job.model.SampleIBGReject;
import com.rhbgroup.dcp.bo.batch.job.model.SampleIBGRejectOut;

import javax.sql.DataSource;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Component
@Lazy
public class SampleJobReadDBToFileStepBuilder  extends BaseStepBuilder {
    static final Logger logger = Logger.getLogger(SampleJobReadDBToFileStepBuilder.class);

    @Value("${job.sample.to.ftp.targetfile.content.names}")
    private String fileContentNames;
    @Value("${job.sample.to.ftp.targetfile.content.columns}")
    private String fileContentColumns;
    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    @Value("${job.sample.to.ftp.targetfile.name}")
    private String targetFileName;
    @Value("${job.sample.to.ftp.targetfile.name.dateformat}")
    private String targetFileDateFormat;

    @Autowired
    @Qualifier("SampleJobReadDBToFile.ItemReader")
    private ItemReader<SampleIBGReject> itemReader;
    @Autowired
    @Qualifier("SampleJobReadDBToFile.ItemProcessor")
    private ItemProcessor<SampleIBGReject, SampleIBGRejectOut> itemProcessor;
    @Autowired
    @Qualifier("SampleJobReadDBToFile.ItemWriter")
    private ItemWriter<SampleIBGRejectOut> itemWriter;

    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder("SampleJobReadDBToFile").<SampleIBGReject,SampleIBGRejectOut>chunk(100)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean("SampleJobReadDBToFile.ItemReader")
    @StepScope
    public JdbcPagingItemReader<SampleIBGReject> sampleJobReader(@Value("#{stepExecution}") StepExecution stepExecution
    ,DataSource dataSource) {
        JdbcPagingItemReader<SampleIBGReject> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(1000);

        PagingQueryProvider queryProvider = createQueryProvider();
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(SampleIBGReject.class));
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT id, date,teller,trace,ref1,name,amount,reject_Code,account_Number,bene_Name,bene_Account");
        queryProvider.setFromClause("FROM TBL_BATCH_SAMPLE_STAGING");
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("Id", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean("SampleJobReadDBToFile.ItemProcessor")
    @StepScope
    public ItemProcessor<SampleIBGReject, SampleIBGRejectOut> sampleJobProcessor() {
        return sampleIBGReject -> {
            SampleIBGRejectOut sampleIBGRejectOut=null;
            try {
                sampleIBGRejectOut = new SampleIBGRejectOut();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                sampleIBGRejectOut.setDate(simpleDateFormat.format(sampleIBGReject.getDate()));
                sampleIBGRejectOut.setTeller(sampleIBGReject.getTeller());
                sampleIBGRejectOut.setTrace(sampleIBGReject.getTrace());
                sampleIBGRejectOut.setRef1(sampleIBGReject.getRef1());
                sampleIBGRejectOut.setName(sampleIBGReject.getName());
                sampleIBGRejectOut.setAmount(sampleIBGReject.getAmount().toString());
                sampleIBGRejectOut.setRejectCode(sampleIBGReject.getRejectCode());
                sampleIBGRejectOut.setAccountNumber(sampleIBGReject.getAccountNumber());
                sampleIBGRejectOut.setBeneName(sampleIBGReject.getBeneName());
                sampleIBGRejectOut.setBeneAccount(sampleIBGReject.getBeneAccount());
            }
            catch (Exception ex)
            {
                sampleIBGRejectOut=null;
                try {
                    throw new BatchValidationException(BatchErrorCode.FIELD_VALIDATION_ERROR,
                            BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE +" "+ JsonUtils.convertObjectToString(sampleIBGReject),
                            ex);
                } catch (BatchValidationException ex2) {
                    logger.error(ex2);
                }
            }
            return sampleIBGRejectOut;
        };
    }

    @Bean("SampleJobReadDBToFile.ItemWriter")
    @StepScope
    public FlatFileItemWriter<SampleIBGRejectOut> sampleJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
        FlatFileItemWriter<SampleIBGRejectOut> flatFileItemWriter=new FlatFileItemWriter();

        String exportFileHeader = "here is header";
        StringHeaderWriter headerWriter = new StringHeaderWriter(exportFileHeader);
        flatFileItemWriter.setHeaderCallback(headerWriter);

        String exportFileFooter = "here is footer, here is footer";
        StringFooterWriter footerWriter = new StringFooterWriter(exportFileFooter);
        flatFileItemWriter.setFooterCallback(footerWriter);

        String batchSystemDatStr=(String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        String targetFileNewName=this.targetFileName.replace("{#date}",  DateUtils.formatDateString(DateUtils.getDateFromString(batchSystemDatStr,DEFAULT_DATE_FORMAT), this.targetFileDateFormat));
        File targetFileFullPath=Paths.get(outputFolderFullPath,targetFileNewName).toFile();

        flatFileItemWriter.setResource(new FileSystemResource(targetFileFullPath.getPath()));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,
                targetFileFullPath.getAbsolutePath());

        LineAggregator<SampleIBGRejectOut> lineAggregator = createSampleIBGRejectLineAggregator();
        flatFileItemWriter.setLineAggregator(lineAggregator);
        return flatFileItemWriter;
    }

    private LineAggregator<SampleIBGRejectOut> createSampleIBGRejectLineAggregator() {
        FormatterLineAggregator<SampleIBGRejectOut> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(fileContentColumns);
        FieldExtractor<SampleIBGRejectOut> fieldExtractor = createSampleIBGRejectFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<SampleIBGRejectOut> createSampleIBGRejectFieldExtractor() {
        BeanWrapperFieldExtractor<SampleIBGRejectOut> extractor = new BeanWrapperFieldExtractor();
        String[] names=fileContentNames.split(",", -1);
        extractor.setNames(names);
        return extractor;
    }
}
