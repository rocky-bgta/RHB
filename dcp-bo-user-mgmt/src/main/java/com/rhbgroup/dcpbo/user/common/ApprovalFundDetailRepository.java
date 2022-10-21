package com.rhbgroup.dcpbo.user.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.user.common.model.bo.ApprovalFundDetail;

@Repository
public interface ApprovalFundDetailRepository extends JpaRepository<ApprovalFundDetail, Integer>{
    
    ApprovalFundDetail findOneByApprovalId(int approvalId);

    @Query("SELECT x from ApprovalFundDetail x WHERE x.approvalId = :approvalId order by state DESC")
    List<ApprovalFundDetail> findByApprovalId(@Param("approvalId") Integer approvalId);
 
}











