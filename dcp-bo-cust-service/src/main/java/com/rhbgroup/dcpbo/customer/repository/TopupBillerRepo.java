package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.TopupBiller;

@Repository
public interface TopupBillerRepo extends JpaRepository<TopupBiller, Integer> {

    @Query(value = "SELECT x FROM TopupBiller x WHERE x.id = :id")
    TopupBiller findById(@Param("id") Integer id);
    
    @Query(value = "SELECT Count(x.id) FROM TopupBiller x where x.status in ('ACTIVE','SUSPENDED')")
    Integer getCount();

}
