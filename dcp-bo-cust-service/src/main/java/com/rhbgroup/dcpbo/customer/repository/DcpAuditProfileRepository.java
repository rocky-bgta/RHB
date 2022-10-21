package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.AuditProfile;
import io.ebean.BeanRepository;
import io.ebean.EbeanServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DcpAuditProfileRepository extends JpaRepository<AuditProfile, Integer> {

    @Query(value = "SELECT x FROM AuditProfile x WHERE x.auditId IN :auditIds")
    List<AuditProfile> getDetails(@Param("auditIds") List<Integer> auditIds);

    @Query(value = "SELECT x FROM AuditProfile x WHERE x.auditId = :auditId")
    AuditProfile getDetail(@Param("auditId") Integer auditId);
}
