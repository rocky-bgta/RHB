package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import org.springframework.stereotype.Service;

public interface DcpCustomerAuditService {

    BoData listing(String frDate, String toDate, int customerId, String auditCategoryIds, Integer pageNo);
}
