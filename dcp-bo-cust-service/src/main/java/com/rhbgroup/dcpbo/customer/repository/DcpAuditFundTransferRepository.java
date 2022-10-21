package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.AuditFundTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DcpAuditFundTransferRepository extends JpaRepository<AuditFundTransfer, Integer>,AuditDetailsTableRepository {

    @Query(value = "SELECT x FROM AuditFundTransfer x WHERE x.auditId IN :auditIds")
    List<AuditFundTransfer> getDetails(@Param("auditIds") List<Integer> auditIds);

    @Query(value = "SELECT x FROM AuditFundTransfer x WHERE x.auditId = :auditId")
    AuditFundTransfer getDetail(@Param("auditId") Integer auditId);

    @Query(value = "select top 1 * from DCP_AUDIT_FUND_TRANSFER where JSON_VALUE(DETAILS,'$.request.refId') = ?1", nativeQuery = true)
    public AuditFundTransfer findAuditFundTransferByRefId(String refId);

}
