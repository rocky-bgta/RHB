package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoUserUsergroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@BoRepo
@Repository
public interface BoUserUsergroupRepository extends JpaRepository<BoUserUsergroup, Integer> {

    @Query(value = "SELECT x.userId FROM BoUserUsergroup x WHERE x.userGroupId IN :groupIdList")
    List<Integer> findUserIdsByGroupIds(@Param("groupIdList") List<Integer> groupIdList);
}