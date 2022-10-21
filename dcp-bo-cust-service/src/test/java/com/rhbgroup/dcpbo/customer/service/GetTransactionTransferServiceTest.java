package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.impl.GetTransactionTransferServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { GetTransactionTransferServiceImpl.class})
public class GetTransactionTransferServiceTest {

    @Autowired
    private GetTransactionTransferService getTransactionTransferService;
    @MockBean
    private AuditDetailConfigRepo AuditDetailConfigRepo;
    @MockBean
    TransferTxnRepository transferTxnRepository;
    @MockBean
    DcpAuditFundTransferRepository auditFundTransferRepository;
    @MockBean
    AuditRepository auditRepository;
    @MockBean
    DcpAuditEventConfigRepository auditEventConfigRepository;
    @MockBean
    BankRepository bankRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(GetTransactionTransferServiceTest.class);
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

    
    @Test(expected = CommonException.class)
    public void testRetrieveTransactionTransferIBGInvalidBankIdForBank(){
        TransferTxn transfertxn = new TransferTxn();
        transfertxn.setMainFunction("IBG");
        transfertxn.setToResidentStatus(true);
        transfertxn.setToBankId(888);
        transfertxn.setToIdType("IBGIdType");
        transfertxn.setToIdNo("1234");
        AuditFundTransfer auditFundTransfer = new AuditFundTransfer();
        auditFundTransfer.setAuditId(22);
        Audit audit = new Audit();
        audit.setEventCode("1101011");
        audit.setChannel("DummyChannel");
        audit.setStatusCode("DummyStatusCode");
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Intrabank Transfer");
        Bank bank = new Bank();
        bank.setBankInitials("UOB");

        when(transferTxnRepository.findByRefId("2")).thenReturn(transfertxn);
        when(auditFundTransferRepository.findAuditFundTransferByRefId("2")).thenReturn(auditFundTransfer);
        when(auditRepository.findOne(22)).thenReturn(audit);
        when(auditEventConfigRepository.findByEventCode("1101011")).thenReturn(auditEventConfig);
        when(bankRepository.getOne(8882)).thenReturn(bank);

        assertEquals( "UOB" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getBank());
        assertEquals( "true" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getResidentStatus());
        assertEquals( "IBGIdType" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdType());
        assertEquals( "1234" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdNo());
        assertEquals( "DummyChannel" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getChannel());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionTransferIBGInvalidEventCodeForAuditEventConfig(){
        TransferTxn transfertxn = new TransferTxn();
        transfertxn.setMainFunction("IBG");
        transfertxn.setToResidentStatus(true);
        transfertxn.setToBankId(888);
        transfertxn.setToIdType("IBGIdType");
        transfertxn.setToIdNo("1234");
        AuditFundTransfer auditFundTransfer = new AuditFundTransfer();
        auditFundTransfer.setAuditId(22);
        Audit audit = new Audit();
        audit.setEventCode("1101011");
        audit.setChannel("DummyChannel");
        audit.setStatusCode("DummyStatusCode");
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Intrabank Transfer");
        Bank bank = new Bank();
        bank.setBankInitials("UOB");

        when(transferTxnRepository.findByRefId("2")).thenReturn(transfertxn);
        when(auditFundTransferRepository.findAuditFundTransferByRefId("2")).thenReturn(auditFundTransfer);
        when(auditRepository.findOne(22)).thenReturn(audit);
        when(auditEventConfigRepository.findByEventCode("11010112")).thenReturn(auditEventConfig);
        when(bankRepository.getOne(888)).thenReturn(bank);

        assertEquals( "UOB" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getBank());
        assertEquals( "true" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getResidentStatus());
        assertEquals( "IBGIdType" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdType());
        assertEquals( "1234" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdNo());
        assertEquals( "DummyChannel" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getChannel());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionTransferIBGInvalidAuditIdForAudit(){
        TransferTxn transfertxn = new TransferTxn();
        transfertxn.setMainFunction("IBG");
        transfertxn.setToResidentStatus(true);
        transfertxn.setToBankId(888);
        transfertxn.setToIdType("IBGIdType");
        transfertxn.setToIdNo("1234");
        AuditFundTransfer auditFundTransfer = new AuditFundTransfer();
        auditFundTransfer.setAuditId(22);
        Audit audit = new Audit();
        audit.setEventCode("1101011");
        audit.setChannel("DummyChannel");
        audit.setStatusCode("DummyStatusCode");
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Intrabank Transfer");
        Bank bank = new Bank();
        bank.setBankInitials("UOB");

        when(transferTxnRepository.findByRefId("2")).thenReturn(transfertxn);
        when(auditFundTransferRepository.findAuditFundTransferByRefId("2")).thenReturn(auditFundTransfer);
        when(auditRepository.findOne(222)).thenReturn(audit);
        when(auditEventConfigRepository.findByEventCode("1101011")).thenReturn(auditEventConfig);
        when(bankRepository.getOne(888)).thenReturn(bank);

        assertEquals( "UOB" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getBank());
        assertEquals( "true" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getResidentStatus());
        assertEquals( "IBGIdType" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdType());
        assertEquals( "1234" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdNo());
        assertEquals( "DummyChannel" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getChannel());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionTransferIBGInvalidRefIdForAuditFundTransfer(){
        TransferTxn transfertxn = new TransferTxn();
        transfertxn.setMainFunction("IBG");
        transfertxn.setToResidentStatus(true);
        transfertxn.setToBankId(888);
        transfertxn.setToIdType("IBGIdType");
        transfertxn.setToIdNo("1234");
        AuditFundTransfer auditFundTransfer = new AuditFundTransfer();
        auditFundTransfer.setAuditId(22);
        Audit audit = new Audit();
        audit.setEventCode("1101011");
        audit.setChannel("DummyChannel");
        audit.setStatusCode("DummyStatusCode");
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Intrabank Transfer");
        Bank bank = new Bank();
        bank.setBankInitials("UOB");

        when(transferTxnRepository.findByRefId("2")).thenReturn(transfertxn);
        when(auditFundTransferRepository.findAuditFundTransferByRefId("21")).thenReturn(auditFundTransfer);
        when(auditRepository.findOne(22)).thenReturn(audit);
        when(auditEventConfigRepository.findByEventCode("1101011")).thenReturn(auditEventConfig);
        when(bankRepository.getOne(888)).thenReturn(bank);

        assertEquals( "UOB" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getBank());
        assertEquals( "true" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getResidentStatus());
        assertEquals( "IBGIdType" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdType());
        assertEquals( "1234" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdNo());
        assertEquals( "DummyChannel" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getChannel());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionTransferIBGInvalidRefIdForTransferTxn(){
        TransferTxn transfertxn = new TransferTxn();
        transfertxn.setMainFunction("IBG");
        transfertxn.setToResidentStatus(true);
        transfertxn.setToBankId(888);
        transfertxn.setToIdType("IBGIdType");
        transfertxn.setToIdNo("1234");
        AuditFundTransfer auditFundTransfer = new AuditFundTransfer();
        auditFundTransfer.setAuditId(22);
        Audit audit = new Audit();
        audit.setEventCode("1101011");
        audit.setChannel("DummyChannel");
        audit.setStatusCode("DummyStatusCode");
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Intrabank Transfer");
        Bank bank = new Bank();
        bank.setBankInitials("UOB");

        when(transferTxnRepository.findByRefId("21")).thenReturn(transfertxn);
        when(auditFundTransferRepository.findAuditFundTransferByRefId("2")).thenReturn(auditFundTransfer);
        when(auditRepository.findOne(22)).thenReturn(audit);
        when(auditEventConfigRepository.findByEventCode("1101011")).thenReturn(auditEventConfig);
        when(bankRepository.getOne(888)).thenReturn(bank);

        assertEquals( "UOB" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getBank());
        assertEquals( "true" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getResidentStatus());
        assertEquals( "IBGIdType" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdType());
        assertEquals( "1234" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdNo());
        assertEquals( "DummyChannel" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getChannel());
    }

    @Test
    public void testRetrieveTransactionTransferIBG(){
        TransferTxn transfertxn = new TransferTxn();
        transfertxn.setMainFunction("IBG");
        transfertxn.setToResidentStatus(true);
        transfertxn.setToBankId(888);
        transfertxn.setToIdType("IBGIdType");
        transfertxn.setToIdNo("1234");
        AuditFundTransfer auditFundTransfer = new AuditFundTransfer();
        auditFundTransfer.setAuditId(22);
        Audit audit = new Audit();
        audit.setEventCode("1101011");
        audit.setChannel("DummyChannel");
        audit.setStatusCode("DummyStatusCode");
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Intrabank Transfer");
        Bank bank = new Bank();
        bank.setBankInitials("UOB");

        when(transferTxnRepository.findByRefId("2")).thenReturn(transfertxn);
        when(auditFundTransferRepository.findAuditFundTransferByRefId("2")).thenReturn(auditFundTransfer);
        when(auditRepository.findOne(22)).thenReturn(audit);
        when(auditEventConfigRepository.findByEventCode("1101011")).thenReturn(auditEventConfig);
        when(bankRepository.getOne(888)).thenReturn(bank);

        assertEquals( "UOB" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getBank());
        assertEquals( "true" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getResidentStatus());
        assertEquals( "IBGIdType" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdType());
        assertEquals( "1234" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdNo());
        assertEquals( "DummyChannel" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getChannel());
    }


    @Test
    public void testRetrieveTransactionTransferDuitnow(){
        TransferTxn transfertxn = new TransferTxn();
        transfertxn.setMainFunction("DUITNOW");
        transfertxn.setDuitnowCountryCode("MY");
        transfertxn.setToIdType("DuitnowId");
        transfertxn.setToBankId(888);
        AuditFundTransfer auditFundTransfer = new AuditFundTransfer();
        auditFundTransfer.setAuditId(22);
        Audit audit = new Audit();
        audit.setEventCode("1101011");
        audit.setChannel("DummyChannel");
        audit.setStatusCode("DummyStatusCode");
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Intrabank Transfer");
        Bank bank = new Bank();
        bank.setBankInitials("UOB");


        when(transferTxnRepository.findByRefId("2")).thenReturn(transfertxn);
        when(auditFundTransferRepository.findAuditFundTransferByRefId("2")).thenReturn(auditFundTransfer);
        when(auditRepository.findOne(22)).thenReturn(audit);
        when(auditEventConfigRepository.findByEventCode("1101011")).thenReturn(auditEventConfig);
        when(bankRepository.getOne(888)).thenReturn(bank);

        assertEquals( null , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getBank());
        assertEquals( "MY" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getDuitnowCountryCode());
        assertEquals( "DuitnowId" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getIdType());
        assertEquals( "DummyChannel" , getTransactionTransferService.retrieveTransactionTransfer("2").getTransfer().getChannel());
    }
}
