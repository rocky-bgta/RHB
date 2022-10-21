package com.rhbgroup.dcpbo.system.downtime.repository;

import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.model.Approval;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Integer> {
    
    @Query("SELECT x FROM Approval x WHERE x.id = :approvalId AND x.actionType = :actionType")
    Approval findByIdAndActionType(@Param("approvalId") int approvalId, @Param("actionType") String actionType);
    
    @Query("SELECT x FROM Approval x WHERE x.functionId = :functionId order by updatedTime desc")
    List<Approval> findByFunctionId(@Param("functionId") int functionId);

    @Query("SELECT COUNT(x) FROM Approval x WHERE x.functionId = :functionId AND x.status = :status order by updatedTime desc")
    Integer findCountByFunctionIdAndStatus(@Param("functionId") int functionId, @Param("status") String status);

    @Query("SELECT x FROM Approval x WHERE x.functionId = :functionId AND x.status = :status order by updatedTime desc")
    List<Approval> findByFunctionIdAndStatus(@Param("functionId") int functionId, @Param("status") String status);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Approval x SET x.status = :status, x.reason = :reason, x.updatedBy = :updatedBy, " +
    		"x.updatedTime = :updatedTime WHERE x.id = :id ")
    Integer updateStatusById(@Param("status") String status,
    		@Param("id") int id,
    		@Param("reason") String reason,
    		@Param("updatedBy") String updatedBy,
    		@Param("updatedTime") Timestamp updatedTime);

    @Query(value = "UPDATE TBL_BO_APPROVAL SET status = :status, reason = :rejectReason, updated_time = :updatedTime, updated_by = :updatedBy " +
            "OUTPUT inserted.function_id, inserted.action_type, inserted.description " +
            " WHERE id = :id ", nativeQuery = true  )
    List<Object[]> updateStatusByIdOutput(@Param("status") String status,
                                          @Param("id") int id,
                                          @Param("rejectReason") String rejectReason,
                                          @Param("updatedTime") Timestamp updatedTime,
                                          @Param("updatedBy") String updatedBy);

    @Query("SELECT x.id FROM Approval x WHERE x.functionId = :functionId AND x.status = :status")
    List<Integer> findIdByFunctionIdAndStatus(@Param("functionId") int functionId, @Param("status") String status);

    @Query(value = "INSERT INTO TBL_BO_APPROVAL (FUNCTION_ID, CREATOR_ID, DESCRIPTION, ACTION_TYPE, STATUS," +
            " CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) " +
            " OUTPUT inserted.id" +
            " VALUES (:functionId,:creatorId,:description,:actionType,:status," +
            " :createdTime, :createdBy, :updatedTime, :updatedBy) ", nativeQuery = true)
    List<Integer> insert(@Param("functionId") Integer functionId, @Param("creatorId") Integer creatorId,
                   @Param("description") String description, @Param("actionType") String actionType,
                   @Param("status") String status,
                   @Param("createdTime") Timestamp createdTime, @Param("createdBy") String createdBy,
                   @Param("updatedTime") Timestamp updatedTime, @Param("updatedBy") String updatedBy);


    @Query("SELECT DISTINCT x.status, COUNT(x.status) FROM Approval x where x.functionId = :functionId GROUP BY x.status")
    List<Object[]> findCountByFunctionIdAndStatusNew(@Param("functionId") int functionId);
}