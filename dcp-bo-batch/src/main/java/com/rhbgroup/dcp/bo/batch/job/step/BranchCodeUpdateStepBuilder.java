package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BranchCodeUpdateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.BranchCodeUpdate;
import com.rhbgroup.dcp.bo.batch.job.repository.BranchCodeUpdateRepositoryImpl;
import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
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
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;

@Component
@Lazy
public class BranchCodeUpdateStepBuilder extends BaseStepBuilder {

    static final Logger logger = Logger.getLogger(BranchCodeUpdateStepBuilder.class);

    private static final String STEPNAME = "BranchCodeUpdate";

    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;
    @Autowired
    private BranchCodeUpdateRepositoryImpl branchCodeUpdateRepository;
    @Autowired
    private BranchCodeUpdateJobConfigProperties configProperties;
    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<BranchCodeUpdate> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<BranchCodeUpdate, BatchSuspense> itemProcessor;

    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<BranchCodeUpdate, BatchSuspense>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<BranchCodeUpdate> validationJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) {

        JdbcPagingItemReader<BranchCodeUpdate> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(configProperties.getJdbcpagingpagesize());

        PagingQueryProvider queryProvider = createQueryProvider(stepExecution);
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(BranchCodeUpdate.class));
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider(StepExecution stepExecution) {

        Map<String, String> initialJobArgs = dcpBatchApplicationContext.getInitialJobArguments();
        String jobexecutionid = "";

        if (null != initialJobArgs) {
            jobexecutionid = initialJobArgs.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
        }

        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM vw_batch_branch_code_update_validation");
        if (StringUtils.isEmpty(jobexecutionid)) {
            queryProvider.setWhereClause("WHERE job_execution_id = " + stepExecution.getJobExecutionId());
        } else {
            queryProvider.setWhereClause("WHERE job_execution_id = " + jobexecutionid);
        }

        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("job_execution_id", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BranchCodeUpdate, BatchSuspense> branchCodeUpdateJobProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return branchCodeUpdate -> {

            BatchSuspense batchSuspense = new BatchSuspense();
            try {
                String bnmBranchCode = branchCodeUpdate.getBnmBranchCode();
                String rhbBranchCode = branchCodeUpdate.getRhbBranchCode();
                String rhbBranchName = branchCodeUpdate.getRhbBranchName();
                String rhbBranchAdd1 = branchCodeUpdate.getRhbBranchAdd1();
                String rhbBranchAdd2 = branchCodeUpdate.getRhbBranchAdd2();
                String rhbBranchAdd3 = branchCodeUpdate.getRhbBranchAdd3();
                String bnm = branchCodeUpdate.getBnm();
                String ctrl3 = branchCodeUpdate.getCtrl3();

                if (StringUtils.isEmpty(bnmBranchCode)){
                    batchSuspense.setSuspenseType("ERROR");
                    batchSuspense.setSuspenseColumn("bnm_branch_code");
                    batchSuspense.setSuspenseMessage("Column value \"bnm_branch_code\" should not be null/empty");
                    batchSuspense.setSuspenseRecord(bnmBranchCode+"|"+rhbBranchCode+"|"+rhbBranchName+"|"+rhbBranchAdd1+"|"+rhbBranchAdd2+"|"+rhbBranchAdd3);
                    branchCodeUpdateRepository.addSuspense(stepExecution,batchSuspense);
                    stepExecution.getJobExecution().setStatus(BatchStatus.FAILED);
                }
                else if(StringUtils.isEmpty(rhbBranchCode)){
                    batchSuspense.setSuspenseType("ERROR");
                    batchSuspense.setSuspenseColumn("rhb_branch_code");
                    batchSuspense.setSuspenseMessage("Column value \"rhb_branch_code\" should not be null/empty");
                    batchSuspense.setSuspenseRecord(bnmBranchCode+"|"+rhbBranchCode+"|"+rhbBranchName+"|"+rhbBranchAdd1+"|"+rhbBranchAdd2+"|"+rhbBranchAdd3);
                    branchCodeUpdateRepository.addSuspense(stepExecution,batchSuspense);
                    stepExecution.getJobExecution().setStatus(BatchStatus.FAILED);
                }
                else if(bnmBranchCode.equalsIgnoreCase(bnm) && rhbBranchCode.equalsIgnoreCase(ctrl3)){
                    batchSuspense.setSuspenseType("WARNING");
                    batchSuspense.setSuspenseColumn("bnm_branch_code, rhb_branch_code");
                    batchSuspense.setSuspenseMessage("Data is not updated due to combination data of bnm_branch_code: \""+bnmBranchCode+"\" and rhb_branch_code: \""+rhbBranchCode+"\" already exist in TBL_BNM_CTRL3");
                    batchSuspense.setSuspenseRecord(bnmBranchCode+"|"+rhbBranchCode+"|"+rhbBranchName+"|"+rhbBranchAdd1+"|"+rhbBranchAdd2+"|"+rhbBranchAdd3);
                    branchCodeUpdateRepository.addSuspense(stepExecution,batchSuspense);
                    branchCodeUpdateRepository.updateIsProcessed(stepExecution.getJobExecutionId().toString(),bnmBranchCode, true);
                    stepExecution.getJobExecution().setStatus(BatchStatus.FAILED);
                }
                else if(bnmBranchCode.equalsIgnoreCase(bnm)){
                    branchCodeUpdateRepository.updateBranchCode(branchCodeUpdate);
                    branchCodeUpdateRepository.updateIsProcessed(stepExecution.getJobExecutionId().toString(),bnmBranchCode, true);
                }
                else{
                    branchCodeUpdateRepository.insertBranchCode(branchCodeUpdate);
                    branchCodeUpdateRepository.updateIsProcessed(stepExecution.getJobExecutionId().toString(),bnmBranchCode, true);
                }
            }
            catch (Exception ex)
            {
                batchSuspense.setSuspenseType("EXCEPTION");
                batchSuspense.setSuspenseColumn("N/A");
                batchSuspense.setSuspenseMessage(ex.getMessage());
                branchCodeUpdateRepository.addSuspense(stepExecution,batchSuspense);
                logger.error(ex);
            }
            return batchSuspense;
        };
    }
}
