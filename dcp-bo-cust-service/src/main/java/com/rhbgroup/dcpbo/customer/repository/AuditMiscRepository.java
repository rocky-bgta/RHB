package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.AuditMisc;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface AuditMiscRepository extends JpaRepository<AuditMisc, Integer>, AuditDetailsTableRepository {
}
