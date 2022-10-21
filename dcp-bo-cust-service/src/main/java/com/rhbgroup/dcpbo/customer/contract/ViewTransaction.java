package com.rhbgroup.dcpbo.customer.contract;

/**
 * Asb transactions history
 * @author Faisal
 */
public interface ViewTransaction {

    public BoData listing(int customerId, String accountNo, Integer pageCounter, String firstKey, String lastKey);
}
