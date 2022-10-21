package com.rhbgroup.dcpbo.user.common;


import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
