package com.rhbgroup.dcpbo.customer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.TelemetryLogPayload;

/**
 * Spring Data JPA repository for the telemetry log payload.
 */
@Repository
public interface TelemetryLogPayloadRepository extends JpaRepository<TelemetryLogPayload, Integer> {

    @Query(value = "SELECT x.MESSAGE_ID, x.OPERATION_NAME, x.AUDIT_DATE_TIME, x.AUDIT_TYPE, x.PAYLOAD, x.AUDIT_PARAM, x.HOSTNAME, x.CIS_NUMBER, x.DEVICE_ID "
    		+ "from DCP_TELEMETRY_LOG x WHERE x.message_id = :messageId AND x.audit_date_time = :auditDateTime", nativeQuery = true)
    List<TelemetryLogPayload> findByMessageIdAndAuditDateTime(@Param("messageId") String messageId, @Param("auditDateTime") String auditDateTime);
}
