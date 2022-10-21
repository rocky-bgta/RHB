package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.CasaProfile;

@Repository
public interface CasaProfileRepository extends CrudRepository<CasaProfile, Integer> {
	public CasaProfile findById(Integer id);
}
