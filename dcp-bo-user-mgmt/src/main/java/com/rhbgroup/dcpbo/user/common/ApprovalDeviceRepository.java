package com.rhbgroup.dcpbo.user.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.user.common.model.bo.ApprovalDevice;

@Repository
public interface ApprovalDeviceRepository extends JpaRepository<ApprovalDevice, Integer>{
	
	public ApprovalDevice findByApprovalId(Integer approvalId);

}
