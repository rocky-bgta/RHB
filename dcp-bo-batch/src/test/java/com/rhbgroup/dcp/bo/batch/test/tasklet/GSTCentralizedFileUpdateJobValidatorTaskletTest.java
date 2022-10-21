package com.rhbgroup.dcp.bo.batch.test.tasklet;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.config.GSTCentralizedFileUpdateJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.model.BatchLookup;
import com.rhbgroup.dcp.bo.batch.job.model.GSTCentralizedFileUpdateDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.GSTCentralizedFileUpdateRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.GSTCentralizedFileUpdateJobValidatorTasklet;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class, GSTCentralizedFileUpdateJobConfiguration.class})
@ActiveProfiles("test")
public class GSTCentralizedFileUpdateJobValidatorTaskletTest {
	private static final Logger logger = Logger.getLogger(GSTCentralizedFileUpdateJobValidatorTaskletTest.class);
	
	String JOB_NAME="GSTCentralizedFileUpdateJob";
	String jobprocessdate = "2018-08-25";

	@MockBean(name="gstCentralizedFileUpdateRepositoryImpl")
	private GSTCentralizedFileUpdateRepositoryImpl mockGSTCentralizedFileUpdateRepository;
	
	@Autowired 
	private GSTCentralizedFileUpdateJobValidatorTasklet gstCentralizedFileUpdateJobValidatorTasklet;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	DataSource dataSource;
	
	@Mock
	StepExecution stepExecution;
	
	@Mock
	StepContribution contribution;
	
	@Mock
	StepContext stepContext;
	
	@Mock
	JobContext jobContext;
	
	@Mock
	JobExecution jobExecution;
	
	@Mock
	ChunkContext chunkContext;
	
	@Mock
	ExecutionContext executionContext;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @Test
    public void testGSTCentralizedFileUpdateJobValidator() throws Exception{
        String jobexecutionid="999999";
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", new Date())
                .addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
                .addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
                .addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
                .toJobParameters();

        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getId()).thenReturn(Long.valueOf(jobexecutionid));

        List<GSTCentralizedFileUpdateDetail> differenceGSTDetailRecord = new ArrayList<>();
        List<GSTCentralizedFileUpdateDetail> gstOldValueExtraction = new ArrayList<>();
        List<GSTCentralizedFileUpdateDetail> gstCentralizedFileStagingList = new ArrayList<>();
        List<GSTCentralizedFileUpdateDetail> gstDCPList = new ArrayList<>();

        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetailA = createGSTDetailA();
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetailSuspense = createGSTDetailSuspsense();

        // Batch lookup mockup
        //DCP_GST_TREATMENT_TYPE
        //DCP_GST_TAX_CODE
        //DCP_GST_CALCULATION_METHOD
        String lookupTreatment = "DCP_GST_TREATMENT_TYPE";
        String lookupTaxCode = "DCP_GST_TAX_CODE";
        String lookupCalculationMethod = "DCP_GST_CALCULATION_METHOD";

        List<BatchLookup> lookUpGSTTreatmentType = new ArrayList<>();
        List<BatchLookup> lookUpGSTTaxCode = new ArrayList<>();
        List<BatchLookup> lookUpGSTCalculationMethod = new ArrayList<>();

        lookUpGSTTreatmentType = createLookUpDetail(lookupTreatment);
        lookUpGSTTaxCode = createLookUpDetail(lookupTaxCode);
        lookUpGSTCalculationMethod = createLookUpDetail(lookupCalculationMethod);
        //gstCentralizedFileUpdateRepositoryImpl.getBatchLookUpValue("DCP_GST_CALCULATION_METHOD");

        differenceGSTDetailRecord.add(gstCentralizedFileUpdateDetailA);
        gstOldValueExtraction.add(gstCentralizedFileUpdateDetailA);

        int recordToBeMocked = 70;
        int gstRate = recordToBeMocked;

        for(int i = 0; i < recordToBeMocked; i++){
            GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetailB = createGSTDetailB();

            gstCentralizedFileUpdateDetailB.setUniqueId(String.valueOf(i));
            gstCentralizedFileUpdateDetailB.setGstRate(String.valueOf(i));
            // Set data to go to null value
/*
            if(i == 11) gstCentralizedFileUpdateDetailB.setEntityIndicator(null);
            if(i == 13) gstCentralizedFileUpdateDetailB.setGstRate(null);
            if(i == 14) gstCentralizedFileUpdateDetailB.setTreatmentType(null);
            if(i == 15) gstCentralizedFileUpdateDetailB.setTaxCode(null);
            if(i == 16) gstCentralizedFileUpdateDetailB.setCalculationMethod(null);
            if(i == 17) gstCentralizedFileUpdateDetailB.setStartDate(null);
            if(i == 18) gstCentralizedFileUpdateDetailB.setEndDate(null);
*/
            if(i == 19) gstCentralizedFileUpdateDetailB.setEndDate("");
            if(i == 20) gstCentralizedFileUpdateDetailB.setBeginDate("");
            if(i == 21) gstCentralizedFileUpdateDetailB.setEntityIndicator("10");
            if(i == 22) gstCentralizedFileUpdateDetailB.setEntityCode("0A");
            if(i == 23) gstCentralizedFileUpdateDetailB.setTreatmentType("01");
            if(i == 24) gstCentralizedFileUpdateDetailB.setTransactionIdentifier("IBK001_A");
            if(i == 25) gstCentralizedFileUpdateDetailB.setSourceSystem("SIR");
            if(i == 26) gstCentralizedFileUpdateDetailB.setEntityCode("OB");
            if(i == 27) gstCentralizedFileUpdateDetailB.setEntityCode("01");
            if(i == 28) gstCentralizedFileUpdateDetailB.setTreatmentType("");
            if(i == 29) gstCentralizedFileUpdateDetailB.setTaxCode("");
            if(i == 30) gstCentralizedFileUpdateDetailB.setCalculationMethod("");
            if(i == 31) gstCentralizedFileUpdateDetailB.setCalculationMethod("I");
            if(i == 32) {gstCentralizedFileUpdateDetailB.setEntityCode("0A");gstCentralizedFileUpdateDetailB.setEntityIndicator("10");}
            if(i == 33) {gstCentralizedFileUpdateDetailB.setGstRate("0");gstCentralizedFileUpdateDetailB.setTreatmentType("04");gstCentralizedFileUpdateDetailB.setEntityCode("0A");gstCentralizedFileUpdateDetailB.setEntityIndicator("10");}
            if(i == 34) {gstCentralizedFileUpdateDetailB.setTaxCode("OS");gstCentralizedFileUpdateDetailB.setGstRate("0");gstCentralizedFileUpdateDetailB.setTreatmentType("04");gstCentralizedFileUpdateDetailB.setEntityCode("0A");gstCentralizedFileUpdateDetailB.setEntityIndicator("10");}
            if(i == 35) {gstCentralizedFileUpdateDetailB.setCalculationMethod("I");gstCentralizedFileUpdateDetailB.setTaxCode("OS");gstCentralizedFileUpdateDetailB.setGstRate("0");gstCentralizedFileUpdateDetailB.setTreatmentType("04");gstCentralizedFileUpdateDetailB.setEntityCode("0A");gstCentralizedFileUpdateDetailB.setEntityIndicator("10");}
            if(i == 36) gstCentralizedFileUpdateDetailB.setGstRate("");
            if(i == 37) gstCentralizedFileUpdateDetailB.setStartDate("");
            if(i == 38) {gstCentralizedFileUpdateDetailB.setGstRate("0");gstCentralizedFileUpdateDetailB.setTreatmentType("04");gstCentralizedFileUpdateDetailB.setEntityCode("0A");gstCentralizedFileUpdateDetailB.setEntityIndicator("10");}
            if(i == 39) gstCentralizedFileUpdateDetailB.setTreatmentType("04");
            if(i == 40) gstCentralizedFileUpdateDetailB.setEntityCode("");
            if(i == 42) gstCentralizedFileUpdateDetailB.setStartDate("");
            if(i == 43) gstCentralizedFileUpdateDetailB.setEndDate("20150401");
            if(i == 45) {gstCentralizedFileUpdateDetailB.setStartDate("20150401");gstCentralizedFileUpdateDetailB.setCalculationMethod("I");gstCentralizedFileUpdateDetailB.setTaxCode("OS");gstCentralizedFileUpdateDetailB.setGstRate("0");gstCentralizedFileUpdateDetailB.setTreatmentType("04");gstCentralizedFileUpdateDetailB.setEntityCode("0A");gstCentralizedFileUpdateDetailB.setEntityIndicator("10");}
            if(i == 48) {gstCentralizedFileUpdateDetailB.setGstRate("0.0");gstCentralizedFileUpdateDetailB.setEntityCode("0A");gstCentralizedFileUpdateDetailB.setEntityIndicator("10");}
            if(i == 49) {gstCentralizedFileUpdateDetailB.setGstRate("0.0");gstCentralizedFileUpdateDetailB.setEntityCode("0A");gstCentralizedFileUpdateDetailB.setEntityIndicator("10");}

            gstCentralizedFileStagingList.add(gstCentralizedFileUpdateDetailB);
        }

        gstRate = recordToBeMocked;

        for(int i = 0; i < recordToBeMocked; i++){
            GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetailC = createGSTDetailC();

            gstCentralizedFileUpdateDetailC.setUniqueId(String.valueOf(i));
            gstCentralizedFileUpdateDetailC.setGstRate(String.valueOf(gstRate));
            // Set data to go to null value
//            if(i == 12) gstCentralizedFileUpdateDetailC.setEntityIndicator(null);
            if(i == 33) gstCentralizedFileUpdateDetailC.setGstRate("0");
            if(i == 34) gstCentralizedFileUpdateDetailC.setGstRate("0");
            if(i == 35) gstCentralizedFileUpdateDetailC.setGstRate("0");
            if(i == 38) gstCentralizedFileUpdateDetailC.setGstRate("0");
            if(i == 41) gstCentralizedFileUpdateDetailC.setTaxCode("");
            if(i == 46) gstCentralizedFileUpdateDetailC.setGstRate("0");
            if(i == 47) gstCentralizedFileUpdateDetailC.setTreatmentType("");
            if(i == 48) gstCentralizedFileUpdateDetailC.setTaxCode("");
            if(i == 49) gstCentralizedFileUpdateDetailC.setGstRate("0.0");
            if(i == 50) gstCentralizedFileUpdateDetailC.setTreatmentType("");
            if(i == 51) gstCentralizedFileUpdateDetailC.setTaxCode("");

            gstDCPList.add(gstCentralizedFileUpdateDetailC);

            gstRate--;
        }

        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetailE = createGSTDetailE();
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetailF = createGSTDetailF();

        // Create an identical record
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetailD = createGSTDetailD();
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetailG = createGSTDetailG();
        gstCentralizedFileStagingList.add(gstCentralizedFileUpdateDetailG);
        gstDCPList.add(gstCentralizedFileUpdateDetailD);

        // Insert suspense record
        gstCentralizedFileStagingList.add(gstCentralizedFileUpdateDetailA);
        gstDCPList.add(gstCentralizedFileUpdateDetailSuspense);
        gstOldValueExtraction.add(gstCentralizedFileUpdateDetailE);
        gstOldValueExtraction.add(gstCentralizedFileUpdateDetailF);

        int newGSTMaxUniqueId = Integer.parseInt(gstCentralizedFileUpdateDetailA.getNewGstMaxUniqueId());

        when(mockGSTCentralizedFileUpdateRepository.checkGSTConfigNewRecord(jobexecutionid)).thenReturn(differenceGSTDetailRecord);
        when(mockGSTCentralizedFileUpdateRepository.getGSTConfigEssentialData(gstCentralizedFileUpdateDetailA.getOldGstMaxUniqueId(), gstCentralizedFileUpdateDetailA.getOldGstSourceSystem(), gstCentralizedFileUpdateDetailA.getOldGstTxnIdentifier())).thenReturn(gstOldValueExtraction);
        when(mockGSTCentralizedFileUpdateRepository.getGSTNewValue(String.valueOf(newGSTMaxUniqueId), gstCentralizedFileUpdateDetailA.getOldGstSourceSystem(), gstCentralizedFileUpdateDetailA.getOldGstTxnIdentifier(), jobexecutionid)).thenReturn(gstCentralizedFileUpdateDetailA);
        when(mockGSTCentralizedFileUpdateRepository.getGSTNewValue(String.valueOf(newGSTMaxUniqueId), gstCentralizedFileUpdateDetailE.getOldGstSourceSystem(), gstCentralizedFileUpdateDetailE.getOldGstTxnIdentifier(), jobexecutionid)).thenReturn(gstCentralizedFileUpdateDetailE);
        when(mockGSTCentralizedFileUpdateRepository.getGSTNewValue(String.valueOf(newGSTMaxUniqueId), gstCentralizedFileUpdateDetailF.getOldGstSourceSystem(), gstCentralizedFileUpdateDetailF.getOldGstTxnIdentifier(), jobexecutionid)).thenReturn(gstCentralizedFileUpdateDetailF);
        when(mockGSTCentralizedFileUpdateRepository.insertNewGSTToDB(gstOldValueExtraction.get(0))).thenReturn(true);
        when(mockGSTCentralizedFileUpdateRepository.insertNewGSTToDB(gstOldValueExtraction.get(2))).thenReturn(false);
        when(mockGSTCentralizedFileUpdateRepository.getUnprocessedGSTCentralizedStatusFromStaging(jobexecutionid)).thenReturn(gstCentralizedFileStagingList);
        when(mockGSTCentralizedFileUpdateRepository.getGSTFromDCP()).thenReturn(gstDCPList);
        when(mockGSTCentralizedFileUpdateRepository.getBatchLookUpValue(lookupTreatment)).thenReturn(lookUpGSTTreatmentType);
        when(mockGSTCentralizedFileUpdateRepository.getBatchLookUpValue(lookupTaxCode)).thenReturn(lookUpGSTTaxCode);
        when(mockGSTCentralizedFileUpdateRepository.getBatchLookUpValue(lookupCalculationMethod)).thenReturn(lookUpGSTCalculationMethod);
        when(mockGSTCentralizedFileUpdateRepository.updateGSTDCP(any(), any())).thenReturn(true);

        assertEquals(RepeatStatus.FINISHED, gstCentralizedFileUpdateJobValidatorTasklet.execute(contribution, chunkContext));
    }

    @Test
    public void testPositiveStringValidationMethod() throws Exception{
        assertEquals(true,gstCentralizedFileUpdateJobValidatorTasklet.stringCompareIsValid("test","test"));
    }

    @Test
    public void testNegativeStringValidationMethod() throws Exception{
        assertEquals(false,gstCentralizedFileUpdateJobValidatorTasklet.stringCompareIsValid("",""));
    }

    @Test
    public void testNegativeStringValidationMethodOriginalStringError() throws Exception{
        assertEquals(false,gstCentralizedFileUpdateJobValidatorTasklet.stringCompareIsValid("Test",""));
    }

    @Test
    public void testNegativeStringValidationMethodUpdatedStringError() throws Exception{
        assertEquals(false,gstCentralizedFileUpdateJobValidatorTasklet.stringCompareIsValid(null,"test"));
    }

    // New record to be inserted
    private GSTCentralizedFileUpdateDetail createGSTDetailA() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = new GSTCentralizedFileUpdateDetail();
        gstCentralizedFileUpdateDetail.setOldGstMaxUniqueId("1");
        gstCentralizedFileUpdateDetail.setOldGstSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setOldGstTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setNewGstMaxUniqueId("2");
        gstCentralizedFileUpdateDetail.setJobExecutionId("999999");
        gstCentralizedFileUpdateDetail.setRecordIndicator("1");
        gstCentralizedFileUpdateDetail.setUniqueId("1");
        gstCentralizedFileUpdateDetail.setSourceSystemId("IBK");
        gstCentralizedFileUpdateDetail.setTransactionIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTransactionDescription("FUND TRANSFER TO OWN ACCOUNT BELOW THRESHOLD");
        gstCentralizedFileUpdateDetail.setGstRate("00.0000");
        gstCentralizedFileUpdateDetail.setTreatmentType("04");
        gstCentralizedFileUpdateDetail.setTaxCode("OS");
        gstCentralizedFileUpdateDetail.setCalculationMethod("I");
        gstCentralizedFileUpdateDetail.setGlAccountCodeCharges("");
        gstCentralizedFileUpdateDetail.setStartDate("20150320");
        gstCentralizedFileUpdateDetail.setEndDate("20150331");
        gstCentralizedFileUpdateDetail.setLastUpdateDate("20180515");
        gstCentralizedFileUpdateDetail.setUpdateTime("175359");
        gstCentralizedFileUpdateDetail.setLastUpdateBy("402849");
        gstCentralizedFileUpdateDetail.setEntityCode("");
        gstCentralizedFileUpdateDetail.setEntityIndicator("");

        return gstCentralizedFileUpdateDetail;
    }

    // New record
    private GSTCentralizedFileUpdateDetail createGSTDetailB() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = new GSTCentralizedFileUpdateDetail();
        gstCentralizedFileUpdateDetail.setOldGstMaxUniqueId("1");
        gstCentralizedFileUpdateDetail.setOldGstSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setOldGstTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setNewGstMaxUniqueId("2");
        gstCentralizedFileUpdateDetail.setJobExecutionId("999999");
        gstCentralizedFileUpdateDetail.setRecordIndicator("1");
        gstCentralizedFileUpdateDetail.setUniqueId("1");
        gstCentralizedFileUpdateDetail.setSourceSystemId("IBK");
        gstCentralizedFileUpdateDetail.setTransactionIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTransactionDescription("FUND TRANSFER TO OWN ACCOUNT BELOW THRESHOLD");
        gstCentralizedFileUpdateDetail.setGstRate("06.0000");
        gstCentralizedFileUpdateDetail.setTreatmentType("03");
        gstCentralizedFileUpdateDetail.setTaxCode("ZRL");
        gstCentralizedFileUpdateDetail.setCalculationMethod("E");
        gstCentralizedFileUpdateDetail.setGlAccountCodeCharges("");
        gstCentralizedFileUpdateDetail.setStartDate("20150401");
        gstCentralizedFileUpdateDetail.setBeginDate("2015-04-01 00:00:00.000");
        gstCentralizedFileUpdateDetail.setEndDate("20150331");
        gstCentralizedFileUpdateDetail.setLastUpdateDate("20180515");
        gstCentralizedFileUpdateDetail.setUpdateTime("175359");
        gstCentralizedFileUpdateDetail.setLastUpdateBy("402849");
        gstCentralizedFileUpdateDetail.setEntityCode("OB");
        gstCentralizedFileUpdateDetail.setEntityIndicator("01");

        return gstCentralizedFileUpdateDetail;
    }

    // Old record
    private GSTCentralizedFileUpdateDetail createGSTDetailC() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = new GSTCentralizedFileUpdateDetail();
        gstCentralizedFileUpdateDetail.setOldGstMaxUniqueId("1");
        gstCentralizedFileUpdateDetail.setOldGstSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setOldGstTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setNewGstMaxUniqueId("2");
        gstCentralizedFileUpdateDetail.setJobExecutionId("999999");
        gstCentralizedFileUpdateDetail.setRecordIndicator("1");
        gstCentralizedFileUpdateDetail.setUniqueId("1");
        gstCentralizedFileUpdateDetail.setSourceSystemId("IBK");
        gstCentralizedFileUpdateDetail.setSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setTransactionIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTransactionDescription("FUND TRANSFER TO OWN ACCOUNT BELOW THRESHOLD");
        gstCentralizedFileUpdateDetail.setGstRate("00.0000");
        gstCentralizedFileUpdateDetail.setTreatmentType("04");
        gstCentralizedFileUpdateDetail.setTaxCode("OS");
        gstCentralizedFileUpdateDetail.setCalculationMethod("I");
        gstCentralizedFileUpdateDetail.setGlAccountCodeCharges("");
        gstCentralizedFileUpdateDetail.setStartDate("20150320");
        gstCentralizedFileUpdateDetail.setBeginDate("2015-04-01 00:00:00.000");
        gstCentralizedFileUpdateDetail.setEndDate("2015-04-01 00:00:00.000");
        gstCentralizedFileUpdateDetail.setLastUpdateDate("20180515");
        gstCentralizedFileUpdateDetail.setUpdateTime("175359");
        gstCentralizedFileUpdateDetail.setLastUpdateBy("402849");
        gstCentralizedFileUpdateDetail.setEntityCode("0A");
        gstCentralizedFileUpdateDetail.setEntityIndicator("10");

        return gstCentralizedFileUpdateDetail;
    }

    // Identical record
    private GSTCentralizedFileUpdateDetail createGSTDetailD() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = new GSTCentralizedFileUpdateDetail();
        gstCentralizedFileUpdateDetail.setOldGstMaxUniqueId("1");
        gstCentralizedFileUpdateDetail.setOldGstSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setOldGstTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setNewGstMaxUniqueId("2");
        gstCentralizedFileUpdateDetail.setJobExecutionId("999999");
        gstCentralizedFileUpdateDetail.setRecordIndicator("1");
        gstCentralizedFileUpdateDetail.setUniqueId("9999");
        gstCentralizedFileUpdateDetail.setSourceSystemId("IBK");
        gstCentralizedFileUpdateDetail.setSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setTransactionIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTransactionDescription("FUND TRANSFER TO OWN ACCOUNT BELOW THRESHOLD");
        gstCentralizedFileUpdateDetail.setGstRate("00.0000");
        gstCentralizedFileUpdateDetail.setTreatmentType("04");
        gstCentralizedFileUpdateDetail.setTaxCode("OS");
        gstCentralizedFileUpdateDetail.setCalculationMethod("I");
        gstCentralizedFileUpdateDetail.setGlAccountCodeCharges("");
        gstCentralizedFileUpdateDetail.setStartDate("20150401");
        gstCentralizedFileUpdateDetail.setBeginDate("2015-04-01 00:00:00.000");
        gstCentralizedFileUpdateDetail.setEndDate("2015-04-01 00:00:00.000");
        gstCentralizedFileUpdateDetail.setLastUpdateDate("20180515");
        gstCentralizedFileUpdateDetail.setUpdateTime("175359");
        gstCentralizedFileUpdateDetail.setLastUpdateBy("402849");
        gstCentralizedFileUpdateDetail.setEntityCode("0A");
        gstCentralizedFileUpdateDetail.setEntityIndicator("10");

        return gstCentralizedFileUpdateDetail;
    }

    // Identical record
    private GSTCentralizedFileUpdateDetail createGSTDetailG() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = new GSTCentralizedFileUpdateDetail();
        gstCentralizedFileUpdateDetail.setOldGstMaxUniqueId("1");
        gstCentralizedFileUpdateDetail.setOldGstSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setOldGstTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setNewGstMaxUniqueId("2");
        gstCentralizedFileUpdateDetail.setJobExecutionId("999999");
        gstCentralizedFileUpdateDetail.setRecordIndicator("1");
        gstCentralizedFileUpdateDetail.setUniqueId("9999");
        gstCentralizedFileUpdateDetail.setSourceSystemId("IBK");
        gstCentralizedFileUpdateDetail.setSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setTransactionIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTransactionDescription("FUND TRANSFER TO OWN ACCOUNT BELOW THRESHOLD");
        gstCentralizedFileUpdateDetail.setGstRate("00.0000");
        gstCentralizedFileUpdateDetail.setTreatmentType("04");
        gstCentralizedFileUpdateDetail.setTaxCode("OS");
        gstCentralizedFileUpdateDetail.setCalculationMethod("I");
        gstCentralizedFileUpdateDetail.setGlAccountCodeCharges("");
        gstCentralizedFileUpdateDetail.setStartDate("20150401");
        gstCentralizedFileUpdateDetail.setBeginDate("2015-04-01 00:00:00.000");
        gstCentralizedFileUpdateDetail.setEndDate("20150401");
        gstCentralizedFileUpdateDetail.setLastUpdateDate("20180515");
        gstCentralizedFileUpdateDetail.setUpdateTime("175359");
        gstCentralizedFileUpdateDetail.setLastUpdateBy("402849");
        gstCentralizedFileUpdateDetail.setEntityCode("0A");
        gstCentralizedFileUpdateDetail.setEntityIndicator("10");

        return gstCentralizedFileUpdateDetail;
    }

    // New record
    private GSTCentralizedFileUpdateDetail createGSTDetailE() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = new GSTCentralizedFileUpdateDetail();
        gstCentralizedFileUpdateDetail.setOldGstMaxUniqueId("1");
        gstCentralizedFileUpdateDetail.setOldGstSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setOldGstTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setNewGstMaxUniqueId("2");
        gstCentralizedFileUpdateDetail.setJobExecutionId("999999");
        gstCentralizedFileUpdateDetail.setRecordIndicator("1");
        gstCentralizedFileUpdateDetail.setUniqueId("1");
        gstCentralizedFileUpdateDetail.setSourceSystemId("IBK");
        gstCentralizedFileUpdateDetail.setTransactionIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTransactionDescription("FUND TRANSFER TO OWN ACCOUNT BELOW THRESHOLD");
        gstCentralizedFileUpdateDetail.setGstRate("06.0000");
        gstCentralizedFileUpdateDetail.setTreatmentType("03");
        gstCentralizedFileUpdateDetail.setTaxCode("ZRL");
        gstCentralizedFileUpdateDetail.setCalculationMethod("E");
        gstCentralizedFileUpdateDetail.setGlAccountCodeCharges("");
        gstCentralizedFileUpdateDetail.setStartDate("20150401");
        gstCentralizedFileUpdateDetail.setBeginDate("2015-04-01 00:00:00.000");
        gstCentralizedFileUpdateDetail.setEndDate("20150331");
        gstCentralizedFileUpdateDetail.setLastUpdateDate("20180515");
        gstCentralizedFileUpdateDetail.setUpdateTime("175359");
        gstCentralizedFileUpdateDetail.setLastUpdateBy("402849");
        gstCentralizedFileUpdateDetail.setEntityCode(null);
        gstCentralizedFileUpdateDetail.setEntityIndicator(null);

        return gstCentralizedFileUpdateDetail;
    }

    // New record
    private GSTCentralizedFileUpdateDetail createGSTDetailF() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = new GSTCentralizedFileUpdateDetail();
        gstCentralizedFileUpdateDetail.setOldGstMaxUniqueId("1");
        gstCentralizedFileUpdateDetail.setOldGstSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setOldGstTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setNewGstMaxUniqueId("2");
        gstCentralizedFileUpdateDetail.setJobExecutionId("999999");
        gstCentralizedFileUpdateDetail.setRecordIndicator("1");
        gstCentralizedFileUpdateDetail.setUniqueId("1");
        gstCentralizedFileUpdateDetail.setSourceSystemId("IBK");
        gstCentralizedFileUpdateDetail.setTransactionIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTransactionDescription("FUND TRANSFER TO OWN ACCOUNT BELOW THRESHOLD");
        gstCentralizedFileUpdateDetail.setGstRate("06.0000");
        gstCentralizedFileUpdateDetail.setTreatmentType("03");
        gstCentralizedFileUpdateDetail.setTaxCode("ZRL");
        gstCentralizedFileUpdateDetail.setCalculationMethod("E");
        gstCentralizedFileUpdateDetail.setGlAccountCodeCharges("");
        gstCentralizedFileUpdateDetail.setStartDate("20150401");
        gstCentralizedFileUpdateDetail.setBeginDate("2015-04-01 00:00:00.000");
        gstCentralizedFileUpdateDetail.setEndDate("20150331");
        gstCentralizedFileUpdateDetail.setLastUpdateDate("20180515");
        gstCentralizedFileUpdateDetail.setUpdateTime("175359");
        gstCentralizedFileUpdateDetail.setLastUpdateBy("402849");
        gstCentralizedFileUpdateDetail.setEntityCode("OB");
        gstCentralizedFileUpdateDetail.setEntityIndicator("01");

        return gstCentralizedFileUpdateDetail;
    }

    // Suspense record
    private GSTCentralizedFileUpdateDetail createGSTDetailSuspsense() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = new GSTCentralizedFileUpdateDetail();
        gstCentralizedFileUpdateDetail.setOldGstMaxUniqueId("1");
        gstCentralizedFileUpdateDetail.setOldGstSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setOldGstTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setNewGstMaxUniqueId("2");
        gstCentralizedFileUpdateDetail.setJobExecutionId("999999");
        gstCentralizedFileUpdateDetail.setRecordIndicator("1");
        gstCentralizedFileUpdateDetail.setUniqueId("1");
        gstCentralizedFileUpdateDetail.setSourceSystemId("IBK");
        gstCentralizedFileUpdateDetail.setSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setTransactionIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTransactionDescription("FUND TRANSFER TO OWN ACCOUNT BELOW THRESHOLD");
        gstCentralizedFileUpdateDetail.setGstRate("0A.0000");
        gstCentralizedFileUpdateDetail.setTreatmentType("");
        gstCentralizedFileUpdateDetail.setTaxCode("");
        gstCentralizedFileUpdateDetail.setCalculationMethod("");
        gstCentralizedFileUpdateDetail.setGlAccountCodeCharges("");
        gstCentralizedFileUpdateDetail.setStartDate("");
        gstCentralizedFileUpdateDetail.setEndDate("");
        gstCentralizedFileUpdateDetail.setLastUpdateDate("20180515");
        gstCentralizedFileUpdateDetail.setUpdateTime("175359");
        gstCentralizedFileUpdateDetail.setLastUpdateBy("402849");
        gstCentralizedFileUpdateDetail.setEntityCode("");
        gstCentralizedFileUpdateDetail.setEntityIndicator("");

        return gstCentralizedFileUpdateDetail;
    }

    private List<BatchLookup> createLookUpDetail (String group){
        List<BatchLookup> batchLookupList = new ArrayList<>();

        int toLoop = 0;

        if(group.equalsIgnoreCase("DCP_GST_TREATMENT_TYPE")){
            toLoop = 4;
        }else if(group.equalsIgnoreCase("DCP_GST_TAX_CODE")){
            toLoop = 5;
        }else if(group.equalsIgnoreCase("DCP_GST_CALCULATION_METHOD")){
            toLoop = 2;
        }

        for(int i = 0; i < toLoop; i++){
            BatchLookup batchLookup = new BatchLookup();
            batchLookup.setGroup(group);

            switch(group) {
                case "DCP_GST_TREATMENT_TYPE" :
                    switch (i){
                        case 0 :
                            batchLookup.setValue("01");break;
                        case 1 :
                            batchLookup.setValue("02");break;
                        case 2 :
                            batchLookup.setValue("03");break;
                        case 3 :
                            batchLookup.setValue("04");break;
                    }
                    break;
                case "DCP_GST_TAX_CODE" :
                    switch (i){
                        case 0 :
                            batchLookup.setValue("SR");break;
                        case 1 :
                            batchLookup.setValue("ZRL");break;
                        case 2 :
                            batchLookup.setValue("ZRE");break;
                        case 3 :
                            batchLookup.setValue("OS");break;
                        case 4 :
                            batchLookup.setValue("TX-RC");break;
                    }
                    break;
                case "DCP_GST_CALCULATION_METHOD" :
                    switch (i){
                        case 0 :
                            batchLookup.setValue("I");break;
                        case 1 :
                            batchLookup.setValue("E");break;
                    }
                    break;
                default : break;
            }

            batchLookupList.add(batchLookup);
        }

        return batchLookupList;
    }
}
