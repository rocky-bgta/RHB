package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;


public interface InvestigationAuditListService {

    BoData listing(String eventCodes, Integer pageNum ,String fromDate, String toDate, String status);
}
