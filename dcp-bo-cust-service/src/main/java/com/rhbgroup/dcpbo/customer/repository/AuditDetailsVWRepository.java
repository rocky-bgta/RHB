package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.AuditDetailsVW;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@BoRepo
@Repository
public interface AuditDetailsVWRepository extends JpaRepository<AuditDetailsVW, Integer> {
//    @Query(value = "    SELECT TOP (:maxRecords) A.ID ,A.EVENT_CODE, A.USER_ID,A.STATUS_CODE,TIMESTAMP,B.DETAILS FROM DCP_AUDIT A JOIN (SELECT * FROM" +
//            "            (SELECT * FROM DCP_AUDIT_BILL_PAYMENT UNION" +
//            "            SELECT * FROM DCP_AUDIT_TOPUP UNION" +
//            "            SELECT * FROM DCP_AUDIT_FUND_TRANSFER UNION" +
//            "            SELECT * FROM DCP_AUDIT_MISC UNION" +
//            "            SELECT * FROM DCP_AUDIT_PROFILE) C WHERE C.AUDIT_ID IN (:auditIds)) B" +
//            "    ON A.ID = B.AUDIT_ID GROUP BY A.ID,A.EVENT_CODE, A.USER_ID,A.STATUS_CODE,TIMESTAMP,B.DETAILS ORDER BY A.TIMESTAMP DESC", nativeQuery = true)
//    List<AuditDetailsVW> getCustomerAuditDetailsByAuditIds(@Param("maxRecords") Integer maxRecords, @Param("auditIds") List<Integer> auditIds);
//
//
//    @Query(value = "    SELECT A.ID ,A.EVENT_CODE, A.USER_ID,A.STATUS_CODE,TIMESTAMP,B.DETAILS FROM DCP_AUDIT A JOIN (SELECT * FROM" +
//            "            (SELECT * FROM DCP_AUDIT_BILL_PAYMENT UNION" +
//            "            SELECT * FROM DCP_AUDIT_TOPUP UNION" +
//            "            SELECT * FROM DCP_AUDIT_FUND_TRANSFER UNION" +
//            "            SELECT * FROM DCP_AUDIT_MISC UNION" +
//            "            SELECT * FROM DCP_AUDIT_PROFILE) C WHERE C.AUDIT_ID " +
//            " IN (SELECT TOP (:maxRecords) ID FROM DCP_AUDIT X WHERE X.STATUS_CODE in (:statusCodes) AND (X.TIMESTAMP BETWEEN :frDateStr AND :toDateStr))) B" +
//            "    ON A.ID = B.AUDIT_ID GROUP BY A.ID,A.EVENT_CODE, A.USER_ID,A.STATUS_CODE,TIMESTAMP,B.DETAILS ORDER BY A.TIMESTAMP DESC", nativeQuery = true)
//    List<AuditDetailsVW> getCustomerAuditDetailsByAuditIds(@Param("maxRecords") Integer maxRecords,
//                                                           @Param("statusCodes") List<Integer> statusCodes,
//                                                           @Param("frDateStr") Timestamp frDateStr,
//                                                           @Param("toDateStr") Timestamp toDateStr);

    @Query(value = "SELECT TOP (:maxRecords) id, event_code, user_id, status_code, timestamp, details,audit_type,channel,username,status_description,event_name FROM VW_BO_INVESTIGATION_AUDIT X WHERE X.EVENT_CODE IN (:eventCodes) AND X.STATUS_CODE IN (:statusCodes) AND (X.TIMESTAMP BETWEEN :frDateStr AND :toDateStr)", nativeQuery = true)
    List<AuditDetailsVW> getCustomerAuditDetailsByAuditIds(@Param("maxRecords") Integer maxRecords,
    													   @Param("eventCodes") List<String> eventCodes,
                                                           @Param("statusCodes") List<Integer> statusCodes,
                                                           @Param("frDateStr") Timestamp frDateStr,
                                                           @Param("toDateStr") Timestamp toDateStr);
    
    
    @Query(value = "SELECT TOP (:maxRecords) id, event_code, user_id, status_code, timestamp, details,audit_type,channel,username,status_description,event_name FROM VW_BO_INVESTIGATION_AUDIT X WHERE X.STATUS_CODE IN (:statusCodes) AND (X.TIMESTAMP BETWEEN :frDateStr AND :toDateStr)", nativeQuery = true)
    List<AuditDetailsVW> getCustomerAuditDetailsByAuditIds(@Param("maxRecords") Integer maxRecords,													
                                                           @Param("statusCodes") List<Integer> statusCodes,
                                                           @Param("frDateStr") Timestamp frDateStr,
                                                           @Param("toDateStr") Timestamp toDateStr);
}


