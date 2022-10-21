package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.UserTxnMainLimit;

@Repository
public interface UserTxnMainLimitRepo extends CrudRepository<UserTxnMainLimit, Integer> {

	UserTxnMainLimit findByUserIdAndTxnType(int userId, String txnType);
}
