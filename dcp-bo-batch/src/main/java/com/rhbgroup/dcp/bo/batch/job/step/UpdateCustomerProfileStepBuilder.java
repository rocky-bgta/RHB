package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.UpdateCustomerProfileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchLookup;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.NewOldCustomerProfile;
import com.rhbgroup.dcp.bo.batch.job.repository.UpdateCustomerProfileRepositoryImpl;
import org.apache.log4j.Logger;
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

import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;

@Component
@Lazy
public class UpdateCustomerProfileStepBuilder extends BaseStepBuilder {

    static final Logger logger = Logger.getLogger(UpdateCustomerProfileStepBuilder.class);

    private static final String STEPNAME = "UpdateCustomerProfile";

    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;
    @Autowired
    private UpdateCustomerProfileRepositoryImpl updateCustomerProfileRepository;
    @Autowired
    private UpdateCustomerProfileJobConfigProperties configProperties;
    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<NewOldCustomerProfile> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<NewOldCustomerProfile, BatchSuspense> itemProcessor;

    @Override
    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<NewOldCustomerProfile, BatchSuspense >chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<NewOldCustomerProfile> stagingJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) {

        JdbcPagingItemReader<NewOldCustomerProfile> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(configProperties.getJdbcpagingpagesize());

        PagingQueryProvider queryProvider = createQueryProvider(stepExecution);
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(NewOldCustomerProfile.class));
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider(StepExecution stepExecution) {

        Map<String,String> initialJobArgs = dcpBatchApplicationContext.getInitialJobArguments();
        String jobexecutionid="";
        jobexecutionid = initialJobArgs.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM vw_batch_update_cust_profile_validation");
        if(StringUtils.isEmpty(jobexecutionid)) {
            queryProvider.setWhereClause("WHERE job_execution_id = " + stepExecution.getJobExecutionId());
        }else{
            queryProvider.setWhereClause("WHERE job_execution_id = " + jobexecutionid);
        }

        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("user_id", Order.ASCENDING);
        return sortConfiguration;
    }

    //Perform validation with lookup & update columns in tbl_user_profile
    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<NewOldCustomerProfile, BatchSuspense> updateCustomerProfileJobProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return newOldCustomerProfile -> {
            BatchSuspense batchSuspense=null;

            boolean isPassedStaffIndicator=false;
            boolean isPassedBirthDate=false;
            String cisNo = newOldCustomerProfile.getNew_cis_no();

            try {
                batchSuspense = new BatchSuspense();

                String birthDate = newOldCustomerProfile.getNew_birth_date();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                birthDate = getFormattedBirthDate(birthDate, formatter);
                newOldCustomerProfile.setNew_birth_date(birthDate);

                if (StringUtils.isEmpty(cisNo)){
                    batchSuspense.setSuspenseType("ERROR");
                    batchSuspense.setSuspenseColumn("cis_no");
                    batchSuspense.setSuspenseMessage("Column value \"cis_no\" should not be null/empty");
                    batchSuspense.setSuspenseRecord(newOldCustomerProfile.getNew_cis_no()+"|"+newOldCustomerProfile.getNew_race()+"|"+newOldCustomerProfile.getNew_birth_date()+"|"+newOldCustomerProfile.getNew_gender());
                    updateCustomerProfileRepository.addSuspense(stepExecution,batchSuspense);
                    logger.error(batchSuspense);
                    stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE));
                }
                else {
                    if (newOldCustomerProfile.getNew_birth_date().trim() != newOldCustomerProfile.getOld_birth_date().trim() || newOldCustomerProfile.getNew_is_staff() != newOldCustomerProfile.getOld_is_staff()) {
                        ArrayList<BatchLookup> lookups = (ArrayList<BatchLookup>) stepExecution.getJobExecution().getExecutionContext().get("batchLookup");

                        isPassedStaffIndicator = isPassedStaffIndicator(newOldCustomerProfile, isPassedStaffIndicator, lookups);

                        processStep(stepExecution, newOldCustomerProfile, batchSuspense, isPassedStaffIndicator, isPassedBirthDate);
                    }
                    else {
                        updateCustomerProfileRepository.updateIsProcessed(stepExecution.getJobExecutionId().toString(), newOldCustomerProfile.getNew_cis_no(), true);
                    }
                }
            }
            catch (Exception e)
            {
                logger.error(e);
                batchSuspense.setSuspenseColumn("N/A");
                batchSuspense.setSuspenseType("EXCEPTION");
                batchSuspense.setSuspenseMessage(e.getMessage());
                updateCustomerProfileRepository.addSuspense(stepExecution,batchSuspense);
                stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE));
            }
            return batchSuspense;
        };
    }

    private void processStep(StepExecution stepExecution, NewOldCustomerProfile newOldCustomerProfile, BatchSuspense batchSuspense, boolean isPassedStaffIndicator, boolean isPassedBirthDate) {
        if (isValidDate(newOldCustomerProfile.getNew_birth_date())) {
            isPassedBirthDate = true;
        } else {
            batchSuspense.setSuspenseType("WARNING");
            batchSuspense.setSuspenseColumn("birth_date");
            batchSuspense.setSuspenseMessage("Column value \"" + newOldCustomerProfile.getNew_birth_date() + "\" is not a valid date");
            batchSuspense.setSuspenseRecord(newOldCustomerProfile.getNew_cis_no() + "|" + newOldCustomerProfile.getNew_race() + "|" + newOldCustomerProfile.getNew_birth_date() + "|" + newOldCustomerProfile.getNew_gender());
            updateCustomerProfileRepository.addSuspense(stepExecution, batchSuspense);
            logger.error(batchSuspense);
            stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE));
        }

        if (!isPassedStaffIndicator) {
            String table = "TBL_BATCH_STAGED_UPDATE_CUST_PROFILE.staff_indicator";
            String group = "DCP_CUSTOMER_IS_STAFF";
            batchSuspense.setSuspenseType("WARNING");
            batchSuspense.setSuspenseColumn("staff_indicator");
            batchSuspense.setSuspenseMessage("Lookup for column value \"" + newOldCustomerProfile.getNew_is_staff() + "\" in table \"" + table + "\" where group = \"" + group + "\" failed");
            batchSuspense.setSuspenseRecord(newOldCustomerProfile.getNew_cis_no() + "|" + newOldCustomerProfile.getNew_race() + "|" + newOldCustomerProfile.getNew_birth_date() + "|" + newOldCustomerProfile.getNew_gender());
            updateCustomerProfileRepository.addSuspense(stepExecution, batchSuspense);
            logger.error(batchSuspense);
            stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE));
        }

        if (isPassedBirthDate && isPassedStaffIndicator) {
            updateCustomerProfileRepository.updateCustomerProfile(stepExecution, newOldCustomerProfile);
            updateCustomerProfileRepository.updateIsProcessed(stepExecution.getJobExecutionId().toString(), newOldCustomerProfile.getNew_cis_no(), true);
        }
    }

    private boolean isPassedStaffIndicator(NewOldCustomerProfile newOldCustomerProfile, boolean isPassedStaffIndicator, ArrayList<BatchLookup> lookups) {
        for (BatchLookup lookup : lookups) {
            if (lookup.getGroup().equalsIgnoreCase("DCP_CUSTOMER_IS_STAFF") && lookup.getValue().equalsIgnoreCase(newOldCustomerProfile.getNew_is_staff())) {
                isPassedStaffIndicator = true;
            }
        }
        return isPassedStaffIndicator;
    }

    private String getFormattedBirthDate(String birthDate, SimpleDateFormat formatter) {
        try {
            Date initDate = new SimpleDateFormat("dd/MM/yyyy").parse(birthDate);
            birthDate = formatter.format(initDate);
        }catch (Exception e){
            logger.error(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE, e));
        }
        return birthDate;
    }

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (Exception pe) {
            logger.error(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE, pe));
            return false;
        }
        return true;
    }
}

