package com.rhbgroup.dcp.bo.batch.test.job;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.PremierCustomerInfoandRMCodeTaggingJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.repository.PremierCustomerInfoandRMCodeTaggingRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class, PremierCustomerInfoandRMCodeTaggingJobConfiguration.class})
@ActiveProfiles("test")
public class PremierCustomerInfoandRMCodeTaggingJobTest extends BaseFTPJobTest {

	public static final String JOB_NAME = "PremierCustomerInfoandRMCodeTaggingJob";
	public static final String JOB_LAUNCHER_UTILS = "PremierCustomerInfoandRMCodeTaggingJobLauncherTestUtils";

	@Autowired
    DcpBatchApplicationContext dcpBatchApplicationContext;

    @Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolderFullPath;

    @Lazy
    @Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private FTPConfigProperties ftpConfigProperties;

    @Autowired
    private BatchParameterRepositoryImpl batchParameterRepository;

    @MockBean
    private PremierCustomerInfoandRMCodeTaggingRepositoryImpl premierCustomerInfoandRMCodeTaggingRepository;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    private Map<String,String> initialArguments;

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", new Date())
                .addString("jobname", JOB_NAME)
                //.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, initialArguments.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY))
                .addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, initialArguments.get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY))
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    @Test
    public void testJobNegativeTrailerDoNotMatch() throws Exception {

        String jobprocessdate = "20180913";

        if(!jobprocessdate.isEmpty())
            initialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,jobprocessdate);

        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);

        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", new Date())
                .addString("jobname", JOB_NAME)
                //.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, initialArguments.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY))
                .addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals(JOB_FAILED, jobExecution.getExitStatus().getExitCode());
    }

    @Before
    public void prepTest() throws Exception {

        List<BatchParameter> batchParameters = batchParameterRepository.getBatchParametres();
        String jobprocessdate = "";

        for (BatchParameter bp: batchParameters)
        {
            if(bp.getName().equalsIgnoreCase(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY))
                jobprocessdate = bp.getValue();
        }

        // Set the psvm args set it manually here if date is not set it will use the system date
        jobprocessdate = "20180914";
        String jobExecutionId = "";

        initialArguments = new HashMap<String,String>();
        if(!jobExecutionId.isEmpty())
            initialArguments.put(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY,jobExecutionId);
        if(!jobprocessdate.isEmpty())
            initialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,jobprocessdate);

        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);

        String resourcePath = "ftp/DCP_PREMIER_RMCODE_TO/DCP_RMCODE_D-"+jobprocessdate+".txt";

        File testFile = getResourceFile(resourcePath);

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

        String workingDir = System.getProperty("user.dir");
        FTPUtils.createFTPFolderIfNotExists( generateFolderPath(workingDir, "target", "DCP_PREMIER_RMCODE_TO"), ftpConfigProperties);
        String testFileFolderPath = generateFolderPath(workingDir, "target", "DCP_PREMIER_RMCODE_TO");
        uploadFileToFTPFolder (testFile, testFileFolderPath);

        prepNegativeFile();
    }

    private void prepNegativeFile() throws BatchValidationException, ParseException, FileNotFoundException {
        String jobprocessdate = "2018-09-13";
        jobprocessdate = DateUtils.convertDateFormat(jobprocessdate, DEFAULT_DATE_FORMAT, DEFAULT_JOB_PARAMETER_DATE_FORMAT);
        String resourcePath = "ftp/DCP_PREMIER_RMCODE_TO/DCP_RMCODE_D-"+jobprocessdate+".txt";

        File testFile = getResourceFile(resourcePath);

        String workingDir = System.getProperty("user.dir");
        FTPUtils.createFTPFolderIfNotExists( generateFolderPath(workingDir, "target", "DCP_PREMIER_RMCODE_TO"), ftpConfigProperties);
        String testFileFolderPath = generateFolderPath(workingDir, "target", "DCP_PREMIER_RMCODE_TO");
        uploadFileToFTPFolder (testFile, testFileFolderPath);
    }
}