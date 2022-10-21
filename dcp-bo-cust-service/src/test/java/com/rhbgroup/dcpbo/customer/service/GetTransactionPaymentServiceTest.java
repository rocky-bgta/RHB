package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.impl.GetTransactionPaymentServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@SpringBootTest(classes = { GetTransactionPaymentServiceImpl.class, GetTransactionPaymentServiceTest.Config.class })
public class GetTransactionPaymentServiceTest {

    @Autowired
    private GetTransactionPaymentServiceImpl getTransactionPaymentService;
    @MockBean
    PaymentTxnRepository paymentTxnRepository;
    @MockBean
    DcpAuditBillPaymentRepository auditBillPaymentRepository;
    @MockBean
    AuditRepository auditRepository;
    @MockBean
    BillerRepository billerRepository;

    @TestConfiguration
   	static class Config {

   		@Bean
   		@Primary
   		public GetTransactionPaymentServiceImpl getGetTransactionPaymentServiceImpl() {
   			return new GetTransactionPaymentServiceImpl();
   		}
   		
   		@Bean
   		public AdditionalDataHolder getAdditionalDataHolder()
   		{
   			return new AdditionalDataHolder();
   		}
   	}
    
    @Test
    public void testRetrieveTransactionPaymentJomPayByRefId() {
        String refId = "79731234";
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setTxnStatus("SUCCESS");
        paymentTxn.setToBillerAccountName("Telekom");
        paymentTxn.setMainFunction("JOM_PAY");
        paymentTxn.setToBillerId(1234);
        AuditBillPayment auditBillPayment = new AuditBillPayment();
        auditBillPayment.setAuditId(88);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");

        when(paymentTxnRepository.findByRefId(refId)).thenReturn(paymentTxn);
        when(auditBillPaymentRepository.findAuditIdByRefId("79731234")).thenReturn(88);
        when(auditRepository.findOne(88)).thenReturn(audit);

        assertEquals( "DMB" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getChannel());
        assertEquals( "SUCCESS" +
                "" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getStatusDescription());
        assertEquals( "Telekom" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getBillerName());
        assertEquals( "JOM_PAY" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getMainFunction());
    }

    @Test
    public void testRetrieveTransactionPaymentOtherBillerByRefId() {
        String refId = "79731234";
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setTxnStatus("SUCCESS");
        paymentTxn.setToBillerAccountName("Telekom");
        paymentTxn.setMainFunction("OTHER_BILLER");
        paymentTxn.setToBillerId(1234);
        AuditBillPayment auditBillPayment = new AuditBillPayment();
        auditBillPayment.setAuditId(88);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");
        Biller biller = new Biller();
        biller.setBillerName("Astro");

        when(paymentTxnRepository.findByRefId(refId)).thenReturn(paymentTxn);
        when(auditBillPaymentRepository.findAuditIdByRefId("79731234")).thenReturn(88);
        when(auditRepository.findOne(88)).thenReturn(audit);
        when(billerRepository.findOne(1234)).thenReturn(biller);

        assertEquals( "DMB" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getChannel());
        assertEquals( "SUCCESS" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getStatusDescription());
        assertEquals( "Astro" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getBillerName());
        assertEquals( "OTHER_BILLER" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getMainFunction());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionPaymentOtherBillerByRefIdInvalidRefIdForPaymentTxn() {
        String refId = "79731234";
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setTxnStatus("SUCCESS");
        paymentTxn.setToBillerAccountName("Telekom");
        paymentTxn.setMainFunction("OTHER_BILLER");
        paymentTxn.setToBillerId(1234);
        AuditBillPayment auditBillPayment = new AuditBillPayment();
        auditBillPayment.setAuditId(88);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");
        Biller biller = new Biller();
        biller.setBillerName("Astro");

        when(paymentTxnRepository.findByRefId("888")).thenReturn(paymentTxn);
        when(auditBillPaymentRepository.findAuditIdByRefId("79731234")).thenReturn(88);
        when(auditRepository.findOne(88)).thenReturn(audit);
        when(billerRepository.findOne(1234)).thenReturn(biller);

        assertEquals( "DMB" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getChannel());
        assertEquals( "SUCCESS" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getStatusDescription());
        assertEquals( "Astro" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getBillerName());
        assertEquals( "OTHER_BILLER" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getMainFunction());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionPaymentOtherBillerByRefIdInvalidRefIdForAuditBillPayment() {
        String refId = "79731234";
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setTxnStatus("SUCCESS");
        paymentTxn.setToBillerAccountName("Telekom");
        paymentTxn.setMainFunction("OTHER_BILLER");
        paymentTxn.setToBillerId(1234);
        AuditBillPayment auditBillPayment = new AuditBillPayment();
        auditBillPayment.setAuditId(88);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");
        Biller biller = new Biller();
        biller.setBillerName("Astro");

        when(paymentTxnRepository.findByRefId(refId)).thenReturn(paymentTxn);
        when(auditBillPaymentRepository.findAuditIdByRefId("8888")).thenReturn(8888);
        when(auditRepository.findOne(88)).thenReturn(audit);
        when(billerRepository.findOne(1234)).thenReturn(biller);

        assertEquals( "DMB" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getChannel());
        assertEquals( "SUCCESS" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getStatusDescription());
        assertEquals( "Astro" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getBillerName());
        assertEquals( "OTHER_BILLER" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getMainFunction());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionPaymentOtherBillerByRefIdInvalidAuditIdForAudit() {
        String refId = "79731234";
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setTxnStatus("SUCCESS");
        paymentTxn.setToBillerAccountName("Telekom");
        paymentTxn.setMainFunction("OTHER_BILLER");
        paymentTxn.setToBillerId(1234);
        AuditBillPayment auditBillPayment = new AuditBillPayment();
        auditBillPayment.setAuditId(88);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");
        Biller biller = new Biller();
        biller.setBillerName("Astro");

        when(paymentTxnRepository.findByRefId(refId)).thenReturn(paymentTxn);
        when(auditBillPaymentRepository.findAuditIdByRefId("79731234")).thenReturn(88);
        when(auditRepository.findOne(888)).thenReturn(audit);
        when(billerRepository.findOne(1234)).thenReturn(biller);

        assertEquals( "DMB" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getChannel());
        assertEquals( "SUCCESS" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getStatusDescription());
        assertEquals( "Astro" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getBillerName());
        assertEquals( "OTHER_BILLER" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getMainFunction());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionPaymentOtherBillerByRefIdInvalidBillerIdForBiller() {
        String refId = "79731234";
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setTxnStatus("SUCCESS");
        paymentTxn.setToBillerAccountName("Telekom");
        paymentTxn.setMainFunction("OTHER_BILLER");
        paymentTxn.setToBillerId(1234);
        AuditBillPayment auditBillPayment = new AuditBillPayment();
        auditBillPayment.setAuditId(88);
        Audit audit = new Audit();
        audit.setChannel("DMB");
        audit.setStatusCode("10000");
        audit.setStatusDescription("SUCCESS");
        Biller biller = new Biller();
        biller.setBillerName("Astro");

        when(paymentTxnRepository.findByRefId(refId)).thenReturn(paymentTxn);
        when(auditBillPaymentRepository.findAuditIdByRefId("79731234")).thenReturn(88);
        when(auditRepository.findOne(88)).thenReturn(audit);
        when(billerRepository.findOne(888)).thenReturn(biller);

        assertEquals( "DMB" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getChannel());
        assertEquals( "SUCCESS" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getStatusDescription());
        assertEquals( "Astro" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getBillerName());
        assertEquals( "OTHER_BILLER" , getTransactionPaymentService.retrieveTransactionPayment("79731234").getPayment().getMainFunction());
    }
}
