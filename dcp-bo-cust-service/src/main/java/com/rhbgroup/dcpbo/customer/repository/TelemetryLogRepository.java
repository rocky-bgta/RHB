package com.rhbgroup.dcpbo.customer.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.TelemetryLog;

/**
 * Spring Data JPA repository for the telemetry operation name entity.
 */
@Repository
public interface TelemetryLogRepository extends JpaRepository<TelemetryLog, Integer> {

    @Query(value = "SELECT TOP 20 dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_DATE_TIME, dtl.AUDIT_TYPE, dtl.USERNAME, COUNT(dtel.MESSAGE_ID) AS TOTAL_ERROR from DCP_TELEMETRY_LOG dtl "
    		+ "LEFT JOIN DCP_TELEMETRY_ERROR_LOG  dtel ON dtl.MESSAGE_ID = dtel.MESSAGE_ID "
    		+ "GROUP BY dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_DATE_TIME, dtl.AUDIT_TYPE, dtl.USERNAME "
    		+ "ORDER BY dtl.AUDIT_DATE_TIME DESC", nativeQuery = true)
    public List<TelemetryLog> findTop20();
    
    @Query(value = "SELECT dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_DATE_TIME, dtl.AUDIT_TYPE, dtl.USERNAME, COUNT(dtel.MESSAGE_ID) AS TOTAL_ERROR from DCP_TELEMETRY_LOG dtl "
    		+ "LEFT JOIN DCP_TELEMETRY_ERROR_LOG  dtel ON dtl.MESSAGE_ID = dtel.MESSAGE_ID "
    		+ "WHERE CONCAT(dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_TYPE, dtl.USERNAME) LIKE :keyword "
    		+ "AND (dtl.AUDIT_DATE_TIME between :fromDate and :toDate) "
    		+ "GROUP BY dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_DATE_TIME, dtl.AUDIT_TYPE, dtl.USERNAME "
    		+ "ORDER BY dtl.AUDIT_DATE_TIME DESC OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY", nativeQuery = true)
    public List<TelemetryLog> findByKeywordAndAuditDateTime(@Param("keyword") String keyword, @Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate, 
    		@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    
    @Query(value = "SELECT COUNT(*) from DCP_TELEMETRY_LOG dtl "
    		+ "LEFT JOIN DCP_TELEMETRY_ERROR_LOG  dtel ON dtl.MESSAGE_ID = dtel.MESSAGE_ID "
    		+ "WHERE CONCAT(dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_TYPE, dtl.USERNAME) LIKE :keyword "
    		+ "AND (dtl.AUDIT_DATE_TIME between :fromDate and :toDate) ", nativeQuery = true)
    public Integer findByKeywordAndAuditDateTimeCount(@Param("keyword") String keyword, @Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate);

    @Query(value = "SELECT dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_DATE_TIME, dtl.AUDIT_TYPE, dtl.USERNAME, COUNT(dtel.MESSAGE_ID) AS TOTAL_ERROR from DCP_TELEMETRY_LOG dtl "
    		+ "LEFT JOIN DCP_TELEMETRY_ERROR_LOG  dtel ON dtl.MESSAGE_ID = dtel.MESSAGE_ID "
    		+ "WHERE CONCAT(dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_TYPE, dtl.USERNAME) LIKE :keyword "
    		+ "AND dtl.AUDIT_TYPE IN (:auditTypes) "
    		+ "AND (dtl.AUDIT_DATE_TIME between :fromDate and :toDate) "    		
    		+ "GROUP BY dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_DATE_TIME, dtl.AUDIT_TYPE, dtl.USERNAME "
    		+ "ORDER BY dtl.AUDIT_DATE_TIME DESC OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY", nativeQuery = true)
    public List<TelemetryLog> findByAuditTypeKeywordAndAuditDateTime(@Param("auditTypes") List<String> auditTypes, @Param("keyword") String keyword, 
    		@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Query(value = "SELECT COUNT(*) from DCP_TELEMETRY_LOG dtl "
    		+ "LEFT JOIN DCP_TELEMETRY_ERROR_LOG  dtel ON dtl.MESSAGE_ID = dtel.MESSAGE_ID "
    		+ "WHERE CONCAT(dtl.MESSAGE_ID, dtl.OPERATION_NAME, dtl.AUDIT_TYPE, dtl.USERNAME) LIKE :keyword "
    		+ "AND dtl.AUDIT_TYPE IN (:auditTypes) "
    		+ "AND (dtl.AUDIT_DATE_TIME between :fromDate and :toDate)" , nativeQuery = true)   		
    public Integer findByAuditTypeKeywordAndAuditDateTimeCount(@Param("auditTypes") List<String> auditTypes, @Param("keyword") String keyword, 
    		@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate);

}
