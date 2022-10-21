package com.rhbgroup.dcpbo.system.downtime.whitelist.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.annotations.DcpRepo;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeWhitelistConfig;

/**
 * Spring Data JPA repository for the System Downtime Whitelist Config entity.
 */
@DcpRepo
@Repository
public interface SystemDowntimeWhitelistConfigRepository extends JpaRepository<SystemDowntimeWhitelistConfig, Integer>, JpaSpecificationExecutor<SystemDowntimeWhitelistConfig> {
   
	List<SystemDowntimeWhitelistConfig> findByUserIdAndType(int userId, String type);
}
