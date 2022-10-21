package com.rhbgroup.dcpbo.user.common;

import com.rhbgroup.dcpbo.user.common.model.bo.ConfigModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigModuleRepo extends JpaRepository<ConfigModule, Integer> {

}