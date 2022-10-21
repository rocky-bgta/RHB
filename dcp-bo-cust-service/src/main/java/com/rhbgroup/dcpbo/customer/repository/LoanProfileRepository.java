package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.LoanProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanProfileRepository extends JpaRepository<LoanProfile, Integer> {
    @Query(value = "SELECT x FROM LoanProfile x WHERE x.id = :accountId")
    public LoanProfile findByAccountId(@Param("accountId") Integer accountId);
}
