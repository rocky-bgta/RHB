package com.rhbgroup.dcpbo.user.common;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.user.annotations.DcpRepo;
import com.rhbgroup.dcpbo.user.common.model.dcp.UserProfile;

@DcpRepo
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE UserProfile x SET x.txnSigningDevice = NULL, x.tncAcceptedSecureplusVersion = NULL WHERE x.id = :customerId AND x.txnSigningDevice = :deviceId")
    Integer nullifyTxnSigningDevice(@Param("customerId") Integer customerId, @Param("deviceId") Integer deviceId);

    @Query(value = "SELECT x FROM UserProfile x WHERE x.id = :customerId")
    public UserProfile findByCustomerId(@Param("customerId") Integer customerId);

}
