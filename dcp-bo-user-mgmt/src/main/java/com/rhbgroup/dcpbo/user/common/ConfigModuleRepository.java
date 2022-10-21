package com.rhbgroup.dcpbo.user.common;

import com.rhbgroup.dcpbo.user.common.model.bo.ConfigModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface ConfigModuleRepository extends JpaRepository<ConfigModule, Integer> {
}
