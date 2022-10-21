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
import com.rhbgroup.dcp.bo.batch.job.config.properties.BillerDynamicPaymentFileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundTxn;

@Component
@Lazy
public class BillerDynamicPaymentFileWithCommaStepBuilder extends BillerDynamicPaymentFileJobBaseStepBuilder{
	private static final Logger logger = Logger.getLogger(BillerDynamicPaymentFileWithCommaStepBuilder.class);

	private static final String STEP_NAME="BillerDynamicPaymentFileJobWithCommaStep";
	
    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    
    @Autowired
    private BillerDynamicPaymentFileJobConfigProperties configProperties;
    
    @Autowired
    @Qualifier( STEP_NAME+".ItemReader")
    private ItemReader<BillerDynamicPaymentOutboundTxn> itemReader;
    
    @Autowired
    @Qualifier(STEP_NAME+".ItemProcessor")
    private ItemProcessor<BillerDynamicPaymentOutboundTxn, BillerDynamicPaymentOutboundDetail> itemProcessor;
    
    @Autowired
    @Qualifier(STEP_NAME+".ItemWriter")
    private ItemWriter<BillerDynamicPaymentOutboundDetail> itemWriter;
    
    //reader
	@Bean(STEP_NAME+".ItemReader")
    @StepScope
	public JdbcPagingItemReader<BillerDynamicPaymentOutboundTxn> billerDynamicPaymentFileReader (@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) {
		return super.billerReader(stepExecution, dataSource);
	}
	
	//processor
	@Bean(STEP_NAME+".ItemProcessor")
	@StepScope
	public ItemProcessor<BillerDynamicPaymentOutboundTxn, BillerDynamicPaymentOutboundDetail> billerDynamicPaymentFileProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.debug(String.format( "Item processor for hash total with digit grouping, biller code=%s ", billerConfig.getBillerCode()));
		return super.billerProcessor(stepExecution);
	}
	
	//writer
	@Bean(STEP_NAME+".ItemWriter")
	@StepScope
    public FlatFileItemWriter<BillerDynamicPaymentOutboundDetail> billerDynamicPaymentFileWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
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
				.<BillerDynamicPaymentOutboundTxn,BillerDynamicPaymentOutboundDetail>chunk(100)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

}
