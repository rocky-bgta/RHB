package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.UserPerTxnDailyLimit;

@Repository
public interface UserPerTransactionRepo extends JpaRepository<UserPerTxnDailyLimit, Integer> {
	
	UserPerTxnDailyLimit findByUserIdAndMainFunction(Integer userId, String mainFunction);

}
