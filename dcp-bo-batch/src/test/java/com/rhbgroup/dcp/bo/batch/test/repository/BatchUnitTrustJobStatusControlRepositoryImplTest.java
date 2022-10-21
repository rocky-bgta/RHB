package com.rhbgroup.dcp.bo.batch.test.repository;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_SUCCESS;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.Assert;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class,BatchUnitTrustJobStatusControlRepositoryImpl.class})
@ActiveProfiles("test")
public class BatchUnitTrustJobStatusControlRepositoryImplTest {
	
	@Autowired
	BatchUnitTrustJobStatusControlRepositoryImpl batchUTJobStatusRepoImpl;
	
	@MockBean(name="jdbcTemplate")
	JdbcTemplate mockJdbc;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testAddRecord() throws Exception {
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setJobExecutionId(1L);
		utJobStatusControl.setBatchProcessDate(new Date());
		utJobStatusControl.setBatchEndDatetime(new Date());
		utJobStatusControl.setTargetDataset(1);
		utJobStatusControl.setCreatedBy("admin");
		utJobStatusControl.setCreatedTime(new Date());
		utJobStatusControl.setUpdatedBy("admin");
		utJobStatusControl.setUpdatedTime(new Date());
		String sql="INSERT INTO TBL_BATCH_UT_JOB_STATUS_CONTROL  "+ 
				"(JOB_EXECUTION_ID,BATCH_PROCESS_DATE,BATCH_END_DATETIME,TARGET_DATASET,CREATED_BY,CREATED_TIME,UPDATED_BY,UPDATED_TIME)" +
				" VALUES " +
				"(?,?,?,?,?,?,?,?)";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getJobExecutionId(),utJobStatusControl.getBatchProcessDate(),utJobStatusControl.getBatchEndDatetime()
				,utJobStatusControl.getTargetDataset(),utJobStatusControl.getCreatedBy(),utJobStatusControl.getCreatedTime()
				,utJobStatusControl.getUpdatedBy(),utJobStatusControl.getUpdatedTime()})).thenReturn(1);
		Assert.assertEquals(1, batchUTJobStatusRepoImpl.addRecord(utJobStatusControl));
	}
	
	@Test
	public void testUpdateJobStatusSuccess() throws Exception{
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setStatus(1);
		utJobStatusControl.setJobExecutionId(0L);
		utJobStatusControl.setBatchEndDatetime(new Date());
		utJobStatusControl.setUpdatedBy("admin");
		utJobStatusControl.setUpdatedTime(new Date());
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set STATUS=?, BATCH_END_DATETIME=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when (mockJdbc.update( sql, new Object[] {utJobStatusControl.getStatus(), utJobStatusControl.getBatchEndDatetime()
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() , utJobStatusControl.getJobExecutionId()}
				)).thenReturn(1);
		Assert.assertEquals(1, batchUTJobStatusRepoImpl.updateJobStatus(utJobStatusControl));
	}
	
	@Test
	public void testUpdTblCustomerStatusOk() throws Exception{
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtCustomerStatus(1) ;
		utJobStatusControl.setUpdatedBy("admin");
		utJobStatusControl.setUpdatedTime(new Date()); 
		utJobStatusControl.setJobExecutionId(0L);
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_CUSTOMER_STATUS=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtCustomerStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() 
				, utJobStatusControl.getJobExecutionId()})).thenReturn(1);
		Assert.assertEquals(1, batchUTJobStatusRepoImpl.updateTblCustomerStatus(utJobStatusControl));
	}
	
	@Test
	public void testUpdTblCustomerRelStatusOk() throws Exception{
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtCustomerRelStatus(1) ;
		utJobStatusControl.setUpdatedBy("admin");
		utJobStatusControl.setUpdatedTime(new Date()); 
		utJobStatusControl.setJobExecutionId(0L);
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_CUSTOMER_REL_STATUS=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtCustomerRelStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() 
				, utJobStatusControl.getJobExecutionId()})).thenReturn(1);
		Assert.assertEquals(1, batchUTJobStatusRepoImpl.updateTblCustomerRelStatus(utJobStatusControl));
	}
	
	@Test
	public void testUpdTblAccountStatusOk() throws Exception{
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtAccountStatus(1) ;
		utJobStatusControl.setUpdatedBy("admin");
		utJobStatusControl.setUpdatedTime(new Date()); 
		utJobStatusControl.setJobExecutionId(0L);
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_ACCOUNT_STATUS=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtAccountStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() 
				, utJobStatusControl.getJobExecutionId()})).thenReturn(1);
		Assert.assertEquals(1, batchUTJobStatusRepoImpl.updateTblAccountStatus(utJobStatusControl));
	}
	
	@Test
	public void testUpdTblAccountHldStatusOk() throws Exception{
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtAccountHoldingStatus(1) ;
		utJobStatusControl.setUpdatedBy("admin");
		utJobStatusControl.setUpdatedTime(new Date()); 
		utJobStatusControl.setJobExecutionId(0L);
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_ACCOUNT_HOLDING_STATUS=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtAccountHoldingStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() 
				, utJobStatusControl.getJobExecutionId()})).thenReturn(1);
		Assert.assertEquals(1, batchUTJobStatusRepoImpl.updateTblAccountHldStatus(utJobStatusControl));
	}
	
	@Test
	public void testUpdTblFundStatusOk() throws Exception{
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtFundMasterStatus(1) ;
		utJobStatusControl.setUpdatedBy("admin");
		utJobStatusControl.setUpdatedTime(new Date()); 
		utJobStatusControl.setJobExecutionId(0L);
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set tbl_ut_fund_master_status=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtFundMasterStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() 
				, utJobStatusControl.getJobExecutionId()})).thenReturn(1);
		Assert.assertEquals(1, batchUTJobStatusRepoImpl.updateTblFundMasterStatus(utJobStatusControl));
	}
	
	@Test
	public void testUpdCompleteJobOk() throws Exception{
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setStatus(1);
		utJobStatusControl.setBatchEndDatetime(new Date());
		utJobStatusControl.setUpdatedBy("admin");
		utJobStatusControl.setUpdatedTime(new Date()); 
		utJobStatusControl.setJobExecutionId(0L);
		utJobStatusControl.setTblUtCustomerStatus(1);
		utJobStatusControl.setTblUtCustomerRelStatus(1);
		utJobStatusControl.setTblUtAccountStatus(1);
		utJobStatusControl.setTblUtAccountHoldingStatus(1);
		utJobStatusControl.setTblUtFundMasterStatus(1);
		String sql="update TBL_BATCH_UT_JOB_STATUS_CONTROL" + 
				" set STATUS=?, BATCH_END_DATETIME=?, UPDATED_BY=?, UPDATED_TIME=?" + 
				" where JOB_EXECUTION_ID=?" + 
				" and TBL_UT_CUSTOMER_STATUS=?" + 
				" and TBL_UT_CUSTOMER_REL_STATUS=?" + 
				" and TBL_UT_ACCOUNT_STATUS=?" + 
				" and TBL_UT_ACCOUNT_HOLDING_STATUS=?" + 
				" and TBL_UT_FUND_MASTER_STATUS=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getStatus(), utJobStatusControl.getBatchEndDatetime()
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() 
				, utJobStatusControl.getJobExecutionId()
				, utJobStatusControl.getTblUtCustomerStatus(), utJobStatusControl.getTblUtCustomerRelStatus()
				, utJobStatusControl.getTblUtAccountStatus(), utJobStatusControl.getTblUtAccountHoldingStatus()
				, utJobStatusControl.getTblUtFundMasterStatus()})).thenReturn(1);
		Assert.assertEquals(1, batchUTJobStatusRepoImpl.updateCompleteJobStatus(utJobStatusControl));

	}
	@Test(expected=BatchException.class)
	public void testUpdateJobStatus() throws Exception {
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setStatus(-1);
		utJobStatusControl.setJobExecutionId(1L);
		utJobStatusControl.setBatchEndDatetime(new Date());
		utJobStatusControl.setUpdatedBy("123");
		utJobStatusControl.setUpdatedTime(new Date());
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set STATUS=?, BATCH_END_DATETIME=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when (mockJdbc.update( sql, 
				new Object[] {utJobStatusControl.getStatus(), utJobStatusControl.getBatchEndDatetime()
						, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() , utJobStatusControl.getJobExecutionId()})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0, batchUTJobStatusRepoImpl.updateJobStatus(utJobStatusControl));
	}
	
	@Test(expected=BatchException.class)
	public void testUpdateTblCustomerStatus() throws Exception {
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtCustomerStatus(-1);
		utJobStatusControl.setJobExecutionId(1L);
		utJobStatusControl.setUpdatedBy("123");
		utJobStatusControl.setUpdatedTime(new Date());
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_CUSTOMER_STATUS=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtCustomerStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime()
				, utJobStatusControl.getJobExecutionId()})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0,batchUTJobStatusRepoImpl.updateTblCustomerStatus(utJobStatusControl));
	}
	
	@Test(expected=BatchException.class)
	public void testUpdateTblCustomerRelStatus() throws Exception {
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtCustomerRelStatus(-1);
		utJobStatusControl.setJobExecutionId(1L);
		utJobStatusControl.setUpdatedBy("123");
		utJobStatusControl.setUpdatedTime(new Date());
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_CUSTOMER_REL_STATUS=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtCustomerRelStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime()
				, utJobStatusControl.getJobExecutionId()})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0,batchUTJobStatusRepoImpl.updateTblCustomerRelStatus(utJobStatusControl));
	}
	
	@Test(expected=BatchException.class)
	public void testUpdateTblAccountStatus() throws Exception {
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtAccountStatus(-1);
		utJobStatusControl.setJobExecutionId(1L);
		utJobStatusControl.setUpdatedBy("123");
		utJobStatusControl.setUpdatedTime(new Date());
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_ACCOUNT_STATUS=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtAccountStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime()
				, utJobStatusControl.getJobExecutionId()})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0,batchUTJobStatusRepoImpl.updateTblAccountStatus(utJobStatusControl));
	}
	
	@Test(expected=BatchException.class)
	public void testUpdateTblAccountHldStatus() throws Exception {
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtAccountHoldingStatus(-1);
		utJobStatusControl.setJobExecutionId(1L);
		utJobStatusControl.setUpdatedBy("123");
		utJobStatusControl.setUpdatedTime(new Date());
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set TBL_UT_ACCOUNT_HOLDING_STATUS=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtAccountHoldingStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime()
				, utJobStatusControl.getJobExecutionId()})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0,batchUTJobStatusRepoImpl.updateTblAccountHldStatus(utJobStatusControl));
	}
	
	
	@Test(expected=BatchException.class)
	public void testUpdateTblFundStatus() throws Exception {
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setTblUtFundMasterStatus(-1);
		utJobStatusControl.setJobExecutionId(1L);
		utJobStatusControl.setUpdatedBy("123");
		utJobStatusControl.setUpdatedTime(new Date());
		String sql="UPDATE TBL_BATCH_UT_JOB_STATUS_CONTROL set tbl_ut_fund_master_status=?, UPDATED_BY=?, UPDATED_TIME=?"
				+" WHERE JOB_EXECUTION_ID=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getTblUtFundMasterStatus() 
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime()
				, utJobStatusControl.getJobExecutionId()})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0,batchUTJobStatusRepoImpl.updateTblFundMasterStatus(utJobStatusControl));
	}
	
	@Test(expected=BatchException.class)
	public void testUpdateCompleteStatus() throws Exception {
		BatchUnitTrustJobStatusControl utJobStatusControl = new BatchUnitTrustJobStatusControl ();
		utJobStatusControl.setStatus(1);
		utJobStatusControl.setBatchEndDatetime(new Date());
		utJobStatusControl.setJobExecutionId(1L);
		utJobStatusControl.setUpdatedBy("123");
		utJobStatusControl.setUpdatedTime(new Date());
		utJobStatusControl.setTblUtCustomerStatus(1);
		utJobStatusControl.setTblUtCustomerRelStatus(1);
		utJobStatusControl.setTblUtAccountStatus(1);
		utJobStatusControl.setTblUtAccountHoldingStatus(1);
		utJobStatusControl.setTblUtFundMasterStatus(1);
		String sql="update TBL_BATCH_UT_JOB_STATUS_CONTROL" + 
				" set STATUS=?, BATCH_END_DATETIME=?, UPDATED_BY=?, UPDATED_TIME=?" + 
				" where JOB_EXECUTION_ID=?" + 
				" and TBL_UT_CUSTOMER_STATUS=?" + 
				" and TBL_UT_CUSTOMER_REL_STATUS=?" + 
				" and TBL_UT_ACCOUNT_STATUS=?" + 
				" and TBL_UT_ACCOUNT_HOLDING_STATUS=?" + 
				" and TBL_UT_FUND_MASTER_STATUS=?";
		when(mockJdbc.update(sql, new Object[] {utJobStatusControl.getStatus(), utJobStatusControl.getBatchEndDatetime()
				, utJobStatusControl.getUpdatedBy(), utJobStatusControl.getUpdatedTime() 
				, utJobStatusControl.getJobExecutionId()
				, utJobStatusControl.getTblUtCustomerStatus(), utJobStatusControl.getTblUtCustomerRelStatus()
				, utJobStatusControl.getTblUtAccountStatus(), utJobStatusControl.getTblUtAccountHoldingStatus()
				, utJobStatusControl.getTblUtFundMasterStatus()})).thenThrow(BadSqlGrammarException.class);
		Assert.assertEquals(0,batchUTJobStatusRepoImpl.updateCompleteJobStatus(utJobStatusControl) );
	}
	
	@Test(expected=Exception.class)
	public void testGetTargetSet() throws Exception{
		String sql=" SELECT TARGET_DATASET FROM TBL_BATCH_UT_JOB_STATUS_CONTROL "+
				" WHERE ID=(SELECT MIN(ID) FROM TBL_BATCH_UT_JOB_STATUS_CONTROL WHERE STATUS=?)";
		when(mockJdbc.queryForObject(sql, new Object[] {STATUS_SUCCESS}, Integer.class)).thenThrow(BadSqlGrammarException.class);
		batchUTJobStatusRepoImpl.getTargetDataSet();
	}
	
}
