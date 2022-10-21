package com.rhbgroup.dcpbo.customer.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.CardProfile;

@Repository
public interface CardProfileRepository extends JpaRepository<CardProfile, Integer> {
	
	   @Transactional
	    @Modifying
	    @Query(value = "UPDATE CardProfile x SET x.failedCardTpinCount = 0 WHERE x.userId = :id ")
	    Integer updateFailedCardTpinCount(@Param("id") Integer id);
	   
	    @Query(value = "select x from CardProfile x where x.failedCardTpinCount  >= :tpinFailCount and x.userId =:userId ")
	    List<CardProfile> getCardProfile(@Param("tpinFailCount") Integer tpinFailCount,@Param("userId") Integer userId);
		
}
