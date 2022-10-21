package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.impl.GetTransactionTopupServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { GetTransactionTopupService.class, GetTransactionTopupServiceTest.Config.class })
public class GetTransactionTopupServiceTest {
    @Autowired
    private GetTransactionTopupService getTransactionTopupService;
    @MockBean
    private TopupTxnRepository topupTxnRepository;
    @MockBean
    private DcpAuditTopupRepository auditTopupRepository;
    @MockBean
    private AuditRepository auditRepository;
    @MockBean
    private TopupBillerRepository topupBillerRepository;
    @MockBean
    private AuditDetailConfigRepo auditDetailConfigRepo;

    private static final Logger LOGGER = LoggerFactory.getLogger(GetTransactionTopupServiceTest.class);
    private static final String CPATH ="$.header.channel";
    private static final String SCPATH ="$.header.status_code";
    private static final String SDPATH ="$.header.status_description";
    private static final String PAYLOAD ="{\n" +
            " \"header\": {\n" +
            " \"channel\" : \"channel dummy\",\n" +
            " \"status_code\" : \"DummyStatusCode\",\n" +
            " \"status_description\" : \"status description\"\n" +
            " }, \n" +
            " \"request\": {\n" +
            " \"ref_id\":\"2\"\n" +
            " },\n" +
            " \"terms and condition acceptance\": \"Y\",\n" +
            " \"device name\": \"John's iphone\"\n" +
            "}";
   /* @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }
*/
    @TestConfiguration
   	static class Config {

   		@Bean
   		@Primary
   		public GetTransactionTopupService getGetTransactionTopupService() {
   			return new GetTransactionTopupServiceImpl();
   		}
   		
   	}
    
   @Test(expected = CommonException.class)
   public void testRetrieveTransactionTopUpInvalidRefIdForTopupTxn() {
       String refId = "79731234";
       TopupTxn topupTxn = new TopupTxn();
       topupTxn.setTxnStatus("SUCCESS");
       topupTxn.setMainFunction("OTHER_BILLER");
       topupTxn.setToBillerId(1234);
       TopupBiller topupBiller = new TopupBiller();
       topupBiller.setBillerName("Astro");
       AuditTopup auditTopup = new AuditTopup();
       auditTopup.setAuditId(2);
       Audit audit = new Audit();
       audit.setChannel("DMB");
       audit.setStatusCode("10000");
       audit.setStatusDescription("SUCCESS");


       when(topupTxnRepository.findByRefId("797312342")).thenReturn(topupTxn);
       when(topupBillerRepository.findOne(1234)).thenReturn(topupBiller);
       when(auditRepository.findOne(2)).thenReturn(audit);
       when(auditTopupRepository.findByRefId("79731234")).thenReturn(auditTopup);

       assertEquals( "DMB" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getChannel());
       assertEquals( "SUCCESS" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getStatusDescription());
       assertEquals( "Astro" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getBillerName());
       assertEquals( "OTHER_BILLER" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getMainFunction());
   }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionTopUpInvalidBillerIdFortopupBiller() {
        String refId = "79731234";
        TopupTxn topupTxn = new TopupTxn();
        topupTxn.setTxnStatus("SUCCESS");
        topupTxn.setMainFunction("OTHER_BILLER");
        topupTxn.setToBillerId(1234);
        TopupBiller topupBiller = new TopupBiller();
        topupBiller.setBillerName("Astro");
        AuditTopup auditTopup = new AuditTopup();
        auditTopup.setAuditId(2);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");


        when(topupTxnRepository.findByRefId("79731234")).thenReturn(topupTxn);
        when(topupBillerRepository.findOne(12342)).thenReturn(topupBiller);
        when(auditRepository.findOne(2)).thenReturn(audit);
        when(auditTopupRepository.findByRefId("79731234")).thenReturn(auditTopup);

        assertEquals( "DMB" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getChannel());
        assertEquals( "SUCCESS" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getStatusDescription());
        assertEquals( "Astro" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getBillerName());
        assertEquals( "OTHER_BILLER" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getMainFunction());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionTopUpOtherBillerByRefIdInvalidAuditIdforAudit() {
        String refId = "79731234";
        TopupTxn topupTxn = new TopupTxn();
        topupTxn.setTxnStatus("SUCCESS");
        topupTxn.setMainFunction("OTHER_BILLER");
        topupTxn.setToBillerId(1234);
        TopupBiller topupBiller = new TopupBiller();
        topupBiller.setBillerName("Astro");
        AuditTopup auditTopup = new AuditTopup();
        auditTopup.setAuditId(2);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");


        when(topupTxnRepository.findByRefId("79731234")).thenReturn(topupTxn);
        when(topupBillerRepository.findOne(1234)).thenReturn(topupBiller);
        when(auditRepository.findOne(22)).thenReturn(audit);
        when(auditTopupRepository.findByRefId("79731234")).thenReturn(auditTopup);

        assertEquals( "DMB" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getChannel());
        assertEquals( "SUCCESS" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getStatusDescription());
        assertEquals( "Astro" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getBillerName());
        assertEquals( "OTHER_BILLER" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getMainFunction());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionTopUpInvalidRefIdforAuditTopup() {
        String refId = "79731234";
        TopupTxn topupTxn = new TopupTxn();
        topupTxn.setTxnStatus("SUCCESS");
        topupTxn.setMainFunction("OTHER_BILLER");
        topupTxn.setToBillerId(1234);
        TopupBiller topupBiller = new TopupBiller();
        topupBiller.setBillerName("Astro");
        AuditTopup auditTopup = new AuditTopup();
        auditTopup.setAuditId(2);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");


        when(topupTxnRepository.findByRefId("79731234")).thenReturn(topupTxn);
        when(topupBillerRepository.findOne(1234)).thenReturn(topupBiller);
        when(auditRepository.findOne(2)).thenReturn(audit);
        when(auditTopupRepository.findByRefId("797312342")).thenReturn(auditTopup);

        assertEquals( "DMB" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getChannel());
        assertEquals( "SUCCESS" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getStatusDescription());
        assertEquals( "Astro" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getBillerName());
        assertEquals( "OTHER_BILLER" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getMainFunction());
    }


    @Test
    public void testRetrieveTransactionTopUpOtherBillerByRefId() {
        String refId = "79731234";
        TopupTxn topupTxn = new TopupTxn();
        topupTxn.setTxnStatus("SUCCESS");
        topupTxn.setMainFunction("OTHER_BILLER");
        topupTxn.setToBillerId(1234);
        TopupBiller topupBiller = new TopupBiller();
        topupBiller.setBillerName("Astro");
        AuditTopup auditTopup = new AuditTopup();
        auditTopup.setAuditId(2);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");


        when(topupTxnRepository.findByRefId("79731234")).thenReturn(topupTxn);
        when(topupBillerRepository.findOne(1234)).thenReturn(topupBiller);
        when(auditRepository.findOne(2)).thenReturn(audit);
        when(auditTopupRepository.findByRefId("79731234")).thenReturn(auditTopup);

        assertEquals( "DMB" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getChannel());
        assertEquals( "SUCCESS" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getStatusDescription());
        assertEquals( "Astro" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getBillerName());
        assertEquals( "OTHER_BILLER" , getTransactionTopupService.retrieveTransactionTopup("79731234").getTopup().getMainFunction());
    }
}
