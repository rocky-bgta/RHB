package com.rhbgroup.dcp.bo.batch.test.repository;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.job.model.IBGRejectStatusTblTransferTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.IBGRejectStatusTblTransferTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.Date;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class,IBGRejectStatusTblTransferTxnRepositoryImpl.class})
@ActiveProfiles("test")
public class IBGRejectStatusTblTransferTxnRepositoryImplTest {
	
	private static final Logger logger = Logger.getLogger(IBGRejectStatusTblTransferTxnRepositoryImplTest.class);
	
//	@MockBean(name="dataSourceDCP")
	@Autowired
	DataSource dataSourceDCP;
    
	@Autowired
	DataSource dataSource;
	
	@MockBean
    JdbcTemplate jdbcTemplate;
	
	@Autowired
	IBGRejectStatusTblTransferTxnRepositoryImpl ibgRejectStatusRepo;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testGetUserEmptyException() throws Exception{
		String date="20180901";
		String tellerId="012345";
		String traceId="45678";
		when(jdbcTemplate.queryForObject(Mockito.anyString(),
				Mockito.any(Object[].class),
				Mockito.any(BeanPropertyRowMapper.class))).thenThrow(EmptyResultDataAccessException.class);
		assertNull(ibgRejectStatusRepo.getUserId(date, tellerId, traceId));
	}
	
	@Test
	public void testGetUserIncorrectSizeException() throws Exception{
		String date="20180901";
		String tellerId="012345";
		String traceId="45678";
		when(jdbcTemplate.queryForObject(Mockito.anyString(),
				Mockito.any(Object[].class),
				Mockito.any(BeanPropertyRowMapper.class))).thenThrow(IncorrectResultSizeDataAccessException.class);
		assertNull(ibgRejectStatusRepo.getUserId(date, tellerId, traceId));
	}
	
	@Test
	public void testGetUserException(){
		String date="20180901";
		String tellerId="012345";
		String traceId="45678";
		when(jdbcTemplate.queryForObject(Mockito.anyString(),
				Mockito.any(Object[].class),
				Mockito.any(BeanPropertyRowMapper.class))).thenThrow(BadSqlGrammarException.class);
		assertNull(ibgRejectStatusRepo.getUserId(date, tellerId, traceId));
	}
	
	@Test
	public void testGetUser(){
		String date="20180901";
		String tellerId="012345";
		String traceId="45678";
		assertNull(ibgRejectStatusRepo.getUserId(date, tellerId, traceId));
	}
	
	@Test
	public void testUpdateStatusException() throws Exception{
		String txnStatus="INACTIVE";
		String date="20180901";
		String tellerId="012345";
		String traceId="45678";
		when(jdbcTemplate.update(Mockito.anyString(), Mockito.anyString(), Mockito.any(Date.class), Mockito.anyString(),
				Mockito.anyString(),Mockito.anyString())).thenThrow(BadSqlGrammarException.class);
		assertEquals(0, ibgRejectStatusRepo.updateTxnStatus(txnStatus, date, tellerId, traceId) );
	}
	
	@Test
	public void testUpdateStatus() throws Exception{
		String txnStatus="INACTIVE";
		String date="20180901";
		String tellerId="012345";
		String traceId="45678";
		String updateSql = " update TBL_TRANSFER_TXN " + "set txn_status=?, updated_time=?"
				+ " where main_function='IBG' and teller_id =? and trace_id =?"
				+ " and convert(varchar(8), updated_time ,112)=?";
		when(jdbcTemplate.update(updateSql, txnStatus, new Date(), tellerId, traceId, date)).thenReturn(0);
		assertEquals(0, ibgRejectStatusRepo.updateTxnStatus(txnStatus, date, tellerId, traceId) );
	}
	
	@Before
	public void setup() throws Exception{
	}
	
	@After
	public void cleanup() throws Exception{
	}
	
}
