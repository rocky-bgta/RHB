package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@BoRepo
@Repository
public interface BoConfigFunctionRepository extends JpaRepository<BoConfigFunction, Integer> {

    @Query(value = "SELECT x FROM BoConfigFunction x WHERE x.id = :functionId")
    BoConfigFunction findByFunctionId(@Param("functionId") Integer functionId);

}