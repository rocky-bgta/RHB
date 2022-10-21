package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PrepaidReloadExtractionJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.PrepaidReloadExtraction;
import com.rhbgroup.dcp.bo.batch.job.model.PrepaidReloadExtractionOut;
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
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.*;
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
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
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

@Component
@Lazy
public class PrepaidReloadExtractionCCStepBuilder extends BaseStepBuilder{
    static final Logger logger = Logger.getLogger(PrepaidReloadExtractionCCStepBuilder.class);
    private static final String STEPNAME = "PrepaidReloadExtractionCC";

    @Autowired
    private PrepaidReloadExtractionJobConfigProperties configProperties;
    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<PrepaidReloadExtraction> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<PrepaidReloadExtraction,PrepaidReloadExtractionOut> itemProcessor;
    @Autowired
    @Qualifier(STEPNAME + ".ItemWriter")
    private ItemWriter<PrepaidReloadExtractionOut> itemWriter;
    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;

    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<PrepaidReloadExtraction,PrepaidReloadExtractionOut>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<PrepaidReloadExtraction> prepaidReloadExtractionJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            ,DataSource dataSource) {
        JdbcPagingItemReader<PrepaidReloadExtraction> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(configProperties.getJdbcpagingpagesize());
        PagingQueryProvider queryProvider = createQueryProvider(stepExecution);
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(PrepaidReloadExtraction.class));
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider(StepExecution stepExecution) {
        String batchSystemDateStr=null;
        String startDate=null;
        String endDate=null;
        // Use the job process date if available
        Map<String, String> jobParameters = dcpBatchApplicationContext.getInitialJobArguments();
        // Check if jobprocessdate is available or not.
        try {
            if(jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)){
                batchSystemDateStr = jobParameters.get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
                stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY,batchSystemDateStr);
                logger.info(String.format("Job process date found : [%s]", batchSystemDateStr));
                Date date = DateUtils.getDateFromString(batchSystemDateStr,DEFAULT_DATE_FORMAT);
                Date eDate = DateUtils.addDays(date,1);
                startDate=batchSystemDateStr;
                endDate=DateUtils.formatDateString(eDate,DEFAULT_DATE_FORMAT);
            } else {
                batchSystemDateStr = (String) stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
                Date date = DateUtils.getDateFromString(batchSystemDateStr,DEFAULT_DATE_FORMAT);
                Date sDate = DateUtils.addDays(date,-1);
                startDate = DateUtils.formatDateString(sDate,DEFAULT_DATE_FORMAT);
                endDate = batchSystemDateStr;
            }
        }catch (ParseException e){
            logger.error(e);
        }
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT txn_time,ref_no,host_ref_no,mobile_no,prepaid_product_code,amount");
        queryProvider.setFromClause("FROM VW_BATCH_MERGED_TOPUP_TXN");
        queryProvider.setWhereClause("WHERE payment_type='CC/DB' AND (txn_time>='"+startDate+"' AND txn_time<'"+endDate+"')");
        queryProvider.setSortKeys(sortByDateTimeAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByDateTimeAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("txn_time", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<PrepaidReloadExtraction, PrepaidReloadExtractionOut> prepaidReloadExtractionJobProcessor() {
        return prepaidReloadExtraction -> {
            PrepaidReloadExtractionOut prepaidReloadExtractionOut=null;
            try {
                prepaidReloadExtractionOut = new PrepaidReloadExtractionOut();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                prepaidReloadExtractionOut.setTxnTime("\"" + simpleDateFormat.format(prepaidReloadExtraction.getTxnTime()) + "\"");
                if (prepaidReloadExtraction.getRefNo()==null)
                    prepaidReloadExtractionOut.setRefNo("\"\"");
                else
                    prepaidReloadExtractionOut.setRefNo("\"" + prepaidReloadExtraction.getRefNo() + "\"");
                if (prepaidReloadExtraction.getHostRefNo()==null)
                    prepaidReloadExtractionOut.setHostRefNo("\"\"");
                else
                    prepaidReloadExtractionOut.setHostRefNo("\"" + prepaidReloadExtraction.getHostRefNo() + "\"");
                prepaidReloadExtractionOut.setMobileNo("\"" + prepaidReloadExtraction.getMobileNo() + "\"");
                if (prepaidReloadExtraction.getPrepaidProductCode()==null)
                    prepaidReloadExtractionOut.setPrepaidProductCode("\"\"");
                else
                    prepaidReloadExtractionOut.setPrepaidProductCode("\"" + prepaidReloadExtraction.getPrepaidProductCode() + "\"");
                prepaidReloadExtractionOut.setAmount("\"" + String.format("%.2f", prepaidReloadExtraction.getAmount()) + "\"");
            }
            catch (Exception ex)
            {
                prepaidReloadExtractionOut=null;
                logger.error(ex);
            }
            return prepaidReloadExtractionOut;
        };
    }

    @Bean(STEPNAME + ".ItemWriter")
    @StepScope
    public FlatFileItemWriter<PrepaidReloadExtractionOut> prepaidReloadJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
        FlatFileItemWriter<PrepaidReloadExtractionOut> flatFileItemWriter=new FlatFileItemWriter();
        flatFileItemWriter.setFooterCallback(getFooterCallback(stepExecution));
        String jobName=stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
        String batchSystemDatStr=(String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        String targetFileNewName=configProperties.getNamecc().replace("{#date}",  DateUtils.formatDateString(DateUtils.getDateFromString(batchSystemDatStr,DEFAULT_DATE_FORMAT), configProperties.getNamedateformat()));
        File targetFileFullPath=Paths.get(outputFolderFullPath,jobName,targetFileNewName).toFile();

        flatFileItemWriter.setResource(new FileSystemResource(targetFileFullPath.getPath()));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,
                targetFileFullPath.getAbsolutePath());

        LineAggregator<PrepaidReloadExtractionOut> lineAggregator = createPrepaidReloadLineAggregator();
        flatFileItemWriter.setLineAggregator(lineAggregator);
        return flatFileItemWriter;
    }

    private LineAggregator<PrepaidReloadExtractionOut> createPrepaidReloadLineAggregator() {
        DelimitedLineAggregator<PrepaidReloadExtractionOut> lineAggregator = new DelimitedLineAggregator<>();
        FieldExtractor<PrepaidReloadExtractionOut> fieldExtractor = createPrepaidReloadFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        lineAggregator.setDelimiter(",");
        return lineAggregator;
    }

    private FieldExtractor<PrepaidReloadExtractionOut> createPrepaidReloadFieldExtractor() {
        BeanWrapperFieldExtractor<PrepaidReloadExtractionOut> extractor = new BeanWrapperFieldExtractor();
        String[] names=configProperties.getDetailnames().split(",", -1);
        extractor.setNames(names);
        return extractor;
    }
    
    @Bean
	@StepScope
	private FlatFileFooterCallback getFooterCallback(@Value("#{stepExecution}")  StepExecution stepExecution) {
	    return new FlatFileFooterCallback() {
	        @Override
	        public void writeFooter(Writer writer) throws IOException {
	            writer.append("\r\n");
	        }
	    };
	}
}
