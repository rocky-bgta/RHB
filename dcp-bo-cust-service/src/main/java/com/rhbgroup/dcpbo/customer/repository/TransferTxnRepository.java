package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;
import com.rhbgroup.dcpbo.customer.model.TransferTxn;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface TransferTxnRepository extends CrudRepository<TransferTxn, Integer> {
	public TransferTxn findOne(Integer refId);

    @Query(value = "select * from TBL_transfer_txn where ref_id = ?1", nativeQuery = true)
    public TransferTxn findByRefId(String refId );

}
