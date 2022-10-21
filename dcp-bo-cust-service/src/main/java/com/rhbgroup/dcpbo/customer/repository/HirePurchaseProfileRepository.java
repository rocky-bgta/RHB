package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.HirePurchaseProfile;

@Repository
public interface HirePurchaseProfileRepository extends CrudRepository<HirePurchaseProfile, Integer> {
	public HirePurchaseProfile findById(Integer id);
}
