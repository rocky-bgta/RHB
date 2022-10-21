package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.PrepaidReloadFileFromIBKJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKPrepaidConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PrepaidReloadFileFromIBKJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.PrepaidReloadFileFromIBK;
import com.rhbgroup.dcp.bo.batch.job.repository.PrepaidReloadFileFromIBKRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
import org.apache.commons.lang3.StringUtils;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class, PrepaidReloadFileFromIBKJobConfiguration.class})
@ActiveProfiles("test")
public class PrepaidReloadFileFromIBKJobTests extends BaseFTPJobTest {

    public static final String JOB_NAME = "LoadIBKPrepaidReloadJob";
    public static final String JOB_LAUNCHER_UTILS = "PrepaidReloadFileFromIBKJobLauncherTestUtils";
    static final Logger logger = Logger.getLogger(PrepaidReloadFileFromIBKJobTests.class);

    @Lazy
    @Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    DcpBatchApplicationContext dcpBatchApplicationContext;
    @Autowired
    private FTPIBKPrepaidConfigProperties ftpConfigProperties;
    @Autowired
    private PrepaidReloadFileFromIBKJobConfigProperties configProperties;
    @Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolderFullPath;
    @Autowired
    private PrepaidReloadFileFromIBKRepositoryImpl prepaidReloadFileFromIBKRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private Map<String,String> initialArguments;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @Test
    public void testJob() throws Exception {

        //when(prepaidReloadFileFromIBKRepository.addToStaging(Mockito.any(PrepaidReloadFileFromIBK.class))).thenReturn(true);
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", new Date())
                .addString("jobname", JOB_NAME)
                .addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, initialArguments.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY))
                .addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, initialArguments.get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY))
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    @Before
    public void setup() throws Exception {

        // Set processing date to retrieve file/Use batchSystemDate if empty. Example: "2018-08-29"
        String jobProcessDate = "20181002";

        // Initialize dcpBatchApplicationContext for partial failure reprocessing
        initialArguments=new HashMap<String,String>();
        if(!jobProcessDate.isEmpty())
            initialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,jobProcessDate);
        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);

        // Format date to specification
        if (!StringUtils.isEmpty(jobProcessDate))
            jobProcessDate = DateUtils.convertDateFormat(jobProcessDate,DEFAULT_JOB_PARAMETER_DATE_FORMAT, configProperties.getNamedateformat());
        else
            throw new Exception("Error: jobProcessDate not assigned");

        // Set file directories
        String resourcePath = "ftp/nibk_prepaid_to/IBKPrepaidReload_CASA_"+jobProcessDate+".txt";
        try {
            File testFile = getResourceFile(resourcePath);
            String workingDir = System.getProperty("user.dir");
            String testFileFolderPath = generateFolderPath(workingDir, "target", "nibk_prepaid_to");
            uploadFileToFTPFolder(testFile,testFileFolderPath);
            File inputFilePath = new File(inputFolderFullPath + "/" + JOB_NAME);
            File inputFileDir = new File(inputFolderFullPath);
            File inputBatchDir = new File(inputFolderFullPath.replace("/input", ""));

            if(!inputBatchDir.exists()) {
                inputBatchDir.mkdir();
            }
            if(!inputFileDir.exists()) {
                inputFileDir.mkdir();
            }
            if(!inputFilePath.exists()) {
                inputFilePath.mkdir();
            }

        }catch (Exception e){
            throw new Exception("File not found: \"test/resources/" + resourcePath + "\"");
        }
        // Set file directories
        resourcePath = "ftp/nibk_prepaid_to/IBKPrepaidReload_CC_"+jobProcessDate+".txt";
        try {
            File testFile = getResourceFile(resourcePath);
            String workingDir = System.getProperty("user.dir");
            String testFileFolderPath = generateFolderPath(workingDir, "target", "nibk_prepaid_to");
            uploadFileToFTPFolder(testFile,testFileFolderPath);
            File inputFilePath = new File(inputFolderFullPath + "/" + JOB_NAME);
            File inputFileDir = new File(inputFolderFullPath);
            File inputBatchDir = new File(inputFolderFullPath.replace("/input", ""));

            if(!inputBatchDir.exists()) {
                inputBatchDir.mkdir();
            }
            if(!inputFileDir.exists()) {
                inputFileDir.mkdir();
            }
            if(!inputFilePath.exists()) {
                inputFilePath.mkdir();
            }

        }catch (Exception e){
            throw new Exception("File not found: \"test/resources/" + resourcePath + "\"");
        }
    }

    @Test
    public void testNegativeRepository() throws Exception {
        PrepaidReloadFileFromIBK prepaidReloadFileFromIBK = new PrepaidReloadFileFromIBK();
        prepaidReloadFileFromIBK.setJobExecutionId(1);
        prepaidReloadFileFromIBK.setFileName("1");
        prepaidReloadFileFromIBK.setPaymentType("1");
        prepaidReloadFileFromIBK.setTxnTime("1");
        prepaidReloadFileFromIBK.setRefNo("1");
        prepaidReloadFileFromIBK.setHostRefNo("1");
        prepaidReloadFileFromIBK.setMobileNo("1");
        prepaidReloadFileFromIBK.setPrepaidProductCode("1");
        prepaidReloadFileFromIBK.setAmount("1");
        prepaidReloadFileFromIBK.setTxnStatus("1");
        prepaidReloadFileFromIBK.setCreatedTime("1");
        prepaidReloadFileFromIBKRepository.addToStaging(prepaidReloadFileFromIBK);
    }
}