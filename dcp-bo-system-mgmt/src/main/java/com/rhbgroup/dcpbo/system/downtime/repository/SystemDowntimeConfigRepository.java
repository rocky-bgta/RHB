package com.rhbgroup.dcpbo.system.downtime.repository;


import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.annotations.DcpRepo;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;

/**
 * Spring Data JPA repository for the System Downtime Config entity.
 */
@DcpRepo
@Repository
public interface SystemDowntimeConfigRepository extends JpaRepository<SystemDowntimeConfig, Integer>, JpaSpecificationExecutor<SystemDowntimeConfig> {

    @Query("SELECT x from SystemDowntimeConfig x WHERE x.type = 'ADHOC' AND x.isActive = '1' AND :startTime <= x.endTime AND :endTime >= x.startTime "
    		+ " AND x.adhocType = :adhocType AND x.adhocTypeCategory = :adhocCategory AND x.bankId = :bankId")
    List<SystemDowntimeConfig> findByStartTimeAndEndTime(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime, @Param("adhocType") String adhocType,
    		@Param("adhocCategory") String adhocCategory, @Param("bankId") Integer bankId);
    
    @Query("SELECT x from SystemDowntimeConfig x WHERE x.type = 'ADHOC' AND x.isActive = '1' AND :startTime <= x.endTime AND :endTime >= x.startTime "
    		+ " AND x.adhocType = :adhocType AND x.adhocTypeCategory = :adhocCategory ")
    List<SystemDowntimeConfig> findByStartTimeAndEndTime(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime, @Param("adhocType") String adhocType,
    		@Param("adhocCategory") String adhocCategory);
    
    @Query("SELECT x from SystemDowntimeConfig x WHERE x.type = 'ADHOC' AND x.isActive = '1' AND :startTime <= x.endTime AND :endTime >= x.startTime AND x.id <> :id "
    		+ " AND x.adhocType = :adhocType AND x.adhocTypeCategory = :adhocCategory AND x.bankId = :bankId")
    List<SystemDowntimeConfig> findByStartTimeAndEndTimeForUpdate(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime, @Param("id") int id,
    		@Param("adhocType") String adhocType, @Param("adhocCategory") String adhocCategory, @Param("bankId") Integer bankId);
    
    @Query("SELECT x from SystemDowntimeConfig x WHERE x.type = 'ADHOC' AND x.isActive = '1' AND :startTime <= x.endTime AND :endTime >= x.startTime AND x.id <> :id "
    		+ " AND x.adhocType = :adhocType AND x.adhocTypeCategory = :adhocCategory ")
    List<SystemDowntimeConfig> findByStartTimeAndEndTimeForUpdate(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime, @Param("id") int id,
    		@Param("adhocType") String adhocType, @Param("adhocCategory") String adhocCategory);
    
    SystemDowntimeConfig findOneById(int id);
    
    @Query("SELECT x from SystemDowntimeConfig x WHERE x.type = 'ADHOC' AND x.isActive = '1' AND :startTime <= x.endTime AND :endTime >= x.startTime  AND x.adhocType = :adhocType")
    List<SystemDowntimeConfig> findByStartTimeEndTimeAndAdhocType(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime, @Param("adhocType") String adhocType);
   
}
