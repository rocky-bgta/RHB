package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.OlaTokenGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OlaTokenGroupRepository extends JpaRepository<OlaTokenGroup, Integer> {

    @Query("select new com.rhbgroup.dcpbo.customer.model.OlaTokenGroup(x.name, x.idType, x.idNo, x.username) " +
            "from OlaToken x " +
            "where x.mobileNo like %:value% " +
            "or x.name like %:value% " +
            "or x.idNo like %:value% " +
            "or x.username like %:value% " +
            "group by x.name, x.idType, x.idNo, x.username")
    Page<OlaTokenGroup> fetchGroupOlaToken(@Param("value") String value, Pageable pageable);

}
