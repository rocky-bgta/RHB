package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@BoRepo
@Repository
public interface BoUserRepository extends JpaRepository<BoUser, Integer> {

    @Query(value = "SELECT x.email FROM BoUser x WHERE x.id IN :userIdList")
    List<String> findEmailsByUserIds(@Param("userIdList") List<Integer> userIdList);
}

