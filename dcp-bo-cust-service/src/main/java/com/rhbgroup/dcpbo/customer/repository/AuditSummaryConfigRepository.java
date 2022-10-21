package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.AuditSummaryConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface AuditSummaryConfigRepository extends JpaRepository<AuditSummaryConfig, Integer> {

    @Query(value = "select x from AuditSummaryConfig x where x.eventCode = ?1")
    public List<AuditSummaryConfig> findPathsByEventCode(String eventCode);
}