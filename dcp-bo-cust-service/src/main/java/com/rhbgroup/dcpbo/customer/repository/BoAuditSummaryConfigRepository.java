package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditSummaryConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@BoRepo
@Repository
public interface BoAuditSummaryConfigRepository extends JpaRepository<BoAuditSummaryConfig, Integer> {

    List<BoAuditSummaryConfig> findByEventId(Integer eventId);

}
