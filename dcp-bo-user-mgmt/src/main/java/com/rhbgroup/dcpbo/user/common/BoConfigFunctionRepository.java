package com.rhbgroup.dcpbo.user.common;

import com.rhbgroup.dcpbo.user.common.model.bo.BoConfigFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoConfigFunctionRepository extends JpaRepository<BoConfigFunction, Integer> {

}
