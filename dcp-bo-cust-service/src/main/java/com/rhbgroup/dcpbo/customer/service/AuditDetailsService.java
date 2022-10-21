package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface AuditDetailsService {
    BoData getAuditDetailsActivity(Integer auditId, String eventCode);
}