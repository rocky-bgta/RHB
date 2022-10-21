package com.rhbgroup.dcpbo.system.downtime.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocData;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BankRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ConfigFunctionRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.impl.DowntimeAdhocServiceImpl;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DowntimeAdhocService.class, GetDowntimeAdhocServiceTest.Config.class })
public class GetDowntimeAdhocServiceTest {
	
	private static final String ADHOC_TYPE = "ADHOC";
	
	private static final String IS_ACTIVE_1 = "1";
	
	private static final String IS_ACTIVE_0 = "0";

	@Autowired
	DowntimeAdhocService downtimeAdhocService;
	
	@MockBean
	private SystemDowntimeConfigRepository systemDowntimeConfigRepositoryMock;
	
	@MockBean
	private ConfigFunctionRepository configFunctionRepositoryMock;
	
	@MockBean
	private ApprovalRepository approvalRepositoryMock;
	
	@MockBean
	private UserRepository userRepositoryMock;
	
	@MockBean
	private BoDowntimeAdhocTypeRepository adhocTypeRepositoryMock;
	
	@MockBean
	private BoSmApprovalDowntimeRepository boSmApprovalDowntimeRepositoryMock;
	
	@MockBean
    AdditionalDataHolder additionalDataHolder;
	
	@MockBean
	private BankRepository bankRepositoryMock;
	
	private java.sql.Date pushDate;
	
	private Timestamp now;
	
	private SystemDowntimeConfig systemDowntimeConfig;
	
	private String userName;
	private String bankName;

	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public DowntimeAdhocService getDowntimeAdhocService() {
			return new DowntimeAdhocServiceImpl();
		}
	}
	
	@Before
    public void setup(){

		Date date= new Date();
		long time = date.getTime();
		now = new Timestamp(System.currentTimeMillis());
		java.sql.Date sqlDate = new java.sql.Date(time);
		pushDate = sqlDate;
		userName = "James";
		bankName = "RHB";
		
		//setting start time and end time to future date
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now.getTime());
		cal.add(Calendar.DATE, 10);
		cal.add(Calendar.HOUR, 2);
		Timestamp endTime = new Timestamp(cal.getTime().getTime());
		cal.add(Calendar.HOUR, -4);
		Timestamp startTime = new Timestamp(cal.getTime().getTime());
		
		systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setId(1);
		systemDowntimeConfig.setName("Downtime adhoc 1");
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setEndTime(endTime);
		systemDowntimeConfig.setPushNotification(true);
		systemDowntimeConfig.setPushDate(pushDate);
		systemDowntimeConfig.setType(ADHOC_TYPE);
		systemDowntimeConfig.setAdhocType("ADHOC");
		systemDowntimeConfig.setIsActive(IS_ACTIVE_1);
		systemDowntimeConfig.setCreatedBy(userName);
		systemDowntimeConfig.setCreatedTime(now);
		systemDowntimeConfig.setUpdatedBy(userName);
		systemDowntimeConfig.setUpdatedTime(now);
		systemDowntimeConfig.setBankId(040);

    }
	
	@Test
	public void testGetDowntimeAdhocSuccessTest() {
		List<SystemDowntimeConfig> expected = new ArrayList<SystemDowntimeConfig>();
		expected.add(systemDowntimeConfig);
        Page foundPage = new PageImpl<SystemDowntimeConfig>(expected);

        when(systemDowntimeConfigRepositoryMock.findAll((Specification<SystemDowntimeConfig>) Mockito.any(Predicate.class), Mockito.any(Pageable.class))).thenReturn(foundPage);
        when(bankRepositoryMock.getBankNameById(Mockito.anyObject())).thenReturn(bankName);
		
		BoData response = downtimeAdhocService.getDowntimeAdhocs(1, "2019-10-01T00:00:00+08:00", "2019-11-06T00:00:00+08:00", "System", "Active,Inactive");
		AdhocData adhocData = (AdhocData) response;
		assertEquals(1, adhocData.getAdhoc().size());
		assertEquals((int)systemDowntimeConfig.getId(), adhocData.getAdhoc().get(0).getId());
		assertEquals(systemDowntimeConfig.getName(), adhocData.getAdhoc().get(0).getName());
		assertEquals(systemDowntimeConfig.getStartTimeString(), adhocData.getAdhoc().get(0).getStartTime());
		assertEquals(systemDowntimeConfig.getEndTimeString(), adhocData.getAdhoc().get(0).getEndTime());
		assertEquals(systemDowntimeConfig.isPushNotification(), adhocData.getAdhoc().get(0).getIsPushNotification());
		assertEquals(systemDowntimeConfig.getPushDateString(), adhocData.getAdhoc().get(0).getPushDate());
		assertEquals(systemDowntimeConfig.getAdhocType(), "ADHOC");
		assertEquals("Inactive", adhocData.getAdhoc().get(0).getStatus());
		assertEquals(1, adhocData.getPagination().getActivityCount());
		assertEquals(1, adhocData.getPagination().getTotalPageNum());
		assertEquals(1, adhocData.getPagination().getPageNum());
		assertEquals("L", adhocData.getPagination().getPageIndicator());

	}
	
	@Test
	public void testGetDowntimeAdhocNoRecordSuccessTest() {
		List<SystemDowntimeConfig> expected = new ArrayList<SystemDowntimeConfig>();
        Page foundPage = new PageImpl<SystemDowntimeConfig>(expected);

        when(systemDowntimeConfigRepositoryMock.findAll((Specification<SystemDowntimeConfig>) Mockito.any(Predicate.class), Mockito.any(Pageable.class))).thenReturn(foundPage);
		
		BoData response = downtimeAdhocService.getDowntimeAdhocs(1, "2019-10-01T00:00:00+08:00", "2019-11-06T00:00:00+08:00", "System", "Active,Inactive");
		AdhocData adhocData = (AdhocData) response;
		assertEquals(0, adhocData.getAdhoc().size());
		assertEquals(0, adhocData.getPagination().getActivityCount());
		assertEquals(1, adhocData.getPagination().getTotalPageNum());
		assertEquals(1, adhocData.getPagination().getPageNum());
		assertEquals("L", adhocData.getPagination().getPageIndicator());

	}
	
}
