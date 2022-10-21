package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.annotation.BoRepo;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigFunctionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@BoRepo
@Repository
public interface BoConfigFunctionModuleRepository extends JpaRepository<BoConfigFunctionModule, Integer> {

    @Query( value = "select cf.id as id, function_name as functionName , module_id as moduleId, module_name as moduleName \n" +
            "from tbl_bo_config_function cf \n" +
            "left join tbl_bo_config_module cm on cm.id = cf.module_id\n" +
            "where cm.id in (:moduleList)",
            nativeQuery = true)
    List<BoConfigFunctionModule> getBoConfigFunctionModule (@Param("moduleList") List<Integer> moduleList);
}
