package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import org.springframework.web.bind.annotation.RequestHeader;
import com.rhbgroup.dcpbo.customer.exception.CommonException;

public interface PersonalLoanService {

	public BoData getPersonalLoanDetails(@RequestHeader("customerId") Integer customerId, String accountNo)
			throws CommonException;
}
