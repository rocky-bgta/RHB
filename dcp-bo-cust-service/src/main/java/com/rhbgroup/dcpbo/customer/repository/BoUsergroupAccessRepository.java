package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoUsergroupAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@BoRepo
@Repository
public interface BoUsergroupAccessRepository extends JpaRepository<BoUsergroupAccess, Integer> {

    @Query(value = "SELECT x.userGroupId FROM BoUsergroupAccess x WHERE x.functionId = :functionId AND accessType = 'C' AND status = 'A'")
    List<Integer> findCheckersIdByFunctionId(@Param("functionId") Integer functionId);

}