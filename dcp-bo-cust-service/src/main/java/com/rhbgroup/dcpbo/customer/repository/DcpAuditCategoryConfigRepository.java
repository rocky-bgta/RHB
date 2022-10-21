package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.DcpAuditCategoryConfig;
import io.ebean.BeanRepository;
import io.ebean.EbeanServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DcpAuditCategoryConfigRepository extends JpaRepository<DcpAuditCategoryConfig, Integer> {

    @Query(value = "SELECT x FROM DcpAuditCategoryConfig x")
    List<DcpAuditCategoryConfig> getAll();

}
