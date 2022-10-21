package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractDebitCardDeliveryJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.ExtractDebitCardDelivery;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
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

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
@Component
@Lazy
public class ExtractDebitCardDeliveryStepBuilder  extends BaseStepBuilder {
    static final Logger logger = Logger.getLogger(ExtractDebitCardDeliveryStepBuilder.class);
    
    private static final String STEPNAME = "ExtractDebitCardDelivery";
    private static final String PROCESS_DATE = "ProcessDate";

    @Autowired
    private ExtractDebitCardDeliveryJobConfigProperties configProperties;

    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<ExtractDebitCardDelivery> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<ExtractDebitCardDelivery, ExtractDebitCardDelivery> itemProcessor;
    @Autowired
    @Qualifier(STEPNAME + ".ItemWriter")
    private ItemWriter<ExtractDebitCardDelivery> itemWriter;

    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<ExtractDebitCardDelivery, ExtractDebitCardDelivery>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<ExtractDebitCardDelivery> extractDebitCardDeliveryJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) throws ParseException {
        JdbcPagingItemReader<ExtractDebitCardDelivery> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(configProperties.getJdbcpagingpagesize());
                
        Date processDate =getProcessDate(stepExecution);
        PagingQueryProvider queryProvider = createQueryProvider(processDate);
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(ExtractDebitCardDelivery.class));
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider(Date processDate) {
        String fromDate = DateUtils.formatDateString(DateUtils.addDays(processDate, -1), configProperties.getNamedateformat()) + " " + configProperties.getBatchtime();
        String toDate = DateUtils.formatDateString(processDate, configProperties.getNamedateformat()) + " " + configProperties.getBatchtime();
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM vw_batch_extract_debit_card_delivery");
        queryProvider.setWhereClause("ACCOUNT_CREATION_DATE between'" + fromDate + "' and '" + toDate + "'");
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("ACCOUNT_NO", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<ExtractDebitCardDelivery, ExtractDebitCardDelivery> extractDebitCardDeliveryJobProcessor() {
        return extractDebitCardDelivery -> extractDebitCardDelivery;
    }

    @Bean(STEPNAME + ".ItemWriter")
    @StepScope
    public FlatFileItemWriter<ExtractDebitCardDelivery> extractDebitCardDeliveryJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
        FlatFileItemWriter<ExtractDebitCardDelivery> flatFileItemWriter = new FlatFileItemWriter<>();
        
        String processDate = DateUtils.formatDateString(getProcessDate(stepExecution), configProperties.getNamedateformat());
        String batchSystemDate = DateUtils.formatDateString(getProcessDate(stepExecution), DEFAULT_DATE_FORMAT);
		logger.info("Processing Date ::" + processDate);
        
        String jobName=stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
        String targetFileNewName=configProperties.getName().replace("{#date}",  processDate);
        File targetFileFullPath=Paths.get(outputFolderFullPath,jobName,targetFileNewName).toFile();

        flatFileItemWriter.setResource(new FileSystemResource(targetFileFullPath.getPath()));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, batchSystemDate);
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,
                targetFileFullPath.getAbsolutePath());

        flatFileItemWriter.setHeaderCallback(getHeaderCallback(stepExecution));
        flatFileItemWriter.setFooterCallback(getFooterCallback(stepExecution));    

        LineAggregator<ExtractDebitCardDelivery> lineAggregator = createExtractDebitCardDeliveryLineAggregator();
        flatFileItemWriter.setLineAggregator(lineAggregator);
        
        return flatFileItemWriter;
    }

    private LineAggregator<ExtractDebitCardDelivery> createExtractDebitCardDeliveryLineAggregator() {
        FormatterLineAggregator<ExtractDebitCardDelivery> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(configProperties.getDetailcolumns());
        FieldExtractor<ExtractDebitCardDelivery> fieldExtractor = createExtractDebitCardDeliveryFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<ExtractDebitCardDelivery> createExtractDebitCardDeliveryFieldExtractor() {
        BeanWrapperFieldExtractor<ExtractDebitCardDelivery> extractor = new BeanWrapperFieldExtractor<>();
        String[] names=configProperties.getDetailnames().split(",", -1);
        extractor.setNames(names);
        return extractor;
    }

    @Bean
	@StepScope
	protected FlatFileHeaderCallback getHeaderCallback(@Value("#{stepExecution}") StepExecution stepExecution) {
		return new FlatFileHeaderCallback() {
			@Override
			public void writeHeader(Writer writer) throws IOException {
				String header = StringUtils.leftPad("",1000, '0');
				writer.append(header);
			}
		};
	}

    @Bean
	@StepScope
	protected FlatFileFooterCallback getFooterCallback(@Value("#{stepExecution}")  StepExecution stepExecution) {
	    return new FlatFileFooterCallback() {
	        @Override
	        public void writeFooter(Writer writer) throws IOException {
	    		String header = StringUtils.leftPad("",1000, '0');
				writer.append(header);
	        }
	    };
	}

    private Date getProcessDate(StepExecution stepExecution) throws ParseException {
        Date processDate = null;
        Date executionContextProcessDate = (Date) stepExecution.getJobExecution().getExecutionContext().get(PROCESS_DATE);
        logger.info("ExtractDailyFirstTimeLoginStepBuilder - getProcessDate - executionContextProcessDate: " + executionContextProcessDate);
        if(executionContextProcessDate != null) {
            // continuous
            processDate = executionContextProcessDate;
        } else {
            // retrieve from JobParameter
            String externalDate = stepExecution.getJobParameters().getString(PROCESS_DATE);
            logger.info("ExtractDailyFirstTimeLoginStepBuilder - getProcessDate - jobparameter externalDate: " + externalDate);
            if(externalDate !=  null) {
                processDate = (Date)new SimpleDateFormat("yyyy-MM-dd").parse(externalDate);
            }

            // retrieve from Config table
            if (processDate == null) {
                String batchSystemDateStr = (String) stepExecution.getJobExecution().getExecutionContext()
                        .get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
                logger.info("ExtractDailyFirstTimeLoginStepBuilder - getProcessDate - batchSystemDateStr: " + batchSystemDateStr);
                Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
                processDate = DateUtils.addDays(batchSystemDate, 0);

                stepExecution.getJobExecution().getExecutionContext().put(PROCESS_DATE, processDate);
            }
        }
        logger.info("ExtractDailyFirstTimeLoginStepBuilder - getProcessDate - final process date: "+processDate);
        return processDate;
    }
}
