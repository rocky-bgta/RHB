package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    @Query(value = "SELECT x.countryName FROM Country x WHERE x.countryCode = :countryCode")
    public String findCountryNameByCountryCode(@Param("countryCode") String countryCode);
}
