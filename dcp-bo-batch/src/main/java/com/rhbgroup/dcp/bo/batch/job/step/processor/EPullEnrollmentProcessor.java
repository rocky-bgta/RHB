package com.rhbgroup.dcp.bo.batch.job.step.processor;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BoLoginConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.RestTemplateConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.EAIUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.EpullEnrollmentJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.factory.EPullAutoEnrollmentRequestFactory;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedUserEpullEnrollment;
import com.rhbgroup.dcp.bo.batch.job.model.DcpFDeStateUpd;
import com.rhbgroup.dcp.bo.batch.job.model.EPullAutoEnrollmentDetails;
import com.rhbgroup.dcp.bo.batch.job.repository.EPullAutoEnrollmentRepositoryImpl;
import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentRequest;
import org.apache.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.RECORD_COUNT;

@Component
public class EPullEnrollmentProcessor implements ItemProcessor<BatchStagedUserEpullEnrollment, DcpFDeStateUpd> {

    static final Logger logger = Logger.getLogger(EPullEnrollmentProcessor.class);

    @Autowired
    EPullAutoEnrollmentRepositoryImpl epullEnrollmentRepo;
    @Autowired
    EPullAutoEnrollmentRequestFactory ePullFactory;
    @Autowired
    BoLoginConfigProperties boLoginConfigProperties;
    @Autowired
    RestTemplateConfigProperties restTemplateConfigProperties;
    @Autowired
    @Qualifier("EpullEnrollmentJobConfigProperties")
    private EpullEnrollmentJobConfigProperties jobConfigProperties;

    private RestTemplate restTemplate;
    private JobExecution jobExecution;
    private int recordCount;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        restTemplate = createRestTemplate();
        jobExecution = stepExecution.getJobExecution();
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext().putInt(RECORD_COUNT, recordCount);
    }

    @Override
    public DcpFDeStateUpd process(BatchStagedUserEpullEnrollment user) throws Exception {
        logger.info("EPullEnrollmentProcessor processing..");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss");
        LocalTime localTime = LocalTime.now();

        ePullFactory.setUserId(user.getUserId());
        EPullAutoEnrollmentRequest request = ePullFactory.buildRequest();

        if(request.getSavings().isEmpty() &&
                request.getCurrents().isEmpty() &&
                request.getMca().isEmpty() &&
                request.getTermDeposits().isEmpty() &&
                request.getMortgages().isEmpty() &&
                request.getHirePurchases().isEmpty() &&
                request.getPersonalFinances().isEmpty() &&
                request.getAsb().isEmpty() &&
                request.getCreditCards().isEmpty() &&
                request.getPrepaidCards().isEmpty()) {
            logger.info(String.format("No request found, updating user id [%d] epull_status to 10000", user.getUserId()));
            epullEnrollmentRepo.updateEPullStatus(user.getUserId());
            return null;
        }

        processStatementEnrollment(user.getUserId(), request);

        if(!getTermDepositNotEPullStatementType(user).isEmpty()) {
            DcpFDeStateUpd fileMapping = new DcpFDeStateUpd();
            fileMapping.setBodyIndicator("01");
            fileMapping.setCisNo(epullEnrollmentRepo.getCisNoBy(String.valueOf(user.getUserId())));
            fileMapping.setDate(getFormattedDate());
            fileMapping.setTime(dtf.format(localTime));
            fileMapping.setStatementType("5");
            recordCount++;
            return fileMapping;
        }
        return null;
    }

    private void processStatementEnrollment(int userId, EPullAutoEnrollmentRequest request) throws BatchException {
        String sessionToken = EAIUtils.getSessionToken(boLoginConfigProperties.getUsername(),
                boLoginConfigProperties.getPassword(), boLoginConfigProperties.getApi(), restTemplate).getSessionToken();
        logger.debug("bologin API: " + boLoginConfigProperties.getApi());
        logger.debug("EPull API: " + jobConfigProperties.getRestAPI());

        ResponseEntity<String> response;
        try {

            // Discarding term deposits
            request.setTermDeposits(new ArrayList<>());

             response = EAIUtils.updateEpullEnrollment(String.valueOf(userId),
                    request,
                    jobConfigProperties.getRestAPI(),
                    sessionToken,
                    restTemplate);
             
        } catch (SocketTimeoutException e) {
       	 	logger.error("Read timed out: ", e);
            throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, e.toString());
        } catch (Exception ex) {
            logger.error("Something went wrong: ", ex);
            throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, ex.toString());
        }

        if(Objects.nonNull(response)) {
            logger.info("body: " + response.getBody());
            logger.info("status code: " + response.getStatusCode());
            logger.info("headers: " + response.getHeaders());
        }
    }

    private String getFormattedDate() {
        LocalDate localDate = LocalDate.parse((String) jobExecution.getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY));
        Date date = DateUtils.addDays(java.sql.Date.valueOf(localDate), -1);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    private List<EPullAutoEnrollmentDetails> getTermDepositNotEPullStatementType(BatchStagedUserEpullEnrollment user) throws BatchException {
        String getTermDepositsStatementTypeNotEPull =
                "select prd.deposit_type as accountType, prf.account_no as accountNo, prf.statement_type as statementType " +
                "from dcp.dbo.tbl_deposit_profile prf\n" +
                "join dcp.dbo.tbl_deposit_product prd on prf.deposit_product_id = prd.id\n" +
                "where prd.deposit_type in ('TERM_DEPOSIT') and user_id = ? " ;
        return epullEnrollmentRepo.getEPullAutoEnrollmentAccount(
                user.getUserId(), getTermDepositsStatementTypeNotEPull);
    }

    private RestTemplate createRestTemplate() {
        HttpComponentsClientHttpRequestFactory clientRequestFactory = new HttpComponentsClientHttpRequestFactory();
        // set the timeout
        clientRequestFactory.setConnectTimeout(restTemplateConfigProperties.getConnectTimeout());
        clientRequestFactory.setConnectionRequestTimeout(restTemplateConfigProperties.getConnectionRequestTimeout());
        clientRequestFactory.setReadTimeout(restTemplateConfigProperties.getReadTimeout());
        return new RestTemplate(clientRequestFactory);
    }
}