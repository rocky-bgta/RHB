package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.vo.GetMcaTermLogicRequestVo;

public interface McaTermService {

	public BoData getMcaTermDetails(Integer customerId, String accountNo, String referenceNo, String currencyCode);

	public BoData getMcaTerm(Integer customerId, GetMcaTermLogicRequestVo request);

}
