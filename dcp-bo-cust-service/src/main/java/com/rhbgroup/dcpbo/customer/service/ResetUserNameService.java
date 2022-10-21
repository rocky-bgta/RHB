package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface ResetUserNameService {

	public BoData resetUserName(String id, String code);
}
