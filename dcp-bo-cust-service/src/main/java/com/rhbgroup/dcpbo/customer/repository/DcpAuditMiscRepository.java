package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.AuditMisc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DcpAuditMiscRepository extends JpaRepository<AuditMisc, Integer> {

    @Query(value = "SELECT x FROM AuditMisc x WHERE x.auditId IN :auditIds")
    List<AuditMisc> getDetails(@Param("auditIds") List<Integer> auditIds);

    @Query(value = "SELECT x FROM AuditMisc x WHERE x.auditId = :auditId")
    AuditMisc getDetail(@Param("auditId") int auditId);
}
