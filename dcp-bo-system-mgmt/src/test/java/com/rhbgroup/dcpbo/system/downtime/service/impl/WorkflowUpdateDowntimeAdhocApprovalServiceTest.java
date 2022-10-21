package com.rhbgroup.dcpbo.system.downtime.service.impl;

import static com.rhbgroup.dcpbo.system.downtime.service.impl.WorkflowDowntimeServiceImpl.TIMESTAMP_FORMAT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.downtime.dto.DowntimeApprovalRequest;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.exception.UpdateAdhocNotAllowedException;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;

/**
 *
 * @author faizal.musa
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WorkflowUpdateDowntimeAdhocApprovalServiceTest.class, WorkflowDowntimeServiceImpl.class})
public class WorkflowUpdateDowntimeAdhocApprovalServiceTest {

    private static SystemDowntimeConfig dcpDowntimeConfig = new SystemDowntimeConfig();
    private static WorkflowDowntimeServiceImpl downtimeService = new WorkflowDowntimeServiceImpl();
    private static List<SystemDowntimeConfig> dcpDowntimeConfigs = new ArrayList<>();
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Autowired
    WorkflowDowntimeServiceImpl workflowDowntimeService;

    @MockBean
    ApprovalRepository approvalRepositoryMock;

    @MockBean
    BoSmApprovalDowntimeRepository boSmApprovalDowntimeRepositoryMock;

    @MockBean
    UserRepository userRepositoryMock;

    @MockBean
    private SystemDowntimeConfigRepository systemDowntimeConfigRepositoryMock;

    @MockBean
    AdditionalDataHolder additionalDataHolder;
    
    @MockBean
    BoDowntimeAdhocTypeRepository boDowntimeAdhocTypeRepositoryMock;
    
    private static Timestamp toTimestamp(String value) {
        Timestamp timestamp = null;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
            timestamp = new Timestamp(simpleDateFormat.parse(value).getTime());
        } catch (ParseException e) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Failed to parse Timestamp for value: " + value);
        }

        return timestamp;
    }
    
    /**
     * Simulate DCP deployment/DR downtime from 2019-03-08 00:00:00 hrs
     * to 2019-03-08 03:00:00 hrs.
     */
    @BeforeClass
    public static void setup() {
        //2019-03-10T00:30:50+08:00
        Timestamp startTime = toTimestamp("2019-03-08T00:00:00+08:00");
        Timestamp endTime = toTimestamp("2019-03-08T03:00:00+08:00");
        dcpDowntimeConfig.setId(1);
        dcpDowntimeConfig.setStartTime(startTime);
        dcpDowntimeConfig.setEndTime(endTime);
        dcpDowntimeConfig.setIsActive(String.valueOf(1));
        
        SystemDowntimeConfig cfg1 = new SystemDowntimeConfig();
        cfg1.setId(2);
        cfg1.setStartTime(toTimestamp("2019-03-07T22:00:00+08:00"));
        cfg1.setEndTime(toTimestamp("2019-03-08T00:00:00+08:00"));
        
        dcpDowntimeConfigs.add(cfg1);
        dcpDowntimeConfigs.add(dcpDowntimeConfig);
        
        //when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
        //when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
        //when(boSmApprovalDowntimeRepositoryMock.findOneByApprovalId(Mockito.anyInt())).thenReturn(boSmApprovalDowntime);
        
    }
    
    /**
     * request.start &gt= dcp.start && request.end &lt= dcp.end.
     */
    @Test
    public void downtimeRequestWithinDCPDowntime() {

        DowntimeApprovalRequest request = new DowntimeApprovalRequest();
        request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-08T00:00:00+08:00");
        request.setEndTime("2019-03-08T02:59:59+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
    
        request = new DowntimeApprovalRequest();
        request.setId(2);
        request.setStartTime("2019-03-08T01:30:00+08:00");
        request.setEndTime("2019-03-08T03:00:00+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
//        
        request = new DowntimeApprovalRequest();
        request.setId(5);
        request.setStartTime("2019-03-08T01:00:00+08:00");
        request.setEndTime("2019-03-08T02:30:00+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        request = new DowntimeApprovalRequest();
        request.setId(3);
        //2019-03-08T01:30:00+08:00
        request.setStartTime("2019-03-08T00:00:01+08:000");
        request.setEndTime("2019-03-08T02:59:59+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        //Equals with DCP downtime config
        request = new DowntimeApprovalRequest();
        request.setId(4);
        request.setStartTime("2019-03-08T00:00:00+08:00");
        request.setEndTime("2019-03-08T03:00:00+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
    }
    
    /**
     * request.start &gt= dcp.start && request start &lt dcp.end
     * request.end &gt= dcp.end.
     */
    @Test
    public void requestStartTimeWithinDCPDowntime() {

        DowntimeApprovalRequest request = new DowntimeApprovalRequest();
        request.setId(1);
        //2019-03-08
        request.setStartTime("2019-03-08T02:00:00+08:00");
        request.setEndTime("2019-03-08T03:01:00+08:00");
        downtimeService = new WorkflowDowntimeServiceImpl();
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        request = new DowntimeApprovalRequest();
        request.setId(7);
        request.setStartTime("2019-03-08T00:00:00+08:00");
        request.setEndTime("2019-03-08T03:00:01+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        request = new DowntimeApprovalRequest();
        request.setId(21);
        request.setStartTime("2019-03-08T00:00:00+08:00");
        request.setEndTime("2019-03-08T18:30:25+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        request = new DowntimeApprovalRequest();
        request.setId(11);
        request.setStartTime("2019-03-08T00:00:00+08:00");
        request.setEndTime("2019-07-08T22:15:25+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        request = new DowntimeApprovalRequest();
        request.setId(3);
        request.setStartTime("2019-03-08T03:00:00+08:00");
        request.setEndTime("2019-03-08T05:00:17+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        
        request = new DowntimeApprovalRequest();
        request.setId(3);
        request.setStartTime("2019-03-08T02:22:09+08:00");
        request.setEndTime("2019-03-08T06:05:09+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
    }

    /**
     * request.end &gt= dcp.start && request.start &lt= dcp.end
     * request.start &lt dcp.start.
     */
    @Test
    public void requestEndTimeWithinDCPDowntime()  {

        DowntimeApprovalRequest request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-07T23:59:59+08:00");
        request.setEndTime("2019-03-08T03:00:00+08:00");
        downtimeService = new WorkflowDowntimeServiceImpl();
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));

        request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-07T20:30:00+08:00");
        request.setEndTime("2019-03-08T03:00:00+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));

        request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-07T20:30:00+08:00");
        request.setEndTime("2019-03-08T01:30:00+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
//
        request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-05T08:30:00+08:00");
        request.setEndTime("2019-03-08T00:00:00+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));

        request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-07T23:30:00+08:00");
        request.setEndTime("2019-03-08T00:00:10+08:00");
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
    }

    @Test
    public void requestConfinesDCPDowntime(){
    
        DowntimeApprovalRequest request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-07T10:35:05+08:00");
        request.setEndTime("2019-03-08T06:00:00+08:00");
        downtimeService = new WorkflowDowntimeServiceImpl();
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-07T00:00:00+08:00");
        request.setEndTime("2019-03-08T00:00:00+08:00");
        downtimeService = new WorkflowDowntimeServiceImpl();
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-08T00:00:00+08:00");
        request.setEndTime("2019-03-09T00:00:30+08:00");
        downtimeService = new WorkflowDowntimeServiceImpl();
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
    }
     
    @Test
    public void requestTimeRangeLessThanDCPDowntime(){
    
        DowntimeApprovalRequest request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-07T00:00:09+08:00");
        request.setEndTime("2019-03-07T03:00:09+08:00");
        downtimeService = new WorkflowDowntimeServiceImpl();
        assertFalse(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-07T20:00:00+08:00");
        request.setEndTime("2019-03-07T23:59:60+08:00");
        downtimeService = new WorkflowDowntimeServiceImpl();
        assertTrue(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
    }
    
    @Test
    public void requestTimeRangeGreaterThanDCPDowntime(){
        
        DowntimeApprovalRequest request = new DowntimeApprovalRequest();
        request.setId(1);
        request.setStartTime("2019-03-08T03:00:01+08:00");
        request.setEndTime("2019-03-08T05:00:00+08:00");
        downtimeService = new WorkflowDowntimeServiceImpl();
        assertFalse(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
        request = new DowntimeApprovalRequest();
        request.setId(3);
        //2019-07-25T20:30:50+08:00
        request.setStartTime("2019-03-10T00:30:50+08:00+08:00");
        request.setEndTime("2019-03-10T05:30:50+08:00+08:00");
        downtimeService = new WorkflowDowntimeServiceImpl();
        assertFalse(downtimeService.isRequestDowntimeOverlap(dcpDowntimeConfig, request));
        
    }
  
    @Test
    public void downtimeConfigActive(){
    
        thrown.expect(UpdateAdhocNotAllowedException.class);
        DowntimeApprovalRequest request = new DowntimeApprovalRequest();
        request.setId(566);
        request.setStartTime("2019-03-08T03:00:00+08:00");
        request.setEndTime("2019-03-08T05:00:00+08:00");
        request.setBankId("040");
        when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTimeForUpdate(Mockito.any(Timestamp.class), Mockito.any(Timestamp.class), Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(dcpDowntimeConfigs);
        
        workflowDowntimeService.validateDowntimeApprovalRequest(request);
    }

    @Test
    public void retrieveApprovalNotFound(){
    
        thrown.expect(CommonException.class);
        when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(null);
        workflowDowntimeService.retrieveApprovalValidate(5);
    }
}
