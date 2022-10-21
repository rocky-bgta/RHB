package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.BoLookUp;


@Repository
public interface BoLookupRepository extends JpaRepository<BoLookUp, Integer>{
	
	 @Query(value = "select x from BoLookUp x where x.type = :type AND x.code = :code ")
	 BoLookUp getLookUp(@Param("type")String type, @Param("code")String code);
}
