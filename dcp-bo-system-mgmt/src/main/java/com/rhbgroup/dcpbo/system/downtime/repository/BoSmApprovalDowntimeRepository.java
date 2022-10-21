package com.rhbgroup.dcpbo.system.downtime.repository;

import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntime;

@Repository
public interface BoSmApprovalDowntimeRepository extends JpaRepository<BoSmApprovalDowntime, Integer>{

    @Query("SELECT x from BoSmApprovalDowntime x WHERE x.approvalId in (:approvalIdList) and x.lockingId = :lockingId ")
    List<BoSmApprovalDowntime> findByApprovalIdAndLockingId(@Param("approvalIdList") List<Integer> approvalIdList, @Param("lockingId") String lockingId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO TBL_BO_SM_APPROVAL_DOWNTIME (APPROVAL_ID, STATE, LOCKING_ID, PAYLOAD," +
            " CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES (:approvalId,:state,:lockingId,:payload," +
            ":createdTime, :createdBy, :updatedTime, :updatedBy)", nativeQuery = true)
    Integer insert(@Param("approvalId") Integer approvalId, @Param("state") String state,
                   @Param("lockingId") String lockingId, @Param("payload") String payload,
                   @Param("createdTime") Timestamp createdTime, @Param("createdBy") String createdBy,
                   @Param("updatedTime") Timestamp updatedTime, @Param("updatedBy") String updatedBy);

    @Query("SELECT x from BoSmApprovalDowntime x WHERE x.approvalId = :approvalId")
    List<BoSmApprovalDowntime> findByApprovalId(@Param("approvalId") Integer approvalId);
    
    BoSmApprovalDowntime findOneByApprovalId(int approvalId);
}











