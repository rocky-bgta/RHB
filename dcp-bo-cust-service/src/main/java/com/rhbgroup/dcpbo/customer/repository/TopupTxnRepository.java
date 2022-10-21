package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.TopupTxn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface TopupTxnRepository extends JpaRepository<TopupTxn, Integer> {

    @Query(value = "select TOP 1 * from TBL_topup_txn where ref_id = ?1", nativeQuery = true)
    public TopupTxn findByRefId(String refId);

}
