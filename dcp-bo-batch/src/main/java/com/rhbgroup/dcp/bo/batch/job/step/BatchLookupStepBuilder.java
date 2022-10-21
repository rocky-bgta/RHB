package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.job.model.BatchLookup;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@Lazy
public class BatchLookupStepBuilder extends BaseStepBuilder {

    static final Logger logger = Logger.getLogger(BatchLookupStepBuilder.class);

    private static final String STEPNAME = "BatchLookup";
    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<BatchLookup> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<BatchLookup, BatchLookup> itemProcessor;
    @Autowired
    @Qualifier(STEPNAME + ".ItemWriter")
    private ItemWriter<BatchLookup> itemWriter;

    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<BatchLookup, BatchLookup>chunk(1000)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<BatchLookup> lookupJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) {
        JdbcPagingItemReader<BatchLookup> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(1000);

        PagingQueryProvider queryProvider = createQueryProvider();
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(BatchLookup.class));
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM TBL_BATCH_LOOKUP");
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("id", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchLookup, BatchLookup> batchLookupJobProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return new ItemProcessor<BatchLookup, BatchLookup>() {
            @Override
            public BatchLookup process(BatchLookup batchLookup) throws Exception {
                return batchLookup;
            }
        };
    }

    @Bean(STEPNAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BatchLookup> updateCustomerProfileJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        return messages -> {

            ArrayList<BatchLookup> lookups = new ArrayList<>();
            for (BatchLookup message : messages) {
                lookups.add(message);
            }
            stepExecution.getJobExecution().getExecutionContext().put("batchLookup",lookups);

        };
    }
}