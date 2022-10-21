package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.UserProfile;

@Repository
public interface UserProfileRepo extends JpaRepository<UserProfile, Integer> {

	UserProfile findOneById(Integer id);
}
