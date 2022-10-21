package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcpbo.customer.model.OlaToken;
import com.rhbgroup.dcpbo.customer.model.OlaTokenGroup;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.OlaCasaService;
import com.rhbgroup.dcpbo.customer.service.impl.AuditEventsService;
import com.rhbgroup.dcpbo.customer.service.impl.OlaCasaServiceImpl;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
        OlaCasaControllerTests.class,
        OlaCasaController.class,
        OlaCasaService.class,
        OlaCasaServiceImpl.class,
        AuditEventsService.class})
@EnableWebMvc
public class OlaCasaControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserProfileRepository userProfileRepository;

    @MockBean
    OlaTokenGroupRepository olaTokenGroupRepository;

    @MockBean
    OlaTokenRepository olaTokenRepositoryMock;

    @MockBean
    BoConfigGenericRepository boConfigGenericRepositoryMock;

    @MockBean
    DcpAuditEventConfigRepository dcpAuditEventConfigRepositoryMock;

    @MockBean
    AuditSummaryConfigRepository auditSummaryConfigRepositoryMock;

    @Autowired
    AuditEventsService auditEventsService;

    @MockBean
    ProfileRepository profileRepository;
    @MockBean
    private DcpAuditFundTransferRepository dcpAuditFundTransferRepository;
    @MockBean
    private DcpAuditProfileRepository dcpAuditProfileRepository;
    @MockBean
    private DcpAuditBillPaymentRepository dcpAuditBillPaymentRepository;
    @MockBean
    private DcpAuditTopupRepository dcpAuditTopupRepository;
    @MockBean
    private DcpAuditMiscRepository dcpAuditMiscRepository;
    @MockBean
    AuditDetailConfigRepo auditDetailConfigRepoMock;

    @Before
    public void setup() {

        List<OlaTokenGroup> olaTokenGroups = new ArrayList<>();
        OlaTokenGroup olaTokenGroup = new OlaTokenGroup("Karim Musa", "IC", "880501455883", "batak123");
        olaTokenGroups.add(olaTokenGroup);
        olaTokenGroup = new OlaTokenGroup("Fahimi Lusa", "IC", "990501005883", "Kira123");
        olaTokenGroups.add(olaTokenGroup);
        olaTokenGroup = new OlaTokenGroup("Wai Chin", "IC", "110504655883", "Key123");
        olaTokenGroups.add(olaTokenGroup);

        Page<OlaTokenGroup> olaTokenGroupPage = new PageImpl(olaTokenGroups);
        when(olaTokenGroupRepository.fetchGroupOlaToken(anyString(), any(Pageable.class))).thenReturn(olaTokenGroupPage);

        OlaToken olaToken = new OlaToken();
        olaToken.setId(1000);
        olaToken.setUsername("batak123");
        olaToken.setToken("5kdfj3432JWEFWFrrr33335");
        olaToken.setRefId("5kdfj3432JWEFWFrrr33335");
        olaToken.setDeviceId("0005");
        olaToken.setDeviceId("0005");
        olaToken.setChannel("MBK");
        olaToken.setName("Karim Musa");
        olaToken.setEmail("test@gmail.com");
        olaToken.setMobileNo("0192355597");
        olaToken.setIdType("IC");
        olaToken.setIdNo("880501455883");
        olaToken.setStatus("N");
        olaToken.setCreatedTime(LocalDateTime.now().toString());
        olaToken.setUpdatedTime(LocalDateTime.now().toString());

        OlaToken olaToken2 = new OlaToken();
        olaToken2.setId(1000);
        olaToken2.setUsername("batak123");
        olaToken2.setToken("ewrwerERerrq233w32rewWEee2");
        olaToken2.setRefId("ewrwerERerrq233w32rewWEee2");
        olaToken2.setDeviceId("0005");
        olaToken2.setDeviceId("0005");
        olaToken2.setChannel("MBK");
        olaToken2.setName("Karim Musa");
        olaToken2.setEmail("test@gmail.com");
        olaToken2.setMobileNo("0192355597");
        olaToken2.setIdType("IC");
        olaToken2.setIdNo("880501455883");
        olaToken2.setStatus("P");
        olaToken2.setAmlScreeningResult("Positive");
        olaToken2.setCreatedTime("2021-03-25 16:30:43.443");
        olaToken2.setUpdatedTime("2021-03-25 16:30:43.443");

        when(olaTokenRepositoryMock.findFirstByIdNoOrderByUpdatedTimeDesc(eq("880501455883"))).thenReturn(olaToken);
        when(userProfileRepository.findByUsername(eq("batak123"))).thenReturn(mock(UserProfile.class));

        List<OlaToken> olaTokens = new ArrayList<>();
        olaTokens.add(olaToken);
        olaTokens.add(olaToken2);

        Page<OlaToken> olaTokenGroupPage1 = new PageImpl(olaTokens);
        when(olaTokenRepositoryMock.findByIdNoAndCreatedTimeAfterAndCreatedTimeBeforeOrderByCreatedTimeDesc(
                anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(olaTokenGroupPage1);
    }

    @Test
    public void searchOlaCasaTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/olacasa/search?value=xyz2&pageNo=1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].username", Matchers.is("batak123")))
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].name", Matchers.is("Karim Musa")))
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].email", Matchers.is("test@gmail.com")))
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].mobileNo", Matchers.is("0192355597")))
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].aaoip", Matchers.is("5kdfj3432JWEFWFrrr33335")))
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].idNo", Matchers.is("880501455883")));
    }

    @Test
    public void listOlaCasaTest() throws Exception {
        String apiString = "/bo/cs/customer/olacasa/audit/list?idNo=4986003678886&fromDate=2021-04-01T00:00:00 08:00&toDate=2021-04-01T23:59:59 08:00&pageNo=1";
        mockMvc.perform(MockMvcRequestBuilders.get(apiString))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("eventCategory[0].eventCategoryId", Matchers.is("10")))
                .andExpect(MockMvcResultMatchers.jsonPath("eventCategory[0].eventCategoryName", Matchers.is("Others")))
                .andExpect(MockMvcResultMatchers.jsonPath("event[1].eventId", Matchers.is("0")))
                .andExpect(MockMvcResultMatchers.jsonPath("event[1].eventCode", Matchers.is("10009")))
                .andExpect(MockMvcResultMatchers.jsonPath("event[1].eventCategoryId", Matchers.is("10")))
                .andExpect(MockMvcResultMatchers.jsonPath("event[1].channel", Matchers.is("MBK")))
                .andExpect(MockMvcResultMatchers.jsonPath("event[1].description", Matchers.is("")))
                .andExpect(MockMvcResultMatchers.jsonPath("event[1].refId", Matchers.is("ewrwerERerrq233w32rewWEee2")));
    }
}
