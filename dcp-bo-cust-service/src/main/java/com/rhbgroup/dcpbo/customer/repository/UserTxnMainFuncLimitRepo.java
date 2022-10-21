package com.rhbgroup.dcpbo.customer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.model.UserTxnMainFuncLimit;

@Repository
public interface UserTxnMainFuncLimitRepo extends CrudRepository<UserTxnMainFuncLimit, Integer> {

	List<UserTxnMainFuncLimit> findAllByUserIdOrderByTxnType(UserProfile userId);

	List<UserTxnMainFuncLimit> findAllByUserIdAndTxnTypeOrderByMainFunction(UserProfile userId, String txnType);

	@Query("select distinct e.txnType from UserTxnMainFuncLimit e where e.userId = ?")
	List<String> findAllTxnType(UserProfile userId);

	@Query(value = "select * from TBL_USER_TXN_ADVANCE_LIMIT e where e.MAIN_FUNCTION != 'DUITNOW_QR' and e.USER_ID = ?" , nativeQuery = true)
	List<UserTxnMainFuncLimit> findByUserId(int userId);
	
	@Query(value = "select * from TBL_USER_TXN_ADVANCE_LIMIT e where e.MAIN_FUNCTION = 'DUITNOW_QR' and e.USER_ID = ?" , nativeQuery = true)
	List<UserTxnMainFuncLimit> findByUserIdAndMainFunction(int userId);
	 //@Query(value = "select * from TBL_transfer_txn where ref_id = ?1", nativeQuery = true)
}
