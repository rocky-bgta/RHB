package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.LookupStatus;

import java.util.List;

@Repository
public interface LookupStatusRepository extends JpaRepository<LookupStatus, Integer> {
    
    @Query(value = "select count(*) from TBL_LOOKUP_STATUS where code = ?1 AND status_type = 'success'", nativeQuery = true)
    public Integer getSuccessStatusCount(String statusCode);

    @Query(value = "select code from LookupStatus where statusType not like ('success') ")
    public List<Integer> getEventCodesByFailStatus();

    @Query(value = "select code from LookupStatus where statusType like ('success') ")
    public List<Integer> getEventCodesBySuccessStatus();

    @Query(value = "select code from LookupStatus")
    public List<Integer> getEventCodes();
}
