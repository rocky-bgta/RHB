package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface CustomerProfileService {

    BoData getCustomerProfile(Integer userId);

}
