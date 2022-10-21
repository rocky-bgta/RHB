package com.rhbgroup.dcpbo.customer.repository;

import java.util.Date;
import javax.transaction.Transactional;

import com.rhbgroup.dcpbo.customer.dto.UnlockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rhbgroup.dcpbo.customer.model.CustomerVerification;

public interface CustomerVerificationRepo extends JpaRepository<CustomerVerification, Long>{

	@Query(value = "select COUNT(x.inputNumber) from CustomerVerification x where (x.inputNumber = ?1 and x.isActive = 1) GROUP BY x.inputNumber")
    public String findByInputNumber(String inputNumber);
	
	@Query(value = "select top (1) token from TBL_CUST_VERIFY_ATTEMPT x where x.input_number in (:inputNumber) ORDER BY x.updated_time DESC", nativeQuery = true)
	public String findByAcctNumberByUpdatedTime(@Param("inputNumber") String inputNumber);

	@Query(value = "Select NAME name, ID_NO idNo, MOBILE_NO mobileNo From dcp.dbo.TBL_USER_PROFILE  u Left Join dcp.dbo.TBL_CARD_PROFILE c on u.ID = c.USER_ID Left Join dcp.dbo.TBL_LOAN_PROFILE  l on u.ID = l.USER_ID Where c.CARD_NO = :inputNumber Or l.ACCOUNT_NO = :inputNumber", nativeQuery = true)
	public UnlockData retrieveUnblockData(@Param("inputNumber") String inputNumber);

	@Transactional
	@Modifying
	@Query(value = "UPDATE CustomerVerification x SET IS_ACTIVE = :isActive, UPDATED_TIME = :updatedTime WHERE x.inputNumber = :inputNumber")
	Integer updateUnlockStatus(@Param("inputNumber") String inputNumber, @Param("isActive") Boolean isActive, @Param("updatedTime") Date updatedTime);
}
