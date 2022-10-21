package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigGeneric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@BoRepo
@Repository
public interface BoConfigGenericRepository extends JpaRepository<BoConfigGeneric, Integer> {
    BoConfigGeneric findFirstByConfigTypeAndConfigCode(String configType, String configCode);

    @Query(value = "select * from dcpbo.dbo.tbl_bo_config_generic where config_type = :configType and config_code =:configCode", nativeQuery = true)
    BoConfigGeneric getConfigGenericBy(@Param("configType") String configType,
                                       @Param("configCode") String configCode);

}