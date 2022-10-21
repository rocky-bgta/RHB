package com.rhbgroup.dcpbo.user.common;

import com.rhbgroup.dcpbo.user.common.model.bo.ConfigDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigDepartmentRepository extends JpaRepository<ConfigDepartment, Integer>{
}
