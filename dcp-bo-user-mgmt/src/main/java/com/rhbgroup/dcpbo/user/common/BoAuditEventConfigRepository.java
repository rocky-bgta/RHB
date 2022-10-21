package com.rhbgroup.dcpbo.user.common;


import com.rhbgroup.dcpbo.user.common.model.bo.BoAuditEventConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface BoAuditEventConfigRepository extends JpaRepository<BoAuditEventConfig, Integer> {
	@Query("SELECT x.eventCode from BoAuditEventConfig x WHERE x.functionId = :functionId and x.actionType = :actionType")
	String findEventCodeByFunctionIdAndActionType(@Param("functionId") Integer functionId, @Param("actionType") String actionType);
}
