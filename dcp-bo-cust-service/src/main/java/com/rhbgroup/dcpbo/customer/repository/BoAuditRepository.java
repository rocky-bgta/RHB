package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@BoRepo
@Repository
public interface BoAuditRepository extends JpaRepository<BoAuditModule, Integer>, BoAuditQueryList {

    /**
     * To extract out data for BO Audit.
     * Use switch to enable or disable the criteria.
     *
     * @param functionList
     * @param username
     * @param switchUsername
     * @param selectedDate
     * @param switchDate
     * @param pageable
     * @return
     */
    @Query( value = MAIN_QUERY,
            countQuery = COUNT_QUERY,
            nativeQuery = true)
    Page<BoAuditModule> getBoAudit (
            @Param("functionList") List<Integer> functionList,
            @Param("switchFunction") int switchFunction,
            @Param("username") String username,
            @Param("switchUsername") int switchUsername,
            @Param("selectedDate") String selectedDate,
            @Param("switchDate") int switchDate,
            Pageable pageable);
}

