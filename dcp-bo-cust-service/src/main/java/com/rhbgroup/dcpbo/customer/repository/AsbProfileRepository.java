package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.AsbProfile;

@Repository
public interface AsbProfileRepository extends CrudRepository<AsbProfile, Integer> {
	public AsbProfile findById(Integer id);
}
