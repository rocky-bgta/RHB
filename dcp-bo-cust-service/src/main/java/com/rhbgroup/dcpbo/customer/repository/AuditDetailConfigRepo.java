package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.AuditDetailConfig;

import java.util.List;

@Repository
public interface AuditDetailConfigRepo extends JpaRepository<AuditDetailConfig, Long> {
    @Query(value = "select x from AuditDetailConfig x where x.eventCode = ?1 and x.fieldName = ?2")
    public AuditDetailConfig findByEventCodeAndFieldName(String eventCode, String fieldName);

    @Query(value = "select x from AuditDetailConfig x where x.eventCode = ?1")
    public List<AuditDetailConfig> findAuditDetailConfigsByEventCode(String eventCode);

    @Query(value = "select x from AuditDetailConfig x where x.eventCode = ?1")
    public List<AuditDetailConfig> findAllByEventCode(String eventCode);

}
