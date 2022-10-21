package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.Audit;
import com.rhbgroup.dcpbo.customer.model.AuditEventConfig;
import com.rhbgroup.dcpbo.customer.model.DcpAuditCategoryConfig;
import io.ebean.BeanRepository;
import io.ebean.EbeanServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DcpAuditEventConfigRepository extends JpaRepository<AuditEventConfig, Integer> {

    @Query(value = "SELECT x FROM AuditEventConfig x WHERE x.eventCode IN (:eventCodes) AND x.eventCategoryId IN (:eventCategoryId)")
    List<AuditEventConfig> getMappings(@Param("eventCodes") List<String> eventCodes, @Param("eventCategoryId") List<Integer> eventCategoryId);

    @Query(value = "select * from dcp_audit_event_config where event_code = ?1 order by event_name asc", nativeQuery = true)
    public AuditEventConfig findByEventCode(String eventCode);

    @Query(value = "SELECT x FROM AuditEventConfig x WHERE x.eventCode IN (:eventCodes)")
    List<AuditEventConfig> getMappingsAll(@Param("eventCodes") List<String> eventCodes);

    @Query(value = "select CAST(event_code as text) from DCP_AUDIT_EVENT_CONFIG where event_category_id in(" +
            "            select id from DCP_AUDIT_CATEGORY_CONFIG where category_name = 'Manage Favourites')", nativeQuery = true)
    List<String> getEventCodesFavourites();


    @Query(value = "SELECT x.eventCode FROM AuditEventConfig x")
    List<String> getAllEventCodes();

    @Query(value = "SELECT x FROM AuditEventConfig x WHERE x.eventCategoryId = :categoryId ORDER BY eventName ASC")
    List<AuditEventConfig> findByCategoryId(@Param("categoryId") Integer categoryId);

}
