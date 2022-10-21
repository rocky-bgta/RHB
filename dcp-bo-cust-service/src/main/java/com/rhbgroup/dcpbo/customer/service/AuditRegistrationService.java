package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface AuditRegistrationService {

	BoData getAuditRegistrationDetails(String token);
	
	BoData listing(String cisNo, Integer pageNo,String frDate, String toDate);
}
