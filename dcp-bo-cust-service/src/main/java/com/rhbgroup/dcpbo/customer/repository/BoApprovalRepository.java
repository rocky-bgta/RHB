package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@BoRepo
@Repository
public interface BoApprovalRepository extends JpaRepository<BoApproval, Integer> {

    @Query(value = "SELECT x.id FROM BoApproval x WHERE x.functionId = :functionId AND x.status = 'P'")
    List<Integer> findPendingRequest(@Param("functionId") Integer functionId);

}