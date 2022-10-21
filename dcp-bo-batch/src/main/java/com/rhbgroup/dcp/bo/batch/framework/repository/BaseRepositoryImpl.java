package com.rhbgroup.dcp.bo.batch.framework.repository;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class BaseRepositoryImpl {
    @Autowired
    protected DataSource dataSource;
    @Autowired @Getter
    protected JdbcTemplate jdbcTemplate;
}
