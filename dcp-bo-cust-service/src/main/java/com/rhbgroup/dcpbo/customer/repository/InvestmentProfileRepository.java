package com.rhbgroup.dcpbo.customer.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.InvestmentProfile;

@Repository
public interface InvestmentProfileRepository extends CrudRepository<InvestmentProfile, Integer> {

	public InvestmentProfile findByAccountNo(String accountNo);
	List<InvestmentProfile> findAllByUserIdOrderByAccountNo(Integer userId);

}
