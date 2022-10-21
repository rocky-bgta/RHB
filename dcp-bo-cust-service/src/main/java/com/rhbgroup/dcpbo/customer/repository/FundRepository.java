package com.rhbgroup.dcpbo.customer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.annotation.DcpRepo;
import com.rhbgroup.dcpbo.customer.model.FundDetails;

@DcpRepo
@Repository
public interface FundRepository extends JpaRepository<FundDetails, Integer>, JpaSpecificationExecutor<FundDetails> {

	Optional<FundDetails> findById(int id);
	
    @Query(value = "SELECT CASE\r\n" + 
    		"    WHEN INVESTMENT_TYPE ='ASNB_FIXED' THEN 'Fixed Price Fund'\r\n" + 
    		"    WHEN INVESTMENT_TYPE ='ASNB_VARIABLE'THEN 'Variable Price Fund'\r\n" + 
    		"    WHEN INVESTMENT_TYPE ='ASNB_VARIABLE_FORWARD'THEN 'Variable Forward Price Fund'\r\n" + 
    		"END AS INVESTMENT_TYPE_LABEL FROM dcp.dbo.TBL_FUND_SETUP where IS_TOPUP_ENABLED=:isTopEnabled and IS_ACTIVE=:isActive " 
    		, nativeQuery = true)
    List<String> getTopEnabledFundSetups(@Param("isTopEnabled") int isTopEnabled,@Param("isActive") int isActive);
    
    FundDetails findByfundId(String fundId);

}
