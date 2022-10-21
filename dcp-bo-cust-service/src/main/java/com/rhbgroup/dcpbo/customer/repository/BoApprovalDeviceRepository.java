package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoApproval;
import com.rhbgroup.dcpbo.customer.dcpbo.BoApprovalDevice;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@BoRepo
@Repository
public interface BoApprovalDeviceRepository extends JpaRepository<BoApprovalDevice, Integer> {

    @Query(value = "SELECT x FROM BoApprovalDevice x WHERE x.approvalId IN :boApprovalList AND x.lockingId = :lockingId")
    BoApprovalDevice findApprovalIdByApprovalList(@Param("boApprovalList") List<Integer> boApprovalList,
                                                  @Param("lockingId") String lockingId);
}