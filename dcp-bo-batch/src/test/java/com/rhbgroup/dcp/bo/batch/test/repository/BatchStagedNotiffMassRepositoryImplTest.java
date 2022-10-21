package com.rhbgroup.dcp.bo.batch.test.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadMassNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotifMass;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotifMassRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BatchStagedNotiffMassRepositoryImplTest extends BaseJobTest {
	
	private static final String BATCH_CODE = "LDCPA6006B";

	@Autowired
	BatchStagedNotifMassRepositoryImpl batchStagedNotifMassRepositoryImpl;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	/**
	 * Positive case for inserting into table TBL_BATCH_STAGED_NOTIF_MASS
	 * @throws Exception
	 */
	@Test
	public void testPositiveAddIntoNotificationsStaging() throws Exception {
		BatchStagedNotifMass batchStagedNotifMass = createBatchStagedNotifMass(1L, "test.text", "90002", "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", 1L);
		
		when(mockJdbcTemplate.update(
				Mockito.anyString(), 
				Mockito.anyLong(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.anyLong(), 
				Mockito.anyBoolean(), 
				(Date) Mockito.any(), 
				Mockito.any(), 
				(Date) Mockito.any(), 
				Mockito.any())).thenReturn(1);
		
		assertEquals(1, batchStagedNotifMassRepositoryImpl.addRecordBatchStagedNotifMass(batchStagedNotifMass));
	}
	
	/**
	 * Positive case for updating table TBL_BATCH_STAGED_NOTIF_MASS
	 * @throws Exception
	 */
	@Test
	public void testUpdateAfterNotificationSuccess() throws Exception {
		
		BatchStagedNotifMass batchStagedNotifMass = createBatchStagedNotifMass(1L, "test.text", "90002", "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", 1L);
		
		when(mockJdbcTemplate.update(
				Mockito.anyString(), 
				(Date) Mockito.any(), 
				Mockito.anyString(), 
				Mockito.anyLong(), 
				Mockito.anyLong())).thenReturn(1);
		
		assertEquals(1, batchStagedNotifMassRepositoryImpl.updateIsProcessed(BATCH_CODE, batchStagedNotifMass));
	}
	
	/**
	 * Negative case for inserting into table TBL_BATCH_STAGED_NOTIF_MASS
	 * @throws Exception
	 */
	@Test(expected = BatchException.class)
	public void testNegativeAddIntoNotificationsStaging() throws Exception {
		BatchStagedNotifMass batchStagedNotifMass = createBatchStagedNotifMass(1L, "test.text", "90002", "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", 1L);
		
		when(mockJdbcTemplate.update(
				Mockito.anyString(), 
				Mockito.anyLong(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.any(), 
				Mockito.anyLong(), 
				Mockito.anyBoolean(), 
				(Date) Mockito.any(), 
				Mockito.any(), 
				(Date) Mockito.any(), 
				Mockito.any())).thenThrow(UncategorizedSQLException.class);
		
		batchStagedNotifMassRepositoryImpl.addRecordBatchStagedNotifMass(batchStagedNotifMass);
	}
	
	/**
	 * Negative case for updating table TBL_BATCH_STAGED_NOTIF_MASS
	 * @throws Exception
	 */
	@Test(expected = BatchException.class)
	public void testUpdateAfterNotificationFailed() throws Exception {
		BatchStagedNotifMass batchStagedNotifMass = createBatchStagedNotifMass(1L, "test.text", "90002", "RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.", 1L);
		
		when(mockJdbcTemplate.update(
				Mockito.anyString(), 
				(Date) Mockito.any(), 
				Mockito.anyString(), 
				Mockito.anyLong(), 
				Mockito.anyLong())).thenThrow(UncategorizedSQLException.class);
		
		batchStagedNotifMassRepositoryImpl.updateIsProcessed(BATCH_CODE, batchStagedNotifMass);
	}
		
	private BatchStagedNotifMass createBatchStagedNotifMass(long jobExecutionId, String sourceFileName, String eventCode, String content, long userId) throws BatchException {
		BatchStagedNotifMass batchStagedNotifMass = new BatchStagedNotifMass();
		
		batchStagedNotifMass.setJobExecutionId(jobExecutionId);
		batchStagedNotifMass.setFileName(sourceFileName);
		batchStagedNotifMass.setEventCode(eventCode);
		batchStagedNotifMass.setContent(content);
		batchStagedNotifMass.setUserId(userId);
		batchStagedNotifMass.setProcessed(false);
		Date now = new Date();
		batchStagedNotifMass.setCreatedTime(now);
		batchStagedNotifMass.setCreatedBy(BATCH_CODE);
		batchStagedNotifMass.setUpdatedTime(now);
		batchStagedNotifMass.setUpdatedBy(BATCH_CODE);
		
		return batchStagedNotifMass;
	}
	
}
