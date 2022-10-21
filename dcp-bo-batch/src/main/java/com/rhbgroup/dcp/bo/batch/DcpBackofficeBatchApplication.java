package com.rhbgroup.dcp.bo.batch;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;

@SpringBootApplication
public class DcpBackofficeBatchApplication {

	static Logger logger=null;
	static Logger jobLogger=null;
	static final String FILE_APPENDER_NAME = "JobLogFileAppender.name";

	public static void main(String[] args) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {

		String batchJobName = "";
		BatchStatus batchStatus = BatchStatus.FAILED;
		Map<String, String> jobInitialArguments = new HashMap<>();
		try {
		
		    logger = LogManager.getLogger(DcpBackofficeBatchApplication.class);
			jobLogger = Logger.getLogger("com.rhbgroup.dcp.bo.batch.log");
		
			if (args.length > 0) {
				jobInitialArguments = initialJobArguments(args);

				batchJobName = setSystemProperty(jobInitialArguments);
			}
			String externalDateStr = System.getenv("BATCH_SYSTEM_DATE_EXTERNAL");
			logger.info("Environment Process Date: " + externalDateStr);
            if (externalDateStr != null && !"".equalsIgnoreCase(externalDateStr)) {
    			logger.info(String.format("getting date from param: %s", externalDateStr));
    			jobInitialArguments.put("ProcessDate", externalDateStr);

    			Date externalDate;
            	externalDate = new SimpleDateFormat("yyyy-MM-dd").parse(externalDateStr);
            	if(externalDate!=null) {
    				logger.info(String.format("getting date from param: %s", externalDateStr));
    				jobInitialArguments.put("ProcessDate", externalDateStr);
            	}
			}
            
			SpringApplication app = new SpringApplication(DcpBackofficeBatchApplication.class);
			app.setBannerMode(Banner.Mode.OFF);
			ConfigurableApplicationContext context = app.run(args);

			DcpBatchApplicationContext dcpBatchApplicationContext = (DcpBatchApplicationContext) context.getBean(DcpBatchApplicationContext.class);
			dcpBatchApplicationContext.setInitialJobArguments(jobInitialArguments);

			JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
			Job job = context.getBean(batchJobName, Job.class);

			JobParametersBuilder jobParametersBuilderparametersBuilder = new JobParametersBuilder();
			for (Map.Entry<String, String> arg : jobInitialArguments.entrySet()) {
				jobParametersBuilderparametersBuilder.addString(arg.getKey(), arg.getValue());
			}

			jobParametersBuilderparametersBuilder.addDate("now", new Date());
			JobParameters jobParameters = jobParametersBuilderparametersBuilder.toJobParameters();

			JobExecution jobExecution = jobLauncher.run(job, jobParameters);
			batchStatus = jobExecution.getStatus();
		} catch (Exception ex) {
			logger.error(ex);
			batchStatus = BatchStatus.FAILED;
		} finally {
			if (batchStatus == BatchStatus.COMPLETED) {
				System.exit(BatchSystemConstant.ExitCode.SUCCESS);
			} else {
				System.exit(BatchSystemConstant.ExitCode.FAILED);
			}
		}
	}
	
	private static String setSystemProperty(Map<String, String> jobInitialArguments) {

		String batchJobName = "";

		if (jobInitialArguments.containsKey(BATCH_JOB_PARAMETER_BATCH_ID_KEY)) {
			String batchId = jobInitialArguments.get(BATCH_JOB_PARAMETER_BATCH_ID_KEY);
			System.setProperty(FILE_APPENDER_NAME, batchId);
		} else if (jobInitialArguments.containsKey(BATCH_JOB_PARAMETER_JOB_NAME_KEY)) {
			batchJobName = jobInitialArguments.get(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
			System.setProperty(FILE_APPENDER_NAME, batchJobName);
		} else
			System.setProperty(FILE_APPENDER_NAME, DcpBackofficeBatchApplication.class.getSimpleName());

		if (jobInitialArguments.containsKey(BATCH_JOB_PARAMETER_JOB_NAME_KEY)) {
			batchJobName = jobInitialArguments.get(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
		} else {
			throw new IllegalArgumentException(BATCH_JOB_PARAMETER_JOB_NAME_KEY + " parameter is required.");
		}

		return batchJobName;
	}

	private static Map<String, String> initialJobArguments(String[] args) {
		Map<String, String> jobInitialArguments = new HashMap<>();
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if (arg.contains("=")) {
					String[] paramKeyValue = arg.split("=");
					String paramKey = paramKeyValue[0];
					String paramValue = paramKeyValue[1];
					
					switch (paramKey.trim()) {
					case BATCH_JOB_PARAMETER_JOB_NAME_KEY:
						jobInitialArguments.put(BATCH_JOB_PARAMETER_JOB_NAME_KEY, paramValue);
						break;

					case BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY:
						jobInitialArguments.put(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, paramValue);
						break;

					case BATCH_JOB_PARAMETER_REPORT_ID_KEY:
						jobInitialArguments.put(BATCH_JOB_PARAMETER_REPORT_ID_KEY, paramValue);
						break;

					case BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY:
						jobInitialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, paramValue);
						break;

					case BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM:
						jobInitialArguments.put(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, paramValue);
						break;

					case BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY:
						jobInitialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY, paramValue);
						break;

					case BATCH_JOB_PARAMETER_BATCH_ID_KEY:
						jobInitialArguments.put(BATCH_JOB_PARAMETER_BATCH_ID_KEY, paramValue);
						break;

					case BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_END_DATE_KEY:
						jobInitialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_END_DATE_KEY, paramValue);
						break;

					case BATCH_JOB_PARAMETER_JOB_BATCH_OFFSET_DAY_KEY:
						jobInitialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_OFFSET_DAY_KEY, paramValue);
						break;

					case BATCH_JOB_PARAMETER_KEY:
						jobInitialArguments.put(BATCH_JOB_PARAMETER_KEY, paramValue);
						break;
						
					 default : break;
					}

				}
			}
		}
		return jobInitialArguments;
	}
}