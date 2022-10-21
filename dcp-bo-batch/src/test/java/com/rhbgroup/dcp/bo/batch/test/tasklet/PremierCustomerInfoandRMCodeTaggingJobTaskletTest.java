package com.rhbgroup.dcp.bo.batch.test.tasklet;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.config.GSTCentralizedFileUpdateJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.model.BatchLookup;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.GSTCentralizedFileUpdateDetail;
import com.rhbgroup.dcp.bo.batch.job.model.PremierCustomerInfoandRMCodeTaggingDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.GSTCentralizedFileUpdateRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.PremierCustomerInfoandRMCodeTaggingRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.GSTCentralizedFileUpdateJobValidatorTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.PremierCustomerInfoandRMCodeTaggingJobTruncateStagingTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.PremierCustomerInfoandRMCodeTaggingJobValidatorTasklet;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class, GSTCentralizedFileUpdateJobConfiguration.class})
@ActiveProfiles("test")
public class PremierCustomerInfoandRMCodeTaggingJobTaskletTest {
	private static final Logger logger = Logger.getLogger(PremierCustomerInfoandRMCodeTaggingJobTaskletTest.class);
	
	String JOB_NAME="PremierCustomerInfoandRMCodeTaggingJob";
	String jobprocessdate = "2018-08-25";

	@MockBean(name="premierCustomerInfoandRMCodeTaggingRepositoryImpl")
	private PremierCustomerInfoandRMCodeTaggingRepositoryImpl premierCustomerInfoandRMCodeTaggingRepository;
	
	@Autowired 
	private PremierCustomerInfoandRMCodeTaggingJobValidatorTasklet premierCustomerInfoandRMCodeTaggingJobValidatorTasklet;

    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobTruncateStagingTasklet premierCustomerInfoandRMCodeTaggingJobTruncateStagingTasklet;

    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet premierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet;

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
    public void testPremierCustomerInfoandRMCodeTaggingJobValidatorTasklet() throws Exception{
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

        List<PremierCustomerInfoandRMCodeTaggingDetail> premierNewUpdatedValue = new ArrayList<>();

        int recordToBeMocked = 70;

        for(int i = 0; i < recordToBeMocked; i++){
            PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = createData();
            if(i==1)premierCustomerInfoandRMCodeTaggingDetail.setCifNo("");
            if(i==2)premierCustomerInfoandRMCodeTaggingDetail.setOldIsPremier("1");
            if(i==3){premierCustomerInfoandRMCodeTaggingDetail.setNewRmCode("10000"); premierCustomerInfoandRMCodeTaggingDetail.setOldRmCode("10001");}
            if(i==4){premierCustomerInfoandRMCodeTaggingDetail.setNewRmCode(""); premierCustomerInfoandRMCodeTaggingDetail.setOldRmCode("10001");}
            if(i==5){premierCustomerInfoandRMCodeTaggingDetail.setNewRmCode("10000"); premierCustomerInfoandRMCodeTaggingDetail.setOldRmCode("");}
            if(i==6){premierCustomerInfoandRMCodeTaggingDetail.setOldIsPremier("0");}
            if(i==7){premierCustomerInfoandRMCodeTaggingDetail.setOldIsPremier(null);}
            if(i==8){premierCustomerInfoandRMCodeTaggingDetail.setNewRmCode(null); premierCustomerInfoandRMCodeTaggingDetail.setOldRmCode(null);}
            premierNewUpdatedValue.add(premierCustomerInfoandRMCodeTaggingDetail);
        }
        String setSQLString = "";
        List<String> parameterToUpdate = new ArrayList<>();

        when(premierCustomerInfoandRMCodeTaggingRepository.getBatchPremierNeworUpdatedValue(jobexecutionid)).thenReturn(premierNewUpdatedValue);
        when(premierCustomerInfoandRMCodeTaggingRepository.updateTableUserProfile(any(), any())).thenReturn(true);
        when(premierCustomerInfoandRMCodeTaggingRepository.updateTableUserProfile(setSQLString, parameterToUpdate)).thenReturn(true);

        Assert.assertEquals(RepeatStatus.FINISHED, premierCustomerInfoandRMCodeTaggingJobValidatorTasklet.execute(contribution, chunkContext));
    }

    @Test
    public void testPremierCustomerInfoandRMCodeTaggingJobValidatorLogInsertSuspense() throws Exception{
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

        BatchSuspense batchSuspense = new BatchSuspense();
        String logMessage = String.format("Staging PremierCustomerInfoandRMCodeTaggingDetail job execution id=%s, cif_no is not found in TBL_USER_PROFILE.", jobexecutionid);
        String suspenseColumn = "cif_no";

        premierCustomerInfoandRMCodeTaggingJobValidatorTasklet.logInsertSuspsenseData(batchSuspense, logMessage, suspenseColumn, "WARN","011123", "", false);
        premierCustomerInfoandRMCodeTaggingJobValidatorTasklet.logInsertSuspsenseData(batchSuspense, logMessage, suspenseColumn, "WARN","011123", "", true);
    }

    @Test
    public void testPremierCustomerInfoandRMCodeTaggingJobTruncateStagingTasklet() throws Exception{
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

        Assert.assertEquals(RepeatStatus.FINISHED, premierCustomerInfoandRMCodeTaggingJobTruncateStagingTasklet.execute(contribution, chunkContext));
    }

    @Test
    public void testPremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet() throws Exception{
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

        List<PremierCustomerInfoandRMCodeTaggingDetail> cifNotFoundRecordList = new ArrayList<>();
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = createDataCIFNotFoundRecord();
        cifNotFoundRecordList.add(premierCustomerInfoandRMCodeTaggingDetail);

        BatchSuspense batchSuspense = new BatchSuspense();

        batchSuspense.setBatchJobName(JOB_NAME);
        batchSuspense.setJobExecutionId(Long.parseLong(jobexecutionid));
        batchSuspense.setCreatedTime(new Date());
        String suspenseRecord = (new StringBuilder().append(premierCustomerInfoandRMCodeTaggingDetail.getCifNo()).append("|")).toString();
        batchSuspense.setSuspenseRecord(suspenseRecord);

        when(premierCustomerInfoandRMCodeTaggingRepository.getCIFNotFoundRecord(jobexecutionid)).thenReturn(cifNotFoundRecordList);
        when(premierCustomerInfoandRMCodeTaggingRepository.insertTblBatchSuspense(batchSuspense)).thenReturn(true);

        Assert.assertEquals(RepeatStatus.FINISHED, premierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet.execute(contribution, chunkContext));
    }

    @Test
    public void testNegativePremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet() throws Exception{
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

        List<PremierCustomerInfoandRMCodeTaggingDetail> cifNotFoundRecordList = new ArrayList<>();
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = createDataCIFNotFoundRecord();
        premierCustomerInfoandRMCodeTaggingDetail.setCifNo(null);
        cifNotFoundRecordList.add(premierCustomerInfoandRMCodeTaggingDetail);

        BatchSuspense batchSuspense = new BatchSuspense();
        boolean isLookUp = false;
        String logMessage = String.format("Staging PremierCustomerInfoandRMCodeTaggingDetail job execution id=%s, cif_no is not found in TBL_USER_PROFILE.", jobexecutionid);
        String suspenseColumn = "cif_no";

        when(premierCustomerInfoandRMCodeTaggingRepository.getCIFNotFoundRecord(jobexecutionid)).thenReturn(cifNotFoundRecordList);
        when(premierCustomerInfoandRMCodeTaggingRepository.insertTblBatchSuspense(batchSuspense)).thenReturn(true);

        Assert.assertEquals(RepeatStatus.FINISHED, premierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet.execute(contribution, chunkContext));
    }

    @Test
    public void testNegativePremierCustomerInfoandRMCodeTaggingJoblogInsertSuspenseData() throws Exception{
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

        BatchSuspense batchSuspense = new BatchSuspense();
        String logMessage = String.format("Staging PremierCustomerInfoandRMCodeTaggingDetail job execution id=%s, cif_no is not found in TBL_USER_PROFILE.", jobexecutionid);
        String suspenseColumn = "cif_no";

        premierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet.logInsertSuspsenseData(batchSuspense, logMessage, suspenseColumn, "WARN","011123", "", false);
        premierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet.logInsertSuspsenseData(batchSuspense, logMessage, suspenseColumn, "WARN","011123", "", true);
    }

    // New record
    private PremierCustomerInfoandRMCodeTaggingDetail createData() {
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = new PremierCustomerInfoandRMCodeTaggingDetail();
        premierCustomerInfoandRMCodeTaggingDetail.setJobExecutionId("999999");
        premierCustomerInfoandRMCodeTaggingDetail.setCifNo("90000000000999");

        return premierCustomerInfoandRMCodeTaggingDetail;
    }

    // CIF Not found to insert into Log
    private PremierCustomerInfoandRMCodeTaggingDetail createDataCIFNotFoundRecord() {
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = new PremierCustomerInfoandRMCodeTaggingDetail();
        premierCustomerInfoandRMCodeTaggingDetail.setJobExecutionId("999999");

        return premierCustomerInfoandRMCodeTaggingDetail;
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
