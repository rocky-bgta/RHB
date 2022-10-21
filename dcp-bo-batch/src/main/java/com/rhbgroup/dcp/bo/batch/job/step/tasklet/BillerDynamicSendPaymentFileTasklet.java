package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundConfig;

@Component
@Lazy
public class BillerDynamicSendPaymentFileTasklet implements Tasklet {
	
	@Autowired
	@Qualifier("BillDynamicPaymentConfigOutboundQueue")
    private Queue<BillerDynamicPaymentOutboundConfig> queue ;
	
	 @Autowired
	 private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier("AuditJMSConfig")
	@Lazy
	public JMSConfig auditJMSConfig;

    static final Logger logger = Logger.getLogger(BillerDynamicSendPaymentFileTasklet.class);
    
	@Autowired
	JMSConfig billerPaymentFileJMSConfig;

    String JOB_NAME;
    
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
		try {
			logger.info("Biller Send Payment File Queue Processing Start");

			BillerDynamicPaymentOutboundConfig billerConfig = queue.element();

			String batchSystemDate=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

			Date batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
			String processingDateStr = DateUtils.formatDateString(batchProcessingDate ,DEFAULT_DATE_FORMAT);

			logger.info(String.format("Biller Send Payment File Queue billerCode %s ,%s", billerConfig.getBillerCode(),processingDateStr ));

			String jsonStr= jsonify(billerConfig.getBillerCode(), processingDateStr);

			JMSUtils.sendMessageToJMS(jsonStr, billerPaymentFileJMSConfig);

			logger.info("Biller Send Payment File Queue Processing Completed ;)");
		} catch (Exception ex) {
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.FAILED);
			logger.info( String.format("Tasklet Send Biller Payment File Path exception %s",ex.getMessage() ));
			logger.error(ex);
		}
		return RepeatStatus.FINISHED;
	}
	
	private String jsonify(String billerCode, String paymentFileDate) {
        Map<String, String> jsonObj = new HashMap<>();
        jsonObj.put("billerCode", billerCode);
        jsonObj.put("paymentFileDate", paymentFileDate);

        String jsonString;
        try {
            jsonString = new ObjectMapper().writeValueAsString(jsonObj);
        } catch (JsonProcessingException e) {
        	logger.error(e);
            jsonString = "";
        }
        return jsonString;
    }
}
