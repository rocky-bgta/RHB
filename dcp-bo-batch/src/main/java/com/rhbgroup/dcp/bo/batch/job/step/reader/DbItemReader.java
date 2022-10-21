package com.rhbgroup.dcp.bo.batch.job.step.reader;

import com.rhbgroup.dcp.bo.batch.job.step.callback.QueryProvider;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

public class DbItemReader<T> {

    private static final int PAGE_SIZE = 1000;

    public JdbcPagingItemReader<T> getReader(DataSource dataSource, QueryProvider queryProviderFunction, Class<T> objClass) {
        JdbcPagingItemReader<T> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(PAGE_SIZE);
        databaseReader.setQueryProvider(queryProviderFunction.call());
        databaseReader.setRowMapper(new BeanPropertyRowMapper<T>(objClass));
        return databaseReader;
    }

}
