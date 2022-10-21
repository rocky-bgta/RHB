package com.rhbgroup.dcpbo.customer.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.rhbgroup.dcpbo.customer.model.UnitTrustAccountHolding;

public interface UnitTrustAccountHoldingRepository extends CrudRepository<UnitTrustAccountHolding, Integer> {

	List<UnitTrustAccountHolding> findAllByAccountNo(String accountNo);

	@Query(value = "SELECT SUM(fundMyrMarketValue), SUM(fundMyrUnrealisedGainLoss) FROM UnitTrustAccountHolding x WHERE x.accountNo = :accountNo")
	BigDecimal[] findFundMyrMarketValueAndUnrealisedGainLossByAccountNo(String accountNo);

}
