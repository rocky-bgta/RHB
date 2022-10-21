package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;

import com.rhbgroup.dcpbo.customer.model.CustomerRelationshipView;

public interface CustomerRelationshipViewRepo extends CrudRepository<CustomerRelationshipView, Integer> {
	
	CustomerRelationshipView findOneByAccountNo(String accountNo);

}
