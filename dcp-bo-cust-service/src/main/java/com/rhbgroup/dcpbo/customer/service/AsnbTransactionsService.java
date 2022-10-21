package com.rhbgroup.dcpbo.customer.service;


import com.rhbgroup.dcpbo.customer.contract.BoData;

/**
 * @author hassan.malik
 */

public interface AsnbTransactionsService {

    /*
     * This is implemented as part of DCP2-1546 user story
     */
    BoData getAsnbTransactions(Integer customerId, String fundId, String identificationNumber,
                               String identificationType, String membershipNumber, boolean isMinor, String guardianIdNumber);

}
