package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@BoRepo
@Repository
public interface BoAuditDetailsRepository extends JpaRepository<BoAuditDetails, Integer> {

    @Query( value = "select id, audit_id, details from tbl_bo_audit_biller where audit_id = :id", nativeQuery = true)
    BoAuditDetails getAuditBillerDetails (@Param("id") int id);

    @Query( value = "select id, audit_id, details from tbl_bo_audit_fund where audit_id = :id", nativeQuery = true)
    BoAuditDetails getAuditFundDetails (@Param("id") int id);

    @Query( value = "select id, audit_id, details from tbl_bo_audit_fundsetup where audit_id = :id", nativeQuery = true)
    BoAuditDetails getAuditFundsetupDetails (@Param("id") int id);

    @Query( value = "select id, audit_id, details from tbl_bo_audit_provide_assist where audit_id = :id", nativeQuery = true)
    BoAuditDetails getAuditProvideAssistanceDetails(@Param("id") int id);

    @Query( value = "select id, audit_id, details from tbl_bo_audit_sm_downtime where audit_id = :id", nativeQuery = true)
    BoAuditDetails getAuditSmDowntimeDetails(@Param("id") int id);

    @Query( value = "select id, audit_id, details from tbl_bo_audit_usermgmt where audit_id = :id", nativeQuery = true)
    BoAuditDetails getAuditUsermgmtDetails(@Param("id") int id);

}
