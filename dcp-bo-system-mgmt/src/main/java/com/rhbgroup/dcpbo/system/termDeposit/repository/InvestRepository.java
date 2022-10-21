package com.rhbgroup.dcpbo.system.termDeposit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.annotations.DcpRepo;
import com.rhbgroup.dcpbo.system.model.InvestTxn;
import com.rhbgroup.dcpbo.system.model.StagedInvestTxn;

@DcpRepo
@Repository
public interface InvestRepository extends JpaRepository<InvestTxn, Integer>{

	@Query(value="SELECT * FROM dcp.dbo.TBL_INVEST_TXN x where x.TXN_TOKEN_ID= :txnToken", nativeQuery = true)
	InvestTxn getInvestTxnByTokenId(@Param("txnToken") String txnToken);
	
}
