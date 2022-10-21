package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

/**
 * Ideally all any deposit call to get its detail
 *
 * @author Faisal
 */
public interface ViewDepositService {

    BoData detail(int customerId, String accountNo);

}
