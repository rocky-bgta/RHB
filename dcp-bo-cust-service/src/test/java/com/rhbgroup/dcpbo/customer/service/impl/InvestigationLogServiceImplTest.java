package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.dcpbo.TelemetryAuditType;
import com.rhbgroup.dcpbo.customer.dcpbo.TelemetryOperationName;
import com.rhbgroup.dcpbo.customer.dto.AuditType;
import com.rhbgroup.dcpbo.customer.dto.OperationName;
import com.rhbgroup.dcpbo.customer.dto.TelemetryData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.TelemetryLog;
import com.rhbgroup.dcpbo.customer.model.TelemetryLogPK;
import com.rhbgroup.dcpbo.customer.repository.TelemetryAuditTypeRepository;
import com.rhbgroup.dcpbo.customer.repository.TelemetryLogPayloadRepository;
import com.rhbgroup.dcpbo.customer.repository.TelemetryLogRepository;
import com.rhbgroup.dcpbo.customer.repository.TelemetryOperationNameRepository;
import com.rhbgroup.dcpbo.customer.service.InvestigationLogService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        InvestigationLogService.class,
        InvestigationLogServiceImpl.class
})
public class InvestigationLogServiceImplTest {

    @MockBean
    TelemetryAuditTypeRepository telemetryAuditTypeRepository;

    @MockBean
    TelemetryLogPayloadRepository telemetryLogPayloadRepositoryMock;

    @MockBean
    TelemetryLogRepository telemetryLogRepository;

    @MockBean
    TelemetryOperationNameRepository telemetryOperationNameRepository;

    @Autowired
    InvestigationLogService service;

    private static Logger logger = LogManager.getLogger(InvestigationLogServiceImplTest.class);

    @Test
    public void getOperationNamesSuccess() {

        List<TelemetryOperationName> operNames = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        TelemetryOperationName telemetryOperationName = new TelemetryOperationName();
        telemetryOperationName.setId(1);
        telemetryOperationName.setOperationName("GetAsbDashboard");
        telemetryOperationName.setCreatedBy("Admin");
        telemetryOperationName.setCreatedTime(currentDate.getTime());
        telemetryOperationName.setUpdatedBy("Admin");
        telemetryOperationName.setUpdatedTime(currentDate.getTime());
        operNames.add(telemetryOperationName);
        telemetryOperationName = new TelemetryOperationName();
        telemetryOperationName.setId(1);
        telemetryOperationName.setOperationName("GetCreditCardDashboard");
        telemetryOperationName.setCreatedBy("Admin");
        telemetryOperationName.setCreatedTime(currentDate.getTime());
        telemetryOperationName.setUpdatedBy("Admin");
        telemetryOperationName.setUpdatedTime(currentDate.getTime());
        operNames.add(telemetryOperationName);

        when(telemetryOperationNameRepository.findAll()).thenReturn(operNames);

        OperationName operName = service.getOperationNames();
        assertEquals(2, operName.getOperationName().size());
        assertEquals("GetAsbDashboard", operName.getOperationName().get(0));
        assertEquals("GetCreditCardDashboard", operName.getOperationName().get(1));
    }

    @Test
    public void getOperationNamesReturnEmptySuccess() {

        List<TelemetryOperationName> operNames = new ArrayList<>();
        when(telemetryOperationNameRepository.findAll()).thenReturn(operNames);

        OperationName operName = service.getOperationNames();
        assertEquals(0, operName.getOperationName().size());
    }

    @Test
    public void getAuditTypesSuccess() {
        logger.debug("getAuditTypesSuccess()");

        Calendar currentDate = Calendar.getInstance();

        List<TelemetryAuditType> auditTypeList = new ArrayList<>();
        TelemetryAuditType telemetryAuditType = new TelemetryAuditType();
        telemetryAuditType.setId(1);
        telemetryAuditType.setAuditType("RQI");
        telemetryAuditType.setCreatedBy("Admin");
        telemetryAuditType.setCreatedTime(currentDate.getTime());
        telemetryAuditType.setUpdatedBy("Admin");
        telemetryAuditType.setUpdatedTime(currentDate.getTime());
        auditTypeList.add(telemetryAuditType);
        telemetryAuditType = new TelemetryAuditType();
        telemetryAuditType.setId(2);
        telemetryAuditType.setAuditType("Adaptorin");
        telemetryAuditType.setCreatedBy("Admin");
        telemetryAuditType.setCreatedTime(currentDate.getTime());
        telemetryAuditType.setUpdatedBy("Admin");
        telemetryAuditType.setUpdatedTime(currentDate.getTime());
        auditTypeList.add(telemetryAuditType);

        when(telemetryAuditTypeRepository.findAll()).thenReturn(auditTypeList);

        AuditType auditType = service.getAuditTypes();
        assertEquals(2, auditType.getAuditType().size());
        assertEquals("RQI", auditType.getAuditType().get(0));
        assertEquals("Adaptorin", auditType.getAuditType().get(1));
    }

    @Test
    public void getAuditTypesReturnEmptySuccess() {

        List<TelemetryAuditType> auditTypeList = new ArrayList<>();
        when(telemetryAuditTypeRepository.findAll()).thenReturn(auditTypeList);

        AuditType auditType = service.getAuditTypes();
        assertEquals(0, auditType.getAuditType().size());
    }

    @Test
    public void getNewLogsSuccess() {

        List<TelemetryLog> telemetryLogs = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        TelemetryLog telemetryLog = new TelemetryLog();
        TelemetryLogPK telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b");
        telemetryLogPK.setAuditType("AdaptorIn");
        telemetryLogPK.setOperationName("GetAccountProfilesLogic");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(currentDate.getTime());
        telemetryLog.setUsername("james");
        telemetryLog.setTotalError("1");
        telemetryLogs.add(telemetryLog);
        telemetryLog = new TelemetryLog();
        telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("89e8492d-4a8f-4786-8207-ecfe68c5bd2e");
        telemetryLogPK.setAuditType("RPO");
        telemetryLogPK.setOperationName("RegisterSecurePlus");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(currentDate.getTime());
        telemetryLog.setUsername("apple");
        telemetryLog.setTotalError("3");
        telemetryLogs.add(telemetryLog);

        when(telemetryLogRepository.findTop20()).thenReturn(telemetryLogs);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strAuditDateTime = dateFormat.format(currentDate.getTime());
        TelemetryData telemetryData = service.getNewLogs();
        assertEquals(2, telemetryData.getData().size());
        assertEquals("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b", telemetryData.getData().get(0).getMessageId());
        assertEquals("GetAccountProfilesLogic", telemetryData.getData().get(0).getOperationName());
        assertEquals("AdaptorIn", telemetryData.getData().get(0).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(0).getAuditDateTime());
        assertEquals("james", telemetryData.getData().get(0).getUsername());
        assertEquals("1", telemetryData.getData().get(0).getTotalError());
        assertEquals("89e8492d-4a8f-4786-8207-ecfe68c5bd2e", telemetryData.getData().get(1).getMessageId());
        assertEquals("RegisterSecurePlus", telemetryData.getData().get(1).getOperationName());
        assertEquals("RPO", telemetryData.getData().get(1).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(1).getAuditDateTime());
        assertEquals("apple", telemetryData.getData().get(1).getUsername());
        assertEquals("3", telemetryData.getData().get(1).getTotalError());

    }

    @Test
    public void getNewLogsReturnEmptySuccess() {

        List<TelemetryLog> telemetryLogs = new ArrayList<>();
        when(telemetryLogRepository.findTop20()).thenReturn(telemetryLogs);

        TelemetryData telemetryData = service.getNewLogs();
        assertEquals(0, telemetryData.getData().size());
    }

    @Test
    public void getLogsWithAllInputParametersSuccess() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+08:00'");
        Instant instant = Instant.now();
        Timestamp toDate = Timestamp.from(instant);
        String toDateStr = dateFormat.format(toDate);
        instant = instant.minus(2, ChronoUnit.HOURS);
        Timestamp fromDate = Timestamp.from(instant);
        String fromDateStr = dateFormat.format(fromDate);

        Integer pageNum = 1;
        ArrayList<String> auditTypes = new ArrayList<>(
                Arrays.asList("RPO",
                        "TCPIP_REQ",
                        "RQI"));
        String auditType = auditTypes.stream().collect(Collectors.joining(","));
        String keyword = "Offers";
        DateFormat dateFormatResp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strAuditDateTime = dateFormatResp.format(fromDate.getTime());

        List<TelemetryLog> telemetryLogs = new ArrayList<TelemetryLog>();
        TelemetryLog telemetryLog = new TelemetryLog();
        TelemetryLogPK telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b");
        telemetryLogPK.setAuditType("AdaptorIn");
        telemetryLogPK.setOperationName("GetAccountProfilesLogic");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(new Date(fromDate.getTime()));
        telemetryLog.setUsername("james");
        telemetryLog.setTotalError("1");
        telemetryLogs.add(telemetryLog);
        telemetryLog = new TelemetryLog();
        telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("89e8492d-4a8f-4786-8207-ecfe68c5bd2e");
        telemetryLogPK.setAuditType("RPO");
        telemetryLogPK.setOperationName("RegisterSecurePlus");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(new Date(fromDate.getTime()));
        telemetryLog.setUsername("apple");
        telemetryLog.setTotalError("3");
        telemetryLogs.add(telemetryLog);

        when(telemetryLogRepository.findByAuditTypeKeywordAndAuditDateTime(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(telemetryLogs);

        TelemetryData telemetryData = service.getLogs(auditType, keyword, pageNum, fromDateStr, toDateStr);

        assertEquals(2, telemetryData.getData().size());
        assertEquals("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b", telemetryData.getData().get(0).getMessageId());
        assertEquals("GetAccountProfilesLogic", telemetryData.getData().get(0).getOperationName());
        assertEquals("AdaptorIn", telemetryData.getData().get(0).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(0).getAuditDateTime());
        assertEquals("james", telemetryData.getData().get(0).getUsername());
        assertEquals("1", telemetryData.getData().get(0).getTotalError());
        assertEquals("89e8492d-4a8f-4786-8207-ecfe68c5bd2e", telemetryData.getData().get(1).getMessageId());
        assertEquals("RegisterSecurePlus", telemetryData.getData().get(1).getOperationName());
        assertEquals("RPO", telemetryData.getData().get(1).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(1).getAuditDateTime());
        assertEquals("apple", telemetryData.getData().get(1).getUsername());
        assertEquals("3", telemetryData.getData().get(1).getTotalError());

    }

    @Test
    public void getLogsWithoutAuditTypeSuccess() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+08:00'");
        Instant instant = Instant.now();
        Timestamp toDate = Timestamp.from(instant);
        String toDateStr = dateFormat.format(toDate);
        instant = instant.minus(2, ChronoUnit.HOURS);
        Timestamp fromDate = Timestamp.from(instant);
        String fromDateStr = dateFormat.format(fromDate);

        Integer pageNum = 1;
        String auditType = "ALL";
        String keyword = "Offers";
        DateFormat dateFormatResp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strAuditDateTime = dateFormatResp.format(fromDate.getTime());

        List<TelemetryLog> telemetryLogs = new ArrayList<>();
        TelemetryLog telemetryLog = new TelemetryLog();
        TelemetryLogPK telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b");
        telemetryLogPK.setAuditType("AdaptorIn");
        telemetryLogPK.setOperationName("GetAccountProfilesLogic");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(new Date(fromDate.getTime()));
        telemetryLog.setUsername("james");
        telemetryLog.setTotalError("1");
        telemetryLogs.add(telemetryLog);
        telemetryLog = new TelemetryLog();
        telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("89e8492d-4a8f-4786-8207-ecfe68c5bd2e");
        telemetryLogPK.setAuditType("RPO");
        telemetryLogPK.setOperationName("RegisterSecurePlus");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(new Date(fromDate.getTime()));
        telemetryLog.setUsername("apple");
        telemetryLog.setTotalError("3");
        telemetryLogs.add(telemetryLog);

        when(telemetryLogRepository.findByKeywordAndAuditDateTime(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(telemetryLogs);

        TelemetryData telemetryData = service.getLogs(auditType, keyword, pageNum, fromDateStr, toDateStr);

        assertEquals(2, telemetryData.getData().size());
        assertEquals("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b", telemetryData.getData().get(0).getMessageId());
        assertEquals("GetAccountProfilesLogic", telemetryData.getData().get(0).getOperationName());
        assertEquals("AdaptorIn", telemetryData.getData().get(0).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(0).getAuditDateTime());
        assertEquals("james", telemetryData.getData().get(0).getUsername());
        assertEquals("1", telemetryData.getData().get(0).getTotalError());
        assertEquals("89e8492d-4a8f-4786-8207-ecfe68c5bd2e", telemetryData.getData().get(1).getMessageId());
        assertEquals("RegisterSecurePlus", telemetryData.getData().get(1).getOperationName());
        assertEquals("RPO", telemetryData.getData().get(1).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(1).getAuditDateTime());
        assertEquals("apple", telemetryData.getData().get(1).getUsername());
        assertEquals("3", telemetryData.getData().get(1).getTotalError());

    }

    @Test
    public void getLogsWithoutAuditTypeFromDateToDateSuccess() {
        Instant instant = Instant.now();
        String toDateStr = "";
        instant = instant.minus(2, ChronoUnit.HOURS);
        Timestamp fromDate = Timestamp.from(instant);
        String fromDateStr = "";

        Integer pageNum = 1;
        String auditType = "ALL";
        String keyword = "Offers";
        DateFormat dateFormatResp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strAuditDateTime = dateFormatResp.format(fromDate.getTime());

        List<TelemetryLog> telemetryLogs = new ArrayList<TelemetryLog>();
        TelemetryLog telemetryLog = new TelemetryLog();
        TelemetryLogPK telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b");
        telemetryLogPK.setAuditType("AdaptorIn");
        telemetryLogPK.setOperationName("GetAccountProfilesLogic");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(new Date(fromDate.getTime()));
        telemetryLog.setUsername("james");
        telemetryLog.setTotalError("1");
        telemetryLogs.add(telemetryLog);
        telemetryLog = new TelemetryLog();
        telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("89e8492d-4a8f-4786-8207-ecfe68c5bd2e");
        telemetryLogPK.setAuditType("RPO");
        telemetryLogPK.setOperationName("RegisterSecurePlus");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(new Date(fromDate.getTime()));
        telemetryLog.setUsername("apple");
        telemetryLog.setTotalError("3");
        telemetryLogs.add(telemetryLog);

        when(telemetryLogRepository.findByKeywordAndAuditDateTime(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(telemetryLogs);

        TelemetryData telemetryData = service.getLogs(auditType, keyword, pageNum, fromDateStr, toDateStr);

        assertEquals(2, telemetryData.getData().size());
        assertEquals("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b", telemetryData.getData().get(0).getMessageId());
        assertEquals("GetAccountProfilesLogic", telemetryData.getData().get(0).getOperationName());
        assertEquals("AdaptorIn", telemetryData.getData().get(0).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(0).getAuditDateTime());
        assertEquals("james", telemetryData.getData().get(0).getUsername());
        assertEquals("1", telemetryData.getData().get(0).getTotalError());
        assertEquals("89e8492d-4a8f-4786-8207-ecfe68c5bd2e", telemetryData.getData().get(1).getMessageId());
        assertEquals("RegisterSecurePlus", telemetryData.getData().get(1).getOperationName());
        assertEquals("RPO", telemetryData.getData().get(1).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(1).getAuditDateTime());
        assertEquals("apple", telemetryData.getData().get(1).getUsername());
        assertEquals("3", telemetryData.getData().get(1).getTotalError());

    }

    @Test(expected = CommonException.class)
    public void getLogsWithInvalidFormatForFromDateToDateFail() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'08:00'");
        Instant instant = Instant.now();
        Timestamp toDate = Timestamp.from(instant);
        String toDateStr = dateFormat.format(toDate);
        instant = instant.minus(2, ChronoUnit.HOURS);
        Timestamp fromDate = Timestamp.from(instant);
        String fromDateStr = dateFormat.format(fromDate);

        Integer pageNum = 1;
        String auditType = "ALL";
        String keyword = "Offers";
        DateFormat dateFormatResp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strAuditDateTime = dateFormatResp.format(fromDate.getTime());

        List<TelemetryLog> telemetryLogs = new ArrayList<TelemetryLog>();
        TelemetryLog telemetryLog = new TelemetryLog();
        TelemetryLogPK telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b");
        telemetryLogPK.setAuditType("AdaptorIn");
        telemetryLogPK.setOperationName("GetAccountProfilesLogic");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(new Date(fromDate.getTime()));
        telemetryLog.setUsername("james");
        telemetryLog.setTotalError("1");
        telemetryLogs.add(telemetryLog);
        telemetryLog = new TelemetryLog();
        telemetryLogPK = new TelemetryLogPK();
        telemetryLogPK.setMessageId("89e8492d-4a8f-4786-8207-ecfe68c5bd2e");
        telemetryLogPK.setAuditType("RPO");
        telemetryLogPK.setOperationName("RegisterSecurePlus");
        telemetryLog.setId(telemetryLogPK);
        telemetryLog.setAuditDateTime(new Date(fromDate.getTime()));
        telemetryLog.setUsername("apple");
        telemetryLog.setTotalError("3");
        telemetryLogs.add(telemetryLog);

        when(telemetryLogRepository.findByKeywordAndAuditDateTime(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(telemetryLogs);

        TelemetryData telemetryData = service.getLogs(auditType, keyword, pageNum, fromDateStr, toDateStr);

        assertEquals(2, telemetryData.getData().size());
        assertEquals("42bd67a1-1dcc-4ea5-85f5-14d0b71f019b", telemetryData.getData().get(0).getMessageId());
        assertEquals("GetAccountProfilesLogic", telemetryData.getData().get(0).getOperationName());
        assertEquals("AdaptorIn", telemetryData.getData().get(0).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(0).getAuditDateTime());
        assertEquals("james", telemetryData.getData().get(0).getUsername());
        assertEquals("1", telemetryData.getData().get(0).getTotalError());
        assertEquals("89e8492d-4a8f-4786-8207-ecfe68c5bd2e", telemetryData.getData().get(1).getMessageId());
        assertEquals("RegisterSecurePlus", telemetryData.getData().get(1).getOperationName());
        assertEquals("RPO", telemetryData.getData().get(1).getAuditType());
        assertEquals(strAuditDateTime, telemetryData.getData().get(1).getAuditDateTime());
        assertEquals("apple", telemetryData.getData().get(1).getUsername());
        assertEquals("3", telemetryData.getData().get(1).getTotalError());

    }

}
