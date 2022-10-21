package com.rhbgroup.dcp.bo.batch.test.repository;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Date;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotificationRaw;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotificationRawRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class,BatchStagedNotificationRawRepositoryImpl.class})
@ActiveProfiles("test")
public class BatchStagedNotificationRawRepositoryImplTest {
	private static final Logger logger = Logger.getLogger(BatchStagedNotificationRawRepositoryImplTest.class);
	
	@Autowired
	private BatchStagedNotificationRawRepositoryImpl notificationRawImpl;
	
	@MockBean
	private JdbcTemplate jdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test(expected=BatchException.class)
	public void testFindRecordFail() throws Exception{
		String fileName="test.txt";
		String sql = "SELECT COUNT(ID) FROM TBL_BATCH_STAGED_NOTIFICATION_RAW WHERE file_name=?";
		when(jdbcTemplate.queryForObject(sql, new Object[] { fileName }, Integer.class)).thenThrow(BadSqlGrammarException.class);
		notificationRawImpl.findNotificationFileLoaded(fileName);
	}
	
	@Test
	public void testFindRecordSuccess() throws Exception{
		String fileName="test.txt";
		String sql = "SELECT COUNT(ID) FROM TBL_BATCH_STAGED_NOTIFICATION_RAW WHERE file_name=?";
		when(jdbcTemplate.queryForObject(sql, new Object[] { fileName }, Integer.class)).thenReturn(1);
		assertEquals(notificationRawImpl.findNotificationFileLoaded(fileName),1);
	}
	
	@Test(expected=BatchException.class)
	public void testInsertFail() throws Exception {
		BatchStagedNotificationRaw  notificationRec = new BatchStagedNotificationRaw();
		String sql = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION_RAW"+
				"(job_execution_id,file_name,process_date,event_code,key_type,data_1,data_2,data_3,data_4,data_5,data_6,data_7,data_8,data_9,data_10,is_processed,created_time,created_by,updated_time,updated_by)" +
				" VALUES " +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		when(jdbcTemplate.update(sql,
				new Object[] { notificationRec.getJobExecutionId(), notificationRec.getFileName(),
						notificationRec.getProcessDate(), notificationRec.getEventCode(),
						notificationRec.getKeyType(), notificationRec.getData1(), notificationRec.getData2(),
						notificationRec.getData3(), notificationRec.getData4(), notificationRec.getData5(),
						notificationRec.getData6(), notificationRec.getData7(), notificationRec.getData8(),
						notificationRec.getData9(), notificationRec.getData10(), notificationRec.isProcessed(),
						notificationRec.getCreatedTime(), notificationRec.getCreatedBy(),
						notificationRec.getUpdatedTime(), notificationRec.getUpdatedBy() })).thenThrow(BadSqlGrammarException.class);
		notificationRawImpl.addRecordNotificationRaw(notificationRec);
	}
	
	@Test
	public void testInsertSuccess() throws Exception {
		BatchStagedNotificationRaw  notificationRec = new BatchStagedNotificationRaw();
		String sql = "INSERT INTO TBL_BATCH_STAGED_NOTIFICATION_RAW "+
				"(job_execution_id,file_name,process_date,event_code,key_type,data_1,data_2,data_3,data_4,data_5,data_6,data_7,data_8,data_9,data_10,is_processed,created_time,created_by,updated_time,updated_by)" +
				" VALUES " +
				" (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		when(jdbcTemplate.update(Mockito.anyString(),
				new Object[] { Mockito.anyLong(), Mockito.anyString(),
						Mockito.any(Date.class), Mockito.anyString(),
						Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(),
						Mockito.any(Date.class), Mockito.anyString(),
						Mockito.any(Date.class), Mockito.anyString()})).thenReturn(0);
		 assertEquals( notificationRawImpl.addRecordNotificationRaw(notificationRec),0);
	}
	
	
	@Before
	public void setup() throws Exception{
	}
	
	@After
	public void cleanup() throws Exception{}
}
