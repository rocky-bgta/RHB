package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.job.model.SnapshotBoUsersGroup;
import com.rhbgroup.dcp.bo.batch.job.repository.SnapshotBoUsersGroupRepositoryImpl;
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

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_TIME_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Lazy
public class SnapshotBoUsersStepBuilder  extends BaseStepBuilder {
    static final Logger logger = Logger.getLogger(SnapshotBoUsersStepBuilder.class);
    static final String CREATED_BY = "SnapshotBoUsersGroupJob (LDCPM5004B)";
    private static final String STEPNAME = "SnapshotBoUsersStepBuilder";

    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    @Value("${job.snapshotbousersgroupjob.chunksize}")
    private int chunkSize;
    @Value("${job.snapshotbousersgroupjob.jdbcpagingpagesize}")
    private int jdbcPagingPageSize;

    @Autowired
    private SnapshotBoUsersGroupRepositoryImpl snapshotBoUsersGroupRepository;
    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<SnapshotBoUsersGroup> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<SnapshotBoUsersGroup, SnapshotBoUsersGroup> itemProcessor;
    @Autowired
    @Qualifier(STEPNAME + ".ItemWriter")
    private ItemWriter<SnapshotBoUsersGroup> itemWriter;

    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<SnapshotBoUsersGroup, SnapshotBoUsersGroup>chunk(chunkSize)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<SnapshotBoUsersGroup> snapshotBoUsersJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) {
        JdbcPagingItemReader<SnapshotBoUsersGroup> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(jdbcPagingPageSize);

        PagingQueryProvider queryProvider = createQueryProvider();
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(SnapshotBoUsersGroup.class));
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT dept_name,user_id,user_name,user_group,role,status,user_created_date,user_created_time,user_updated_time,user_updated_by,last_login_date,last_login_time");
        queryProvider.setFromClause("FROM vw_snapshot_bo_user_profile_access");
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("dept_name", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<SnapshotBoUsersGroup, SnapshotBoUsersGroup> extractCustomerProfileJobProcessor() {
        return snapshotBoUsers -> snapshotBoUsers;
    }

    @Bean(STEPNAME + ".ItemWriter")
    @StepScope
    public ItemWriter<SnapshotBoUsersGroup> snapshotBoUsersItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        return records -> {

            int jobExecutionId = stepExecution.getJobExecution().getId().intValue();
            String createdTime = new SimpleDateFormat(DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT).format(new Date());

            for (SnapshotBoUsersGroup record : records) {
                record.setJobExecutionId(jobExecutionId);
                record.setCreatedTime(createdTime);
                record.setCreatedBy(CREATED_BY);
                snapshotBoUsersGroupRepository.insertBoUser(record);
            }
        };
    }
}
