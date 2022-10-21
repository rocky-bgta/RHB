package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.MortgageProfile;

@Repository
public interface MortgageProfileRepository extends CrudRepository<MortgageProfile, Integer> {
	public MortgageProfile findById(Integer id);
}
