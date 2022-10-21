package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BillerPaymentFileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundTxn;

@Component
@Lazy
public class BillerPaymentFileWithCommaStepBuilder extends BillerPaymentFileJobBaseStepBuilder{
	private static final Logger logger = Logger.getLogger(BillerPaymentFileWithCommaStepBuilder.class);

	private static final String STEP_NAME="BillerPaymentFileJobWithCommaStep";
	
    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    
    @Autowired
    private BillerPaymentFileJobConfigProperties configProperties;
    
    @Autowired
    @Qualifier( STEP_NAME+".ItemReader")
    private ItemReader<BillerPaymentOutboundTxn> itemReader;
    
    @Autowired
    @Qualifier(STEP_NAME+".ItemProcessor")
    private ItemProcessor<BillerPaymentOutboundTxn, BillerPaymentOutboundDetail> itemProcessor;
    
    @Autowired
    @Qualifier(STEP_NAME+".ItemWriter")
    private ItemWriter<BillerPaymentOutboundDetail> itemWriter;
    
    //reader
	@Bean(STEP_NAME+".ItemReader")
    @StepScope
	public JdbcPagingItemReader<BillerPaymentOutboundTxn> billerPaymentFileReader (@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) {
		return super.billerReader(stepExecution, dataSource);
	}
	
	//processor
	@Bean(STEP_NAME+".ItemProcessor")
	@StepScope
	public ItemProcessor<BillerPaymentOutboundTxn, BillerPaymentOutboundDetail> billerPaymentFileProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.debug(String.format( "Item processor for hash total with digit grouping, biller code=%s ", billerConfig.getBillerCode()));
		return super.billerProcessor(stepExecution);
	}
	
	//writer
	@Bean(STEP_NAME+".ItemWriter")
	@StepScope
    public FlatFileItemWriter<BillerPaymentOutboundDetail> billerPaymentFileWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.debug(String.format( "Item writer for hash total with digit grouping, biller code=%s ", billerConfig.getBillerCode()));
		return super.billerWriter(stepExecution);
	}
	
	@Override
	protected String formatHashTotal() {
		return String.format("%,.6f", BatchUtils.roundDecimal(hashTotal,6));
	}
	
	@Override
	protected String getOutboundFileName() {
		return BatchUtils.generateSourceFileName(billerConfig.getFileNameFormat().replace("${", configProperties.getMiddleName().concat("${")), fileNameSystemDate) ;
	}
	
	@Override
    @Bean
    public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME)
				.<BillerPaymentOutboundTxn,BillerPaymentOutboundDetail>chunk(100)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

}
