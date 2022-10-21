package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.RegistrationToken;

@Repository
public interface RegistrationTokenRepo extends JpaRepository<RegistrationToken, Long>{
	
	@Query(value = "select distinct x.accountNumber,CASE WHEN (x.mobileNo = '' OR x.mobileNo IS NULL OR LEN((x.mobileNo))  = 0) THEN '' ELSE x.mobileNo END AS  mobileNo,x.name,x.cisNo,x.idNo,x.idType,x.isPremier from RegistrationToken x where (x.accountNumber = ?1 or x.idNo = ?2)")
    public List<Object> findByAccountNumberOrIdNo(String accountNumber, String idNo);
	
	@Query(value = "select top (1) * from TBL_REGISTRATION_TOKEN x where x.account_number in (:accountNumber) ORDER BY x.updated_time DESC", nativeQuery = true)
	public RegistrationToken findByAcctNumberForTopValue(@Param("accountNumber") String accountNumber);
	
	@Query(value = "select x from RegistrationToken x where x.token = ?1")
    public RegistrationToken findByToken(String token);

	@Query(value = "select x from RegistrationToken x where x.accountNumber = ?1")
	public RegistrationToken findByAccountNumber(String accountNumber);

	@Query(value = "select x.audit_additionaldata,x.channel,x.updated_time,x.token from TBL_REGISTRATION_TOKEN x where x.cis_no in (:cisNo) and (x.created_time between :frDateStr and :toDateStr) order by x.created_time DESC offset :offset rows fetch next :pageSize rows only",nativeQuery = true)
	public List<Object> findByCisNo(@Param("cisNo") String cisNo,@Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
	
	@Query(value = "select count(*) from TBL_REGISTRATION_TOKEN x where x.cis_no in (:cisNo) and (x.created_time between :frDateStr and :toDateStr)", nativeQuery = true)
    Integer getCountByCisNo(@Param("cisNo") String cisNo, @Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr);
	
}