package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
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

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.GSTCentralizedFileUpdateJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.repository.GSTCentralizedFileUpdateRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class, GSTCentralizedFileUpdateJobConfiguration.class})
@ActiveProfiles("test")
public class GSTCentralizedFileUpdateJobTests extends BaseFTPJobTest {

	public static final String JOB_NAME = "GSTCentralizedFileUpdateJob";
	public static final String JOB_LAUNCHER_UTILS = "GSTCentralizedFileUpdateJobLauncherTestUtils";

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
    private GSTCentralizedFileUpdateRepositoryImpl gstCentralizedFileUpdateRepository;

    private Map<String,String> initialArguments;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

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
         jobprocessdate = "20180830";
        String jobExecutionId = "";


        initialArguments = new HashMap<String,String>();
        if(!jobExecutionId.isEmpty())
            initialArguments.put(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY,jobExecutionId);
        if(!jobprocessdate.isEmpty())
            initialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,jobprocessdate);

        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);
        
        String resourcePath = "ftp/GST_DCP_TO/GST_DCP_1B-"+jobprocessdate+".txt";

        File testFile = getResourceFile(resourcePath);

        String workingDir = System.getProperty("user.dir");

        FTPUtils.createFTPFolderIfNotExists(generateFolderPath(workingDir, "target", "GST_DCP_TO"), ftpConfigProperties);

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

        String testFileFolderPath = generateFolderPath(workingDir, "target", "GST_DCP_TO");
        uploadFileToFTPFolder (testFile, testFileFolderPath);
    }
}