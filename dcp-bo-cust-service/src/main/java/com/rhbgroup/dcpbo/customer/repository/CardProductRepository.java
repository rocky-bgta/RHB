package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.CardProduct;


@Repository
public interface CardProductRepository extends JpaRepository<CardProduct, Integer> {
   
	@Query(value = "select * from TBL_CARD_PRODUCT x where LEFT(:cardNo,8) = x.PRODUCT_CODE", nativeQuery = true)
	CardProduct getProductCategory(@Param("cardNo") String cardNo); 
}
