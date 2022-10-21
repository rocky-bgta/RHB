package com.rhbgroup.dcpbo.user.common;

import com.rhbgroup.dcpbo.user.common.model.bo.BoApprovalDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoApprovalDeviceRepository extends JpaRepository<BoApprovalDevice, Integer> {

    @Query("SELECT x FROM BoApprovalDevice x WHERE x.approvalId = :approvalId")
    BoApprovalDevice findByApprovalId(@Param("approvalId") Integer approvalId);
}
