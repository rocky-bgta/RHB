package com.rhbgroup.dcpbo.system.downtime.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.model.ConfigFunction;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface ConfigFunctionRepository extends JpaRepository<ConfigFunction, Integer> {

    @Query("SELECT x from ConfigFunction x WHERE x.id = :functionId")
    ConfigFunction findByFunctionId(@Param("functionId") Integer functionId);

	ConfigFunction findOneById(int id);

    @Query("SELECT x.functionName from ConfigFunction x WHERE x.id = :id")
	String findFunctionNameById(@Param("id") Integer id);

}
