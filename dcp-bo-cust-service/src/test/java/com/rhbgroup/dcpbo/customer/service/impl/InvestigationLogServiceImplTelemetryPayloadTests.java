package com.rhbgroup.dcpbo.customer.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.customer.dto.TelemetryLogPayloadData;
import com.rhbgroup.dcpbo.customer.model.TelemetryLogPK;
import com.rhbgroup.dcpbo.customer.model.TelemetryLogPayload;
import com.rhbgroup.dcpbo.customer.repository.TelemetryAuditTypeRepository;
import com.rhbgroup.dcpbo.customer.repository.TelemetryLogPayloadRepository;
import com.rhbgroup.dcpbo.customer.repository.TelemetryLogRepository;
import com.rhbgroup.dcpbo.customer.repository.TelemetryOperationNameRepository;
import com.rhbgroup.dcpbo.customer.service.InvestigationLogService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        InvestigationLogService.class,
        InvestigationLogServiceImpl.class
})
public class InvestigationLogServiceImplTelemetryPayloadTests {

    @MockBean
    TelemetryOperationNameRepository telemetryOperationNameRepository;

    @MockBean
    TelemetryAuditTypeRepository telemetryAuditTypeRepositoryMock;
    
    @MockBean
    TelemetryLogRepository telemetryLogRepositoryMock;
    
    @MockBean
    TelemetryLogPayloadRepository telemetryLogPayloadRepository;

    @Autowired
    InvestigationLogService investigationLogService;

    private static Logger logger = LogManager.getLogger(InvestigationLogServiceImplTelemetryPayloadTests.class);


	String messageId = "2342342352352343";
	String payload = "{\"status\":{\"code\":\"80000\",\"statusType\":\"system_error\",\"description\":\"There seems to be a slight issue. Please try again later.\",\"title\":\"Oops!\"},\"data\":{}}";
	String auditParam = "{quick_login=false, op_status=false, referenceId=null, op_status_code=80000}";
	String cisNumber = null;
	String deviceId = "a05d47e9-76fe-4454-9781-399e032de6b2";
	String hostname = "myrhbodcpap01vs";

    @Test
    public void get() throws Exception {
    	logger.debug("get()");
    	
		String sAuditDateTime = "2019-04-19 00:00:00.000";
		executeTest(sAuditDateTime);
    }
    
    @Test
    public void anotherGet() throws Exception {
    	logger.debug("getWithNegativeTime()");
    	
		String sAuditDateTime = "2019-04-19 12:34:56.123";
		executeTest(sAuditDateTime);
    }
    
    private void executeTest(String sAuditDateTime) throws Exception {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp auditDateTime = new Timestamp(simpleDateFormat.parse("2019-02-27 12:43:00.23").getTime());
		
		List<TelemetryLogPayload> telemetryLogPayloads = new ArrayList<TelemetryLogPayload>();
		TelemetryLogPayload telemetryLogPayload = new TelemetryLogPayload();
        TelemetryLogPK telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b");
        telemetryLogPK.setAuditType("AdaptorIn");
        telemetryLogPK.setOperationName("GetAccountProfilesLogic");
		telemetryLogPayload.setAuditDateTime(auditDateTime);
		telemetryLogPayload.setPayload(payload.getBytes());
		telemetryLogPayload.setAuditParam(auditParam);
		telemetryLogPayload.setHostname(hostname);
		telemetryLogPayload.setCisNumber(cisNumber);
		telemetryLogPayload.setDeviceId(deviceId);
		telemetryLogPayloads.add(telemetryLogPayload);
		when(telemetryLogPayloadRepository.findByMessageIdAndAuditDateTime(Mockito.anyString(), Mockito.any())).thenReturn(telemetryLogPayloads);

        TelemetryLogPayloadData telemetryPayloadData = investigationLogService.getTelemetryPayloadData(messageId, sAuditDateTime);
        logger.debug("    telemetryPayload: " + telemetryPayloadData);
        
        assertEquals(auditDateTime.toString(), telemetryPayloadData.getData().getAuditDateTime());
        assertEquals(payload, telemetryPayloadData.getData().getPayload());
        assertEquals(auditParam, telemetryPayloadData.getData().getAuditParam());
        assertEquals(hostname, telemetryPayloadData.getData().getHostname());
        assertEquals(cisNumber, telemetryPayloadData.getData().getCisNumber());
        assertEquals(deviceId, telemetryPayloadData.getData().getDeviceId());
    }

}
