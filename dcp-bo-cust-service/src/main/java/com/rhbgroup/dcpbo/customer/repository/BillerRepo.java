package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.Biller;

@Repository
public interface BillerRepo extends JpaRepository<Biller, Integer> {

    @Query(value = "SELECT x FROM Biller x WHERE x.id = :id")
    Biller findById(@Param("id") Integer id);
    
    @Query(value = "SELECT Count(x.id) FROM Biller x where x.status in ('ACTIVE','SUSPENDED')")
    Integer getCount();
}
