package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.AuditBillPayment;
import io.ebean.BeanRepository;
import io.ebean.EbeanServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DcpAuditBillPaymentRepository extends JpaRepository<AuditBillPayment, Integer>,AuditDetailsTableRepository {

    @Query(value = "SELECT x FROM AuditBillPayment x WHERE x.auditId IN :auditIds")
    List<AuditBillPayment> getDetails(@Param("auditIds") List<Integer> auditIds);

    @Query(value = "SELECT x FROM AuditBillPayment x WHERE x.auditId = :auditId")
    AuditBillPayment getDetail(@Param("auditId") int auditId);

    @Query(value = "select audit_id from DCP_audit_bill_payment where JSON_VALUE(details,'$.request.refId')=?1", nativeQuery = true)
    public Integer findAuditIdByRefId(String refId);
}
