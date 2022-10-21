package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_REPORT_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.RunReportTasklet;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.JMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExternalInterfacesCheckTaskletJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKPrepaidConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.repository.UserProfileRepositoryImpl;

@Component
@Lazy
public class ExternalInterfacesCheckWriteDBTasklet implements Tasklet, InitializingBean {

    private static final Logger logger = Logger.getLogger(ExternalInterfacesCheckWriteDBTasklet.class);

    @Autowired
    private ExternalInterfacesCheckTaskletJobConfigProperties jobConfigProperties;

    @Autowired
    private FTPConfigProperties ftpDCPConfigProperties;

    @Autowired
    private FTPIBKConfigProperties ftpIBKConfigProperties;

    @Autowired
    private FTPIBKPrepaidConfigProperties ftpIBKPrepaidConfigProperties;

    @Autowired
    private UserProfileRepositoryImpl userProfileRepositoryImpl;

    @Autowired
    private BatchParameterRepositoryImpl batchParameterRepository;

    @Autowired
    private RunReportTasklet runReportTasklet;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));

        // Task 1: Check DCP and DCPBO DB connectivity
        checkDatabaseAccess();

        return RepeatStatus.FINISHED;
    }

    private void checkDatabaseAccess() {
        logger.info(String.format("Checking DCP & DCPBO DB connectivities isEnabled::[%b]", jobConfigProperties.isEnableDBCheckConnectivity()));
        if(jobConfigProperties.isEnableDBCheckConnectivity()) {
            logger.info("Start checking DCP & DCPBO DB connectivities");
            try {
                logger.info("Getting TOP10 UserProfiles from DCP DB");
                List<Map<String, Object>> userProfiles = userProfileRepositoryImpl.getUserProfiles();
                logger.info(String.format("Result of TOP10 UserProfiles:[%s]", userProfiles));

                Date currentDate = new Date();
                logger.info(String.format("Update [%s] in DCPBO DB BatchConfig table with values [%s]", BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, currentDate));
                jdbcTemplate.setDataSource(dataSource);
                batchParameterRepository.updateBatchSystemDate(currentDate);

                logger.info(String.format("[%s] updated successfully in DCPBO DB BatchConfig table with values [%s]", BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, currentDate));
            } catch (Exception e) {
                logger.error("Failed to check DCP and DCPBO DB connectivity", e);
            }
            logger.info("Finished checking DCP & DCPBO DB connectivities");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Do nothing for now
    }

}
