package com.rhbgroup.dcpbo.system.downtime.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocCategory;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocType;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BankRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ConfigFunctionRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.impl.DowntimeAdhocServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DowntimeAdhocService.class, GetDowntimeAdhocServiceListTest.Config.class })
public class GetDowntimeAdhocServiceListTest {

	@Autowired
	DowntimeAdhocService downtimeAdhocService;

	@MockBean
	private BankRepository bankRepositoryMock;

	@MockBean
	private BoDowntimeAdhocTypeRepository boDowntimeAdhocTypeRepositoryMock;
	
	@MockBean
	private ConfigFunctionRepository configFunctionRepositoryMock;
	
	@MockBean
	private SystemDowntimeConfigRepository systemDowntimeConfigRepositoryMock;

	@MockBean
	private ApprovalRepository approvalRepositoryMock;

	@MockBean
	private UserRepository userRepositoryMock;

	@MockBean
	private BoSmApprovalDowntimeRepository boSmApprovalDowntimeRepositoryMock;

	@MockBean
	AdditionalDataHolder additionalDataHolderMock;
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public DowntimeAdhocService getDowntimeAdhocService() {
			return new DowntimeAdhocServiceImpl();
		}
	}

	@Test
	public void testGetAdhocCategoryListSuccessTest() {
		List<String> adhocCategoryList = new ArrayList();

		adhocCategoryList.add("System");
		adhocCategoryList.add("Internal");
		adhocCategoryList.add("External");

        when(boDowntimeAdhocTypeRepositoryMock.getAllAdhocCategoryTypes()).thenReturn(adhocCategoryList);
		
        ResponseEntity<?> response = downtimeAdhocService.getAdhocCategoryList();
        AdhocCategory adhocCategory = (AdhocCategory) response.getBody();
		assertEquals(3, adhocCategory.getAdhocCategory().size());
		assertEquals("System",adhocCategory.getAdhocCategory().get(0));
		assertEquals("Internal",adhocCategory.getAdhocCategory().get(1));
		assertEquals("External",adhocCategory.getAdhocCategory().get(2));
		
	}
	
	@Test
	public void testGetAdhocTypeListSuccessTest() {
		List<String> adhocTypeList = new ArrayList();

		adhocTypeList.add("IBG");
		adhocTypeList.add("Instant_Transfer");
		
		List<String> adhocTypeNameList = new ArrayList();

		adhocTypeNameList.add("IBG");
		adhocTypeNameList.add("Instant Transfer");
        when(boDowntimeAdhocTypeRepositoryMock.getAdhocTypeByCategory(Mockito.anyString())).thenReturn(adhocTypeList);
        when(boDowntimeAdhocTypeRepositoryMock.getAdhocTypeNameByCategory(Mockito.anyString())).thenReturn(adhocTypeNameList);
		
        BoData response = (BoData) downtimeAdhocService.getAdhocTypesList(Mockito.anyString());
        AdhocType adhocType = (AdhocType) response;
		assertEquals(2, adhocType.getAdhocType().size());
		assertEquals("IBG",adhocType.getAdhocType().get(0));
		assertEquals("Instant_Transfer",adhocType.getAdhocType().get(1));
		
		assertEquals(2, adhocType.getAdhocTypeNames().size());
		assertEquals("IBG",adhocType.getAdhocTypeNames().get(0));
		assertEquals("Instant Transfer",adhocType.getAdhocTypeNames().get(1));
		
	}
	
	@Test
	public void testGetAdhocTypeListSuccessWithCategoryAll() {
		List<String> adhocTypeList = new ArrayList();

		adhocTypeList.add("IBG");
		adhocTypeList.add("Instant_Transfer");
		adhocTypeList.add("DUITNOW");
		adhocTypeList.add("WESTERN_UNION");
		
		List<String> adhocTypeNameList = new ArrayList();

		adhocTypeNameList.add("IBG");
		adhocTypeNameList.add("Instant Transfer");
		adhocTypeNameList.add("DuitNow");
		adhocTypeNameList.add("Western Union");
		
        when(boDowntimeAdhocTypeRepositoryMock.getAllAdhocTypes()).thenReturn(adhocTypeList);
        when(boDowntimeAdhocTypeRepositoryMock.getAllAdhocTypeNames()).thenReturn(adhocTypeNameList);
		
        BoData response = (BoData) downtimeAdhocService.getAdhocTypesList("ALL");
        AdhocType adhocType = (AdhocType) response;
		assertEquals(4, adhocType.getAdhocType().size());
		assertEquals("IBG",adhocType.getAdhocType().get(0));
		assertEquals("Instant_Transfer",adhocType.getAdhocType().get(1));
		assertEquals("DUITNOW",adhocType.getAdhocType().get(2));
		assertEquals("WESTERN_UNION",adhocType.getAdhocType().get(3));
		
		assertEquals(4, adhocType.getAdhocTypeNames().size());
		assertEquals("IBG",adhocType.getAdhocTypeNames().get(0));
		assertEquals("Instant Transfer",adhocType.getAdhocTypeNames().get(1));
		assertEquals("DuitNow",adhocType.getAdhocTypeNames().get(2));
		assertEquals("Western Union",adhocType.getAdhocTypeNames().get(3));
		
	}
	
	@Test
	public void testGetAdhocTypeListSuccessWithCategorySystem() {
		List<String> adhocTypeList = new ArrayList();

		List<String> adhocTypeNameList = new ArrayList();
		
        BoData response = (BoData) downtimeAdhocService.getAdhocTypesList("System");
        AdhocType adhocType = (AdhocType) response;
		assertEquals(0, adhocType.getAdhocType().size());
		
		assertEquals(0, adhocType.getAdhocTypeNames().size());
		
	}
	
}
