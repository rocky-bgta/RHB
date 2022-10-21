package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.dto.TelemetryErrorLog;
import com.rhbgroup.dcpbo.customer.dto.TelemetryErrorLogs;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.DcpTelemetryErrorLog;
import com.rhbgroup.dcpbo.customer.repository.DcpTelemetryErrorLogRepository;
import com.rhbgroup.dcpbo.customer.service.DcpTelemetryErrorLogService;
import org.apache.commons.lang3.StringUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        DcpTelemetryErrorLogService.class,
        DcpTelemetryErrorLogServiceImpl.class,
        DcpTelemetryErrorLogRepository.class
})
public class DcpTelemetryErrorLogServiceImplTests {

    private static final String FORMAT_DATE = "yyyy-MM-dd HH:mm:ss.SSS";

    @Autowired
    private DcpTelemetryErrorLogService dcpTelemetryErrorLogService;

    @MockBean
    private DcpTelemetryErrorLogRepository dcpTelemetryErrorLogRepository;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Test
    public void testBoDataListingSuccess() {
        Integer pageNo = 1;
        String keyword = "ERROR_CODE";
        String frDateStr = "2019-04-18T00:00:00+08:00";
        String toDateStr = "2019-04-19T00:00:00+08:00";
        Integer totalRecord = 10;

        List<DcpTelemetryErrorLog> mockDcpTelemetryErrorLogs = new ArrayList<>();
        for (int i = 0; i < totalRecord; i++) {
            DcpTelemetryErrorLog mockTelemetryErrorLog = new DcpTelemetryErrorLog();

            int count = totalRecord + i;
            String mockAuditDateTime = "2019-04-18T" + Integer.toString(count) + ":15:30.00Z";
            Instant instant = Instant.parse(mockAuditDateTime);
            Timestamp timestamp = Timestamp.from(instant);
            mockTelemetryErrorLog.setAuditDateTime(timestamp);

            mockTelemetryErrorLog.setErrorCode("ERROR_CODE_" + Integer.toString(i));
            mockTelemetryErrorLog.setMessageId(Integer.toString(i));
            mockTelemetryErrorLog.setOperationName("OPERATION_NAME_" + Integer.toString(i));
            mockDcpTelemetryErrorLogs.add(mockTelemetryErrorLog);
        }

        when(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(mockDcpTelemetryErrorLogs);
        when(dcpTelemetryErrorLogRepository.countFindByKeywordAndAuditDateTime(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(totalRecord);

        TelemetryErrorLogs telemetryErrorLogs = (TelemetryErrorLogs) dcpTelemetryErrorLogService.listing(keyword, frDateStr, toDateStr, pageNo);

        assertEquals("L", telemetryErrorLogs.getPagination().getPageIndicator());
        assertEquals(mockDcpTelemetryErrorLogs.size(), telemetryErrorLogs.getPagination().getActivityCount());
        assertEquals(pageNo.intValue(), telemetryErrorLogs.getPagination().getPageNum());
        assertEquals(1, telemetryErrorLogs.getPagination().getTotalPageNum());
        assertEquals(totalRecord.intValue(), telemetryErrorLogs.getPagination().getActivityCount());
    }

    @Test
    public void testBoDataListingSuccessMoreThanPageSize() {
        Integer pageNo = 1;
        String keyword = "ERROR_CODE";
        String frDateStr = "2019-04-18T00:00:00+08:00";
        String toDateStr = "2019-04-19T00:00:00+08:00";
        Integer totalRecord = 24;

        List<DcpTelemetryErrorLog> mockDcpTelemetryErrorLogs = new ArrayList<>();
        for (int i = 0; i < totalRecord; i++) {
            DcpTelemetryErrorLog mockTelemetryErrorLog = new DcpTelemetryErrorLog();

            String count = Integer.toString(i);
            String mockAuditDateTime = "2019-04-18T" + StringUtils.leftPad(count, 2, "0") + ":15:30.00Z";
            Instant instant = Instant.parse(mockAuditDateTime);
            Timestamp timestamp = Timestamp.from(instant);
            mockTelemetryErrorLog.setAuditDateTime(timestamp);

            mockTelemetryErrorLog.setErrorCode("ERROR_CODE_" + Integer.toString(i));
            mockTelemetryErrorLog.setMessageId(Integer.toString(i));
            mockTelemetryErrorLog.setOperationName("OPERATION_NAME_" + Integer.toString(i));
            mockDcpTelemetryErrorLogs.add(mockTelemetryErrorLog);
        }

        when(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(mockDcpTelemetryErrorLogs);
        when(dcpTelemetryErrorLogRepository.countFindByKeywordAndAuditDateTime(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(totalRecord);

        TelemetryErrorLogs telemetryErrorLogs = (TelemetryErrorLogs) dcpTelemetryErrorLogService.listing(keyword, frDateStr, toDateStr, pageNo);

        assertEquals("N", telemetryErrorLogs.getPagination().getPageIndicator());
        assertEquals(mockDcpTelemetryErrorLogs.size(), telemetryErrorLogs.getPagination().getActivityCount());
        assertEquals(pageNo.intValue(), telemetryErrorLogs.getPagination().getPageNum());
        assertEquals(2, telemetryErrorLogs.getPagination().getTotalPageNum());
        assertEquals(totalRecord.intValue(), telemetryErrorLogs.getPagination().getActivityCount());
    }

    @Test
    public void testBoDataListingSuccessNotFound() {
        Integer pageNo = 1;
        String keyword = "AAAAA";
        String frDateStr = "2019-04-18T00:00:00+08:00";
        String toDateStr = "2019-04-19T00:00:00+08:00";

        when(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ArrayList<>());

        TelemetryErrorLogs telemetryErrorLogs = (TelemetryErrorLogs) dcpTelemetryErrorLogService.listing(keyword, frDateStr, toDateStr, pageNo);

        assertEquals("L", telemetryErrorLogs.getPagination().getPageIndicator());
        assertEquals(0, telemetryErrorLogs.getPagination().getActivityCount());
        assertEquals(pageNo.intValue(), telemetryErrorLogs.getPagination().getPageNum());
        assertEquals(0, telemetryErrorLogs.getPagination().getTotalPageNum());
    }

    @Test
    public void testBoDataListingSuccessFromDateIsEmpty() {
        Integer pageNo = 1;
        String keyword = "NullPointerException";
        String frDateStr = "";
        String toDateStr = "2019-04-19T00:00:00+08:00";
        Integer totalRecord = 1;

        List<DcpTelemetryErrorLog> mockDcpTelemetryErrorLogs = new ArrayList<>();
        DcpTelemetryErrorLog mockTelemetryErrorLog = new DcpTelemetryErrorLog();

        String mockAuditDateTime = "2019-04-18T00:15:30.00Z";
        Instant instant = Instant.parse(mockAuditDateTime);
        Timestamp timestamp = Timestamp.from(instant);
        mockTelemetryErrorLog.setAuditDateTime(timestamp);

        mockTelemetryErrorLog.setErrorCode(null);
        mockTelemetryErrorLog.setMessageId("02ea5015-bb4f-4669-b5b9-394bb50d04b4");
        mockTelemetryErrorLog.setOperationName("InitUserNotification");
        mockDcpTelemetryErrorLogs.add(mockTelemetryErrorLog);

        when(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(mockDcpTelemetryErrorLogs);
        when(dcpTelemetryErrorLogRepository.countFindByKeywordAndAuditDateTime(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(totalRecord);

        TelemetryErrorLogs telemetryErrorLogs = (TelemetryErrorLogs) dcpTelemetryErrorLogService.listing(keyword, frDateStr, toDateStr, pageNo);

        assertEquals("L", telemetryErrorLogs.getPagination().getPageIndicator());
        assertEquals(mockDcpTelemetryErrorLogs.size(), telemetryErrorLogs.getPagination().getActivityCount());
        assertEquals(pageNo.intValue(), telemetryErrorLogs.getPagination().getPageNum());
        assertEquals(1, telemetryErrorLogs.getPagination().getTotalPageNum());
    }

    @Test
    public void testBoDataListingSuccessFromDateIsNull() {
        Integer pageNo = 1;
        String keyword = "NullPointerException";
        String frDateStr = null;
        String toDateStr = "2019-04-19T00:00:00+08:00";
        Integer totalRecord = 1;

        List<DcpTelemetryErrorLog> mockDcpTelemetryErrorLogs = new ArrayList<>();
        DcpTelemetryErrorLog mockTelemetryErrorLog = new DcpTelemetryErrorLog();

        String mockAuditDateTime = "2019-04-18T00:15:30.00Z";
        Instant instant = Instant.parse(mockAuditDateTime);
        Timestamp timestamp = Timestamp.from(instant);
        mockTelemetryErrorLog.setAuditDateTime(timestamp);

        mockTelemetryErrorLog.setErrorCode("80000");
        mockTelemetryErrorLog.setMessageId("02ea5015-bb4f-4669-b5b9-394bb50d04b4");
        mockTelemetryErrorLog.setOperationName("InitUserNotification");
        mockDcpTelemetryErrorLogs.add(mockTelemetryErrorLog);

        when(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(mockDcpTelemetryErrorLogs);
        when(dcpTelemetryErrorLogRepository.countFindByKeywordAndAuditDateTime(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(totalRecord);

        TelemetryErrorLogs telemetryErrorLogs = (TelemetryErrorLogs) dcpTelemetryErrorLogService.listing(keyword, frDateStr, toDateStr, pageNo);

        assertEquals("L", telemetryErrorLogs.getPagination().getPageIndicator());
        assertEquals(mockDcpTelemetryErrorLogs.size(), telemetryErrorLogs.getPagination().getActivityCount());
        assertEquals(pageNo.intValue(), telemetryErrorLogs.getPagination().getPageNum());
        assertEquals(1, telemetryErrorLogs.getPagination().getTotalPageNum());
    }

    @Test
    public void testBoDataListingSuccessToDateIsEmpty() {
        Integer pageNo = 1;
        String keyword = "NullPointerException";
        String frDateStr = "2019-04-18T00:00:00+08:00";
        String toDateStr = "";
        Integer totalRecord = 1;

        List<DcpTelemetryErrorLog> mockDcpTelemetryErrorLogs = new ArrayList<>();
        DcpTelemetryErrorLog mockTelemetryErrorLog = new DcpTelemetryErrorLog();

        String mockAuditDateTime = "2019-04-18T00:15:30.00Z";
        Instant instant = Instant.parse(mockAuditDateTime);
        Timestamp timestamp = Timestamp.from(instant);
        mockTelemetryErrorLog.setAuditDateTime(timestamp);

        mockTelemetryErrorLog.setErrorCode("80000");
        mockTelemetryErrorLog.setMessageId("02ea5015-bb4f-4669-b5b9-394bb50d04b4");
        mockTelemetryErrorLog.setOperationName("InitUserNotification");
        mockDcpTelemetryErrorLogs.add(mockTelemetryErrorLog);

        when(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(mockDcpTelemetryErrorLogs);
        when(dcpTelemetryErrorLogRepository.countFindByKeywordAndAuditDateTime(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(totalRecord);

        TelemetryErrorLogs telemetryErrorLogs = (TelemetryErrorLogs) dcpTelemetryErrorLogService.listing(keyword, frDateStr, toDateStr, pageNo);

        assertEquals("L", telemetryErrorLogs.getPagination().getPageIndicator());
        assertEquals(mockDcpTelemetryErrorLogs.size(), telemetryErrorLogs.getPagination().getActivityCount());
        assertEquals(pageNo.intValue(), telemetryErrorLogs.getPagination().getPageNum());
        assertEquals(1, telemetryErrorLogs.getPagination().getTotalPageNum());
    }

    @Test
    public void testBoDataListingSuccessToDateIsNull() {
        Integer pageNo = 1;
        String keyword = "NullPointerException";
        String frDateStr = "2019-04-18T00:00:00+08:00";
        String toDateStr = null;
        Integer totalRecord = 1;

        List<DcpTelemetryErrorLog> mockDcpTelemetryErrorLogs = new ArrayList<>();
        DcpTelemetryErrorLog mockTelemetryErrorLog = new DcpTelemetryErrorLog();

        String mockAuditDateTime = "2019-04-18T00:15:30.00Z";
        Instant instant = Instant.parse(mockAuditDateTime);
        Timestamp timestamp = Timestamp.from(instant);
        mockTelemetryErrorLog.setAuditDateTime(timestamp);

        mockTelemetryErrorLog.setErrorCode("80000");
        mockTelemetryErrorLog.setMessageId("02ea5015-bb4f-4669-b5b9-394bb50d04b4");
        mockTelemetryErrorLog.setOperationName("InitUserNotification");
        mockDcpTelemetryErrorLogs.add(mockTelemetryErrorLog);

        when(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(mockDcpTelemetryErrorLogs);
        when(dcpTelemetryErrorLogRepository.countFindByKeywordAndAuditDateTime(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(totalRecord);

        TelemetryErrorLogs telemetryErrorLogs = (TelemetryErrorLogs) dcpTelemetryErrorLogService.listing(keyword, frDateStr, toDateStr, pageNo);

        assertEquals("L", telemetryErrorLogs.getPagination().getPageIndicator());
        assertEquals(mockDcpTelemetryErrorLogs.size(), telemetryErrorLogs.getPagination().getActivityCount());
        assertEquals(pageNo.intValue(), telemetryErrorLogs.getPagination().getPageNum());
        assertEquals(1, telemetryErrorLogs.getPagination().getTotalPageNum());
    }

    @Test
    public void testBoDataListingErrorBothDateEmpty() {
        Integer pageNo = 1;
        String keyword = "NullPointerException";
        String frDateStr = "";
        String toDateStr = "";
        Integer totalRecord = 1;

        List<DcpTelemetryErrorLog> mockDcpTelemetryErrorLogs = new ArrayList<>();
        DcpTelemetryErrorLog mockTelemetryErrorLog = new DcpTelemetryErrorLog();

        String mockAuditDateTime = "2019-04-18T00:15:30.00Z";
        Instant instant = Instant.parse(mockAuditDateTime);
        Timestamp timestamp = Timestamp.from(instant);
        mockTelemetryErrorLog.setAuditDateTime(timestamp);

        mockTelemetryErrorLog.setErrorCode("80000");
        mockTelemetryErrorLog.setMessageId("02ea5015-bb4f-4669-b5b9-394bb50d04b4");
        mockTelemetryErrorLog.setOperationName("InitUserNotification");
        mockDcpTelemetryErrorLogs.add(mockTelemetryErrorLog);

        when(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(mockDcpTelemetryErrorLogs);
        when(dcpTelemetryErrorLogRepository.countFindByKeywordAndAuditDateTime(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(totalRecord);

        TelemetryErrorLogs telemetryErrorLogs = (TelemetryErrorLogs) dcpTelemetryErrorLogService.listing(keyword, frDateStr, toDateStr, pageNo);

        assertEquals("L", telemetryErrorLogs.getPagination().getPageIndicator());
        assertEquals(mockDcpTelemetryErrorLogs.size(), telemetryErrorLogs.getPagination().getActivityCount());
        assertEquals(pageNo.intValue(), telemetryErrorLogs.getPagination().getPageNum());
        assertEquals(1, telemetryErrorLogs.getPagination().getTotalPageNum());
    }

    @Test
    public void testBoDataListingErrorBothDateNull() {
        Integer pageNo = 1;
        String keyword = "NullPointerException";
        String frDateStr = null;
        String toDateStr = null;
        Integer totalRecord = 1;

        List<DcpTelemetryErrorLog> mockDcpTelemetryErrorLogs = new ArrayList<>();
        DcpTelemetryErrorLog mockTelemetryErrorLog = new DcpTelemetryErrorLog();

        // 2019-04-18 08:15:30.0
        String mockAuditDateTime = "2019-04-18T00:15:30.00Z";
        Instant instant = Instant.parse(mockAuditDateTime);
        Timestamp timestamp = Timestamp.from(instant);
        mockTelemetryErrorLog.setAuditDateTime(timestamp);

        mockTelemetryErrorLog.setErrorCode("80000");
        mockTelemetryErrorLog.setMessageId("02ea5015-bb4f-4669-b5b9-394bb50d04b4");
        mockTelemetryErrorLog.setOperationName("InitUserNotification");
        mockDcpTelemetryErrorLogs.add(mockTelemetryErrorLog);

        when(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(mockDcpTelemetryErrorLogs);
        when(dcpTelemetryErrorLogRepository.countFindByKeywordAndAuditDateTime(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(totalRecord);

        TelemetryErrorLogs telemetryErrorLogs = (TelemetryErrorLogs) dcpTelemetryErrorLogService.listing(keyword, frDateStr, toDateStr, pageNo);

        assertEquals("L", telemetryErrorLogs.getPagination().getPageIndicator());
        assertEquals(mockDcpTelemetryErrorLogs.size(), telemetryErrorLogs.getPagination().getActivityCount());
        assertEquals(pageNo.intValue(), telemetryErrorLogs.getPagination().getPageNum());
        assertEquals(1, telemetryErrorLogs.getPagination().getTotalPageNum());
        assertEquals(dateFormat.format(timestamp), telemetryErrorLogs.getTelemetryErrorLogs().get(0).getAuditDateTime());
        assertNotEquals(timestamp.toString(), telemetryErrorLogs.getTelemetryErrorLogs().get(0).getAuditDateTime());
    }

    @Test
    public void testTelemetryErrorLogDetailsSuccess() {
        String messageId = "1ea2ff9b-794c-4a32-b82c-7577161be060";
        String auditDateTime = "2018-12-06 17:02:15.123";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);

        LocalDateTime localDateTimeFrom = LocalDateTime.parse(auditDateTime, formatter);
        Timestamp auditDateTimeFrom = Timestamp.valueOf(localDateTimeFrom);

        LocalDateTime localDateTimeTo = LocalDateTime.parse(auditDateTime, formatter).plusSeconds(1);
        Timestamp auditDateTimeTo = Timestamp.valueOf(localDateTimeTo);

        List<DcpTelemetryErrorLog> dcpTelemetryErrorLogs = new ArrayList<>();

        DcpTelemetryErrorLog dcpTelemetryErrorLog = new DcpTelemetryErrorLog();
        dcpTelemetryErrorLog.setAuditDateTime(auditDateTimeFrom);
        dcpTelemetryErrorLog.setErrorDetails("java.lang.NullPointerException\n" +
                "	at com.rhbgroup.dcp.identityservice.controller.IdentityController.authenticate(IdentityController.java:128)\n" +
                "	at sun.reflect.GeneratedMethodAccessor1574.invoke(Unknown Source)\n" +
                "	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
                "	at java.lang.reflect.Method.invoke(Method.java:498)\n" +
                "	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)\n" +
                "	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:133)\n" +
                "	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:97)\n" +
                "	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:827)\n" +
                "	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:738)\n" +
                "	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:85)\n" +
                "	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:967)\n" +
                "	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:901)\n" +
                "	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:970)\n" +
                "	at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:872)\n" +
                "	at javax.servlet.http.HttpServlet.service(HttpServlet.java:707)\n" +
                "	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:846)\n" +
                "	at javax.servlet.http.HttpServlet.service(HttpServlet.java:790)\n" +
                "	at io.undertow.servlet.handlers.ServletHandler.handleRequest(ServletHandler.java:85)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:129)\n" +
                "	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:99)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at org.springframework.web.filter.HttpPutFormContentFilter.doFilterInternal(HttpPutFormContentFilter.java:108)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:81)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:197)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at org.springframework.boot.web.support.ErrorPageFilter.doFilter(ErrorPageFilter.java:115)\n" +
                "	at org.springframework.boot.web.support.ErrorPageFilter.access$000(ErrorPageFilter.java:59)\n" +
                "	at org.springframework.boot.web.support.ErrorPageFilter$1.doFilterInternal(ErrorPageFilter.java:90)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at org.springframework.boot.web.support.ErrorPageFilter.doFilter(ErrorPageFilter.java:108)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler.handleRequest(FilterHandler.java:84)\n" +
                "	at io.undertow.servlet.handlers.security.ServletSecurityRoleHandler.handleRequest(ServletSecurityRoleHandler.java:62)\n" +
                "	at io.undertow.servlet.handlers.ServletDispatchingHandler.handleRequest(ServletDispatchingHandler.java:36)\n" +
                "	at org.wildfly.extension.undertow.security.SecurityContextAssociationHandler.handleRequest(SecurityContextAssociationHandler.java:78)\n" +
                "	at io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
                "	at io.undertow.servlet.handlers.security.SSLInformationAssociationHandler.handleRequest(SSLInformationAssociationHandler.java:131)\n" +
                "	at io.undertow.servlet.handlers.security.ServletAuthenticationCallHandler.handleRequest(ServletAuthenticationCallHandler.java:57)\n" +
                "	at io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
                "	at io.undertow.security.handlers.AbstractConfidentialityHandler.handleReque");
        dcpTelemetryErrorLog.setErrorReason("logins|Oops!|There seems to be a slight issue. Please try again later.");
        dcpTelemetryErrorLog.setMessageId(messageId);
        dcpTelemetryErrorLog.setOperationName("InitUserNotification");

        dcpTelemetryErrorLogs.add(dcpTelemetryErrorLog);

        when(dcpTelemetryErrorLogRepository.findByMessageIdAndAuditDateTime(messageId, auditDateTime)).thenReturn(dcpTelemetryErrorLogs);

        TelemetryErrorLog telemetryErrorLog = (TelemetryErrorLog) dcpTelemetryErrorLogService.getTelemetryErrorLogDetails(messageId, auditDateTime);

        assertEquals("java.lang.NullPointerException\n" +
                "	at com.rhbgroup.dcp.identityservice.controller.IdentityController.authenticate(IdentityController.java:128)\n" +
                "	at sun.reflect.GeneratedMethodAccessor1574.invoke(Unknown Source)\n" +
                "	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
                "	at java.lang.reflect.Method.invoke(Method.java:498)\n" +
                "	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)\n" +
                "	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:133)\n" +
                "	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:97)\n" +
                "	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:827)\n" +
                "	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:738)\n" +
                "	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:85)\n" +
                "	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:967)\n" +
                "	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:901)\n" +
                "	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:970)\n" +
                "	at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:872)\n" +
                "	at javax.servlet.http.HttpServlet.service(HttpServlet.java:707)\n" +
                "	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:846)\n" +
                "	at javax.servlet.http.HttpServlet.service(HttpServlet.java:790)\n" +
                "	at io.undertow.servlet.handlers.ServletHandler.handleRequest(ServletHandler.java:85)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:129)\n" +
                "	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:99)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at org.springframework.web.filter.HttpPutFormContentFilter.doFilterInternal(HttpPutFormContentFilter.java:108)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:81)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:197)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at org.springframework.boot.web.support.ErrorPageFilter.doFilter(ErrorPageFilter.java:115)\n" +
                "	at org.springframework.boot.web.support.ErrorPageFilter.access$000(ErrorPageFilter.java:59)\n" +
                "	at org.springframework.boot.web.support.ErrorPageFilter$1.doFilterInternal(ErrorPageFilter.java:90)\n" +
                "	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)\n" +
                "	at org.springframework.boot.web.support.ErrorPageFilter.doFilter(ErrorPageFilter.java:108)\n" +
                "	at io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
                "	at io.undertow.servlet.handlers.FilterHandler.handleRequest(FilterHandler.java:84)\n" +
                "	at io.undertow.servlet.handlers.security.ServletSecurityRoleHandler.handleRequest(ServletSecurityRoleHandler.java:62)\n" +
                "	at io.undertow.servlet.handlers.ServletDispatchingHandler.handleRequest(ServletDispatchingHandler.java:36)\n" +
                "	at org.wildfly.extension.undertow.security.SecurityContextAssociationHandler.handleRequest(SecurityContextAssociationHandler.java:78)\n" +
                "	at io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
                "	at io.undertow.servlet.handlers.security.SSLInformationAssociationHandler.handleRequest(SSLInformationAssociationHandler.java:131)\n" +
                "	at io.undertow.servlet.handlers.security.ServletAuthenticationCallHandler.handleRequest(ServletAuthenticationCallHandler.java:57)\n" +
                "	at io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
                "	at io.undertow.security.handlers.AbstractConfidentialityHandler.handleReque", telemetryErrorLog.getErrorDetails());
        assertEquals("logins|Oops!|There seems to be a slight issue. Please try again later.", telemetryErrorLog.getErrorReason());
        assertEquals(messageId, telemetryErrorLog.getMessageId());
        assertEquals(auditDateTimeFrom.toString(), telemetryErrorLog.getAuditDateTime());
    }

    @Test
    public void testTelemetryMultipleErrorLogDetailsSuccess() {
        String messageId = "1ea2ff9b-794c-4a32-b82c-7577161be060";
        String auditDateTime = "2018-12-06 17:02:15.123";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);

        LocalDateTime localDateTimeFrom = LocalDateTime.parse(auditDateTime, formatter);
        Timestamp auditDateTimeFrom = Timestamp.valueOf(localDateTimeFrom);

        LocalDateTime localDateTimeTo = LocalDateTime.parse(auditDateTime, formatter).plusSeconds(1);
        Timestamp auditDateTimeTo = Timestamp.valueOf(localDateTimeTo);

        List<DcpTelemetryErrorLog> dcpTelemetryErrorLogs = new ArrayList<>();

        DcpTelemetryErrorLog dcpTelemetryErrorLog = new DcpTelemetryErrorLog();
        dcpTelemetryErrorLog.setAuditDateTime(auditDateTimeFrom);
        dcpTelemetryErrorLog.setErrorDetails("Error No.1");
        dcpTelemetryErrorLog.setErrorReason("1st time login");
        dcpTelemetryErrorLog.setMessageId(messageId);
        dcpTelemetryErrorLog.setOperationName("InitUserNotification");

        dcpTelemetryErrorLogs.add(dcpTelemetryErrorLog);

        DcpTelemetryErrorLog dcpTelemetryErrorLog2 = new DcpTelemetryErrorLog();
        dcpTelemetryErrorLog2.setAuditDateTime(auditDateTimeTo);
        dcpTelemetryErrorLog2.setErrorDetails("Error No.2");
        dcpTelemetryErrorLog2.setErrorReason("2nd time login");
        dcpTelemetryErrorLog2.setMessageId(messageId);
        dcpTelemetryErrorLog2.setOperationName("InitUserNotification");

        dcpTelemetryErrorLogs.add(dcpTelemetryErrorLog2);

        when(dcpTelemetryErrorLogRepository.findByMessageIdAndAuditDateTime(messageId, auditDateTime)).thenReturn(dcpTelemetryErrorLogs);

        TelemetryErrorLog telemetryErrorLog = (TelemetryErrorLog) dcpTelemetryErrorLogService.getTelemetryErrorLogDetails(messageId, auditDateTime);

        assertEquals("Error No.2", telemetryErrorLog.getErrorDetails());
        assertEquals("2nd time login", telemetryErrorLog.getErrorReason());
        assertEquals(messageId, telemetryErrorLog.getMessageId());
        assertEquals(auditDateTimeTo.toString(), telemetryErrorLog.getAuditDateTime());
    }

    @Test(expected = CommonException.class)
    public void testTelemetryErrorLogDetailsSuccessNull() {
        String messageId = "01e4abd2-f3c1-4668-95af-d1a24be853c7";
        String auditDateTime = "2018-12-06 17:02:15.123";

        when(dcpTelemetryErrorLogRepository.findByMessageIdAndAuditDateTime(Mockito.anyString(), Mockito.anyObject())).thenReturn(null);

        TelemetryErrorLog telemetryErrorLog = (TelemetryErrorLog) dcpTelemetryErrorLogService.getTelemetryErrorLogDetails(messageId, auditDateTime);
    }

}
