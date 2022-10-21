package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanProductRepository extends JpaRepository<LoanProduct, Integer> {
    @Query(value = "SELECT x FROM LoanProduct x WHERE x.id = :productId")
    public LoanProduct findByProductId(@Param("productId") Integer productId);
}
