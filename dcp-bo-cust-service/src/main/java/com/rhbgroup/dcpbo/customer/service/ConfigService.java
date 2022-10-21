package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.vo.CustomerTrxLimitVo;

import java.util.List;

public interface ConfigService {

    List<CustomerTrxLimitVo> getCustomerTrxLimits(String customerId);

}
