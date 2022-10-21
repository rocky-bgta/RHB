package com.rhbgroup.dcpbo.user.function.repository;

import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FunctionRepository extends JpaRepository<ConfigFunction, Integer> {
	public List<ConfigFunction> findByFunctionNameContainingIgnoreCaseOrderByModuleId(String keyword);
}
