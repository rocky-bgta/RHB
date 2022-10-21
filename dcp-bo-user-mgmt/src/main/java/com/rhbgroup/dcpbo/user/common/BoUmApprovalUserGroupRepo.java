package com.rhbgroup.dcpbo.user.common;

import java.util.List;

import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BoUmApprovalUserGroupRepo extends JpaRepository<BoUmApprovalUserGroup, Integer> {

	List<BoUmApprovalUserGroup> findAllByLockingId(String lockingId);
	BoUmApprovalUserGroup findOneByApprovalIdAndLockingId(Integer approvalId, String lockingId);
	
	List<BoUmApprovalUserGroup> findAllByApprovalId(Integer approvalId);

	BoUmApprovalUserGroup findOneByApprovalId(Integer approvalId);

	List<BoUmApprovalUserGroup> findAllByApprovalIdAndLockingId(Integer approvalId, String lockingId);
}
