package com.rhbgroup.dcpbo.system.termDeposit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.annotations.DcpRepo;
import com.rhbgroup.dcpbo.system.model.TdTxn;

@DcpRepo
@Repository
public interface TxnRepository extends JpaRepository<TdTxn, Integer>{

	@Query(value="SELECT * FROM dcp.dbo.TBL_TD_TXN x where x.TXN_TOKEN_ID= :txnToken", nativeQuery = true)
	TdTxn getTdTxnByTokenId(@Param("txnToken") String txnToken);
}
