package com.rhbgroup.dcpbo.user.common;

import com.rhbgroup.dcpbo.user.common.model.bo.UmApprovalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UmApprovalUserRepository extends JpaRepository<UmApprovalUser, Integer> {
	UmApprovalUser findOneByApprovalId(int approvalId);
}
