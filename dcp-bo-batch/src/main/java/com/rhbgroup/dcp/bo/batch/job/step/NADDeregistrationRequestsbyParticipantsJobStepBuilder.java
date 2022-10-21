package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JsonUtils;
import com.rhbgroup.dcp.bo.batch.job.flatfilecallback.StringFooterWriter;
import com.rhbgroup.dcp.bo.batch.job.flatfilecallback.StringHeaderWriter;
import com.rhbgroup.dcp.bo.batch.job.model.NADDeregistrationRequestsbyParticipantsJobDetail;
import com.rhbgroup.dcp.bo.batch.job.model.NADDeregistrationRequestsbyParticipantsJobDetailOut;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileFooterCallback;
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

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

@Component
@Lazy
public class NADDeregistrationRequestsbyParticipantsJobStepBuilder extends BaseStepBuilder {
    static final Logger logger = Logger.getLogger(NADDeregistrationRequestsbyParticipantsJobStepBuilder.class);

    @Value("${job.nadderegistrationrequestsbyparticipantsjob.detailnames}")
    private String fileContentNames;
    @Value("${job.nadderegistrationrequestsbyparticipantsjob.detailcolumns}")
    private String fileContentColumns;
    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    @Value("${job.nadderegistrationrequestsbyparticipantsjob.name}")
    private String targetFileName;
    @Value("${job.nadderegistrationrequestsbyparticipantsjob.namedateformat}")
    private String targetFileDateFormat;

    @Autowired
    @Qualifier("NADDeregistrationRequestsbyParticipantsJob.ItemReader")
    private ItemReader<NADDeregistrationRequestsbyParticipantsJobDetail> itemReader;
    @Autowired
    @Qualifier("NADDeregistrationRequestsbyParticipantsJob.ItemProcessor")
    private ItemProcessor<NADDeregistrationRequestsbyParticipantsJobDetail, NADDeregistrationRequestsbyParticipantsJobDetailOut> itemProcessor;
    @Autowired
    @Qualifier("NADDeregistrationRequestsbyParticipantsJob.ItemWriter")
    private ItemWriter<NADDeregistrationRequestsbyParticipantsJobDetailOut> itemWriter;

    // Database and view to be queried from
    private String viewName = "vw_batch_duitnow_proxy_deregistration";
    private String jobName = "/NADDeregistrationRequestsbyParticipantsJob";

    // Header and Trailer
    private int processCounter;
    private String exportFileHeader = "01|";
    private String exportFileFooter = "03|";

    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder("NADDeregistrationRequestsbyParticipantsJob").<NADDeregistrationRequestsbyParticipantsJobDetail,NADDeregistrationRequestsbyParticipantsJobDetailOut>chunk(100)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean("NADDeregistrationRequestsbyParticipantsJob.ItemReader")
    @StepScope
    public JdbcPagingItemReader<NADDeregistrationRequestsbyParticipantsJobDetail> nADDeregistrationRequestsbyParticipantsJobReader(@Value("#{stepExecution}") StepExecution stepExecution
    ,DataSource dataSource) {
        JdbcPagingItemReader<NADDeregistrationRequestsbyParticipantsJobDetail> databaseReader = new JdbcPagingItemReader<>();

        try{
            databaseReader.setDataSource(dataSource);
            databaseReader.setPageSize(1000);
            logger.info("Reader NADDeregistration");

            // Convert string to be passed as WHERE SQL query
            String batchSystemDateStr = (String) stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT, Locale.ENGLISH);

            LocalDate batchProcessDate = LocalDate.parse(batchSystemDateStr, formatter);

            PagingQueryProvider queryProvider = createQueryProvider(batchProcessDate.toString());
            databaseReader.setQueryProvider(queryProvider);
            databaseReader.setRowMapper(new BeanPropertyRowMapper<>(NADDeregistrationRequestsbyParticipantsJobDetail.class));

        }catch(Exception ex ) {
            stepExecution.getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS, BatchSystemConstant.ExitCode.FAILED);
            String message = String.format("NADDeregistration item reader exception %s", ex.getMessage()) ;
            logger.info(message);
        }

        return databaseReader;
    }

    // Process the raw data into output
    @Bean("NADDeregistrationRequestsbyParticipantsJob.ItemProcessor")
    @StepScope
    public ItemProcessor<NADDeregistrationRequestsbyParticipantsJobDetail, NADDeregistrationRequestsbyParticipantsJobDetailOut> nADDeregistrationRequestsbyParticipantsJobProcessor() {

        processCounter = 0;

        return nADDeregistrationRequestsbyParticipantsJobDetail -> {
            NADDeregistrationRequestsbyParticipantsJobDetailOut nADDeregistrationRequestsbyParticipantsJobDetailOut=null;
            try {

                nADDeregistrationRequestsbyParticipantsJobDetailOut = new NADDeregistrationRequestsbyParticipantsJobDetailOut();

                // Setting the raw data from database view
                nADDeregistrationRequestsbyParticipantsJobDetailOut.setId(String.valueOf(nADDeregistrationRequestsbyParticipantsJobDetail.getId()));
                nADDeregistrationRequestsbyParticipantsJobDetailOut.setAuditId(String.valueOf(nADDeregistrationRequestsbyParticipantsJobDetail.getAuditId()));
                nADDeregistrationRequestsbyParticipantsJobDetailOut.setDetails(nADDeregistrationRequestsbyParticipantsJobDetail.getDetails());

                // Identifier to search for proxy_id in details
                String searchProxyId = "oldMobileNo";

                // Find if the data has the correct format in this case will only check for Proxy ID
                int indexProxyId = nADDeregistrationRequestsbyParticipantsJobDetail.getDetails().toLowerCase().indexOf(searchProxyId.toLowerCase());

                // Extract the proxy id, proxy type, secondary id, secondary type from details
                // Sanity of data from the database

                String originalText = nADDeregistrationRequestsbyParticipantsJobDetail.getDetails().substring(indexProxyId, nADDeregistrationRequestsbyParticipantsJobDetail.getDetails().length());
                originalText = originalText.replaceAll("[^a-zA-Z0-9,:|]+","");
                originalText = originalText.trim();
                String[] originalTextSplit = originalText.split(":");

                // Check if data has proxy id. Proxy ID = finalTextSplit[0], Proxy Type = finalTextSplit[1], Secondary ID = finalTextSplit[2], Secondary Type = finalTextSplit[3]
                if (indexProxyId != -1) {
                    String[] proxyIDSplit = originalTextSplit[1].split(",");
                    String[] proxyTypeSplit = originalTextSplit[2].split(",");
                    String[] secondaryIDSplit = originalTextSplit[3].split(",");
                    String[] secondaryTypeSplit = originalTextSplit[4].split(",");

                    // Setting all output details
                    nADDeregistrationRequestsbyParticipantsJobDetailOut.setProxyId(proxyIDSplit[0].trim());
                    nADDeregistrationRequestsbyParticipantsJobDetailOut.setProxyType(proxyTypeSplit[0].trim());
                    nADDeregistrationRequestsbyParticipantsJobDetailOut.setSecondaryId(secondaryIDSplit[0].trim());
                    nADDeregistrationRequestsbyParticipantsJobDetailOut.setSecondaryType(secondaryTypeSplit[0].trim());

                    // Log input to logger what is being put to the data.
                    String log = "Proxy ID : " + proxyIDSplit[0]  +
                            ", Proxy Type : " + proxyTypeSplit[0] +
                            ", Secondary ID : " + secondaryIDSplit[0] +
                            ", Secondary Type : " + secondaryTypeSplit[0];

                    logger.info(log);
                }
                processCounter++;
            }
            catch (Exception ex)
            {
                nADDeregistrationRequestsbyParticipantsJobDetailOut=null;
                try {
                    throw new BatchValidationException(BatchErrorCode.FIELD_VALIDATION_ERROR,
                            BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE +" "+ JsonUtils.convertObjectToString(nADDeregistrationRequestsbyParticipantsJobDetail), ex);
                } catch (BatchValidationException ex2) {
                    logger.error(ex2);
                }
            }
            return nADDeregistrationRequestsbyParticipantsJobDetailOut;
        };
    }

    @Bean("NADDeregistrationRequestsbyParticipantsJob.ItemWriter")
    @StepScope
    public FlatFileItemWriter<NADDeregistrationRequestsbyParticipantsJobDetailOut> nADDeregistrationRequestsbyParticipantsJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
        FlatFileItemWriter<NADDeregistrationRequestsbyParticipantsJobDetailOut> flatFileItemWriter=new FlatFileItemWriter();

        // This is the header information
        String batchSystemDate=stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        Date batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
        exportFileHeader += DateUtils.formatDateString(batchProcessingDate ,"yyyyMMdd");

        StringHeaderWriter headerWriter = new StringHeaderWriter(exportFileHeader);
        flatFileItemWriter.setHeaderCallback(headerWriter);
        logger.info("Header : " + exportFileHeader);

        // This is the footer that will write the count data
        StringFooterWriter footerWriter = new StringFooterWriter(exportFileFooter);
        flatFileItemWriter.setFooterCallback(footerWriter);
        flatFileItemWriter.setFooterCallback(getFooterCallback(stepExecution));

        String batchSystemDatStr=(String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        String targetFileNewName=this.targetFileName.replace("{#date}",  DateUtils.formatDateString(DateUtils.getDateFromString(batchSystemDatStr,DEFAULT_DATE_FORMAT), this.targetFileDateFormat));

        // Put the file into the folder batch
        outputFolderFullPath += jobName;
        File targetFileFullPath= Paths.get(outputFolderFullPath, targetFileNewName).toFile();

        flatFileItemWriter.setResource(new FileSystemResource(targetFileFullPath.getPath()));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,
                targetFileFullPath.getAbsolutePath());

        LineAggregator<NADDeregistrationRequestsbyParticipantsJobDetailOut> lineAggregator = createNADDeregistrationLineAggregator();
        flatFileItemWriter.setLineAggregator(lineAggregator);
        return flatFileItemWriter;
    }

    private PagingQueryProvider createQueryProvider(String batchProcessingDate) throws ParseException {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();

        String sqlSelect = "SELECT ID, AUDIT_ID, DETAILS, TIMESTAMP ";
        String sqlFrom ="FROM " + viewName;
        String whereClause = "WHERE TIMESTAMP >= DATEADD(DAY,DATEDIFF(DAY,1,'"+batchProcessingDate+"'),0) AND TIMESTAMP < DATEADD(DAY,DATEDIFF(DAY,0,'"+batchProcessingDate+"'),0)";
        logger.info(String.format("NADDeregistration SQL : %s %s %s", sqlSelect , sqlFrom , whereClause));

        queryProvider.setSelectClause(sqlSelect);
        queryProvider.setFromClause(sqlFrom);
        queryProvider.setWhereClause(whereClause);

        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("id", Order.ASCENDING);
        return sortConfiguration;
    }

    private LineAggregator<NADDeregistrationRequestsbyParticipantsJobDetailOut> createNADDeregistrationLineAggregator() {
        FormatterLineAggregator<NADDeregistrationRequestsbyParticipantsJobDetailOut> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(fileContentColumns);
        FieldExtractor<NADDeregistrationRequestsbyParticipantsJobDetailOut> fieldExtractor = createNADDeregistrationFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<NADDeregistrationRequestsbyParticipantsJobDetailOut> createNADDeregistrationFieldExtractor() {
        BeanWrapperFieldExtractor<NADDeregistrationRequestsbyParticipantsJobDetailOut> extractor = new BeanWrapperFieldExtractor();
        String[] names = fileContentNames.split(",", -1);
        extractor.setNames(names);
        return extractor;
    }

    @Bean
    @StepScope
    private FlatFileFooterCallback getFooterCallback(@Value("#{stepExecution}")  StepExecution stepExecution) {
        return new FlatFileFooterCallback() {
            @Override
            public void writeFooter(Writer writer) throws IOException {
                exportFileFooter += processCounter;
                writer.append(exportFileFooter);
                logger.info("Footer : " + exportFileFooter);
            }
        };
    }
}
