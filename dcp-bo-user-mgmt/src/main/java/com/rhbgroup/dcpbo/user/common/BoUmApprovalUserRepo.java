package com.rhbgroup.dcpbo.user.common;

import java.util.List;

import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoUmApprovalUserRepo extends JpaRepository<BoUmApprovalUser, Integer> {

	List<BoUmApprovalUser> findAllByLockingId(String lockingId);

	BoUmApprovalUser findOneByApprovalIdAndLockingId(Integer approvalId, String lockingId);

	List<BoUmApprovalUser> findAllByApprovalId(Integer approvalId);

	BoUmApprovalUser findOneByApprovalId(Integer approvalId);

	List<BoUmApprovalUser> findAllByApprovalIdAndLockingId(Integer approvalId, String lockingId);
}
