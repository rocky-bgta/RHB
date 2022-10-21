package com.rhbgroup.dcpbo.customer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.ProfileFavourite;
import com.rhbgroup.dcpbo.customer.model.UserProfile;

@Repository
public interface ProfileFavouriteRepo extends JpaRepository<ProfileFavourite, Integer> {

	List<ProfileFavourite> findAllByUserId(Integer userId);

	@Query(value = "SELECT x FROM ProfileFavourite x WHERE x.id = :id")
	ProfileFavourite findById(@Param("id") Integer id);

}
