package com.rhbgroup.dcpbo.user.common;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.rhbgroup.dcpbo.user.common.model.bo.BoUserApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface BoUserApprovalRepo extends JpaRepository<BoUserApproval, Integer> {

	List<BoUserApproval> findAllByFunctionIdAndStatus(Integer functionId, String status);

    @Transactional
    @Modifying
    @Query(value = "UPDATE BoUserApproval x SET x.status = :status, x.reason = :reason, x.updatedBy = :updatedBy, x.updatedTime = :updatedTime WHERE x.id = :approvalId")
    Integer updateDeviceStatus(@Param("status") String status,
                               @Param("reason") String reason,
                               @Param("approvalId") Integer approvalId,
                               @Param("updatedBy") String updatedBy,
                               @Param("updatedTime")Date updatedTime);
}
