package com.rhbgroup.dcpbo.system.termDeposit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.annotations.DcpRepo;
import com.rhbgroup.dcpbo.system.model.ServerConfig;

@DcpRepo
@Repository
public interface ServerConfigRepository extends JpaRepository<ServerConfig, Integer>{

	@Query(value="SELECT x.PARAMETER_VALUE FROM dcp.dbo.TBL_SERVER_CONFIG x where x.PARAMETER_KEY= :parameterKey", nativeQuery = true)
	String getParameterValue(@Param("parameterKey") String parameterKey);
}