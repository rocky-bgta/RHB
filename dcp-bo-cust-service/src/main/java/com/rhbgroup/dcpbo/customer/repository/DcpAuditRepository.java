package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface DcpAuditRepository extends JpaRepository<Audit, Integer> {

    @Query(value = "select * from DCP_AUDIT x where x.event_code in(select EVENT_CODE from DCP_AUDIT_EVENT_CONFIG) and x.user_id = :customerId and (x.timestamp between :frDateStr and :toDateStr) order by x.timestamp DESC offset :offset rows fetch next :pageSize rows only", nativeQuery = true)
    List<Audit> getCustomerAuditEvents(@Param("customerId") int customerId, @Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Query(value = "select * from DCP_AUDIT x where x.event_code in(select EVENT_CODE from dcp_audit_event_config where event_category_id in (:eventCategoryIds)) and x.user_id = :customerId and (x.timestamp between :frDateStr and :toDateStr) order by x.timestamp DESC offset :offset rows fetch next :pageSize rows only", nativeQuery = true)
    List<Audit> getCustomerAuditEventsByCategories(@Param("customerId") int customerId, @Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize, @Param("eventCategoryIds") List<Integer> eventCategoryIds);

    @Query(value = "select count(*) from DCP_AUDIT x where x.event_code in(select EVENT_CODE from DCP_AUDIT_EVENT_CONFIG) and x.user_id = :customerId and (x.timestamp between :frDateStr and :toDateStr)", nativeQuery = true)
    Integer getCustomerAuditEventsCount(@Param("customerId") int customerId, @Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr);

    @Query(value = "select count(*) from DCP_AUDIT x where x.event_code in(select EVENT_CODE from dcp_audit_event_config where event_category_id in (:eventCategoryIds)) and x.user_id = :customerId and (x.timestamp between :frDateStr and :toDateStr)", nativeQuery = true)
    Integer getCustomerAuditEventsCountByCategories(@Param("customerId") int customerId, @Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("eventCategoryIds") List<Integer> eventCategoryIds);

    @Query(value = "select * from DCP_AUDIT x where x.status_code in (:statusCodes) and x.event_code in (:eventCode) and (x.timestamp between :frDateStr and :toDateStr) order by x.timestamp DESC offset :offset rows fetch next :pageSize rows only", nativeQuery = true)
    List<Audit> getCustomerAuditEventsByEventCodeAndStatusCode(@Param("eventCode") List<String> eventCode, @Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize, @Param("statusCodes") List<Integer> statusCodes);

    @Query(value = "select * from DCP_AUDIT x where x.status_code in (:statusCodes) and (x.timestamp between :frDateStr and :toDateStr) order by x.timestamp DESC offset :offset rows fetch next :pageSize rows only", nativeQuery = true)
    List<Audit> getCustomerAuditEventsByStatusCode(@Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize, @Param("statusCodes") List<Integer> statusCodes);

    @Query(value = "select count(x) from Audit x where x.eventCode in (:eventCode) and x.statusCode in (:statusCodes) and (x.timestamp between :frDateStr and :toDateStr)")
    Integer getCustomerAuditEventsCountByEventCodeAndStatusCode(@Param("eventCode") List<String> eventCode, @Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("statusCodes") List<Integer> statusCodes);

    @Query(value = "select count(x) from Audit x where x.statusCode in (:statusCodes) and (x.timestamp between :frDateStr and :toDateStr)")
    Integer getCustomerAuditEventsCountByStatusCode(@Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("statusCodes") List<Integer> statusCodes);

    @Query(value = "select top (:maxRecords) * from DCP_AUDIT x where x.status_code in (:statusCodes) and x.event_code in (:eventCode) and (x.timestamp between :frDateStr and :toDateStr) order by x.timestamp DESC", nativeQuery = true)
    List<Audit> getCustomerAuditEventsByEventCodeAndStatusCodeForCSV(@Param("maxRecords") Integer maxRecords, @Param("eventCode") List<String> eventCode, @Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("statusCodes") List<Integer> statusCodes);

    @Query(value = "select top (:maxRecords) * from DCP_AUDIT x where x.status_code in (:statusCodes) and (x.timestamp between :frDateStr and :toDateStr) order by x.timestamp DESC", nativeQuery = true)
    List<Audit> getCustomerAuditEventsByStatusCodeForCSV(@Param("maxRecords") Integer maxRecords,@Param("frDateStr") Timestamp frDateStr, @Param("toDateStr") Timestamp toDateStr, @Param("statusCodes") List<Integer> statusCodes);
}

