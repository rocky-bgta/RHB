package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.job.config.UpdateIBGRejectStatusJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectStatusTxn;
import com.rhbgroup.dcp.bo.batch.job.model.IBGRejectStatusTblTransferTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBGRejectTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.IBGRejectStatusTblTransferTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.IBGRejectStatusValidatorTasklet;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class,UpdateIBGRejectStatusJobConfiguration.class})
@ActiveProfiles("test")
public class UpdateIBGRejectStatusValidatorTaskletTest {
	private static final Logger logger = Logger.getLogger(UpdateIBGRejectStatusValidatorTaskletTest.class);
	
	String JOB_NAME="UpdateIBGRejectedStatusJob";
	String JOB_EXEC_ID="";
	String SUCCESS_RUN_WINDOW="w1";
	String FAILED_RUN_WINDOW="w2";
	String jobprocessdate = "2018-08-25";

	@MockBean(name ="tblTransferTxnRepoImpl")
	private IBGRejectStatusTblTransferTxnRepositoryImpl mockTransferTxnRepoImpl;
	
	@MockBean(name="ibgRejectStatusStagingRepositoryImpl")
	private BatchStagedIBGRejectTxnRepositoryImpl mockRejectStatusStagingRepositoryImpl;
	
	@Autowired 
	private IBGRejectStatusValidatorTasklet ibgRejectStatusValidatorTasklet;
	
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
    public void testUpdateReasonFail() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        String tellerId="123456";
        String traceId="5887";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20180825";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
		when(mockTransferTxnRepoImpl.getUserId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(transferTxn);
		when(mockTransferTxnRepoImpl.updateTxnStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(1);
        
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        BatchStagedIBGRejectStatusTxn ibgRejectStatus=createIBGRejectTxn();
        List<BatchStagedIBGRejectStatusTxn> ibgRejectStatusList = new ArrayList<>();
        ibgRejectStatusList.add(ibgRejectStatus);
        when(mockRejectStatusStagingRepositoryImpl.getUnprocessedIBGRejectStatusFromStaging(jobexecutionid)).thenReturn(ibgRejectStatusList);
        when(mockRejectStatusStagingRepositoryImpl.updateRejectDescription(ibgRejectStatus)).thenReturn(0);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(ibgRejectStatus)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.updateUserId(String.valueOf(userId) ,ibgRejectStatus)).thenReturn(1);

        Assert.assertEquals(RepeatStatus.FINISHED,ibgRejectStatusValidatorTasklet.execute(contribution, chunkContext));
    }
    
    @Test
    public void testEmptyValue() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        String tellerId="123456";
        String traceId="5887";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20180825";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
		when(mockTransferTxnRepoImpl.getUserId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(transferTxn);
		when(mockTransferTxnRepoImpl.updateTxnStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),Mockito.anyString())).thenReturn(1);

        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        List<BatchStagedIBGRejectStatusTxn> ibgRejectStatusList = new ArrayList<>();

        BatchStagedIBGRejectStatusTxn amountEmpty=createIBGRejectTxn();
        amountEmpty.setAmount("");
        
        BatchStagedIBGRejectStatusTxn beneEmpty=createIBGRejectTxn();
        beneEmpty.setBeneName("");
        
        BatchStagedIBGRejectStatusTxn dateEmpty=createIBGRejectTxn();
        dateEmpty.setDate("");
        
        BatchStagedIBGRejectStatusTxn tellerEmpty=createIBGRejectTxn();
        tellerEmpty.setTeller("");
        
        BatchStagedIBGRejectStatusTxn traceEmpty=createIBGRejectTxn();
        traceEmpty.setTrace("");
        
        ibgRejectStatusList.add(amountEmpty);
        ibgRejectStatusList.add(beneEmpty);
        ibgRejectStatusList.add(dateEmpty);
        ibgRejectStatusList.add(tellerEmpty);
        ibgRejectStatusList.add(traceEmpty);
        
        when(mockRejectStatusStagingRepositoryImpl.getUnprocessedIBGRejectStatusFromStaging(jobexecutionid)).thenReturn(ibgRejectStatusList);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(amountEmpty)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(beneEmpty)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(dateEmpty)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(tellerEmpty)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(traceEmpty)).thenReturn(1);

        Assert.assertEquals(RepeatStatus.FINISHED,ibgRejectStatusValidatorTasklet.execute(contribution, chunkContext));
    }
    
    @Test
    public void testFailUserId() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        String tellerId="123456";
        String traceId="5887";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20180825";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
        when(mockTransferTxnRepoImpl.updateTxnStatus(Mockito.anyString() , Mockito.anyString() , Mockito.anyString()  , Mockito.anyString() )).thenReturn(1);
        
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        BatchStagedIBGRejectStatusTxn ibgRejectStatus=createIBGRejectTxn();
        List<BatchStagedIBGRejectStatusTxn> ibgRejectStatusList = new ArrayList<>();
        ibgRejectStatusList.add(ibgRejectStatus);
        when(mockRejectStatusStagingRepositoryImpl.getUnprocessedIBGRejectStatusFromStaging(jobexecutionid)).thenReturn(ibgRejectStatusList);
        when(mockRejectStatusStagingRepositoryImpl.updateRejectDescription(ibgRejectStatus)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(ibgRejectStatus)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.updateUserId(String.valueOf(userId) ,ibgRejectStatus)).thenReturn(1);

        Assert.assertEquals(RepeatStatus.FINISHED,ibgRejectStatusValidatorTasklet.execute(contribution, chunkContext));
    }
    
    @Test
    public void testUpdateFailUserId() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        String tellerId="123456";
        String traceId="5887";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20180825";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
		when(mockTransferTxnRepoImpl.getUserId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(transferTxn);
        when(mockTransferTxnRepoImpl.updateTxnStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString() , Mockito.anyString())).thenReturn(1);
        
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).thenReturn(executionContext);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        BatchStagedIBGRejectStatusTxn ibgRejectStatus=createIBGRejectTxn();
        List<BatchStagedIBGRejectStatusTxn> ibgRejectStatusList = new ArrayList<>();
        ibgRejectStatusList.add(ibgRejectStatus);
        when(mockRejectStatusStagingRepositoryImpl.getUnprocessedIBGRejectStatusFromStaging(jobexecutionid)).thenReturn(ibgRejectStatusList);
        when(mockRejectStatusStagingRepositoryImpl.updateRejectDescription(ibgRejectStatus)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(ibgRejectStatus)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.updateUserId(String.valueOf(userId) ,ibgRejectStatus)).thenReturn(0);

        Assert.assertEquals(RepeatStatus.FINISHED,ibgRejectStatusValidatorTasklet.execute(contribution, chunkContext));
    }
    @Test
    public void testUpdateSuccess() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        String tellerId="123456";
        String traceId="5887";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20180825";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
		when(mockTransferTxnRepoImpl.getUserId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(transferTxn);
		when(mockTransferTxnRepoImpl.updateTxnStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(1);
        
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        BatchStagedIBGRejectStatusTxn ibgRejectStatus=createIBGRejectTxn();
        List<BatchStagedIBGRejectStatusTxn> ibgRejectStatusList = new ArrayList<>();
        ibgRejectStatusList.add(ibgRejectStatus);
        when(mockRejectStatusStagingRepositoryImpl.getUnprocessedIBGRejectStatusFromStaging(jobexecutionid)).thenReturn(ibgRejectStatusList);
        when(mockRejectStatusStagingRepositoryImpl.updateRejectDescription(ibgRejectStatus)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(ibgRejectStatus)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.updateUserId(String.valueOf(userId) ,ibgRejectStatus)).thenReturn(1);

        Assert.assertEquals(RepeatStatus.FINISHED,ibgRejectStatusValidatorTasklet.execute(contribution, chunkContext));
    }
    
    @Test
    public void testNewTasklet() throws Exception{
		String jobexecutionid="0";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.toJobParameters();
        String tellerId="123456";
        String traceId="5887";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20180825";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
		when(mockTransferTxnRepoImpl.getUserId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(transferTxn);
		when(mockTransferTxnRepoImpl.updateTxnStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(1);
        
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        BatchStagedIBGRejectStatusTxn ibgRejectStatus=createIBGRejectTxn();
        List<BatchStagedIBGRejectStatusTxn> ibgRejectStatusList = new ArrayList<>();
        ibgRejectStatusList.add(ibgRejectStatus);
        when(mockRejectStatusStagingRepositoryImpl.getUnprocessedIBGRejectStatusFromStaging(jobexecutionid)).thenReturn(ibgRejectStatusList);
        when(mockRejectStatusStagingRepositoryImpl.updateRejectDescription(ibgRejectStatus)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(ibgRejectStatus)).thenReturn(1);
        when(mockRejectStatusStagingRepositoryImpl.updateUserId(String.valueOf(userId) ,ibgRejectStatus)).thenReturn(1);

        Assert.assertEquals(RepeatStatus.FINISHED,ibgRejectStatusValidatorTasklet.execute(contribution, chunkContext));
    }
    
    
    private BatchStagedIBGRejectStatusTxn createIBGRejectTxn() {
    	BatchStagedIBGRejectStatusTxn ibgRejectStatus = new BatchStagedIBGRejectStatusTxn();
    	ibgRejectStatus.setDate("20180825");
    	ibgRejectStatus.setTeller("123456");
    	ibgRejectStatus.setTrace("5887");
    	ibgRejectStatus.setRef1("reference 1");
    	ibgRejectStatus.setName("TUAN");
    	ibgRejectStatus.setAmount("987.00");
    	ibgRejectStatus.setRejectCode("R03");
    	ibgRejectStatus.setAccountNo("11301000099330");
    	ibgRejectStatus.setBeneName("LEE");
    	ibgRejectStatus.setBeneAccount("0660015527419");
    	ibgRejectStatus.setFileName("DCP_IBGSta_W1_250818.txt");
    	ibgRejectStatus.setJobExecutionId("999999");
    	return ibgRejectStatus;
    }
    
    @Before
    public void setup() throws Exception {
        jdbcTemplate.setDataSource(dataSource);
        insertRejectCode();
    }

    @After
    public void after() throws Exception {
    	deleteRejectCode();        
    }

	private void insertRejectCode() {
		int row = 0;
		String sql = "insert into TBL_BATCH_IBG_REJECT_CODE " + "(REJECT_CODE, REJECT_DESCRIPTION) " + "values "
				+ "('R03','REJECT TEST DESC')";
		row = jdbcTemplate.update(sql);
		logger.info(String.format("insert %s row into TBL_BATCH_IBG_REJECT_CODE", row));
	}
	
	private void deleteRejectCode() {
		int row = 0;
		String sql = "delete from TBL_BATCH_IBG_REJECT_CODE" ;
		row = jdbcTemplate.update(sql);
		logger.info(String.format("delete %s row into TBL_BATCH_IBG_REJECT_CODE", row));
	}
}
