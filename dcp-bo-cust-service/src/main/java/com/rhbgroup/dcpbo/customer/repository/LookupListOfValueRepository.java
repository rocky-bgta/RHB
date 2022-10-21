package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LookupListOfValueRepository{

    @Autowired
    JdbcTemplate jdbcTemplate;

    public final static String QUERY_LIST_OF_VALUE = "SELECT TOP 1 DESCRIPTION_EN FROM TBL_LOOKUP_LIST_OF_VALUE WHERE type = ? AND code= ?";

    public String findDescriptionByTypeAndCode(String type, String code){
        return jdbcTemplate.queryForObject(QUERY_LIST_OF_VALUE, new Object[] {type, code},String.class  );
    }
}
