package com.rhbgroup.dcpbo.customer.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.DcpTelemetryErrorLog;

@Repository
public interface DcpTelemetryErrorLogRepository extends JpaRepository<DcpTelemetryErrorLog, Timestamp> {
		
	@Query(value = "SELECT * FROM DCP_TELEMETRY_ERROR_LOG c " + 
			"WHERE (c.MESSAGE_ID LIKE %?1% OR c.OPERATION_NAME LIKE %?1% OR c.ERROR_CODE LIKE %?1% OR c.ERROR_DETAILS LIKE %?1% OR c.ERROR_REASON LIKE %?1%) " + 
			"AND c.AUDIT_DATE_TIME BETWEEN ?2 AND ?3 ORDER BY c.AUDIT_DATE_TIME DESC OFFSET ?4 ROWS FETCH NEXT ?5 ROWS ONLY", nativeQuery = true)
	public List<DcpTelemetryErrorLog> findByAuditDateTimeAndKeyword(String keyword, String fromDate, String toDate, int offset, int pageSize);
	
	@Query(value = "SELECT COUNT(*) FROM DCP_TELEMETRY_ERROR_LOG c " + 
			"WHERE (c.MESSAGE_ID LIKE %?1% OR c.OPERATION_NAME LIKE %?1% OR c.ERROR_CODE LIKE %?1% OR c.ERROR_DETAILS LIKE %?1% OR c.ERROR_REASON LIKE %?1%) " + 
			"AND c.AUDIT_DATE_TIME BETWEEN ?2 AND ?3", nativeQuery = true)
	public Integer countFindByKeywordAndAuditDateTime(String keyword, String fromDate, String toDate);
	
	@Query(value = "SELECT * FROM DCP_TELEMETRY_ERROR_LOG c " + 
			"WHERE c.MESSAGE_ID = ?1 AND c.AUDIT_DATE_TIME = ?2", nativeQuery = true)
	public List<DcpTelemetryErrorLog> findByMessageIdAndAuditDateTime(String messageId, String auditDateTimeTo);
	
}
