package com.rhbgroup.dcpbo.customer.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.customer.dto.AuditDetailsActivity;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.service.AuditDetailsService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
	AuditDetailsServiceImpl.class,
	AuditDetailsServiceImplTests.class,
	AuditDetailsServiceImplTests.Config.class
})
public class AuditDetailsServiceImplTests {
	@Autowired
	AuditDetailsService auditDetailsService;
	@MockBean
	LookupStatusRepository lookupStatusRepository;
	@MockBean
	AuditRepository auditRepositoryMock;

	@MockBean
    DcpAuditEventConfigRepository auditEventConfigRepositoryMock;

	@MockBean
	AuditDetailConfigRepo auditDetailConfigRepositoryMock;

	@MockBean
	DcpAuditBillPaymentRepository auditBillPaymentRepositoryMock;

	@MockBean
	DcpAuditFundTransferRepository auditFundTransferRepositoryMock;

	@MockBean
	AuditMiscRepository auditMiscRepositoryMock;

	@MockBean
	AuditProfileRepository auditProfileRepositoryMock;

	@MockBean
	DcpAuditTopupRepository auditTopupRepositoryMock;
	
	@MockBean(name = "auditDetailsTableRepository")
	AuditDetailsTableRepository auditDetailsTableRepositoryMock;

	@TestConfiguration
	class Config {
		@Bean(name = "auditDetailsTableRepository")
		public AuditDetailsTableRepository getTable() {
			return auditFundTransferRepositoryMock;
		}
	}

	int auditId = 1;
	String eventCode = "10001";
	String evntCode = "31016";


	private static Logger logger = LogManager.getLogger(AuditDetailsServiceImplTests.class);

	@Test
	public void getAuditDetailsTest() throws Throwable {
		logger.debug("getAuditDetailsTest()");
		logger.debug("    auditDetailsService: " + auditDetailsService);
		logger.debug("    auditRepositoryMock: " + auditRepositoryMock);
		logger.debug("    auditEventConfigRepositoryMock: " + auditEventConfigRepositoryMock);
		logger.debug("    auditDetailConfigRepositoryMock: " + auditDetailConfigRepositoryMock);
		logger.debug("    auditFundTransferRepositoryMock: " + auditFundTransferRepositoryMock);

		Audit audit = new Audit();
		audit.setId(1);
		audit.setEventCode("10001");
		audit.setDeviceId("b21ded15-3bb0-4e50-bfec-dded6079a873");
		audit.setChannel("DMB");
		audit.setStatusCode("10000");
		audit.setStatusDescription("success");
		audit.setTimestamp(new Date(System.currentTimeMillis()));
		when(auditRepositoryMock.findOne(Mockito.anyInt())).thenReturn(audit);

		AuditEventConfig auditEventConfig = new AuditEventConfig();
		auditEventConfig.setDetailsTableName("dcp_audit_fund_transfer");
		auditEventConfig.setEventName("Transfer - IBG");
		when(auditEventConfigRepositoryMock.findByEventCode(Mockito.any())).thenReturn(auditEventConfig);

		List<AuditDetailConfig> auditDetailConfigList = new LinkedList<AuditDetailConfig>();
		addAuditDetailConfig(auditDetailConfigList, "Channel", "$.header.channel");
		addAuditDetailConfig(auditDetailConfigList, "Status Code", "$.header.status_code");
		addAuditDetailConfig(auditDetailConfigList, "Status Description", "$.header.status_description");
		when(auditDetailConfigRepositoryMock.findAllByEventCode(Mockito.anyString())).thenReturn(auditDetailConfigList);

        InputStream is = getClass().getClassLoader().getResourceAsStream("AuditFundTransfer.findByAuditId.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
                sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        AuditFundTransfer auditFundTransfer = new AuditFundTransfer();
        auditFundTransfer.setId(2);
        auditFundTransfer.setAuditId(1);
        auditFundTransfer.setDetails(jsonStr);
		when(auditFundTransferRepositoryMock.findAuditFundTransferByRefId(Mockito.any())).thenReturn(auditFundTransfer);

		AuditDetailsRecord auditDetailsRecord = new MyAuditDetailsRecord();
		when(auditFundTransferRepositoryMock.findByAuditId(Mockito.any())).thenReturn(auditDetailsRecord);

		AuditDetailsActivity auditDetailsActivity = (AuditDetailsActivity) auditDetailsService.getAuditDetailsActivity(auditId, eventCode);
		logger.debug("    auditDetailsActivity: " + auditDetailsActivity);
		assertNotNull(auditDetailsActivity);
	}
	
	@Test
	public void getAuditDetailsSpecificTest() throws Throwable {
		logger.debug("getAuditDetailsTest()");
		logger.debug("    auditDetailsService: " + auditDetailsService);
		logger.debug("    auditRepositoryMock: " + auditRepositoryMock);
		logger.debug("    auditEventConfigRepositoryMock: " + auditEventConfigRepositoryMock);
		logger.debug("    auditDetailConfigRepositoryMock: " + auditDetailConfigRepositoryMock);
		logger.debug("    auditFundTransferRepositoryMock: " + auditFundTransferRepositoryMock);

		Audit audit = new Audit();
		audit.setId(1);
		audit.setEventCode("31016");
		audit.setDeviceId("b21ded15-3bb0-4e50-bfec-dded6079a873");
		audit.setChannel("DMB");
		audit.setStatusCode("10000");
		audit.setStatusDescription("success");
		audit.setTimestamp(new Date(System.currentTimeMillis()));
		when(auditRepositoryMock.findOne(Mockito.anyInt())).thenReturn(audit);

		AuditEventConfig auditEventConfig = new AuditEventConfig();
		auditEventConfig.setDetailsTableName("dcp_audit_fund_transfer");
		auditEventConfig.setEventName("Transfer - IBG");
		when(auditEventConfigRepositoryMock.findByEventCode(Mockito.any())).thenReturn(auditEventConfig);

		List<AuditDetailConfig> auditDetailConfigList = new LinkedList<AuditDetailConfig>();
		addAuditDetailConfig(auditDetailConfigList, "Channel", "$.header.channel");
		addAuditDetailConfig(auditDetailConfigList, "Status Code", "$.header.status_code");
		addAuditDetailConfig(auditDetailConfigList, "Status Description", "$.header.status_description");
		when(auditDetailConfigRepositoryMock.findAllByEventCode(Mockito.anyString())).thenReturn(auditDetailConfigList);

        InputStream is = getClass().getClassLoader().getResourceAsStream("AuditFundTransfer.findByAuditId.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
                sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        AuditFundTransfer auditFundTransfer = new AuditFundTransfer();
        auditFundTransfer.setId(2);
        auditFundTransfer.setAuditId(1);
        auditFundTransfer.setDetails(jsonStr);
		when(auditFundTransferRepositoryMock.findAuditFundTransferByRefId(Mockito.any())).thenReturn(auditFundTransfer);

		AuditDetailsRecord auditDetailsRecord = new MyAuditDetailsRecord();
		when(auditFundTransferRepositoryMock.findByAuditId(Mockito.any())).thenReturn(auditDetailsRecord);

		AuditDetailsActivity auditDetailsActivity = (AuditDetailsActivity) auditDetailsService.getAuditDetailsActivity(auditId, evntCode);
		logger.debug("    auditDetailsActivity: " + auditDetailsActivity);
		assertNotNull(auditDetailsActivity);
	}
	
	class MyAuditDetailsRecord implements AuditDetailsRecord {

		@Override
		public String getDetails() {
			return "This is mock fund transfer details.";
		}
		
	}

	private void addAuditDetailConfig(List<AuditDetailConfig> auditDetailConfigList, String fieldName, String path) {
		AuditDetailConfig auditDetailConfig = new AuditDetailConfig();
		auditDetailConfig.setFieldName(fieldName);
		auditDetailConfig.setPath(path);
		auditDetailConfigList.add(auditDetailConfig);
	}

	@Test(expected = CommonException.class)
	public void getAuditDetailsTest_notFound() throws Exception {
		logger.debug("getAuditDetailsTest_notFound()");

		when(auditRepositoryMock.findOne(Mockito.anyInt())).thenReturn(null);

		auditDetailsService.getAuditDetailsActivity(auditId, eventCode);
	}
}
