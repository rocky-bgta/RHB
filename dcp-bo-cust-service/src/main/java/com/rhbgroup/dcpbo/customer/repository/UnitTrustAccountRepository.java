package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;

import com.rhbgroup.dcpbo.customer.model.UnitTrustAccount;

public interface UnitTrustAccountRepository extends CrudRepository<UnitTrustAccount, Integer> {
	public UnitTrustAccount findByAccountNo(String accountNo);
}
