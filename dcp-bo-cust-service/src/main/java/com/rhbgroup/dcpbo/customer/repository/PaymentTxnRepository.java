package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.PaymentTxn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface PaymentTxnRepository extends JpaRepository<PaymentTxn, Integer> {
    @Query(value = "select * from TBL_payment_txn where ref_id = ?1", nativeQuery = true)
    public PaymentTxn findByRefId(String refId);
}
