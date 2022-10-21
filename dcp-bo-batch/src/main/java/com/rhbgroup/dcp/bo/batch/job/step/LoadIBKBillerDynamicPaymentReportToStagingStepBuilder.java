package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.core.mapping.SkipFooterLineMapper;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.vo.LoadDynamicBiller;
import com.rhbgroup.dcp.bo.batch.job.FileVerificationSkipper;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKBillerPaymentJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedDynamicIBKPaymentTxnDetailMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedDynamicIBKPaymentTxnHeaderMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedDynamicIBKPaymentTxnTrailerMapper;
import com.rhbgroup.dcp.bo.batch.job.model.*;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerDynamicPaymentConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBKPaymentTxnRepositoryImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;

@Component
@Lazy
public class LoadIBKBillerDynamicPaymentReportToStagingStepBuilder extends BaseStepBuilder {

    private static final Logger logger = Logger.getLogger(LoadIBKBillerDynamicPaymentReportToStagingStepBuilder.class);

    private static final String STEP_NAME = "LoadIBKBillerDynamicPaymentReportToStagingStep";
    private static final String RECORD_TYPE = "recordType";
    private static final String HEADER = "HEADER";
    private static final String BODY = "BODY";
    private static final String FOOTER = "FOOTER";

    private static final String EMTPY_IBK_BILLER_PAYMENT_FILE_PATH = "classpath:batch/input/LoadIBKBillerPaymentJob/EmptyIBKBillerPaymentFile.txt";

    @Autowired
    private LoadIBKBillerPaymentJobConfigProperties configProperties;

    @Autowired
    @Qualifier(STEP_NAME + ".ItemReader")
    private FlatFileItemReader<BatchStagedIBKPaymentTxn> itemReader;

    @Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn> itemProcessor;

    @Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<BatchStagedIBKPaymentTxn> itemWriter;

    @Autowired
    @Qualifier("BillerPaymentInboundConfigQueue")
    private Queue<BillerPaymentInboundConfig> billerPaymentInboundConfigQueue;

    @Autowired
    private BatchStagedIBKPaymentTxnRepositoryImpl batchStagedIBKPaymentTxnRepository;

    @Autowired
    BatchBillerDynamicPaymentConfigRepositoryImpl batchBillerDynamicPaymentConfigRepositoryImpl;

    @Bean
    public SkipPolicy fileVerificationSkipper() {
        return new FileVerificationSkipper();
    }

    @Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public FlatFileItemReader<BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxnItemReader(@Value("#{stepExecution}") StepExecution stepExecution) {

        logInfo("Creating ItemReader [%s]", STEP_NAME + ".ItemReader");

        String billerCode = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_CODE);

        logInfo("billerCode [%s] :", billerCode);

        LoadIbkPaymentTemplate loadIbkPaymentTemplate = new LoadIbkPaymentTemplate();

        BatchBillerPaymentConfig batchBillerPaymentConfig = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerPaymentConfigDtls(billerCode);

        logInfo("batchBillerPaymentConfig.getIbkFtpFolder [%s] :", batchBillerPaymentConfig.getIbkFtpFolder());

        LoadDynamicBiller loadDynamicBiller = new LoadDynamicBiller();

        logInfo("batchBillerPaymentConfig.getReportTemplateId [%d] :", batchBillerPaymentConfig.getReportTemplateId());

        BoBillerTemplateConfig boBillerTemplateConfig = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateConfigDtls(batchBillerPaymentConfig.getReportTemplateId());

        logInfo("batchBillerPaymentConfig.getReportTemplateId() [%d] :", boBillerTemplateConfig.getTemplateId());

        List<BoBillerTemplateTagConfig> boBillerTemplateTagConfigLst = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagConfigDtls(boBillerTemplateConfig.getTemplateId());

        logInfo("boBillerTemplateTagConfigLst.size [%d] :", boBillerTemplateTagConfigLst.size());

        loadIbkPaymentTemplate.setTemplateName(boBillerTemplateConfig.getTemplateName());
        loadIbkPaymentTemplate.setViewName(boBillerTemplateConfig.getViewName());

        stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_TEMPLATE_NAME, boBillerTemplateConfig.getTemplateName());

        for (BoBillerTemplateTagConfig boBillerTemplateTagConfig : boBillerTemplateTagConfigLst) {
            List<BoBillerTemplateTagFieldConfig> boBillerTemplateTagFieldConfigList = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagFieldConfigDtls(boBillerTemplateTagConfig.getTemplateTagId());

            boBillerTemplateTagConfig.setTagFields(boBillerTemplateTagFieldConfigList);

            loadDynamicBiller = dynamicIBKProcessEngine(loadDynamicBiller, boBillerTemplateTagFieldConfigList,
                    boBillerTemplateTagConfig);
            logInfo("loadDynamicBiller for:[%s]", loadDynamicBiller);
        }


        loadIbkPaymentTemplate.setTemplateTags(boBillerTemplateTagConfigLst);

        logInfo("headerPrefixPattern  [%s] created successfully", loadDynamicBiller.getHeaderPrefixPattern());
        logInfo("detailPrefixPattern  [%s] created successfully", loadDynamicBiller.getDetailPrefixPattern());
        logInfo("trailerPrefixPattern  [%s] created successfully", loadDynamicBiller.getTrailerPrefixPattern());

        logInfo("headerNames  [%s] created successfully", loadDynamicBiller.getHeaderNames());
        logInfo("headerColumns  [%s] created successfully", loadDynamicBiller.getHeaderColumns());
        logInfo("detailNames  [%s] created successfully", loadDynamicBiller.getDetailNames());
        logInfo("detailColumns  [%s] created successfully", loadDynamicBiller.getDetailColumns());
        logInfo("trailerNames  [%s] created successfully", loadDynamicBiller.getTrailerNames());
        logInfo("trailerColumns  [%s] created successfully", loadDynamicBiller.getTrailerColumns());

        // Create the tokenizer to parse specific line in the file, e.g. header, detail, trailer

        LineTokenizer headerTokenizer = BatchUtils.getFixedLengthTokenizer(loadDynamicBiller.getHeaderNames(), loadDynamicBiller.getHeaderColumns());
        LineTokenizer detailTokenizer = BatchUtils.getFixedLengthTokenizer(loadDynamicBiller.getDetailNames(), loadDynamicBiller.getDetailColumns());

        LineTokenizer trailerTokenizer = !loadDynamicBiller.getTrailerNames().equals("") ? BatchUtils.getFixedLengthTokenizer(loadDynamicBiller.getTrailerNames(), loadDynamicBiller.getTrailerColumns()) : null;


        Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
        if (headerTokenizer != null) {
            tokenizerMap.put(loadDynamicBiller.getHeaderPrefixPattern(), headerTokenizer);

        }
        tokenizerMap.put(loadDynamicBiller.getDetailPrefixPattern(), detailTokenizer);
        if (trailerTokenizer != null) {
            tokenizerMap.put(loadDynamicBiller.getTrailerPrefixPattern(), trailerTokenizer);
        }

        logDebug("Tokenizer map [%s] created successfully", tokenizerMap);
        // Create the fieldmappers to map the line details to object for haeder, detail, trailer
        FieldSetMapper<BatchStagedIBKPaymentTxn> headerMapper = new BatchStagedDynamicIBKPaymentTxnHeaderMapper();
        FieldSetMapper<BatchStagedIBKPaymentTxn> detailMapper = new BatchStagedDynamicIBKPaymentTxnDetailMapper();
        FieldSetMapper<BatchStagedIBKPaymentTxn> trailerMapper = new BatchStagedDynamicIBKPaymentTxnTrailerMapper();

        Map<String, FieldSetMapper<BatchStagedIBKPaymentTxn>> fieldSetMapperMap = new HashMap<>();
        fieldSetMapperMap.put(loadDynamicBiller.getHeaderPrefixPattern(), headerMapper);
        fieldSetMapperMap.put(loadDynamicBiller.getDetailPrefixPattern(), detailMapper);
        fieldSetMapperMap.put(loadDynamicBiller.getTrailerPrefixPattern(), trailerMapper);
        logDebug("FieldSetMapper map [%s] created successfully", fieldSetMapperMap);
        // Create the linemapper that hold the tokenizers and fieldmappers
        SkipFooterLineMapper<BatchStagedIBKPaymentTxn> lineMapper = new SkipFooterLineMapper<>();

        lineMapper.setTokenizers(tokenizerMap);
        lineMapper.setFieldSetMappers(fieldSetMapperMap);
        logDebug("Line mapper [%s] created successfully", lineMapper);
        //get resource from the file
        Resource resource = getResolver(stepExecution);
        long totalLinesInFile = 0l;
        try (Stream<String> stream = Files.lines(resource.getFile().toPath())) {
            totalLinesInFile = stream.count();
        } catch (IOException e) {
            logger.error("FAILED TO GET No. Of Lines", e);
        }
        logInfo("NUMBER OF LINES : " + totalLinesInFile);
        lineMapper.setTotalItemsToRead((int) (totalLinesInFile - boBillerTemplateConfig.getLineSkipFromBottom()));

        // Create the reader that hold the linemapper and also target with the input file
        FlatFileItemReader<BatchStagedIBKPaymentTxn> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(boBillerTemplateConfig.getLineSkipFromTop());
        reader.setLineMapper(lineMapper);
        reader.setResource(resource);

        logInfo("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader");
        return reader;
    }

    public LoadDynamicBiller dynamicIBKProcessEngine(LoadDynamicBiller loadDynamicBiller, List<BoBillerTemplateTagFieldConfig> boBillerTemplateTagFieldConfigList,
                                                     BoBillerTemplateTagConfig boBillerTemplateTagConfig) {

        for (BoBillerTemplateTagFieldConfig boBillerTemplateTagField : boBillerTemplateTagFieldConfigList) {

            loadDynamicBiller = dynamicIBKProcessInternalEngine(loadDynamicBiller,
                    boBillerTemplateTagConfig, boBillerTemplateTagField);

        }
        logInfo("loadDynamicBiller [%s] ", loadDynamicBiller);
        return loadDynamicBiller;
    }

    public LoadDynamicBiller dynamicIBKProcessInternalEngine(LoadDynamicBiller loadDynamicBiller,
                                                             BoBillerTemplateTagConfig boBillerTemplateTagConfig, BoBillerTemplateTagFieldConfig boBillerTemplateTagField) {
        if (boBillerTemplateTagConfig.getTagName().contains(HEADER)) {
            loadDynamicBiller.setHeaderNames(loadDynamicBiller.getHeaderNames() + getHeaderNames(loadDynamicBiller.getHeaderNames(), boBillerTemplateTagField));
            if (loadDynamicBiller.getPreviousHeaderLength() == 1) {
                loadDynamicBiller.setHeaderColumns(loadDynamicBiller.getPreviousHeaderLength() + "-" + boBillerTemplateTagField.getLength());
                loadDynamicBiller.setPreviousHeaderLength(loadDynamicBiller.getPreviousHeaderLength() + 1);
            } else {
                int startPosition = loadDynamicBiller.getPreviousHeaderLength();
                int endPosition = loadDynamicBiller.getPreviousHeaderLength() + boBillerTemplateTagField.getLength() - 1;
                loadDynamicBiller.setHeaderColumns(loadDynamicBiller.getHeaderColumns() + "," + startPosition + "-" + endPosition);
                loadDynamicBiller.setPreviousHeaderLength(endPosition + 1);
            }

            loadDynamicBiller = setHeaderPrefixPattern(boBillerTemplateTagField, loadDynamicBiller);

        } else if (boBillerTemplateTagConfig.getTagName().contains(BODY)) {
            loadDynamicBiller.setDetailNames(loadDynamicBiller.getDetailNames() + getDetailNames(loadDynamicBiller.getDetailNames(), boBillerTemplateTagField));
            if (loadDynamicBiller.getPreviousBodyLength() == 1) {
                int endPosition = loadDynamicBiller.getPreviousBodyLength() + boBillerTemplateTagField.getLength() - 1;
                loadDynamicBiller.setDetailColumns(loadDynamicBiller.getPreviousBodyLength() + "-" + boBillerTemplateTagField.getLength());
                loadDynamicBiller.setPreviousBodyLength(endPosition + 1);
            } else {
                int startPosition = loadDynamicBiller.getPreviousBodyLength();
                int endPosition = loadDynamicBiller.getPreviousBodyLength() + boBillerTemplateTagField.getLength() - 1;
                loadDynamicBiller.setDetailColumns(loadDynamicBiller.getDetailColumns() + "," + startPosition + "-" + endPosition);

                loadDynamicBiller.setPreviousBodyLength(endPosition + 1);
            }

            loadDynamicBiller = setBodyPrefixPattern(boBillerTemplateTagField, loadDynamicBiller);


        } else if (boBillerTemplateTagConfig.getTagName().contains(FOOTER)) {
            loadDynamicBiller.setTrailerNames(loadDynamicBiller.getTrailerNames() + getTrailerNames(loadDynamicBiller.getTrailerNames(), boBillerTemplateTagField));
            if (loadDynamicBiller.getPreviousTrailerLength() == 1) {
                loadDynamicBiller.setTrailerColumns(loadDynamicBiller.getPreviousTrailerLength() + "-" + boBillerTemplateTagField.getLength());
                loadDynamicBiller.setPreviousTrailerLength(loadDynamicBiller.getPreviousTrailerLength() + 1);
            } else {
                int startPosition = loadDynamicBiller.getPreviousTrailerLength();
                int endPosition = loadDynamicBiller.getPreviousTrailerLength() + boBillerTemplateTagField.getLength() - 1;

                loadDynamicBiller.setTrailerColumns(loadDynamicBiller.getTrailerColumns() + "," + startPosition + "-" + endPosition);
                loadDynamicBiller.setPreviousTrailerLength(endPosition + 1);
            }

            loadDynamicBiller = setTrailerPrefixPattern(boBillerTemplateTagField, loadDynamicBiller);

        }

        return loadDynamicBiller;
    }

    public LoadDynamicBiller setHeaderPrefixPattern(BoBillerTemplateTagFieldConfig boBillerTemplateTagField, LoadDynamicBiller loadDynamicBiller) {

        if (boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE)) {
            loadDynamicBiller.setHeaderPrefixPattern(getPrefixPattern(boBillerTemplateTagField, loadDynamicBiller, HEADER));
        }

        return loadDynamicBiller;
    }

    public LoadDynamicBiller setBodyPrefixPattern(BoBillerTemplateTagFieldConfig boBillerTemplateTagField, LoadDynamicBiller loadDynamicBiller) {

        if (boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE)) {
            loadDynamicBiller.setDetailPrefixPattern(getPrefixPattern(boBillerTemplateTagField, loadDynamicBiller, BODY));
        }

        return loadDynamicBiller;
    }

    public LoadDynamicBiller setTrailerPrefixPattern(BoBillerTemplateTagFieldConfig boBillerTemplateTagField, LoadDynamicBiller loadDynamicBiller) {

        if (boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE)) {
            loadDynamicBiller.setTrailerPrefixPattern(getPrefixPattern(boBillerTemplateTagField, loadDynamicBiller, FOOTER));
        }

        return loadDynamicBiller;
    }

    public String getHeaderNames(String headerNames, BoBillerTemplateTagFieldConfig boBillerTemplateTagField) {
        return headerNames.equals("") ? boBillerTemplateTagField.getFieldName() : "," + boBillerTemplateTagField.getFieldName();
    }

    public String getDetailNames(String detailNames, BoBillerTemplateTagFieldConfig boBillerTemplateTagField) {
        return detailNames.equals("") ? boBillerTemplateTagField.getViewFieldName() : "," + boBillerTemplateTagField.getViewFieldName();
    }

    public String getTrailerNames(String trailerNames, BoBillerTemplateTagFieldConfig boBillerTemplateTagField) {
        return trailerNames.equals("") ? boBillerTemplateTagField.getFieldName() : "," + boBillerTemplateTagField.getFieldName();
    }

    public String getPrefixPattern(BoBillerTemplateTagFieldConfig boBillerTemplateTagField, LoadDynamicBiller loadDynamicBiller, String recordType) {
        String prefixPattern = "";
        logInfo("getPrefixPattern fieldName[%s] ", boBillerTemplateTagField.getFieldName());
        if (boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE) && recordType.equals(HEADER) && loadDynamicBiller.getHeaderPrefixPattern().equals("")) {
            prefixPattern = boBillerTemplateTagField.getDefaultValue() + "*";
        }
        if (boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE) && recordType.equals(BODY) && loadDynamicBiller.getDetailPrefixPattern().equals("")) {
            prefixPattern = boBillerTemplateTagField.getDefaultValue() + "*";
        }
        if (boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE) && recordType.equals(FOOTER) && loadDynamicBiller.getTrailerPrefixPattern().equals("")) {
            prefixPattern = boBillerTemplateTagField.getDefaultValue() + "*";
        }
        logInfo("getPrefixPattern [%s] ", prefixPattern);
        return prefixPattern;
    }


    public Resource getResolver(StepExecution stepExecution) {
        Resource resource = null;
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // Retrieve back the input file path that saved in previous step
        if (!stepExecution.getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)) {
            logger.warn("No input file generated in previous step, skip the ItemReader");
            // Pass in empty file for the read to process so that it don't break the entire job
            resource = resolver.getResource(EMTPY_IBK_BILLER_PAYMENT_FILE_PATH);
        } else {
            String inputFilePath = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
            logDebug("Input filepath [%s] fetched from context", inputFilePath);
            resource = resolver.getResource("file:" + inputFilePath);
        }
        logDebug("Resource [%s] used in ItemReader", resource);
        return resource;
    }


    @Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxnItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        logInfo("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor");
        return new ItemProcessor<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn>() {
            private int lineNumber;

            @BeforeChunk
            public void init() {

                lineNumber = 0;
            }

            @Override
            public BatchStagedIBKPaymentTxn process(BatchStagedIBKPaymentTxn batchStagedIBKPaymentTxn) throws Exception {
                // Header condition

                if (batchStagedIBKPaymentTxn instanceof BatchStagedIBKPaymentTxnHeader) {
                    processBatchStagedIBKPaymentTxnHeader((BatchStagedIBKPaymentTxnHeader) batchStagedIBKPaymentTxn, stepExecution);
                    // Detail condition
                } else if (batchStagedIBKPaymentTxn instanceof BatchStagedIBKPaymentTxnDetail) {
                    BatchStagedIBKPaymentTxnDetail detail = (BatchStagedIBKPaymentTxnDetail) batchStagedIBKPaymentTxn;
                    processBatchStagedIBKPaymentTxnDetail(detail, stepExecution);
                    detail.setLineNo(++lineNumber);
                    logTrace("Return BatchStagedIBKPaymentTxnDetail [%s] with updated header info", detail);
                    return batchStagedIBKPaymentTxn;
                } else {
                    BatchStagedIBKPaymentTxnTrailer trailer = (BatchStagedIBKPaymentTxnTrailer) batchStagedIBKPaymentTxn;
                    logTrace("BatchStagedIBKPaymentTxnTrailer [%s]", trailer);
                }

                return null;
            }
        };
    }

    private void processBatchStagedIBKPaymentTxnHeader(BatchStagedIBKPaymentTxnHeader header, StepExecution stepExecution) throws ParseException {
        logTrace("BatchStagedIBKPaymentTxnHeader [%s]", header);
        // Validating the header process date
        String processDate = header.getProcessDate();

        String batchSystemDate = stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        Date batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT);
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String transactionDate = dateFormat.format(batchProcessingDate);

        // Storing necessary header information to context for reference later in detail section
        logInfo("Storing header ProcessDate [%s] BillerAccountName [%s] BillerAccountNo. [%s] Transaction Date [%s] to context for reference in detail later",
                processDate, header.getBillerAccountName(), header.getBillerAccountNo(), transactionDate);

        stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_PROCESS_DATE, processDate);
        stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NAME, header.getBillerAccountName());
        stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NO, header.getBillerAccountNo());
    }

    private void processBatchStagedIBKPaymentTxnDetail(BatchStagedIBKPaymentTxnDetail detail, StepExecution stepExecution) throws ParseException {
        logInfo("Original BatchStagedIBKPaymentTxnDetail [%s]", detail);
        String batchSystemDate = stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        Date batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT);
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String transactionDate = dateFormat.format(batchProcessingDate);
        detail.setTxnTime(detail.getTxnTime().replace(":", ""));
        // Setting the header information to the detail object which will be inserted to DB later
        detail.setProcessDate(transactionDate);
    }

    @Bean(STEP_NAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxnItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        logInfo("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter");
        return new ItemWriter<BatchStagedIBKPaymentTxn>() {
            @Override
            public void write(List<? extends BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxns) throws Exception {
                String inputFilePath = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
                if (logger.isDebugEnabled()) {
                    logDebug("Fetching input file path [%s] from context", inputFilePath);
                }
                String fileName = new File(inputFilePath).getName();
                String billerCode = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_CODE);

                logDebug("Fetching biller code [%s] from context", billerCode);

                String username = "";
                String billerRefNo1 = "";
                String billerRefNo2 = "";
                for (BatchStagedIBKPaymentTxn batchStagedIBKPaymentTxn : batchStagedIBKPaymentTxns) {

                    BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail = (BatchStagedIBKPaymentTxnDetail) batchStagedIBKPaymentTxn;
                    batchStagedIBKPaymentTxnDetail.setJobExecutionId(stepExecution.getJobExecution().getId().intValue());
                    batchStagedIBKPaymentTxnDetail.setFileName(fileName);
                    batchStagedIBKPaymentTxnDetail.setCreatedTime(new Date());
                    batchStagedIBKPaymentTxnDetail.setBillerCode(billerCode);
                    logTrace("Inserting BatchStagedIBKPaymentReport object [%s] to DB", batchStagedIBKPaymentTxnDetail);
                    if (StringUtils.isNotBlank(batchStagedIBKPaymentTxnDetail.getTxnId())) {
                        username = batchStagedIBKPaymentTxnDetail.getUsername();
                        billerRefNo1 = batchStagedIBKPaymentTxnDetail.getBillerRefNo1();
                        billerRefNo2 = batchStagedIBKPaymentTxnDetail.getBillerRefNo2();
                        batchStagedIBKPaymentTxnRepository.addBatchStagedIBKPaymentReportToStaging(batchStagedIBKPaymentTxnDetail);
                    } else {
                        username = StringUtils.normalizeSpace(username + " " + batchStagedIBKPaymentTxnDetail.getUsername());
                        batchStagedIBKPaymentTxnDetail.setUsername(username);
                        billerRefNo1 = StringUtils.trim(billerRefNo1) + StringUtils.trim(batchStagedIBKPaymentTxnDetail.getBillerRefNo1());
                        batchStagedIBKPaymentTxnDetail.setBillerRefNo1(billerRefNo1);
                        billerRefNo2 = StringUtils.trim(billerRefNo2) + StringUtils.trim(batchStagedIBKPaymentTxnDetail.getBillerRefNo2());
                        batchStagedIBKPaymentTxnDetail.setBillerRefNo2(billerRefNo2);
                        batchStagedIBKPaymentTxnRepository.updateBatchStagedIBKPaymentReportDetailToStaging(batchStagedIBKPaymentTxnDetail);
                    }
                }
            }
        };
    }

    @Override
    @Bean(STEP_NAME)
    public Step buildStep() {
        logInfo("Building step [%s]", STEP_NAME);

        Step step = getDefaultStepBuilder(STEP_NAME)
                .<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn>chunk(configProperties.getChunkSize())
                .reader(itemReader).faultTolerant().skipPolicy(fileVerificationSkipper()).skipLimit(1000)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();

        logInfo("[%s] step build successfully", STEP_NAME);
        return step;
    }

    private void logInfo(String format, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format(format, args));
        }
    }

    private void logTrace(String format, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(String.format(format, args));
        }
    }

    private void logDebug(String format, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(format, args));
        }
    }

}
