package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;

import com.rhbgroup.dcpbo.customer.model.UnitTrustFundMaster;

public interface UnitTrustFundMasterRepository extends CrudRepository<UnitTrustFundMaster, Integer> {

	public UnitTrustFundMaster findByFundId(String fundId);
}
