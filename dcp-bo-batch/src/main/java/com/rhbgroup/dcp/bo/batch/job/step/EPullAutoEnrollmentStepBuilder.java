package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedUserEpullEnrollment;
import com.rhbgroup.dcp.bo.batch.job.model.DcpFDeStateUpd;
import com.rhbgroup.dcp.bo.batch.job.step.callback.QueryProvider;
import com.rhbgroup.dcp.bo.batch.job.step.processor.EPullEnrollmentProcessor;
import com.rhbgroup.dcp.bo.batch.job.step.reader.DbItemReader;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
@Lazy
public class EPullAutoEnrollmentStepBuilder extends BaseStepBuilder {

    static final Logger logger = Logger.getLogger(EPullAutoEnrollmentStepBuilder.class);

    private static final String STEPNAME = "EPullAutoEnrollmentStepBuilder";
    private static final int CHUNK_SIZE = 10000;

    @Autowired
    @Qualifier("DBReader.ItemReader")
    ItemReader<BatchStagedUserEpullEnrollment> itemReader;

    @Autowired
    EPullEnrollmentProcessor itemProcessor;

    @Autowired
    @Qualifier("EPullEnrollmentWriter")
    ItemWriter<DcpFDeStateUpd> itemWriter;

    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<BatchStagedUserEpullEnrollment, DcpFDeStateUpd>chunk(CHUNK_SIZE)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean("DBReader.ItemReader")
    public JdbcPagingItemReader<BatchStagedUserEpullEnrollment> dbReader(DataSource dataSource) {
        DbItemReader<BatchStagedUserEpullEnrollment> reader = new DbItemReader<>();
        return reader.getReader(dataSource, queryProviderFunction, BatchStagedUserEpullEnrollment.class);
    }

    private QueryProvider queryProviderFunction = () -> {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("select user_id");
        queryProvider.setFromClause("from tbl_batch_staged_user_epull_enrollment");

        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("user_id", Order.ASCENDING);
        queryProvider.setSortKeys(sortConfiguration);
        logger.debug("select user_id from tbl_batch_staged_user_epull_enrollment");
        return queryProvider;
    };
}


