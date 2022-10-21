package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.BillerCategory;

@Repository
public interface BillerCategoryRepo extends JpaRepository<BillerCategory, Integer> {

}
