package com.rhbgroup.dcpbo.user.common;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.user.annotations.DcpRepo;
import com.rhbgroup.dcpbo.user.common.model.dcp.FundDetails;

@DcpRepo
@Repository
public interface FundDetailsRepository extends JpaRepository<FundDetails, Integer>, JpaSpecificationExecutor<FundDetails> {

	Optional<FundDetails> findById(int id);
	
    Optional<FundDetails> findByFundId(String fundId);
}
