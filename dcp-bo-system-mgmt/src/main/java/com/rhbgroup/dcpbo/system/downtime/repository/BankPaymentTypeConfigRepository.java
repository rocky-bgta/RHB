package com.rhbgroup.dcpbo.system.downtime.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.annotations.DcpRepo;
import com.rhbgroup.dcpbo.system.model.BankPaymentTypeConfig;

@DcpRepo
@Repository
public interface BankPaymentTypeConfigRepository extends JpaRepository<BankPaymentTypeConfig, Integer> {
	
	@Query(value = "SELECT * FROM dcp.dbo.TBL_BANK_PAYMENT_TYPE_CONFIG x Where x.BANK_ID = :bankId" , nativeQuery = true)
    List<BankPaymentTypeConfig> getBankPaymentTypeConfigDetail(@Param("bankId") int bankId);
	
}

