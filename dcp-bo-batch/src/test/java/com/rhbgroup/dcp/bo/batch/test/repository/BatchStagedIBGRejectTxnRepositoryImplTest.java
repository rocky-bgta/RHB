package com.rhbgroup.dcp.bo.batch.test.repository;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectStatusTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBGRejectTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

import static org.junit.Assert.assertEquals;
import javax.sql.DataSource;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class,BatchStagedIBGRejectTxnRepositoryImpl.class})
@ActiveProfiles("test")
public class BatchStagedIBGRejectTxnRepositoryImplTest {
	private static final Logger logger = Logger.getLogger(BatchStagedIBGRejectTxnRepositoryImplTest.class);

	@Autowired
	DataSource dataSource;
    
	@Autowired
    JdbcTemplate jdbcTemplate;
    
	@Autowired
	BatchStagedIBGRejectTxnRepositoryImpl batchStagedIBGRejectRepo ;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	BatchStagedIBGRejectStatusTxn ibgRejectStatus;
	
	@Test
	public void testUpdateNullRecord() throws Exception {
		ibgRejectStatus = new BatchStagedIBGRejectStatusTxn();
		assertEquals(0, batchStagedIBGRejectRepo.updateUserId("",ibgRejectStatus));
	}
	@Test
	public void testAddNullRecord() throws Exception {
		ibgRejectStatus = new BatchStagedIBGRejectStatusTxn();
		assertEquals(0, batchStagedIBGRejectRepo.addBatchStagedIBGRejectStatusStaging(ibgRejectStatus));
	}
	
	@Test 	
	public void testUpdateRejectDesc() throws Exception {
		ibgRejectStatus = new BatchStagedIBGRejectStatusTxn();
		assertEquals(0, batchStagedIBGRejectRepo.updateRejectDescription(ibgRejectStatus));
	}
	
	@Before
	public void setup() throws Exception{
	}
	
	@After
	public void cleanup() throws Exception{}
}
