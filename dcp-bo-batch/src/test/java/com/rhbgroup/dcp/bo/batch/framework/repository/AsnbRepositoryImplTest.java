package com.rhbgroup.dcp.bo.batch.framework.repository;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BoInvestTxn;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class AsnbRepositoryImplTest{

	@Autowired
	AsnbRepositoryImpl asnbRepositoryImpl;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;
	
	Date date= new Date();
	
	
	@Test
	public void testDeleteBoInvestTxnData() throws Exception {		
		when(mockJdbcTemplate.update(
				Mockito.anyString() 
				)).thenReturn(1);		
		int row = asnbRepositoryImpl.deleteBoInvestTxnData();
		assertEquals(1, row);
	}
	
	@Test
	public void testDeleteBoAsnbTxnData() throws Exception {		
		when(mockJdbcTemplate.update(
				Mockito.anyString() 
				)).thenReturn(1);		
		int row = asnbRepositoryImpl.deleteBoAsnbTxnData();
		assertEquals(1, row);
	}
	
	@Test
	public void testInsertBoInvestTxn() throws Exception {
		
		String processedDateStr = DateUtils.formatDateString(new Date(), DEFAULT_DATE_FORMAT);
		
		when(mockJdbcTemplate.update(
				Mockito.anyString(),
				Mockito.anyString(), 
				Mockito.anyString() 
				)).thenReturn(1);	
		int row = asnbRepositoryImpl.insertBoInvestTxn(processedDateStr);
		assertEquals(1, row);
	}
	
	@Test
	public void testInsertBoAsnbTxn() throws Exception {
		
		String processedDateStr = DateUtils.formatDateString(new Date(), DEFAULT_DATE_FORMAT);
		
		when(mockJdbcTemplate.update(
				Mockito.anyString(),
				Mockito.anyString(), 
				Mockito.anyString() 
				)).thenReturn(1);	
		int row = asnbRepositoryImpl.insertBoAsnbTxn(processedDateStr);
		assertEquals(1, row);
	}
	
	@Test
	public void testDeleteBatchStagedSummaryAsnb() throws Exception {		
        when(mockJdbcTemplate.update(anyString(), any(Object.class))).thenReturn(1);		
		int row = asnbRepositoryImpl.deleteBatchStagedSummaryAsnb(date);
		assertEquals(1, row);
	}
	
	@Test
	public void testInsertStagedAsnbTxn() {
		BigDecimal bigDecimal = new BigDecimal(1);
        when(mockJdbcTemplate.update(anyString(), any(Object.class))).thenReturn(1);
        assertEquals(Boolean.TRUE, asnbRepositoryImpl.insertStagedAsnbTxn(bigDecimal, bigDecimal, bigDecimal, date, 0, "channel", "status"));

	}
	
	@Test
	public void testGetBoInvestTxnList() throws Exception {	
		BoInvestTxn boInvestTxn = new BoInvestTxn();
		boInvestTxn.setId(1);
		
		List<BoInvestTxn> boInvestList = new ArrayList<>();
		boInvestList.add(boInvestTxn);
		
        when(mockJdbcTemplate.query(Mockito.anyString(), Mockito.any(Object[].class), Mockito.any(BeanPropertyRowMapper.class))).thenReturn(boInvestList);

		List<BoInvestTxn> boInvestTxnList = asnbRepositoryImpl.getBoInvestTxnList(date);
		assertEquals(1, boInvestTxnList.size());
	}
	
}