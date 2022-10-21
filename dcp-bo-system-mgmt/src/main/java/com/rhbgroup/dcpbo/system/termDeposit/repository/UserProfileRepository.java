package com.rhbgroup.dcpbo.system.termDeposit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.annotations.DcpRepo;
import com.rhbgroup.dcpbo.system.model.UserProfile;

@DcpRepo
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer>{

	@Query(value = "SELECT x FROM UserProfile x where x.id = :userId ")
	UserProfile getProfileByUserId(@Param("userId") Integer userId);
}
