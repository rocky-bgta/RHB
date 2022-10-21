package com.rhbgroup.dcpbo.system.downtime.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.system.model.BoDowntimeAdhocType;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface BoDowntimeAdhocTypeRepository extends JpaRepository<BoDowntimeAdhocType, Integer> {

    @Query(value = "SELECT DISTINCT(x.adhocType) FROM BoDowntimeAdhocType x")
    List<String> getAllAdhocTypes();
    
    @Query(value = "SELECT DISTINCT(x.adhocTypeName) FROM BoDowntimeAdhocType x")
    List<String> getAllAdhocTypeNames();
    
    @Query(value = "SELECT DISTINCT(x.adhocTypeCategory) FROM BoDowntimeAdhocType x order by x.adhocTypeCategory desc")
    List<String> getAllAdhocCategoryTypes();
    
    @Query(value = "SELECT DISTINCT(x.adhocType) FROM BoDowntimeAdhocType x Where x.adhocTypeCategory = :category")
    List<String> getAdhocTypeByCategory(@Param("category") String category);
    
    @Query(value = "SELECT DISTINCT(x.adhocTypeName) FROM BoDowntimeAdhocType x Where x.adhocTypeCategory = :category")
    List<String> getAdhocTypeNameByCategory(@Param("category") String category);

    @Query(value = "FROM BoDowntimeAdhocType x Where x.adhocTypeCategory = :category")
    List<BoDowntimeAdhocType> getDownTimeAdhocTypeByCategory(@Param("category") String category);

}
