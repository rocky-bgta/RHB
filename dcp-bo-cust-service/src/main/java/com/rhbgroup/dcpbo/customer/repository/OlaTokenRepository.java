package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.OlaToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OlaTokenRepository extends JpaRepository<OlaToken, Integer> {
    OlaToken findFirstByNameLikeOrIdNoLikeOrMobileNoOrUsernameOrderByUpdatedTimeDesc(String name, String idNo, String MobileNo, String username);
    OlaToken findFirstByIdNoOrderByUpdatedTimeDesc(String idNo);
    Page<OlaToken> findByIdNoAndCreatedTimeAfterAndCreatedTimeBeforeOrderByCreatedTimeDesc(String idNo, String fromDate, String toDate, Pageable pageable);
    OlaToken findFirstByToken(String token);
}
