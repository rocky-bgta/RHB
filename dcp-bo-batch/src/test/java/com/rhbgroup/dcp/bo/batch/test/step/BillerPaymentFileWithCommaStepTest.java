package com.rhbgroup.dcp.bo.batch.test.step;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundTxn;
import com.rhbgroup.dcp.bo.batch.job.step.BillerPaymentFileWithCommaStepBuilder;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import com.rhbgroup.dcp.bo.batch.test.step.BillerPaymentBuildFileStepTest.BillerPaymentDataPrep;
import com.rhbgroup.dcp.bo.batch.test.step.BillerPaymentBuildFileStepTest.BillerPaymentLHDN;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class BillerPaymentFileWithCommaStepTest extends BaseJobTest {
	private static final Logger logger = Logger.getLogger(BillerPaymentFileWithCommaStepTest.class);
	private static final String JOB_NAME = "BillerPaymentFileJob";
	private static final String STEP_NAME="BillerPaymentFileJobWithCommaStep";
	
	private String TXN_DATE="20180905";
	private String TXN_TIME="111213";
	private String TXN_TYPE="R";
	private String testprocessdate="2018-09-06";

	@Autowired
	@Lazy
	private BillerPaymentFileWithCommaStepBuilder billerPaymentFileWithCommaStep;
	
	@Autowired
	@Qualifier(STEP_NAME+".ItemReader")
	private ItemReader<BillerPaymentOutboundTxn> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME+".ItemProcessor")
	private ItemProcessor<BillerPaymentOutboundTxn, BillerPaymentOutboundDetail> itemProcessor;
	
    @Autowired
    @Qualifier(STEP_NAME+".ItemWriter")
    private ItemWriter<BillerPaymentOutboundDetail> itemWriter;
	
	@MockBean(name="BillPaymentConfigOutboundQueue")
	private Queue<BillerPaymentOutboundConfig> queue ;
	
	@Autowired
	private DataSource dataSource;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	private StepExecution stepExecution;
	
	private void testBillerWriter(List<BillerPaymentOutboundDetail> outboundDetails, String billerCode) throws Exception {
        StepScopeTestUtils.doInStepScope(stepExecution, () -> {
        	FlatFileItemWriter<BillerPaymentOutboundDetail> writer= billerPaymentFileWithCommaStep.billerWriter(stepExecution);
        	writer.open(stepExecution.getExecutionContext());
        	writer.write(outboundDetails);
        	writer.close();
        	return null;
        });
       String filePath= stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY);
       assertTrue(FileUtils.getFile(filePath).exists());
	}
	
	private BillerPaymentOutboundDetail testBillerProcessor(BillerPaymentOutboundTxn outboundTxn) throws Exception{
		logger.info("testing biller processor..");
		BillerPaymentOutboundDetail outboundDetail = new BillerPaymentOutboundDetail();
		try {
			outboundDetail= itemProcessor.process(outboundTxn);
			assertEquals(outboundTxn.getTxnDate() , outboundDetail.getTxnDate());
			assertEquals(outboundTxn.getBillRefNo1(), outboundDetail.getBillRefNo1());
			assertEquals(outboundTxn.getBillRefNo2(), outboundDetail.getBillRefNo2());
			assertEquals(outboundTxn.getBillRefNo3(), outboundDetail.getBillRefNo3());
		}catch(Exception ex) {
			logger.info("Exception testing biller processor="+ex.getMessage());
		}
		return outboundDetail;
	}
	
	@Test
	public void testPositiveBillerPaymentIWK() throws Exception{
		logger.info("testing biller reader..");
		try {
			String REPORT_NAME="DMBUD999_5022";
			BillerPaymentDataPrep prep = new BillerPaymentDataPrep();
			BillerPaymentOutboundConfig billerConfig=prep.billerConfigIWK();
			prep.prepBillerDataIWK();
			
			JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
	    			.addString("templatename",BillerPaymentLHDN.TEMPLATE_NAME)
	    			.addString("reportid",REPORT_NAME)
					.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
					.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, testprocessdate).toJobParameters();
			when(queue.element()).thenReturn(billerConfig);
			when(queue.poll()).thenReturn(billerConfig);
			stepExecution = getStepExecution();
			List<BillerPaymentOutboundTxn> results= 
					StepScopeTestUtils.doInStepScope(stepExecution,new Callable<List<BillerPaymentOutboundTxn>>() {
					public List<BillerPaymentOutboundTxn> call() throws Exception {
						BillerPaymentOutboundTxn paymentTxn;
						List<BillerPaymentOutboundTxn> paymentTxnLists= new ArrayList<>();
						while ( (paymentTxn=itemReader.read())!=null ) {
							paymentTxnLists.add(paymentTxn);
						}
						return paymentTxnLists;
						}
					});
	    	assertEquals(4 , results.size());
	    	assertEquals("2000.00", results.get(0).getTxnAmount());
	    	assertEquals("20180905", results.get(0).getTxnDate());
			List<BillerPaymentOutboundDetail> outboundDetails = new ArrayList<>();

			for(BillerPaymentOutboundTxn outboundTxn : results) {
				BillerPaymentOutboundDetail outboundDetail = testBillerProcessor(outboundTxn);
				outboundDetails.add(outboundDetail);
			}
			
			testBillerWriter(outboundDetails, billerConfig.getBillerCode());

		}catch(Exception ex) {
			logger.info("test Biller Reader exception:"  + ex.getLocalizedMessage());
			logger.error(ex);
		}
	}
	
	@Test
	public void testBuildStep() throws Exception {
		assertNotNull(billerPaymentFileWithCommaStep.buildStep());
	}
	
	@Test
	public void testNagativeReader() throws Exception {
		stepExecution = getStepExecution();
		assertNotNull(billerPaymentFileWithCommaStep.billerReader(stepExecution,dataSource));
	}
	
	@After
	public void cleanup() throws Exception {
		logger.info("Clean up after test..");
		delBillerTxn();
		delVwTblBiller();
		delBillerConfig();
		logger.info("Done");
	}
	
	private StepExecution getStepExecution() {
        StepExecution execution = MetaDataInstanceFactory.createStepExecution();
        execution.getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-09-06");
        return execution;
    }
	
	private void delVwTblBiller() throws Exception{
		logger.info("delete record from vw_batch_tbl_biller..");
		String deleteSQL="delete from vw_batch_tbl_biller";
		logger.info("delete sql=" + deleteSQL);
		int row = jdbcTemplate.update(deleteSQL);
		logger.info("delete row="+ row);
	}
	
	private void delBillerTxn() throws Exception {
		try {
			logger.info("delete record from vw_batch_biller_payment_txn..");
			String deleteSQL = "delete from vw_batch_biller_payment_txn ";
			logger.info("delete sql=" + deleteSQL);
			int row = jdbcTemplate.update(deleteSQL);
			logger.info("delete row="+ row);
		}catch(Exception ex) {
			logger.info("delete vw_batch_biller_payment_txn  Exception:"  + ex.getMessage());
		}
	}
	
	private void delBillerConfig() throws Exception {
		try {
			logger.info("delete record from TBL_BATCH_BILLER_PAYMENT_CONFIG..");
			String deleteSQL = "delete from TBL_BATCH_BILLER_PAYMENT_CONFIG ";
			logger.info("delete sql=" + deleteSQL);
			int row = jdbcTemplate.update(deleteSQL);
			logger.info("delete row="+ row);
		}catch(Exception ex) {
			logger.info("delete TBL_BATCH_BILLER_PAYMENT_CONFIG  Exception:"  + ex.getMessage());
		}
	}
	
	
	static class BillerPaymentIWK {
		public static final String BILLER_CODE = "5022";
		public static final String BILLER_ACCOUNT_NAME = "Indah Water Konsortium Sdn Bhd";
		public static final String BILLER_ACCOUNT_NO = "21412900305056";
		public static final String FTP_FOLDER = "${user.dir}/target/dcp_bpf_5022_IndahWater_from";
		public static final String FILE_NAME_FMT = "5022${yyyyMMdd}.TXT";
		public static final String REPORT_URI = "/reports/DEV/Financial/DMBUD999/daily_successful_bill";
		public static final String TEMPLATE_NAME = "Standard_01";
	}
	
	public class BillerPaymentDataPrep {
		
		public BillerPaymentDataPrep() {}
		
		public void prepBillerDataIWK() {
			insertBillerConfigIWK();
			insertBillerTxnIWK();
		}
		
		
		public BillerPaymentOutboundConfig billerConfigIWK() {
			BillerPaymentOutboundConfig billerConfig = new BillerPaymentOutboundConfig();
			billerConfig.setId(1);
			billerConfig.setBillerCode(BillerPaymentIWK.BILLER_CODE);
			billerConfig.setTemplateName(BillerPaymentIWK.TEMPLATE_NAME);
			billerConfig.setFtpFolder(BillerPaymentIWK.FTP_FOLDER);
			billerConfig.setFileNameFormat(BillerPaymentIWK.FILE_NAME_FMT);
			billerConfig.setReportUnitUri(BillerPaymentIWK.REPORT_URI);
			billerConfig.setStatus("A");
			billerConfig.setBillerAccNo(BillerPaymentIWK.BILLER_ACCOUNT_NO);
			billerConfig.setBillerAccName(BillerPaymentIWK.BILLER_ACCOUNT_NAME);
			return billerConfig;
		}
		
		private void insertBillerConfigIWK() {
			logger.info("insert into TBL_BATCH_BILLER_PAYMENT_CONFIG..");
			try {
				String insertSQL = String.format("insert into TBL_BATCH_BILLER_PAYMENT_CONFIG "
						+ "(BILLER_CODE, TEMPLATE_NAME, FTP_FOLDER, FILE_NAME_FORMAT,REPORT_UNIT_URI,STATUS, IS_REQUIRED_TO_EXECUTE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY, IBK_FTP_FOLDER )"
						+ " values " 
						+ "('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
						, BillerPaymentIWK.BILLER_CODE, BillerPaymentIWK.TEMPLATE_NAME, BillerPaymentIWK.FTP_FOLDER,BillerPaymentIWK.FILE_NAME_FMT,BillerPaymentIWK.REPORT_URI
						, "A","1","now","admin","now", "admin","'BPF_FROM/5022/");
				logger.info("inserted sql=" + insertSQL);
				int row = jdbcTemplate.update(insertSQL);
				logger.info("insert row="+ row);
			}catch(Exception ex) {
				logger.info("insert into TBL_BATCH_BILLER_PAYMENT_CONFIG exception:"  + ex.getMessage());
			}
		}
		
		private void insertBillerTxnIWK() {
			logger.info("insert into vw_batch_biller_payment_txn..");
			try {
				String insertSQL = String.format("insert into vw_batch_tbl_biller (biller_code,biller_collection_account_no,biller_name) values ('%s','%s','%s')"
						, BillerPaymentIWK.BILLER_CODE, BillerPaymentIWK.BILLER_ACCOUNT_NO, BillerPaymentIWK.BILLER_ACCOUNT_NAME);
				
				String insertSQL1 = "insert into vw_batch_biller_payment_txn " +
						"(biller_code,biller_account_no,biller_account_name,txn_id,txn_date,txn_amount,txn_type,txn_description,biller_ref_no1,biller_ref_no2,biller_ref_no3,txn_time)" +
						" values "+
						"('"+BillerPaymentIWK.BILLER_CODE+"','"+BillerPaymentIWK.BILLER_ACCOUNT_NO+"','"+BillerPaymentIWK.BILLER_ACCOUNT_NAME+"','20180509111213','"+TXN_DATE+"','2000.00','"+TXN_TYPE+"','','834743882','0125289475','IWK Test1','"+TXN_TIME+"')" ;
				
				String insertSQL2 = "insert into vw_batch_biller_payment_txn " +
						"(biller_code,biller_account_no,biller_account_name,txn_id,txn_date,txn_amount,txn_type,txn_description,biller_ref_no1,biller_ref_no2,biller_ref_no3,txn_time)" +
						" values "+
						"('"+BillerPaymentIWK.BILLER_CODE+"','"+BillerPaymentIWK.BILLER_ACCOUNT_NO+"','"+BillerPaymentIWK.BILLER_ACCOUNT_NAME+"','20180509111214','"+TXN_DATE+"','3100.00','"+TXN_TYPE+"','','834743883','0125289476','IWK Test2','"+TXN_TIME+"')" ;
				int []row = jdbcTemplate.batchUpdate(insertSQL, insertSQL1, insertSQL2);
				logger.info("added row="+row.length);
			}catch(Exception ex) {
				logger.info("insert into vw_batch_biller_payment_txn exception:"  + ex.getMessage());
			}
		}
		
	}

}
