package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.AppConfig;


@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Integer>{
	
	 @Query(value = "select x from AppConfig x where x.parameterKey  = :parameterKey  ")
	 AppConfig getParameterValue(@Param("parameterKey")String parameterKey);
	
}
