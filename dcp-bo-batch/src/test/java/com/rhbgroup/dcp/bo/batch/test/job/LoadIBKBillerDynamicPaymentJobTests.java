package com.rhbgroup.dcp.bo.batch.test.job;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.LoadIBKBillerDynamicPaymentJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.*;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerDynamicPaymentConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class, LoadIBKBillerDynamicPaymentJobConfiguration.class, LoadIBKBillerDynamicPaymentJobTests.Config.class})
@ActiveProfiles("test")
public class LoadIBKBillerDynamicPaymentJobTests extends BaseFTPJobTest {

    private static final Logger logger = Logger.getLogger(LoadIBKBillerDynamicPaymentJobTests.class);

    public static final String JOB_NAME = "LoadIBKBillerDynamicPaymentJob";
    public static final String JOB_LAUNCHER_UTILS = "LoadIBKBillerDynamicPaymentJobLauncherTestUtils";

    @MockBean
    private BatchBillerDynamicPaymentConfigRepositoryImpl batchBillerDynamicPaymentConfigRepositoryImpl;

    @TestConfiguration
    static class Config {
        @Bean
        @Lazy
        @Qualifier(LoadIBKBillerDynamicPaymentJobTests.JOB_LAUNCHER_UTILS)
        public JobLauncherTestUtils getLoadIBKBillerDynamicPaymentJobLauncherTestUtils() {
            return new JobLauncherTestUtils() {
                @Override
                @Autowired
                public void setJob(@Qualifier(LoadIBKBillerDynamicPaymentJobTests.JOB_NAME) Job job) {
                    super.setJob(job);
                }
            };
        }
    }

    @Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    @Lazy
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private FTPIBKConfigProperties ftpConfigProperties;

    @Autowired
    private BatchParameterRepositoryImpl batchParameterRepository;

    @Before
    public void beforeLocalTest() throws IOException {
        setCustomFTP(ftpConfigProperties);
        super.beforeFTPTest();

        BoBillerTemplateConfig boBillerTemplateConfig = new BoBillerTemplateConfig();
        boBillerTemplateConfig.setTemplateId(1);
        boBillerTemplateConfig.setTemplateCode("T01");
        boBillerTemplateConfig.setTemplateName("Template_01");
        boBillerTemplateConfig.setViewName("vw_batch_biller_payment_txn_template");
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateConfigDtls(1)).thenReturn(boBillerTemplateConfig);

        boBillerTemplateConfig = new BoBillerTemplateConfig();
        boBillerTemplateConfig.setTemplateId(9);
        boBillerTemplateConfig.setTemplateCode("T10");
        boBillerTemplateConfig.setTemplateName("Template_10");
        boBillerTemplateConfig.setViewName("vw_batch_biller_payment_txn_template");
        boBillerTemplateConfig.setLineSkipFromBottom(4);
        boBillerTemplateConfig.setLineSkipFromTop(8);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateConfigDtls(9)).thenReturn(boBillerTemplateConfig);

        List<BoBillerTemplateTagConfig> boBillerTemplateTagConfigList = new ArrayList<>();
        BoBillerTemplateTagConfig boBillerTemplateTagConfig = new BoBillerTemplateTagConfig();
        boBillerTemplateTagConfig.setTemplateId(1);
        boBillerTemplateTagConfig.setTemplateTagId(1);
        boBillerTemplateTagConfig.setTagName("T01_HEADER");
        boBillerTemplateTagConfig.setRecurring(false);
        boBillerTemplateTagConfig.setSequence(0);
        boBillerTemplateTagConfigList.add(boBillerTemplateTagConfig);

        boBillerTemplateTagConfig = new BoBillerTemplateTagConfig();
        boBillerTemplateTagConfig.setTemplateId(1);
        boBillerTemplateTagConfig.setTemplateTagId(2);
        boBillerTemplateTagConfig.setTagName("T01_BODY");
        boBillerTemplateTagConfig.setRecurring(true);
        boBillerTemplateTagConfig.setSequence(1);
        boBillerTemplateTagConfigList.add(boBillerTemplateTagConfig);

        boBillerTemplateTagConfig = new BoBillerTemplateTagConfig();
        boBillerTemplateTagConfig.setTemplateId(1);
        boBillerTemplateTagConfig.setTemplateTagId(3);
        boBillerTemplateTagConfig.setTagName("T01_FOOTER");
        boBillerTemplateTagConfig.setRecurring(false);
        boBillerTemplateTagConfig.setSequence(2);
        boBillerTemplateTagConfigList.add(boBillerTemplateTagConfig);

        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagConfigDtls(1)).thenReturn(boBillerTemplateTagConfigList);

        List<BoBillerTemplateTagConfig> boBillerTemplateTagConfig2List = new ArrayList<>();
        boBillerTemplateTagConfig = new BoBillerTemplateTagConfig();
        boBillerTemplateTagConfig.setTemplateId(9);
        boBillerTemplateTagConfig.setTemplateTagId(24);
        boBillerTemplateTagConfig.setTagName("T10_BODY");
        boBillerTemplateTagConfig.setRecurring(true);
        boBillerTemplateTagConfig.setSequence(1);
        boBillerTemplateTagConfig2List.add(boBillerTemplateTagConfig);

        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagConfigDtls(9)).thenReturn(boBillerTemplateTagConfig2List);

		/*
		*
		select CONCAT('fieldConfigList1.add(constructFieldConfig(',TEMPLATE_FIELD_ID, ',',
			TEMPLATE_TAG_ID,',"',
			FIELD_NAME,'","',
			FIELD_TYPE,'",',
			LENGTH,',',
			case
			when IS_MANDATORY = 0 then
			'false'
			else 'true'
			end,',"',
			VALUE_TYPE,'",',
			case
			when DEFAULT_VALUE is null then
			'null'
			else CONCAT('"',DEFAULT_VALUE,'"')
			end,',',
			case when IS_AGGREGATION_REQUIRED = 0 then
			'false'
			else 'true'
			end,',',
			case
			when AGGREGATION_TYPE is null then
			'null'
			else CONCAT('"',AGGREGATION_TYPE,'"')
			end,',',
			case when IS_PADDING_REQUIRED = 0 then
			'false'
			else 'true'
			end,',"',
			PADDING_TYPE,'","',
			PADDING_FILL_VALUE,'",',
			case
			when VIEW_FIELD_NAME is null then
			'null'
			else CONCAT('"',VIEW_FIELD_NAME,'"')
			end,',',
			SEQUENCE,'));')
			from dcpbo.dbo.TBL_BO_BILLER_TEMPLATE_TAG_FIELD_CONFIG tbbttfc where template_tag_id  =1;
		* */
        List<BoBillerTemplateTagFieldConfig> fieldConfigList1 = new ArrayList<>();
        fieldConfigList1.add(constructFieldConfig(1, 1, "recordType", "String", 1, true, "DEFAULT", "H", false, null, true, "RIGHT", "  ", null, 0));
        fieldConfigList1.add(constructFieldConfig(2, 1, "batchNumber", "String", 4, true, "DEFAULT", "0001", false, null, true, "RIGHT", "  ", null, 1));
        fieldConfigList1.add(constructFieldConfig(3, 1, "processDate", "Date", 8, true, "VIEW", null, false, null, true, "RIGHT", "  ", "txn_date", 2));
        fieldConfigList1.add(constructFieldConfig(4, 1, "billerAccountNumber", "Integer", 14, true, "VIEW", null, false, null, true, "RIGHT", "  ", "biller_account_no", 3));
        fieldConfigList1.add(constructFieldConfig(5, 1, "billerAccountName", "String", 20, true, "VIEW", null, false, null, true, "RIGHT", "  ", "biller_account_name", 4));
        fieldConfigList1.add(constructFieldConfig(6, 1, "filler", "String", 105, true, "DEFAULT", null, false, null, true, "RIGHT", "  ", null, 5));
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagFieldConfigDtls(1)).thenReturn(fieldConfigList1);

        List<BoBillerTemplateTagFieldConfig> fieldConfigList2 = new ArrayList<>();
        fieldConfigList2.add(constructFieldConfig(7, 2, "recordType", "String", 1, true, "DEFAULT", "D", false, null, true, "RIGHT", "  ", null, 0));
        fieldConfigList2.add(constructFieldConfig(8, 2, "transactionId", "Integer", 6, true, "VIEW", null, false, null, true, "LEFT", "0", "txn_id", 1));
        fieldConfigList2.add(constructFieldConfig(9, 2, "transactionDate", "Date", 8, true, "VIEW", null, false, null, true, "RIGHT", "  ", "txn_date", 2));
        fieldConfigList2.add(constructFieldConfig(10, 2, "transactionAmount", "Integer", 15, true, "VIEW", null, false, null, true, "LEFT", "0", "txn_amount", 3));
        fieldConfigList2.add(constructFieldConfig(11, 2, "transactionType", "String", 2, true, "DEFAULT", "CR", false, null, true, "RIGHT", "  ", null, 4));
        fieldConfigList2.add(constructFieldConfig(12, 2, "transactionDescription", "String", 20, true, "DEFAULT", null, false, null, true, "RIGHT", "  ", null, 5));
        fieldConfigList2.add(constructFieldConfig(13, 2, "billReferenceNo1", "String", 25, true, "VIEW", null, false, null, true, "RIGHT", "  ", "biller_ref_no1", 6));
        fieldConfigList2.add(constructFieldConfig(14, 2, "billReferenceNo2", "String", 25, true, "VIEW", null, false, null, true, "RIGHT", "  ", "biller_ref_no2", 7));
        fieldConfigList2.add(constructFieldConfig(15, 2, "billReferenceNo3", "String", 32, true, "VIEW", null, false, null, true, "RIGHT", "  ", "biller_ref_no3", 8));
        fieldConfigList2.add(constructFieldConfig(16, 2, "time", "Time", 6, true, "VIEW", null, false, null, true, "RIGHT", "  ", "txn_time", 9));
        fieldConfigList2.add(constructFieldConfig(17, 2, "filler", "String", 12, true, "DEFAULT", null, false, null, true, "RIGHT", "  ", null, 10));
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagFieldConfigDtls(2)).thenReturn(fieldConfigList2);

        List<BoBillerTemplateTagFieldConfig> fieldConfigList3 = new ArrayList<>();
        fieldConfigList3.add(constructFieldConfig(18, 3, "recordType", "String", 1, true, "VALUE", "T", false, null, true, "RIGHT", "  ", null, 0));
        fieldConfigList3.add(constructFieldConfig(19, 3, "processingFlag", "String", 1, true, "VALUE", "Y", false, null, true, "RIGHT", "  ", null, 1));
        fieldConfigList3.add(constructFieldConfig(20, 3, "batchTotal", "Integer", 8, true, "VIEW", null, true, "COUNT", true, "LEFT", "0", "biller_code", 2));
        fieldConfigList3.add(constructFieldConfig(21, 3, "batchAmount", "Integer", 15, true, "VIEW", null, true, "SUM", true, "LEFT", "0", "txn_amount", 3));
        fieldConfigList3.add(constructFieldConfig(22, 3, "hashTotal", "Integer", 15, true, "HASH", null, false, null, true, "LEFT", "0", null, 4));
        fieldConfigList3.add(constructFieldConfig(23, 3, "filler", "String", 112, true, "DEFAULT", null, false, null, true, "RIGHT", "  ", null, 5));
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagFieldConfigDtls(3)).thenReturn(fieldConfigList3);

        List<BoBillerTemplateTagFieldConfig> fieldConfigList24 = new ArrayList<>();
        fieldConfigList24.add(constructFieldConfig(203,24,"recordType","String",8,true,"DEFAULT","",false,null,true,"RIGHT","  ","txn_date",0));
        fieldConfigList24.add(constructFieldConfig(204,24,"transactionId","Integer",6,true,"VIEW",null,false,null,true,"LEFT","0","txn_id",1));
        fieldConfigList24.add(constructFieldConfig(205,24,"filler1","String",2,true,"VIEW",null,false,null,true,"LEFT","0",null,2));
        fieldConfigList24.add(constructFieldConfig(206,24,"transactionDate","String",10,true,"VIEW",null,false,null,true,"RIGHT","  ",null,3));
        fieldConfigList24.add(constructFieldConfig(207,24,"filler2","String",1,true,"VIEW",null,false,null,true,"LEFT","0",null,4));
        fieldConfigList24.add(constructFieldConfig(208,24,"time","String",8,true,"VIEW",null,false,null,true,"RIGHT","  ","txn_time",5));
        fieldConfigList24.add(constructFieldConfig(209,24,"filler3","String",6,true,"VIEW",null,false,null,true,"LEFT","0",null,6));
        fieldConfigList24.add(constructFieldConfig(210,24,"username","String",24,true,"VIEW",null,false,null,true,"LEFT","  ","username",7));
        fieldConfigList24.add(constructFieldConfig(211,24,"filler4","String",1,true,"VIEW",null,false,null,true,"LEFT","0",null,8));
        fieldConfigList24.add(constructFieldConfig(212,24,"billReferenceNo1","String",16,true,"VIEW",null,false,null,true,"LEFT","  ","biller_ref_no1",9));
        fieldConfigList24.add(constructFieldConfig(213,24,"filler5","String",2,true,"VIEW",null,false,null,true,"LEFT","0",null,10));
        fieldConfigList24.add(constructFieldConfig(214,24,"billReferenceNo2","String",15,true,"VIEW",null,false,null,true,"LEFT","  ","biller_ref_no2",11));
        fieldConfigList24.add(constructFieldConfig(215,24,"filler6","String",1,true,"VIEW",null,false,null,true,"LEFT","0",null,12));
        fieldConfigList24.add(constructFieldConfig(216,24,"transactionAmount","Integer",15,true,"VIEW",null,false,null,true,"LEFT","0","txn_amount",13));
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateTagFieldConfigDtls(24)).thenReturn(fieldConfigList24);

    }

    @After
    public void afterLocalTest() throws Exception {
        setCustomFTP(null);
        super.afterFTPTest();
    }

    /*
     * Test to test using batch system date from the DB and calculate the processing date from it and use it on the JOB execution
     */
    @Test
    public void testPositiveJobWithMultilineUserName() throws Exception {


        BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

        String billerCode = "5052";

        File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s14122021.rpt", billerCode, "YayasanT"));

        String workingDir = System.getProperty("user.dir");
        // Create in target for easy removal
        String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);

        // Need to prefix with working directory because we might not have permission in other folder level
        uploadFileToFTPFolder(file, fileFolderPath);

        file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s14122021.TXT", billerCode, "YayasanT"));

        workingDir = System.getProperty("user.dir");
        // Create in target for easy removal
        fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);

        // Need to prefix with working directory because we might not have permission in other folder level
        uploadFileToFTPFolder(file, fileFolderPath);

        String beforeSQL4 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'Yayasan Terengganu'";
        jdbcTemplate.batchUpdate(beforeSQL4);
        batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2021-12-15");

        List<BillerPaymentInboundConfig> billerPaymentInboundConfigList = new ArrayList<>();
        BillerPaymentInboundConfig billerPaymentInboundConfig = new BillerPaymentInboundConfig();
        billerPaymentInboundConfig.setBillerCode(billerCode);
        billerPaymentInboundConfig.setTemplateName("Standard_01");
        billerPaymentInboundConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
        billerPaymentInboundConfig.setFtpFolder(String.format("dcp_bpf_%s_from", billerCode));
        billerPaymentInboundConfig.setFileNameFormat("YayasanT${ddMMyyyy}.TXT");
        billerPaymentInboundConfig.setReportNameFormat("YayasanT${ddMMyyyy}.rpt");
        billerPaymentInboundConfig.setReportUnitUri("/reports/PROD/Financial/DMBUD999/daily_successful_bill");
        billerPaymentInboundConfig.setStatus("A");
        billerPaymentInboundConfigList.add(billerPaymentInboundConfig);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerConfigInbound()).thenReturn(billerPaymentInboundConfigList);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerConfigInboundReport()).thenReturn(billerPaymentInboundConfigList);

        BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
        batchBillerPaymentConfig.setTemplateId(1);
        batchBillerPaymentConfig.setReportTemplateId(9);
        batchBillerPaymentConfig.setBillerCode(billerCode);
        batchBillerPaymentConfig.setFtpFolder(String.format("dcp_bpf_%s_from", billerCode));
        batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
        batchBillerPaymentConfig.setReportUnitUri("/reports/PROD/Financial/DMBUD999/daily_successful_bill");
        batchBillerPaymentConfig.setStatus("A");
        batchBillerPaymentConfig.setRequiredToExecute(true);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerPaymentConfigDtls(eq(billerCode))).thenReturn(batchBillerPaymentConfig);

        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", new Date())
                .addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());

        String afterSQL1 = "SELECT COUNT(*) FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'Yayasan Terengganu' GROUP BY BILLER_ACCOUNT_NAME";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(afterSQL1);
        long counter = (long) results.get(0).get("C1");
        assertEquals(2, counter);

        String afterSQL5 = String.format("SELECT username,BILLER_REF_NO1,BILLER_REF_NO2 FROM TBL_BATCH_STAGED_IBK_PAYMENT_RPT WHERE BILLER_CODE = '%s' ", billerCode);
        results = jdbcTemplate.queryForList(afterSQL5);
        assertEquals("INAZ MIB NWELDAL H DSAUJ LRUSU", (String) results.get(0).get("username"));
        assertEquals("INAZ MIB NWELDAL H", (String) results.get(1).get("username"));
        String afterSQL2 = String.format("DELETE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE = '%s'", billerCode);
        String afterSQL3 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'Yayasan Terengganu'";
        String afterSQL4 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 1";
        jdbcTemplate.batchUpdate(afterSQL2, afterSQL3, afterSQL4);

        String afterSQL6 = String.format("SELECT ID,JOB_EXECUTION_ID,PROCESS_DATE,BILLER_CODE,TXN_ID,TXN_DATE,TXN_AMOUNT,BILLER_REF_NO1,BILLER_REF_NO2,TXN_TIME,FILE_NAME,USERNAME,LINE_NO " +
                "FROM TBL_BATCH_STAGED_IBK_PAYMENT_RPT WHERE BILLER_CODE = '%s' ", billerCode);
        List<Map<String,Object>> rptResults = jdbcTemplate.queryForList(afterSQL6);
        rptResults.stream().forEach(logger::debug);

        batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
    }

    /*
     * Test to test using batch system date from the DB and calculate the processing date from it and use it on the JOB execution
     */
    @Test
    public void testPositiveJobWithMultiplePages() throws Exception {


        BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

        String billerCode = "5300";

        File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20220106.rpt", billerCode, "Celcom"));

        String workingDir = System.getProperty("user.dir");
        // Create in target for easy removal
        String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);

        // Need to prefix with working directory because we might not have permission in other folder level
        uploadFileToFTPFolder(file, fileFolderPath);

        file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20220106.txt", billerCode, "Celcom"));

        workingDir = System.getProperty("user.dir");
        // Create in target for easy removal
        fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);

        // Need to prefix with working directory because we might not have permission in other folder level
        uploadFileToFTPFolder(file, fileFolderPath);

        String beforeSQL4 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'Celcom Berhad'";
        jdbcTemplate.batchUpdate(beforeSQL4);
        batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2022-01-07");

        List<BillerPaymentInboundConfig> billerPaymentInboundConfigList = new ArrayList<>();
        BillerPaymentInboundConfig billerPaymentInboundConfig = new BillerPaymentInboundConfig();
        billerPaymentInboundConfig.setBillerCode(billerCode);
        billerPaymentInboundConfig.setTemplateName("Standard_01");
        billerPaymentInboundConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
        billerPaymentInboundConfig.setFtpFolder(String.format("dcp_bpf_%s_from", billerCode));
        billerPaymentInboundConfig.setFileNameFormat("Celcom${yyyyMMdd}.txt");
        billerPaymentInboundConfig.setReportNameFormat("Celcom${yyyyMMdd}.rpt");
        billerPaymentInboundConfig.setReportUnitUri("/reports/PROD/Financial/DMBUD999/daily_successful_bill");
        billerPaymentInboundConfig.setStatus("A");
        billerPaymentInboundConfigList.add(billerPaymentInboundConfig);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerConfigInbound()).thenReturn(billerPaymentInboundConfigList);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerConfigInboundReport()).thenReturn(billerPaymentInboundConfigList);

        BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
        batchBillerPaymentConfig.setTemplateId(1);
        batchBillerPaymentConfig.setReportTemplateId(9);
        batchBillerPaymentConfig.setBillerCode(billerCode);
        batchBillerPaymentConfig.setFtpFolder(String.format("dcp_bpf_%s_from", billerCode));
        batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
        batchBillerPaymentConfig.setReportUnitUri("/reports/PROD/Financial/DMBUD999/daily_successful_bill");
        batchBillerPaymentConfig.setStatus("A");
        batchBillerPaymentConfig.setRequiredToExecute(true);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerPaymentConfigDtls(eq(billerCode))).thenReturn(batchBillerPaymentConfig);

        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", new Date())
                .addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());

        String afterSQL1 = "SELECT COUNT(*) FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'Celcom Berhad' GROUP BY BILLER_ACCOUNT_NAME";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(afterSQL1);
        long counter = (long) results.get(0).get("C1");
        assertEquals(6, counter);

        String afterSQL5 = String.format("SELECT username,BILLER_REF_NO1,BILLER_REF_NO2 FROM TBL_BATCH_STAGED_IBK_PAYMENT_RPT WHERE BILLER_CODE = '%s' ", billerCode);
        results = jdbcTemplate.queryForList(afterSQL5);
        assertEquals("FAETAF BINTI MUHANSDD SUBOR", (String) results.get(0).get("username"));
        assertEquals("YTREEW DSDDD ANN", (String) results.get(1).get("username"));
        String afterSQL2 = String.format("DELETE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE = '%s'", billerCode);
        String afterSQL3 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'Celcom Berhad'";
        String afterSQL4 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 1";
        jdbcTemplate.batchUpdate(afterSQL2, afterSQL3, afterSQL4);

        String afterSQL6 = String.format("SELECT ID,JOB_EXECUTION_ID,PROCESS_DATE,BILLER_CODE,TXN_ID,TXN_DATE,TXN_AMOUNT,BILLER_REF_NO1,BILLER_REF_NO2,TXN_TIME,FILE_NAME,USERNAME,LINE_NO " +
                "FROM TBL_BATCH_STAGED_IBK_PAYMENT_RPT WHERE BILLER_CODE = '%s' ", billerCode);
        List<Map<String,Object>> rptResults = jdbcTemplate.queryForList(afterSQL6);
        rptResults.stream().forEach(logger::debug);

        batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
    }

    /*
     * Test to test using batch system date from the DB and calculate the processing date from it and use it on the JOB execution
     */
    @Test
    public void testPositiveJobWithSkipReport() throws Exception {

        BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

        String billerCode = "5052";

        File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s06012022.rpt", billerCode, "YayasanT"));

        String workingDir = System.getProperty("user.dir");
        // Create in target for easy removal
        String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);

        // Need to prefix with working directory because we might not have permission in other folder level
        uploadFileToFTPFolder(file, fileFolderPath);

        file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s06012022.TXT", billerCode, "YayasanT"));

        workingDir = System.getProperty("user.dir");
        // Create in target for easy removal
        fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);

        // Need to prefix with working directory because we might not have permission in other folder level
        uploadFileToFTPFolder(file, fileFolderPath);

        String billerCode2 = "5300";

        file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20220106.rpt", billerCode2, "Celcom"));

        workingDir = System.getProperty("user.dir");
        // Create in target for easy removal
        String fileFolderPath2 = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode2);

        // Need to prefix with working directory because we might not have permission in other folder level
        uploadFileToFTPFolder(file, fileFolderPath2);

        file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20220106.txt", billerCode2, "Celcom"));

        workingDir = System.getProperty("user.dir");
        // Create in target for easy removal
        fileFolderPath2 = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode2);

        // Need to prefix with working directory because we might not have permission in other folder level
        uploadFileToFTPFolder(file, fileFolderPath2);

        String beforeSQL4 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME IN ('Yayasan Terengganu','Celcom Berhad')";
        jdbcTemplate.batchUpdate(beforeSQL4);
        batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2022-01-07");

        List<BillerPaymentInboundConfig> billerPaymentInboundConfigList = new ArrayList<>();
        List<BillerPaymentInboundConfig> billerPaymentReportInboundConfigList = new ArrayList<>();
        BillerPaymentInboundConfig billerPaymentInboundConfig = new BillerPaymentInboundConfig();
        billerPaymentInboundConfig.setBillerCode(billerCode);
        billerPaymentInboundConfig.setTemplateName("Standard_01");
        billerPaymentInboundConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
        billerPaymentInboundConfig.setFtpFolder(String.format("dcp_bpf_%s_from", billerCode));
        billerPaymentInboundConfig.setFileNameFormat("YayasanT${ddMMyyyy}.TXT");
        billerPaymentInboundConfig.setReportNameFormat("YayasanT${ddMMyyyy}.rpt");
        billerPaymentInboundConfig.setReportUnitUri("/reports/PROD/Financial/DMBUD999/daily_successful_bill");
        billerPaymentInboundConfig.setStatus("A");
        billerPaymentInboundConfigList.add(billerPaymentInboundConfig);

        billerPaymentInboundConfig = new BillerPaymentInboundConfig();
        billerPaymentInboundConfig.setBillerCode(billerCode2);
        billerPaymentInboundConfig.setTemplateName("Standard_01");
        billerPaymentInboundConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath2));
        billerPaymentInboundConfig.setFtpFolder(String.format("dcp_bpf_%s_from", billerCode2));
        billerPaymentInboundConfig.setFileNameFormat("Celcom${yyyyMMdd}.txt");
        billerPaymentInboundConfig.setReportNameFormat("Celcom${yyyyMMdd}.rpt");
        billerPaymentInboundConfig.setReportUnitUri("/reports/PROD/Financial/DMBUD999/daily_successful_bill");
        billerPaymentInboundConfig.setStatus("A");
        billerPaymentInboundConfigList.add(billerPaymentInboundConfig);
        billerPaymentReportInboundConfigList.add(billerPaymentInboundConfig);

        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerConfigInbound()).thenReturn(billerPaymentInboundConfigList);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerConfigInboundReport()).thenReturn(billerPaymentReportInboundConfigList);

        BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
        batchBillerPaymentConfig.setTemplateId(1);
        batchBillerPaymentConfig.setReportTemplateId(9);
        batchBillerPaymentConfig.setBillerCode(billerCode);
        batchBillerPaymentConfig.setFtpFolder(String.format("dcp_bpf_%s_from", billerCode));
        batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
        batchBillerPaymentConfig.setReportUnitUri("/reports/PROD/Financial/DMBUD999/daily_successful_bill");
        batchBillerPaymentConfig.setStatus("A");
        batchBillerPaymentConfig.setRequiredToExecute(true);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerPaymentConfigDtls(eq(billerCode))).thenReturn(batchBillerPaymentConfig);

        batchBillerPaymentConfig = new BatchBillerPaymentConfig();
        batchBillerPaymentConfig.setTemplateId(1);
        batchBillerPaymentConfig.setReportTemplateId(9);
        batchBillerPaymentConfig.setBillerCode(billerCode2);
        batchBillerPaymentConfig.setFtpFolder(String.format("dcp_bpf_%s_from", billerCode2));
        batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
        batchBillerPaymentConfig.setReportUnitUri("/reports/PROD/Financial/DMBUD999/daily_successful_bill");
        batchBillerPaymentConfig.setStatus("A");
        batchBillerPaymentConfig.setRequiredToExecute(true);
        when(batchBillerDynamicPaymentConfigRepositoryImpl.getBillerPaymentConfigDtls(eq(billerCode2))).thenReturn(batchBillerPaymentConfig);


        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", new Date())
                .addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());

        String afterSQL1 = "SELECT COUNT(*),BILLER_ACCOUNT_NAME FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME IN ('Yayasan Terengganu','Celcom Berhad') GROUP BY BILLER_ACCOUNT_NAME";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(afterSQL1);
        assertEquals(2, (long) results.get(0).get("C1"));
        assertEquals(6, (long) results.get(1).get("C1"));

        String afterSQL5 = String.format("SELECT username,BILLER_REF_NO1,BILLER_REF_NO2 FROM TBL_BATCH_STAGED_IBK_PAYMENT_RPT WHERE BILLER_CODE = '%s' ", billerCode2);
        results = jdbcTemplate.queryForList(afterSQL5);
        assertEquals("FAETAF BINTI MUHANSDD SUBOR", (String) results.get(0).get("username"));
        assertEquals("YTREEW DSDDD ANN", (String) results.get(1).get("username"));
        String afterSQL2 = String.format("DELETE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE IN ('%s','%s')", billerCode, billerCode2);
        String afterSQL3 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME IN ('Yayasan Terengganu','Celcom Berhad')";
        String afterSQL4 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 1";
        jdbcTemplate.batchUpdate(afterSQL2, afterSQL3, afterSQL4);
        batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
    }

    private BoBillerTemplateTagFieldConfig constructFieldConfig(int id, int tagId, String fieldName, String fieldType, int length, boolean isMandatory, String valueType, String defaultValue,
                                                                boolean aggregationRequired, String aggregationType, boolean paddingRequired, String paddingType, String paddingFillValue,
                                                                String viewFieldName, int sequence) {
        BoBillerTemplateTagFieldConfig fieldConfig = new BoBillerTemplateTagFieldConfig();
        fieldConfig.setTemplateFieldId(id);
        fieldConfig.setTemplateTagId(tagId);
        fieldConfig.setFieldName(fieldName);
        fieldConfig.setFieldType(fieldType);
        fieldConfig.setLength(length);
        fieldConfig.setMandatory(isMandatory);
        fieldConfig.setValueType(valueType);
        fieldConfig.setDefaultValue(defaultValue);
        fieldConfig.setAggregationRequired(aggregationRequired);
        fieldConfig.setAggregationType(aggregationType);
        fieldConfig.setPaddingRequired(paddingRequired);
        fieldConfig.setPaddingType(paddingType);
        fieldConfig.setPaddingFillValue(paddingFillValue);
        fieldConfig.setViewFieldName(viewFieldName);
        fieldConfig.setSequence(sequence);
        return fieldConfig;
    }
}