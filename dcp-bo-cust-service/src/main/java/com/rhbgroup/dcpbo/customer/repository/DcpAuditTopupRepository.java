package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.AuditTopup;
import io.ebean.BeanRepository;
import io.ebean.EbeanServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DcpAuditTopupRepository extends JpaRepository<AuditTopup, Integer> ,AuditDetailsTableRepository{

    @Query(value = "SELECT x FROM AuditTopup x WHERE x.auditId IN :auditIds")
    List<AuditTopup> getDetails(@Param("auditIds") List<Integer> auditIds);

    @Query(value = "SELECT x FROM AuditTopup x WHERE x.auditId = :auditId")
    AuditTopup getDetail(@Param("auditId") Integer auditId);

    @Query(value = "select top 1 * from DCP_AUDIT_TOPUP where JSON_VALUE(DETAILS,'$.request.refId') = ?1", nativeQuery = true)
    public AuditTopup findByRefId(String refId);
}
