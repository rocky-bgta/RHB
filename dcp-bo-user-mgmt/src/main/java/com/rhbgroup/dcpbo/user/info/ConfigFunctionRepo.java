package com.rhbgroup.dcpbo.user.info;

import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigFunctionRepo extends JpaRepository<ConfigFunction, Integer> {

}
