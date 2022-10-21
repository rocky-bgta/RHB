package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.vo.LoadDynamicBiller;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKBillerPaymentJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.*;
import com.rhbgroup.dcp.bo.batch.job.model.*;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerDynamicPaymentConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBKPaymentTxnRepositoryImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Lazy
public class LoadIBKBillerDynamicPaymentFileToStagingStepBuilder extends BaseStepBuilder {

    private static final Logger logger = Logger.getLogger(LoadIBKBillerDynamicPaymentFileToStagingStepBuilder.class);

    private static final String STEP_NAME = "LoadIBKBillerDynamicPaymentFileToStagingStep";
    private static final String RECORD_TYPE ="recordType";
    private static final String HEADER ="HEADER";
    private static final String BODY ="BODY";
    private static final String FOOTER ="FOOTER";
	private  static final String TEMPLATE_SIX="Template_06";
	protected String transactionDate;

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

    LoadIbkPaymentTemplate loadIbkPaymentTemplate = null;

    @Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public FlatFileItemReader<BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxnItemReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));

        String billerCode = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_CODE);

        logger.info(String.format("billerCode [%s] :", billerCode));
        loadIbkPaymentTemplate = new LoadIbkPaymentTemplate();

        BatchBillerPaymentConfig batchBillerPaymentConfig = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerPaymentConfigDtls(billerCode);
        logger.info(String.format("batchBillerPaymentConfig.getIbkFtpFolder [%s] :", batchBillerPaymentConfig.getIbkFtpFolder()));

        //	RECORD_TYPE,batchNumber,processDate,billerAccountNo,billerAccountName,filter - 1-1,2-5,6-13,14-27,28-47,48-152

        LoadDynamicBiller loadDynamicBiller=new LoadDynamicBiller();
            logger.info(String.format("batchBillerPaymentConfig.getTemplateId [%d] :", batchBillerPaymentConfig.getTemplateId()));

            BoBillerTemplateConfig boBillerTemplateConfig = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateConfigDtls(batchBillerPaymentConfig.getTemplateId());
            logger.info(String.format("batchBillerPaymentConfig.getTemplateId() [%d] :", boBillerTemplateConfig.getTemplateId()));

            List<BoBillerTemplateTagConfig> boBillerTemplateTagConfigLst = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagConfigDtls(boBillerTemplateConfig.getTemplateId());
            logger.info(String.format("boBillerTemplateTagConfigLst.size [%d] :", boBillerTemplateTagConfigLst.size()));

            loadIbkPaymentTemplate.setTemplateName(boBillerTemplateConfig.getTemplateName());
            loadIbkPaymentTemplate.setViewName(boBillerTemplateConfig.getViewName());

            stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_TEMPLATE_NAME, boBillerTemplateConfig.getTemplateName());

            for (BoBillerTemplateTagConfig boBillerTemplateTagConfig : boBillerTemplateTagConfigLst) {
                List<BoBillerTemplateTagFieldConfig> boBillerTemplateTagFieldConfigList = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagFieldConfigDtls(boBillerTemplateTagConfig.getTemplateTagId());

                boBillerTemplateTagConfig.setTagFields(boBillerTemplateTagFieldConfigList);

                 loadDynamicBiller= dynamicIBKProcessEngine( loadDynamicBiller, boBillerTemplateTagFieldConfigList,
                         boBillerTemplateTagConfig);
                logger.info(String.format("loadDynamicBiller for:[%s]", loadDynamicBiller));
            }


            loadIbkPaymentTemplate.setTemplateTags(boBillerTemplateTagConfigLst);



		logger.info(String.format("headerPrefixPattern  [%s] created successfully", loadDynamicBiller.getHeaderPrefixPattern()));
		logger.info(String.format("detailPrefixPattern  [%s] created successfully", loadDynamicBiller.getDetailPrefixPattern()));
		logger.info(String.format("trailerPrefixPattern  [%s] created successfully", loadDynamicBiller.getTrailerPrefixPattern()));

        logger.info(String.format("headerNames  [%s] created successfully", loadDynamicBiller.getHeaderNames()));
        logger.info(String.format("headerColumns  [%s] created successfully", loadDynamicBiller.getHeaderColumns()));
		logger.info(String.format("detailNames  [%s] created successfully", loadDynamicBiller.getDetailNames()));
		logger.info(String.format("detailColumns  [%s] created successfully", loadDynamicBiller.getDetailColumns()));
		logger.info(String.format("trailerNames  [%s] created successfully", loadDynamicBiller.getTrailerNames()));
		logger.info(String.format("trailerColumns  [%s] created successfully", loadDynamicBiller.getTrailerColumns()));

        // Create the tokenizer to parse specific line in the file, e.g. header, detail, trailer

        LineTokenizer headerTokenizer = BatchUtils.getFixedLengthTokenizer(loadDynamicBiller.getHeaderNames(), loadDynamicBiller.getHeaderColumns());
        LineTokenizer detailTokenizer = BatchUtils.getFixedLengthTokenizer(loadDynamicBiller.getDetailNames(),loadDynamicBiller.getDetailColumns());

        LineTokenizer trailerTokenizer = !loadDynamicBiller.getTrailerNames().equals("") ? BatchUtils.getFixedLengthTokenizer(loadDynamicBiller.getTrailerNames(),loadDynamicBiller.getTrailerColumns()): null;


        Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
        tokenizerMap.put(loadDynamicBiller.getHeaderPrefixPattern(), headerTokenizer);
        tokenizerMap.put(loadDynamicBiller.getDetailPrefixPattern(), detailTokenizer);
        if(trailerTokenizer!=null) {
            tokenizerMap.put(loadDynamicBiller.getTrailerPrefixPattern(), trailerTokenizer);
        }
        logger.debug(String.format("Tokenizer map [%s] created successfully", tokenizerMap));

        // Create the fieldmappers to map the line details to object for haeder, detail, trailer
        FieldSetMapper<BatchStagedIBKPaymentTxn> headerMapper = new BatchStagedDynamicIBKPaymentTxnHeaderMapper();
        FieldSetMapper<BatchStagedIBKPaymentTxn> detailMapper = new BatchStagedDynamicIBKPaymentTxnDetailMapper();
        FieldSetMapper<BatchStagedIBKPaymentTxn> trailerMapper = new BatchStagedDynamicIBKPaymentTxnTrailerMapper();

        Map<String, FieldSetMapper<BatchStagedIBKPaymentTxn>> fieldSetMapperMap = new HashMap<>();
        fieldSetMapperMap.put(loadDynamicBiller.getHeaderPrefixPattern(), headerMapper);
        fieldSetMapperMap.put(loadDynamicBiller.getDetailPrefixPattern(), detailMapper);
        fieldSetMapperMap.put(loadDynamicBiller.getTrailerPrefixPattern(), trailerMapper);
        logger.debug(String.format("FieldSetMapper map [%s] created successfully", fieldSetMapperMap));

        // Create the linemapper that hold the tokenizers and fieldmappers
        PatternMatchingCompositeLineMapper<BatchStagedIBKPaymentTxn> lineMapper = new PatternMatchingCompositeLineMapper<>();
        lineMapper.setTokenizers(tokenizerMap);
        lineMapper.setFieldSetMappers(fieldSetMapperMap);
        logger.debug(String.format("Line mapper [%s] created successfully", lineMapper));

        //get resource from the file
        Resource resource = getResolver(stepExecution);

        // Create the reader that hold the linemapper and also target with the input file
        FlatFileItemReader<BatchStagedIBKPaymentTxn> reader = new FlatFileItemReader<>();
        reader.setLineMapper(lineMapper);
        reader.setResource(resource);

        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return reader;
    }
    public LoadDynamicBiller dynamicIBKProcessEngine(LoadDynamicBiller loadDynamicBiller, List<BoBillerTemplateTagFieldConfig> boBillerTemplateTagFieldConfigList,
                                                     BoBillerTemplateTagConfig boBillerTemplateTagConfig){

        for (BoBillerTemplateTagFieldConfig boBillerTemplateTagField : boBillerTemplateTagFieldConfigList) {

            loadDynamicBiller=dynamicIBKProcessInternalEngine(loadDynamicBiller,
                     boBillerTemplateTagConfig, boBillerTemplateTagField);

        }
        logger.info(String.format("loadDynamicBiller [%s] ", loadDynamicBiller));

        return loadDynamicBiller;
    }

  public  LoadDynamicBiller dynamicIBKProcessInternalEngine(LoadDynamicBiller loadDynamicBiller,
                                    BoBillerTemplateTagConfig boBillerTemplateTagConfig,BoBillerTemplateTagFieldConfig boBillerTemplateTagField) {
        if (boBillerTemplateTagConfig.getTagName().contains(HEADER) ) {
            loadDynamicBiller.setHeaderNames(loadDynamicBiller.getHeaderNames()+getHeaderNames(loadDynamicBiller.getHeaderNames(),boBillerTemplateTagField));
            if (loadDynamicBiller.getPreviousHeaderLength() == 1) {
                loadDynamicBiller.setHeaderColumns(loadDynamicBiller.getPreviousHeaderLength() + "-" + boBillerTemplateTagField.getLength());
                loadDynamicBiller.setPreviousHeaderLength(loadDynamicBiller.getPreviousHeaderLength()+1);
            } else {
                int startPosition = loadDynamicBiller.getPreviousHeaderLength();
                int endPosition = loadDynamicBiller.getPreviousHeaderLength() + boBillerTemplateTagField.getLength()-1;
                loadDynamicBiller.setHeaderColumns(loadDynamicBiller.getHeaderColumns()+"," + startPosition + "-" + endPosition);
                loadDynamicBiller.setPreviousHeaderLength(endPosition + 1);
            }
            
            loadDynamicBiller = setHeaderPrefixPattern(boBillerTemplateTagField,loadDynamicBiller);

        } else if (boBillerTemplateTagConfig.getTagName().contains(BODY)) {
            loadDynamicBiller.setDetailNames(loadDynamicBiller.getDetailNames()+getDetailNames(loadDynamicBiller.getDetailNames(),boBillerTemplateTagField));
            if (loadDynamicBiller.getPreviousBodyLength() == 1) {
                loadDynamicBiller.setDetailColumns(loadDynamicBiller.getPreviousBodyLength()  + "-" + boBillerTemplateTagField.getLength());
                loadDynamicBiller.setPreviousBodyLength(loadDynamicBiller.getPreviousBodyLength()+1);
            } else {
                int startPosition = loadDynamicBiller.getPreviousBodyLength() ;
                int endPosition = loadDynamicBiller.getPreviousBodyLength()  + boBillerTemplateTagField.getLength()-1;
                loadDynamicBiller.setDetailColumns( loadDynamicBiller.getDetailColumns() +"," + startPosition + "-" + endPosition);

                loadDynamicBiller.setPreviousBodyLength(endPosition + 1);
            }
            
            loadDynamicBiller = setBodyPrefixPattern(boBillerTemplateTagField,loadDynamicBiller);

            
        } else if (boBillerTemplateTagConfig.getTagName().contains(FOOTER)) {
            loadDynamicBiller.setTrailerNames(loadDynamicBiller.getTrailerNames()+getTrailerNames(loadDynamicBiller.getTrailerNames(),boBillerTemplateTagField));
            if (loadDynamicBiller.getPreviousTrailerLength() == 1) {
                loadDynamicBiller.setTrailerColumns(loadDynamicBiller.getPreviousTrailerLength()  + "-" + boBillerTemplateTagField.getLength());
                loadDynamicBiller.setPreviousTrailerLength(loadDynamicBiller.getPreviousTrailerLength() +1);
            } else {
                int startPosition = loadDynamicBiller.getPreviousTrailerLength() ;
                int endPosition = loadDynamicBiller.getPreviousTrailerLength()  + boBillerTemplateTagField.getLength()-1;

                loadDynamicBiller.setTrailerColumns(loadDynamicBiller.getTrailerColumns() + "," + startPosition + "-" + endPosition);
                loadDynamicBiller.setPreviousTrailerLength(endPosition + 1);
            }
            
            loadDynamicBiller = setTrailerPrefixPattern(boBillerTemplateTagField,loadDynamicBiller);
	
        }

    return loadDynamicBiller;
    }
  
  	public LoadDynamicBiller setHeaderPrefixPattern(BoBillerTemplateTagFieldConfig boBillerTemplateTagField,LoadDynamicBiller loadDynamicBiller) {
  		
        if(boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE)) {
            loadDynamicBiller.setHeaderPrefixPattern(getPrefixPattern(boBillerTemplateTagField,loadDynamicBiller,HEADER));
        }
        
        return loadDynamicBiller;
  	}
  	
	public LoadDynamicBiller setBodyPrefixPattern(BoBillerTemplateTagFieldConfig boBillerTemplateTagField,LoadDynamicBiller loadDynamicBiller) {
  		
		  if(boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE)) {
              loadDynamicBiller.setDetailPrefixPattern(getPrefixPattern(boBillerTemplateTagField, loadDynamicBiller, BODY));
          }
        
        return loadDynamicBiller;
  	}
	
	public LoadDynamicBiller setTrailerPrefixPattern(BoBillerTemplateTagFieldConfig boBillerTemplateTagField,LoadDynamicBiller loadDynamicBiller) {
  		
        if(boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE)) {
            loadDynamicBiller.setTrailerPrefixPattern(getPrefixPattern(boBillerTemplateTagField, loadDynamicBiller, FOOTER));
        }
      
      return loadDynamicBiller;
	}
  
    public String getHeaderNames(String headerNames,BoBillerTemplateTagFieldConfig boBillerTemplateTagField){
       return headerNames.equals( "") ? boBillerTemplateTagField.getFieldName() : "," + boBillerTemplateTagField.getFieldName();
    }
    public String  getDetailNames(String detailNames,BoBillerTemplateTagFieldConfig boBillerTemplateTagField){
        return  detailNames.equals("") ? boBillerTemplateTagField.getViewFieldName() : "," + boBillerTemplateTagField.getViewFieldName();
    }
    public String  getTrailerNames(String trailerNames,BoBillerTemplateTagFieldConfig boBillerTemplateTagField){
        return   trailerNames.equals( "") ? boBillerTemplateTagField.getFieldName() : "," + boBillerTemplateTagField.getFieldName();
    }
    public String getPrefixPattern(BoBillerTemplateTagFieldConfig boBillerTemplateTagField, LoadDynamicBiller loadDynamicBiller, String recordType){
        String prefixPattern="";
        logger.info(String.format("getPrefixPattern fieldName[%s] ", boBillerTemplateTagField.getFieldName()));

        if(boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE) && recordType.equals(HEADER) && loadDynamicBiller.getHeaderPrefixPattern().equals("")){
            prefixPattern =boBillerTemplateTagField.getDefaultValue()+ "*";
        }
        if(boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE) && recordType.equals(BODY)&& loadDynamicBiller.getDetailPrefixPattern().equals("")){
            prefixPattern =boBillerTemplateTagField.getDefaultValue()+ "*";
        }
        if(boBillerTemplateTagField.getFieldName().equalsIgnoreCase(RECORD_TYPE) && recordType.equals(FOOTER) && loadDynamicBiller.getTrailerPrefixPattern().equals("")){
            prefixPattern =boBillerTemplateTagField.getDefaultValue()+ "*";
        }
        logger.info(String.format("getPrefixPattern [%s] ", prefixPattern));

        return prefixPattern;
    }


    public Resource getResolver(StepExecution stepExecution){
        Resource resource=null;
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // Retrieve back the input file path that saved in previous step
        if (!stepExecution.getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)) {
            logger.warn("No input file generated in previous step, skip the ItemReader");
            // Pass in empty file for the read to process so that it don't break the entire job
            resource = resolver.getResource(EMTPY_IBK_BILLER_PAYMENT_FILE_PATH);
        } else {
            String inputFilePath = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
            logger.debug(String.format("Input filepath [%s] fetched from context", inputFilePath));
            resource = resolver.getResource("file:" + inputFilePath);
        }
        logger.debug(String.format("Resource [%s] used in ItemReader", resource));
        return resource;
    }


    @Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxnItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
        return new ItemProcessor<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn>() {
            @Override
            public BatchStagedIBKPaymentTxn process(BatchStagedIBKPaymentTxn batchStagedIBKPaymentTxn) throws Exception {
                // Header condition

                if (batchStagedIBKPaymentTxn instanceof BatchStagedIBKPaymentTxnHeader) {
                    processBatchStagedIBKPaymentTxnHeader((BatchStagedIBKPaymentTxnHeader) batchStagedIBKPaymentTxn, stepExecution);
                    // Detail condition
                } else if (batchStagedIBKPaymentTxn instanceof BatchStagedIBKPaymentTxnDetail) {
                    BatchStagedIBKPaymentTxnDetail detail = (BatchStagedIBKPaymentTxnDetail) batchStagedIBKPaymentTxn;
                    processBatchStagedIBKPaymentTxnDetail(detail, stepExecution);
                    logger.trace(String.format("Return BatchStagedIBKPaymentTxnDetail [%s] with updated header info", detail));
                    return batchStagedIBKPaymentTxn;
                } else {
                    BatchStagedIBKPaymentTxnTrailer trailer = (BatchStagedIBKPaymentTxnTrailer) batchStagedIBKPaymentTxn;
                    logger.trace(String.format("BatchStagedIBKPaymentTxnTrailer [%s]", trailer));
                }

                return null;
            }
        };
    }

    private void processBatchStagedIBKPaymentTxnHeader(BatchStagedIBKPaymentTxnHeader header, StepExecution stepExecution) throws ParseException  {
        logger.trace(String.format("BatchStagedIBKPaymentTxnHeader [%s]", header));

        // Validating the header process date
		String processDate = header.getProcessDate();

		String batchSystemDate = stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		Date batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		transactionDate = dateFormat.format(batchProcessingDate);

        // Storing necessary header information to context for reference later in detail section
        logger.info(String.format("Storing header ProcessDate [%s] BillerAccountName [%s] BillerAccountNo. [%s] Transaction Date [%s] to context for reference in detail later",
                processDate, header.getBillerAccountName(), header.getBillerAccountNo(),transactionDate));

     if( stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_TEMPLATE_NAME).equalsIgnoreCase(TEMPLATE_SIX) && !StringUtils.isBlank(processDate)){
         processDate=processDate.substring(4,processDate.length())+processDate.substring(2,4)+processDate.substring(0,2);
     }
        stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_PROCESS_DATE, processDate);
        stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NAME, header.getBillerAccountName());
		stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NO, header.getBillerAccountNo());
    }

    private void processBatchStagedIBKPaymentTxnDetail(BatchStagedIBKPaymentTxnDetail detail, StepExecution stepExecution) {
        logger.info(String.format("Original BatchStagedIBKPaymentTxnDetail [%s]", detail));

        String billerProcessDate = stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_PROCESS_DATE);
        String billerAccountName = stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NAME);
        String billerAccountNo = stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NO);
        logger.info(String.format("Header ProcessDate [%s] BillerAccountName [%s] BillerAccountNo. [%s] fetched from context",
                billerProcessDate, billerAccountName, billerAccountNo));

        if( stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_TEMPLATE_NAME).equalsIgnoreCase("Template_05") && !StringUtils.isBlank(transactionDate)){
            detail.setTxnDate(transactionDate);
        }

        if( stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_TEMPLATE_NAME).equalsIgnoreCase(TEMPLATE_SIX) && !StringUtils.isBlank(detail.getTxnDate())){
            detail.setTxnDate(detail.getTxnDate().substring(4,detail.getTxnDate().length())+detail.getTxnDate().substring(2,4)+detail.getTxnDate().substring(0,2));
        }

        if( stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_TEMPLATE_NAME).equalsIgnoreCase(TEMPLATE_SIX) && !StringUtils.isBlank(detail.getTxnAmount())){
        	detail.setTxnAmount(detail.getTxnAmount().substring(0,6)+"."+detail.getTxnAmount().substring(6,8));
        }

        if( stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_TEMPLATE_NAME).equalsIgnoreCase("Template_07") && !StringUtils.isBlank(detail.getTxnAmount())){
        	detail.setTxnAmount(detail.getTxnAmount().substring(0,7)+"."+detail.getTxnAmount().substring(7,9));
        }

        // Setting the header information to the detail object which will be inserted to DB later
        detail.setProcessDate(billerProcessDate);
        if(!billerAccountName.equals("")) {
            detail.setBillerAccountName(billerAccountName);
        }
        if(!billerAccountNo.equals("")) {
            detail.setBillerAccountNo(billerAccountNo);
        }
    }

    @Bean(STEP_NAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxnItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
        return new ItemWriter<BatchStagedIBKPaymentTxn>() {
            @Override
            public void write(List<? extends BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxns) throws Exception {
                String inputFilePath = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
                logger.debug(String.format("Fetching input file path [%s] from context", inputFilePath));
                String fileName = new File(inputFilePath).getName();
                String billerCode = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_CODE);
                logger.debug(String.format("Fetching biller code [%s] from context", billerCode));

                for(BatchStagedIBKPaymentTxn batchStagedIBKPaymentTxn : batchStagedIBKPaymentTxns) {
                    BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail = (BatchStagedIBKPaymentTxnDetail)batchStagedIBKPaymentTxn;
                    batchStagedIBKPaymentTxnDetail.setJobExecutionId(stepExecution.getJobExecution().getId().intValue());
                    batchStagedIBKPaymentTxnDetail.setFileName(fileName);
                    batchStagedIBKPaymentTxnDetail.setCreatedTime(new Date());
                    batchStagedIBKPaymentTxnDetail.setBillerCode(billerCode);

                    logger.trace(String.format("Inserting BatchStagedIBKPaymentTxn object [%s] to DB", batchStagedIBKPaymentTxnDetail));
                    batchStagedIBKPaymentTxnRepository.addBatchStagedIBKPaymentTxnToStaging(batchStagedIBKPaymentTxnDetail);
                }
            }
        };
    }

    @Override
    @Bean(STEP_NAME)
    public Step buildStep() {
        logger.info(String.format("Building step [%s]", STEP_NAME));

        Step step = getDefaultStepBuilder(STEP_NAME)
                .<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn>chunk(configProperties.getChunkSize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();

        logger.info(String.format("[%s] step build successfully", STEP_NAME));
        return step;
    }


}
