
package com.rhbgroup.dcpbo.system.downtime.whitelist.repository;

import com.rhbgroup.dcpbo.system.annotations.DcpRepo;
import com.rhbgroup.dcpbo.system.model.DowntimeWhitelist;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 
 * Repository mainly for TBL_SYSTEM_DOWNTIME_WHITELIST 
 * 
 * @author Faizal Musa
 */
@DcpRepo
@Repository
public interface DowntimeWhitelistRepository extends JpaRepository<DowntimeWhitelist, Integer>, JpaSpecificationExecutor<DowntimeWhitelist>{
    
    @Query("SELECT x from DowntimeWhitelist x WHERE x.type = :type")
    Page<DowntimeWhitelist> findByType(@Param("type") String type, Pageable page);
    
    @Query("SELECT COUNT(x) from DowntimeWhitelist x WHERE x.type = :type")
    Integer getTotalByType(@Param("type") String type);
}
