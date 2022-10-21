package com.rhbgroup.dcpbo.customer.controller;


import com.rhbgroup.dcpbo.customer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

public class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected TransferTxnRepository transferTxnRepository;

//    @MockBean
//    protected AuditFundTransferRepository auditFundTransferRepository;

    @MockBean
    protected AuditRepository auditRepository;

//    @MockBean
//    protected AuditEventConfigRepository auditEventConfigRepository;

    @MockBean
    protected BankRepository bankRepository;
}
