package com.rhbgroup.dcpbo.user.common;

import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface BoUmApprovalUsergroupRepository extends JpaRepository<BoUmApprovalUserGroup, Integer>{

    @Query("SELECT x from BoUmApprovalUserGroup x WHERE x.approvalId in (:approvalIdList) and x.lockingId = :lockingId ")
    List<BoUmApprovalUserGroup> findByApprovalIdAndLockingId(@Param("approvalIdList") List<Integer> approvalIdList, @Param("lockingId") String lockingId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO TBL_BO_UM_APPROVAL_USERGROUP (APPROVAL_ID, STATE, LOCKING_ID, PAYLOAD," +
            " CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES (:approvalId,:state,:lockingId,:payload," +
            ":createdTime, :createdBy, :updatedTime, :updatedBy)", nativeQuery = true)
    Integer insert(@Param("approvalId") Integer approvalId, @Param("state") String state,
                   @Param("lockingId") String lockingId, @Param("payload") String payload,
                   @Param("createdTime") Timestamp createdTime, @Param("createdBy") String createdBy,
                   @Param("updatedTime") Timestamp updatedTime, @Param("updatedBy") String updatedBy);
}











