package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    @Query(value = "SELECT x FROM UserProfile x WHERE x.id = :customerId")
    public UserProfile findByCustomerId(@Param("customerId") Integer customerId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE UserProfile SET FAILED_CHALLENGE_COUNT = 0, USER_STATUS = :userStatus WHERE ID = :customerId")
    public Integer updateProfileStatus(@Param("customerId") Integer customerId, @Param("userStatus") String userStatus);

    @Transactional
    @Modifying
    @Query(value = "UPDATE UserProfile x SET x.txnSigningDevice = NULL, x.tncAcceptedSecureplusVersion = NULL WHERE x.id = :customerId AND x.txnSigningDevice = :deviceId")
    Integer nullifyTxnSigningDevice(@Param("customerId") Integer customerId, @Param("deviceId") Integer deviceId);
    
    @Query(value = "select x from UserProfile x where ID = :id ")
    public UserProfile getUserProfile(@Param("id") Integer id);
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE UserProfile x SET x.failedLoginCount = 0, x.userStatus = 'A' WHERE x.id = :id ")
    Integer updateFailedLoginCount(@Param("id") Integer id);
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE UserProfile x SET x.failedChallengeCount = 0, x.userStatus = 'A' WHERE x.id = :id ")
    Integer updateFailedChallengeCount(@Param("id") Integer id);

    UserProfile findByUsername(String username);
}
