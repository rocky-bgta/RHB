package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.TelemetryOperationName;

/**
 * Spring Data JPA repository for the telemetry operation name entity.
 */
@BoRepo
@Repository
public interface TelemetryOperationNameRepository extends JpaRepository<TelemetryOperationName, Integer> {

}
