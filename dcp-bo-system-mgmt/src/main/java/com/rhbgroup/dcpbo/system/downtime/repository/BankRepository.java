package com.rhbgroup.dcpbo.system.downtime.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.annotations.DcpRepo;
import com.rhbgroup.dcpbo.system.model.Bank;

/**
 * Spring Data JPA repository for the User entity.
 */
@DcpRepo
@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {
	
	 @Query(value = "SELECT x.bank_name FROM dcp.dbo.TBL_BANK x where x.ID = :bankId" , nativeQuery = true)
	    String getBankNameById(@Param("bankId") String bankId);
	 
	 @Query(value = "SELECT * FROM dcp.dbo.TBL_BANK x order by x.bank_name ASC offset :offset rows fetch next :pageSize rows only" , nativeQuery = true)
	    List<Bank> getBankList(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
	 
	 @Query(value = "SELECT count(*) FROM dcp.dbo.TBL_BANK x " , nativeQuery = true)
	    Integer getBankListCount();
	 		
}
