package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ASNBReconSettlementJobParameter.ASNB_OUTPUT_FILE_LIST_SETTLEMENT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ASNBReconSettlementJobParameter.ASNB_OUTPUT_FILE_LIST_RECON;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.DoubleFunction;
import java.util.regex.Pattern;


import com.rhbgroup.dcp.bo.batch.framework.utils.ASNBUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ASNBReconSettlementJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.AsnbRepository;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.DailyAutoSettlementJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.DailyDIBAutoSettlementJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.DailyDMBAutoSettlementJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbBatch;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbBatchDetails;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbSuccessList;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbVarianceDetails;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbHelperDTO;

@Component
@Lazy
public class RunAsnbReportTasklet extends BaseRepositoryImpl implements Tasklet, InitializingBean {

	@Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
	private String inputFolderFullPath;

	@Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
	private String outputFolderFullPath;

	private static final Logger logger = Logger.getLogger(RunAsnbReportTasklet.class);

	@Autowired
	private AsnbRepository asnbRepository;

	@Autowired
	private DailyAutoSettlementJobConfigProperties config;

	@Autowired
	private DailyDMBAutoSettlementJobConfigProperties dmbConfig;

	@Autowired
	private DailyDIBAutoSettlementJobConfigProperties dibConfig;

	@Autowired
	EmailTemplate emailTemplate;

	private static final String AMOUNT = "AMOUNT";
	private static final String NOFTRNX = "NO. OF TRX";
	private static final String RUNDATE = "RUN DATE:";

	private String dateFormat = "dd/MM/yyyy";
	Timestamp date = new Timestamp(System.currentTimeMillis());
	String nowDate = new SimpleDateFormat(dateFormat).format(date);
	String nowTime = new SimpleDateFormat("hhmmss").format(date);
	String nowDate2 = new SimpleDateFormat("yyyyMMdd").format(date);
	NumberFormat numberFormat = NumberFormat.getInstance();
	private String reportDate = "";
	private String channelType = "";
	private static final String MOBILEAPP = "MOBILEAPP";
	private static final String MOBILE = "MA00";
	private static final String MOBILEBANKING = "(MOBILE BANKING)";
	private static final String ASNBREPORTJOB = "AsnbReportJob";
	private static final String DECIMALFORMAT = "###.#";

	private static final String INTERNETWEB = "INTERNETWEB";
	private static final String INTERNET = "IB00";
	private static final String INTERNETBANKING = "(INTERNET BANKING - DIB)";



	@Getter
	private DoubleFunction<String> topupAmountFormatted = number -> {
		String numberFormatted = number % 1 == 0 ? String.format("%015.0f", number) + "00" : String.format("%018.2f", number);
		if(numberFormatted.contains(".")) {
			String[] finalAmount = numberFormatted.split("\\.");
			return finalAmount[0] + finalAmount[1];
		}
		else
			return numberFormatted;
	};

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		numberFormat.setGroupingUsed(true);
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);
		logger.info("ASNB Reconciliation Reports Batch");
		logger.info("Asnb Report TaskLet Executed");

		jdbcTemplate.setDataSource(dataSource);
		try {
			String batchSystemDateStr = (String) chunkContext.getStepContext().getStepExecution().getJobExecution()
					.getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			if (batchSystemDateStr == null) {
				throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Missing mandatory batch parameter");
			}
			generate2680(chunkContext);

		} catch (

		BatchException ex) {
			throw ex;
		} catch (Exception ex) {
			logger.error(ex);
			throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Batch error during RunReport", ex);
		}

		logger.info("Success 1: ");

		return RepeatStatus.FINISHED;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("After Properties run");
	}

	private void generate2680(ChunkContext chunkContext) throws BatchException {
		logger.info("Inside");
		try {
			String d = DateUtils.getProcessDate(chunkContext);
			logger.info("Processing Date ::" + d);
		
		File folder = new File(inputFolderFullPath + "/AsnbReportJob");
		logger.info("Folder Path" + folder);
		logger.info("Folder Path" + folder.getAbsolutePath());
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles.length == 0) {
			noFileFromPnb(chunkContext, MOBILEAPP);
			noFileFromPnb(chunkContext, INTERNETWEB);
		} else {
			havingFileFromPnb(d, listOfFiles, chunkContext);
		}
		} catch (ParseException ex) {
			throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE,
			ex);
		}

	}

	private void havingFileFromPnb(String d, File[] listOfFiles, ChunkContext chunkContext) {
		for (File file2 : listOfFiles) {
			if (checkFileProcessValidation(file2,d)) {
				logger.info("File Name : " + file2.getName());
				logger.info("File Length : " + file2.length());
				getChannelType(file2.getName());
				List<AsnbBatchDetails> detailsLst = new CopyOnWriteArrayList<>();
				if (file2.length() > 0) {
					parseInputFile(file2, detailsLst);
				}
				int generatedId = saveDetailsInDB(file2, 0);

				if (generatedId > 0) {
					logger.info("Data Inserted");
				}
				logger.info("List size : " + detailsLst.size());
				printFile(detailsLst, file2, generatedId, chunkContext);

			}
		}
	}

	private boolean checkFileProcessValidation(File file2,String date) {

		String name = file2.getName();
		logger.info("Name -- "+name);
		
		String fileDate = name.substring(10, 18);
		logger.info("File Date : "+fileDate + " -- Process Date : " + date);
		
		int count = asnbRepository.checkFileProcessValidation(file2);
		if (!fileDate.equals(date)) {
			return false;
		}else if (count > 1) {
			return false;
		}else {
			return true;
		}

	}

	private void getChannelType(String name) {
		channelType = name.substring(6, 10);
		if (channelType.equals(MOBILE)) {
			channelType = MOBILEAPP;
		} else if (channelType.equals(INTERNET)) {
			channelType = INTERNETWEB;
		}
	}

	private int saveDetailsInDB(File file, int rowValue) {

		if (rowValue > 0) {
			logger.info("Value to be updated is :" + rowValue);
			int row = asnbRepository.updateDailyFileProcessorTracker(rowValue);

			logger.info(String.format("Update %s record into select * from dcpbo.dbo.TBL_BO_DAILY_FILE_PROCESS_TRACKER",
					row));

			return row;
		} else {
			int keyvalue = asnbRepository.insertDailyFileProcessorTracker(file);

			logger.info(String.format("Add %s record into select * from dcpbo.dbo.TBL_BO_DAILY_FILE_PROCESS_TRACKER",
					keyvalue));
			logger.info("KeyValue is : " + keyvalue);

			return keyvalue;
		}
	}

	private static Scanner getScanner(String str) {
		return new Scanner(str);
	}

	private List<AsnbBatchDetails> parseInputFile(File file, List<AsnbBatchDetails> detailsLst) {

		Scanner scanner = null;
		String st = null;

		try (BufferedReader br = new BufferedReader(new FileReader(file));) {

			AsnbBatch b = new AsnbBatch();

			int i = 0;

			while ((st = br.readLine()) != null) {
				scanner = getScanner(st);
				if (i == 0) {

					scanner.useDelimiter(Pattern.compile("[|\n]"));
					while (scanner.hasNext()) {
						b.setHeaderInd(scanner.next());
						b.setAgetCode(scanner.next());
						b.setTranDate(scanner.next());
						b.setTotalRecords(scanner.next());
						b.setTotalAmount(scanner.next());
						b.setEndOfRecord(scanner.next());
						i++;
					}
					continue;
				}

				scanner.useDelimiter(Pattern.compile("[|\n]"));
				if (st.length() > 10) {
					while (scanner.hasNext()) {

						AsnbBatchDetails details = new AsnbBatchDetails();
						details.setBodyInd(scanner.next());
						details.setChannelType(scanner.next());
						details.setRequestIdentificaiton(scanner.next());
						details.setDeviceOwner(scanner.next());
						details.setUnitHolderId(scanner.next());
						details.setUhName(scanner.next());
						details.setIdentificationType(scanner.next());
						details.setIdentificationNumber(scanner.next());
						details.setFundId(scanner.next());
						details.setAmountApplied(scanner.next());
						details.setTransactionDate(scanner.next());
						details.setTransactionTime(scanner.next());
						details.setBnkTxnRefNumber(scanner.next());
						details.setBnkCustomerPhnNumber(scanner.next());
						details.setBankAccountNumber(scanner.next());
						details.setTransactionStatus(scanner.next());
						details.setUnitsAlloted(scanner.next());
						details.setTransactionNumber(scanner.next());
						details.setTransactionCode(scanner.next());
						details.setEndOfRecord(scanner.next());

						detailsLst.add(details);

					}
					continue;
				}

				b.setAsnbBtachDetails(detailsLst);
				b.setTrlInd(scanner.next());
				b.setEndOfFile(scanner.next());
				reportDate = b.getTranDate();

			}

		} catch (Exception e) {
			logger.error(e);
		} finally {
			scanner.close();
		}

		return detailsLst;

	}

	private void printFile(List<AsnbBatchDetails> detailsLst, File file, int id, ChunkContext chunkContext) {

		logger.info("File length :" + file.length());
		logger.info("File Name :" + file.getName());

		logger.info("Report Date is ::" + reportDate);		
		String now = null;
		if (reportDate == null || reportDate.equals("")) {
			Timestamp time = new Timestamp(System.currentTimeMillis());
			now = new SimpleDateFormat(dateFormat).format(time);
		} else {
			SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat format1 = new SimpleDateFormat(dateFormat);
			Date d = null;
			try {
				d = format1.parse(reportDate);
			} catch (ParseException e) {
				logger.error(e);
			}

			now = format2.format(d);
			logger.info("Date to be proceed is :" + now);
		}
		int pnbDataLength = detailsLst.size();
		AsnbHelperDTO helperClass = asnbRepository.getTranscationDetails(detailsLst, now, channelType);

		int dataAvailible = helperClass.getAsnbMap().size() + helperClass.getAnsbVarDetails().size();
		boolean postingFileRecords = helperClass.isCheckForPnbRecords();
		if (dataAvailible > 0 && pnbDataLength > 0) {
			
			
			generateAutoSettlement(helperClass, false, now, postingFileRecords, helperClass.getPnbFundList(), false, chunkContext);
			double[] d = generateReconciliationReport(helperClass, id, file.getName(), chunkContext);
			sendSuccessfulTnxEmails(helperClass, d);
		}

		if (pnbDataLength <= 0) {
			

			if (dataAvailible == 0) {

				generateAutoSettlement(helperClass, true, now, postingFileRecords, helperClass.getPnbFundList(), false, chunkContext);
				emailTemplate.getEmptyFileWithNoBankData(DateUtils.getBatchCurrentDate());
				generateReconciliationReport(helperClass, id, file.getName(), chunkContext);
			} else {
				
				generateReconciliationReport(helperClass, id, file.getName(), chunkContext);
				generateAutoSettlement(helperClass, false, now, postingFileRecords, helperClass.getPnbFundList(),
						true, chunkContext); //Changing false to true to handle empty file scenario similar like no file scenario
				emailTemplate.getEmptyFileWithBankData(DateUtils.getBatchCurrentDate(), helperClass);

			}

		}

	}

	private void noFileFromPnb(ChunkContext chunkContext, String channelType) {
		
		String dateEV = null;
		try {
			dateEV = DateUtils.getProcessDate(chunkContext);
		} catch (ParseException e) {
			logger.error(e);
		} catch (BatchException e) {
			logger.error(e);
		}
		String formttedDate = DateUtils.getFormattedDate(dateEV);
		List<AsnbBatchDetails> detailsLst = new CopyOnWriteArrayList<>();

		
		AsnbHelperDTO helperClass = asnbRepository.getTranscationDetails(detailsLst, formttedDate, channelType);
		int dataAvailible = helperClass.getAsnbMap().size() + helperClass.getAnsbVarDetails().size();
		if (dataAvailible <= 0) {

			emailTemplate.getNoFileNoBankData(DateUtils.getBatchCurrentDate());
		} else {
			
			emailTemplate.getNoFileWithBankData(DateUtils.getBatchCurrentDate(), helperClass);
			generateAutoSettlement(helperClass, false, formttedDate, true, helperClass.getPnbFundList(), true, chunkContext);
			String filename = getFormattedFileName("", helperClass, dateEV);
			generateReconciliationReport(helperClass, 0, filename, chunkContext);

		}

	}

	private void sendSuccessfulTnxEmails(AsnbHelperDTO helperClass, double[] d) {

		if (d[0] == d[1]) {
			emailTemplate.getAsnbScuccessBnkTnx(DateUtils.getBatchCurrentDate(), helperClass);
		} else if (d[0] > d[1]) {	
			emailTemplate.getAsnbScuccessLessTnx(DateUtils.getBatchCurrentDate(), helperClass);
		} else if (d[1] > d[0]) {
			emailTemplate.getAsnbScuccessGreaterTnx(DateUtils.getBatchCurrentDate(), helperClass);
		}

	}

	private void getProperties(String channelType) {
		if (channelType.equals(MOBILEAPP)) {
			config = dmbConfig;
		} else {
			config = dibConfig;
		}
	}

	private void generateAutoSettlement(AsnbHelperDTO helperClass, boolean check, String dateEnv,
			boolean postingFileRecords, List<String> bnkFundList, boolean checkforSummaryReords, ChunkContext chunkContext) {

		getProperties(channelType);
		DecimalFormat df = new DecimalFormat(DECIMALFORMAT);

		StringBuilder strBuilder = new StringBuilder();
		int hashValue = 0;
		int hashValueCumulutive = 0;
		int i = 0;
		String sequenceNumber = "1";
		double totalAmount = 0;
		List<String> seqList = new ArrayList();
		long counter = 9100000000L;

		if (check) {
			
			generateHeader(strBuilder, dateEnv);
			generateFooter(strBuilder, i, totalAmount, hashValue);
		} else {
			generateHeader(strBuilder, dateEnv);

				
				for (Map.Entry<String, AsnbSuccessList> row : helperClass.getAsnbMap().entrySet()) {

					seqList.add(sequenceNumber);
					if (seqList.contains(sequenceNumber)) {
						sequenceNumber = "1";
					}
					AsnbSuccessList asnbSuccessList = row.getValue();
						if(checkDisplaySummaryCriteria(asnbSuccessList, bnkFundList, checkforSummaryReords, helperClass, postingFileRecords)) {
						//check the variable type and update
						Double topupAmount = getTopupAmount(asnbSuccessList); 

						strBuilder.append(StringUtils.rightPad("01", 2, "") + getAccountType(asnbSuccessList.getName(), false)
										+ StringUtils.rightPad(config.getStlmtCtl2Code(), 3, "")
										+ StringUtils.rightPad(getCtl3(asnbSuccessList.getName()), 3, ""));
						strBuilder.append(StringUtils.rightPad(getcollectionAccountNumber(asnbSuccessList.getName()),14, "") + getAccountType(config.getFAccNum(), true)
								+ StringUtils.rightPad(config.getStlmtCtl2Code(), 3, "")+ StringUtils.rightPad(
										getCtl3ForFundControlAccountNumber(config.getFAccNum()),3, ""));
						strBuilder.append(StringUtils.rightPad(config.getFAccNum(), 14, ""));
						strBuilder.append(StringUtils.rightPad(config.getDrApplID(), 2, "")
								+ StringUtils.rightPad(config.getCrApplID(), 2, "")
								+ StringUtils.rightPad(config.getDrTranCode(), 4, "")
								+ StringUtils.rightPad(config.getCrTranCode(), 4, ""));
						strBuilder.append(StringUtils.rightPad(topupAmountFormatted.apply(topupAmount), 17, "")
								+ StringUtils.rightPad(config.getChannelCode(), 3, "")
								+ StringUtils.rightPad(config.getSubChannel(), 3, "")
								+ StringUtils.rightPad(config.getPgmID(), 8, ""));
						strBuilder.append(nowDate2 + nowTime + Long.toString(counter++) + StringUtils.rightPad("", 2, "")+ StringUtils.rightPad("", 30, "") + StringUtils.rightPad("", 30, ""));
						strBuilder.append(StringUtils.rightPad(ASNBUtils.getDRCRRecipRef(config.getDrCrRecipRefPrefix(),asnbSuccessList.getName()), 40, ""));
						strBuilder.append(StringUtils.rightPad("", 140, "") + StringUtils.rightPad("", 140, "")
										+ StringUtils.rightPad("", 30, "") + StringUtils.rightPad("", 30, ""));
						strBuilder.append(StringUtils.rightPad(ASNBUtils.getDRCRRecipRef(config.getDrCrRecipRefPrefix(),asnbSuccessList.getName()), 40, ""));
						strBuilder.append(StringUtils.rightPad("", 140, "") + StringUtils.rightPad("", 140, "")
								+ StringUtils.rightPad("", 1527, "") + "!");strBuilder.append("\r\n");
						i++;
						totalAmount = totalAmount + topupAmount;
						hashValue = hashValue + Integer
								.parseInt(calculateHashAmount(getcollectionAccountNumber(asnbSuccessList.getName()),
										topupAmount));
						hashValueCumulutive = hashValueCumulutive + hashValue;

					}

				}

			generateFooter(strBuilder, i, totalAmount, hashValueCumulutive);

		}

		String fileName = null;
		try {
			fileName = "ASNB_" + (channelType.equals(MOBILEAPP)?ASNBReconSettlementJobParameter.ASNB_RECON_MBK:ASNBReconSettlementJobParameter.ASNB_RECON_IBK) + "_Settlement_" + DateUtils.convertDateFormat(dateEnv,"yyyy-MM-dd", "yyyyMMdd")  + ".txt";
		} catch (ParseException e) {
			logger.error(e);
		}
		try (PrintWriter writer = new PrintWriter(outputFolderFullPath + "/" + ASNBREPORTJOB + "/"
				+ fileName, "UTF-8")) {
			writer.print(strBuilder);
			updateFileListForSendToFTP(chunkContext, fileName, ASNB_OUTPUT_FILE_LIST_SETTLEMENT);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.info(e);
		}

	}

	private void updateFileListForSendToFTP(ChunkContext chunkContext, String fileName, String jobParameter) {
	List<String> fileList = (List<String>) chunkContext.getStepContext().getJobExecutionContext().get(jobParameter);
		if (fileList == null) {
			fileList = new ArrayList<>();
            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(jobParameter, fileList);
		}
		fileList.add(fileName);
	}

	private Double getTopupAmount(AsnbSuccessList asnbSuccessList) {
		Double topupAmount;
		//Take the amount value from ASNB amount if Bank has more records	
		if(asnbSuccessList.getAsnbNoOfTran()!=0 && asnbSuccessList.getBankNoOfTran()> asnbSuccessList.getAsnbNoOfTran())
			topupAmount=asnbSuccessList.getAsnbAmount();
		else //take the bank amount
			topupAmount=asnbSuccessList.getBankAmount();
		return topupAmount;
	}



	private void generateHeader(StringBuilder strBuilder, String date) {
		String nowDateEV = DateUtils.getPostingFileEVDate(date);
		strBuilder.append(StringUtils.rightPad("00", 2, "") + nowDateEV + nowDateEV + nowTime
				+ StringUtils.rightPad(config.getJobName(), 8, "")
				+ StringUtils.rightPad(config.getJobNumber(), 8, "")
				+ StringUtils.rightPad(config.getProcStep(), 8, "")
				+ StringUtils.rightPad(config.getProgId(), 8, "")
				+ StringUtils.rightPad(config.getUserId(), 8, "")
				+ StringUtils.rightPad("", 2334, ""));
		strBuilder.append("\r\n");

	}

	private void generateFooter(StringBuilder strBuilder, int totalRecords, double totalAmount, int hashValue) {
		DecimalFormat df = new DecimalFormat(DECIMALFORMAT);

		strBuilder.append(StringUtils.rightPad(config.getFtpTypeFooter(), 2, "")
				+ StringUtils.leftPad(Integer.toString(totalRecords), 18, "0")
				+ StringUtils.leftPad(df.format(totalAmount * 100), 18, "0")
				+ StringUtils.leftPad(String.valueOf(hashValue), 18, "0") + StringUtils.rightPad("", 2346, ""));
		strBuilder.append("\r\n");
	}
	
	private boolean checkDisplaySummaryCriteria(AsnbSuccessList asnbSuccessList, List<String> bnkFundList,
			boolean checkforSummaryReords, AsnbHelperDTO helperClass, boolean postingFileRecords) {
		if (helperClass.getAsnbMap().size() > 0 && postingFileRecords && bnkFundList.contains(asnbSuccessList.getName()) && getFundListCheck(bnkFundList, asnbSuccessList.getBnkTxnRefNum()) || checkforSummaryReords) {
			return true;
		}
		return false;
	}

	private boolean getFundListCheck(List<String> bnkFundList, List<String> bnkTxnRefNum) {

		if(bnkTxnRefNum != null)
		{
			for (String name : bnkFundList) {
				if (bnkTxnRefNum.contains(name)) {
					return true;
				}
			}
		}
		return false;
	}

	private String getAccountType(String name, boolean check) {
		String accNum;
		if (!check) {
			accNum = getcollectionAccountNumber(name);
		} else {
			accNum = name;
		}
		String a2 = Character.toString(accNum.charAt(1));
		String a7 = Character.toString(accNum.charAt(6));
		logger.info("second value is: " + a2 + " and 7th value is: " + a7);
		if (!a2.equals("5") && !a2.equals("6") && !a7.equals("6")) {
			return "18";
		}
		if (!a2.equals("5") && !a2.equals("6") && a7.equals("6")) {
			return "19";
		}
		if (a2.equals("5") || a2.equals("6")) {
			return "19";
		}

		return null;
	}

	private String getCtl3(String name) {
		String accNum = getcollectionAccountNumber(name);
		return asnbRepository.getCtl3Number(accNum.substring(1, 6));
	}

	private String getCtl3ForFundControlAccountNumber(String name) {
		return asnbRepository.getCtl3Number(name.substring(1, 6));
	}

	private static String calculateHashAmount(String controllAccntNumber, double transactionNumber) {

		int[] transactionAmount = new int[10];
		int[] accountNumber = new int[10];
		long finalAmount = 0;

		long transactionNr = (long) (transactionNumber * 100);

		String transNr = String.valueOf(transactionNr);
		if (transNr.length() > 10) {
			transNr = transNr.substring(transNr.length() - 10);
		} else {
			transNr = StringUtils.leftPad(transNr, 10, "0");
		}

		if (controllAccntNumber.length() > 10) {
			controllAccntNumber = controllAccntNumber.substring(controllAccntNumber.length() - 10);
		}

		// add 1 to each number of transaction amount
		for (int i = 0; i < transNr.length(); i++) {
			transactionAmount[i] = Integer.parseInt(String.valueOf(transNr.charAt(i))) + 1;
		}

		// getting position based on the above value from account number

		for (int i = 0; i < controllAccntNumber.length(); i++) {
			accountNumber[i] = Integer.parseInt(String.valueOf(controllAccntNumber.charAt(transactionAmount[i] - 1)));
		}

		for (int i = 0; i < 10; i++) {
			finalAmount = finalAmount + (transactionAmount[i] * accountNumber[i]);
		}
		return StringUtils.leftPad(String.valueOf(finalAmount), 18, "0");
	}

	private String getcollectionAccountNumber(String fundId) {
		return asnbRepository.getCollectionAccountNUmber(fundId);

	}

	private double[] generateReconciliationReport(AsnbHelperDTO helperClass, int id, String fileName, ChunkContext chunkContext) {
		Timestamp time = new Timestamp(System.currentTimeMillis());
		String now = new SimpleDateFormat(dateFormat).format(time);

		double alltotalSubBankAmount = 0;
		double alltotalSubBankNoOfTnx = 0;

		double alltotalSubAsnbAmount = 0;
		double alltotalSubAsnbNoOfTnx = 0;

		double alltotalSubVarAmount = 0;
		double alltotalSubVarNoOfTnx = 0;
		double alltotalSubVarianceNoOfTnx = 0;

		NumberFormat formatter = new DecimalFormat("#0.00");
		DecimalFormat df = new DecimalFormat(DECIMALFORMAT);
		String formattedFileName = getFormattedFileName(fileName, helperClass, "");

		logger.info("File Path : " + outputFolderFullPath + "/" + ASNBREPORTJOB + "/"
				+ formattedFileName);
		String outputFolderPath = outputFolderFullPath + "/AsnbReportJob/";
		try (PrintWriter writer = new PrintWriter(
				checkFolderExists(outputFolderPath) + formattedFileName, "UTF-8")) {
			List<String> refIdList = new ArrayList<String>();

			String varHeaderFormat = "%1$-10s %2$-8s %3$-26s %4$-25s %5$-15s %6$-17s %7$-14s %8$-25s %9$-26s %10$-15s %11$-18s";

			String rDate = null;
			if (!helperClass.getAnsbVarDetails().isEmpty()) {
				rDate = helperClass.getAnsbVarDetails().get(0).getDate();
			} else {
				rDate = reportDate;
			}
			// Print Variance Header
			writer.println("AGENT NAME: RHB BANK" + addSpace(46, false) + "ASNB/BANK MATCHING REPORT"
					+ getAppType(fileName) + (channelType.equals(MOBILEAPP)?addSpace(29, false):addSpace(14, false)) + RUNDATE + now + "\r\n"
					+ "PART A: LIST OF VARIANCE RECORDS" + addSpace(34, false) + "REPORT DATE: " + rDate
					+ addSpace(47, false) + "REPORT: ASNB-QC6A\r\n" + "" + addSpace(137, false) + "PAGE: 1\r\n\n"
					+ String.format(varHeaderFormat, "No", "Type", "UH/Beneficiary ASNB ID", "UH/Beneficiary IC No",
							"Fund", "Date", "Time", "Bank reference Number", "FDS reference number", "Amount(RM)",
							"Variance Remarks\r"));

			logger.info("Variance Header Printed");

			// Print Variance Values
			printVarianceValues(helperClass.getAnsbVarDetails(), writer);

			logger.info("Variance Values Printed");

			// Print Summary Header
			writer.println("PART B: SUMMARY OF BANK/ASNB RECONCILIATION" + addSpace(23, false) + "REPORT DATE: " + rDate
					+ addSpace(47, false) + "REPORT: ASNB-QC6B\r\n" + addSpace(137, false) + "PAGE: 1\r\n" + "\r\n"
					+ addSpace(47, false) + "BANK" + addSpace(34, false) + "ASNB" + addSpace(37, false) + "Variance\r\n"
					+ addSpace(37, false) + AMOUNT + addSpace(13, false) + NOFTRNX + addSpace(10, false) + AMOUNT
					+ addSpace(13, false) + NOFTRNX + addSpace(10, false) + AMOUNT + addSpace(13, false) + NOFTRNX
					+ "\r\n");

			logger.info("Summary Header Printed");

			// Print Summary Values

			String summaryValuesFormat = "%1$-37s %2$15s %3$14s %4$27s %5$14s %6$26s %7$14s %8$-39s %9$15s %10$14s %11$27s %12$14s %13$26s %14$14s";
			String totalValuesFormat = "%1$-37s %2$15s %3$14s %4$27s %5$14s %6$26s %7$14s";
			String grandTotalFormat = "%1$-39s %2$15s %3$14s %4$27s %5$14s %6$26s %7$16s";

			for (Map.Entry<String, AsnbSuccessList> row : helperClass.getAsnbMap().entrySet()) {
				AsnbSuccessList asnbSuccessList = row.getValue();

				alltotalSubBankAmount = alltotalSubBankAmount + asnbSuccessList.getBankAmount();
				alltotalSubBankNoOfTnx = alltotalSubBankNoOfTnx + asnbSuccessList.getBankNoOfTran();

				alltotalSubAsnbAmount = alltotalSubAsnbAmount + asnbSuccessList.getAsnbAmount();
				alltotalSubAsnbNoOfTnx = alltotalSubAsnbNoOfTnx + asnbSuccessList.getAsnbNoOfTran();

				alltotalSubVarAmount = alltotalSubVarAmount
						+ (Math.abs(asnbSuccessList.getBankAmount() - asnbSuccessList.getAsnbAmount()));

				int varCount = 0;
				for (AsnbVarianceDetails d : helperClass.getAnsbVarDetails()) {
					if (d.getFund().equals(asnbSuccessList.getName())) {
						varCount = varCount + 1;
					}
				}

				alltotalSubVarNoOfTnx = alltotalSubVarNoOfTnx + varCount;

				writer.println(asnbSuccessList.getName() + "\r\n"
						+ String.format(summaryValuesFormat, "Total Subscription",
								numberFormat.format(asnbSuccessList.getBankAmount()),
								df.format(asnbSuccessList.getBankNoOfTran()),
								numberFormat.format(asnbSuccessList.getAsnbAmount()),
								df.format(asnbSuccessList.getAsnbNoOfTran()),
								formatter.format(
										Math.abs(asnbSuccessList.getBankAmount() - asnbSuccessList.getAsnbAmount())),
								df.format(varCount), "\r\nTotal Redemption",
								numberFormat.format(asnbSuccessList.getTotalRedBankAmount()),
								df.format(asnbSuccessList.getTotalRedBankNoOfTnx()),
								formatter.format(asnbSuccessList.getTotalRedAsnbAmount()),
								df.format(asnbSuccessList.getTotalRedAsnbNoOfTnx()),
								numberFormat.format(asnbSuccessList.getTotalRedVarAmount()),
								df.format(asnbSuccessList.getTotalRedVarNoOfTnx()))
						+ "\r\n"

						
						+ addSpace(25, false) + addSpace(26, true) + addSpace(2, false) + addSpace(11, true)
						+ addSpace(3, false) + addSpace(23, true) + addSpace(2, false) + addSpace(11, true)
						+ addSpace(2, false) + addSpace(23, true) + addSpace(2, false) + addSpace(11, true) + "\r\n"
						+ String.format(totalValuesFormat, "Sub Total",
								numberFormat.format(
										asnbSuccessList.getBankAmount() + asnbSuccessList.getTotalRedBankAmount()),
								df.format(asnbSuccessList.getBankNoOfTran() + asnbSuccessList.getTotalRedBankNoOfTnx()),
								numberFormat.format(
										asnbSuccessList.getAsnbAmount() + asnbSuccessList.getTotalRedAsnbAmount()),
								df.format(asnbSuccessList.getAsnbNoOfTran() + asnbSuccessList.getTotalRedAsnbNoOfTnx()),
								numberFormat.format(Math.abs(asnbSuccessList.getBankAmount()
										- asnbSuccessList.getAsnbAmount() + asnbSuccessList.getTotalRedBankAmount()
										- asnbSuccessList.getTotalRedAsnbAmount())),
								df.format(varCount))
						+ "\r\n" + addSpace(25, false) + addSpace(26, true) + addSpace(2, false) + addSpace(11, true)
						+ addSpace(3, false) + addSpace(23, true) + addSpace(2, false) + addSpace(11, true)
						+ addSpace(2, false) + addSpace(23, true) + addSpace(2, false) + addSpace(11, true) + "\r\n");
				alltotalSubVarianceNoOfTnx = alltotalSubVarianceNoOfTnx + alltotalSubVarNoOfTnx;
				alltotalSubVarNoOfTnx = 0;

			}

			writer.println(String.format(totalValuesFormat, "Total Subscription All Funds",
					numberFormat.format(alltotalSubBankAmount), df.format(alltotalSubBankNoOfTnx),
					numberFormat.format(alltotalSubAsnbAmount), df.format(alltotalSubAsnbNoOfTnx),
					numberFormat.format(alltotalSubVarAmount), df.format(alltotalSubVarianceNoOfTnx))
					+ String.format(grandTotalFormat, "\r\nTotal Redemption All Funds", "0.00", "0", "0.00", "0",
							"0.00", "0\r\n")
					+ // hyphen start
					addSpace(25, false) + addSpace(26, true) + addSpace(2, false) + addSpace(11, true)
					+ addSpace(3, false) + addSpace(23, true) + addSpace(2, false) + addSpace(11, true)
					+ addSpace(2, false) + addSpace(23, true) + addSpace(2, false) + addSpace(11, true) + "\r\n"

					+ String.format(totalValuesFormat, "Grand Total", numberFormat.format(alltotalSubBankAmount + 0.00),
							df.format(alltotalSubBankNoOfTnx),
							numberFormat.format(Math.abs(alltotalSubAsnbAmount + 0.00)),
							df.format(alltotalSubAsnbNoOfTnx),
							numberFormat.format(Math.abs(alltotalSubVarAmount + 0.00)),
							df.format(alltotalSubVarianceNoOfTnx))
					+ "\r\n" + addSpace(25, false) + addSpace(26, true) + addSpace(2, false) + addSpace(11, true)
					+ addSpace(3, false) + addSpace(23, true) + addSpace(2, false) + addSpace(11, true)
					+ addSpace(2, false) + addSpace(23, true) + addSpace(2, false) + addSpace(11, true) + "\r\n");


			updateFileListForSendToFTP(chunkContext, formattedFileName, ASNB_OUTPUT_FILE_LIST_RECON);
			logger.info("Reconciliation File Generated");

			int row = saveDetailsInDB(null, id);
			if (row > 0) {
				logger.info("File Manipulation Status changed to Completed" + row);
			}

		} catch (Exception e) {

			logger.error(e);
		}
		double[] tmpData = { alltotalSubBankNoOfTnx, alltotalSubAsnbNoOfTnx, alltotalSubVarNoOfTnx };

		// alltotalSubBankNoOfTnx, alltotalSubAsnbNoOfTnx, alltotalSubVarNoOfTnx
		Path path = Paths.get(inputFolderFullPath + "/" + ASNBREPORTJOB + "/" + fileName);
		if(fileName !=null && !fileName.isEmpty()) {
			try {
				Files.delete(path);
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return tmpData;
	}

	private String getAppType(String name) {
		if (name.substring(6, 10).equals(MOBILE)) {
			return MOBILEBANKING;
		} else if (name.substring(6, 10).equals(INTERNET)) {
			return INTERNETBANKING;
		} else {
			return null;
		}
	}

	private String getFormattedFileName(String name, AsnbHelperDTO helperClass, String date) {
		String partA = "RHB002";

		if (name.contains(ASNBReconSettlementJobParameter.DIB_FILE_NAME_ENDING_PREFIX)) {
			name = name.replace(ASNBReconSettlementJobParameter.DIB_FILE_NAME_ENDING_PREFIX, StringUtils.EMPTY);
		}

		if (!name.equals("") && (name.substring(6, 10).equals(MOBILE) || name.substring(6, 10).equals(INTERNET))) {

			String partB = name.substring(6, 25);
			name = partA + partB;
			logger.info("File Name" + name);
			return name;
		}

		if (name.equals("")) {
			name = partA + (helperClass.getChannelTypeWithOutFile().equals(ASNBReconSettlementJobParameter.ASNB_MOBILE_BANKING)?MOBILE:INTERNET) + date + "006.txt";
			return name;
		}

		return name;
	}

	private static String checkFolderExists(String targetFileFullPath) {

		File directory = new File(targetFileFullPath);
		if (!directory.exists()) {
			directory.mkdir();
		}
		return targetFileFullPath;
	}

	private PrintWriter printVarianceValues(List<AsnbVarianceDetails> rows, PrintWriter writer) {

		String varValuesFormat = "%1$-10s %2$-8s %3$-26s %4$-25s %5$-15s %6$-14s %7$-17s %8$-25s %9$-26s %10$-12s %11$-27s";
		if (rows != null && !rows.isEmpty()) {

			int no = 1;
			for (AsnbVarianceDetails row : rows) {
				writer.println(String.format(varValuesFormat, no, channelType, row.getUhBenificiaryAsnbId(),
						row.getUhBenificiaryIcNo(), row.getFund(), row.getDate(), row.getTime(), row.getBnkRefNum(),
						row.getFdsRefNum().equals("") || row.getFdsRefNum().isEmpty() ? addSpace(15, false)
								: row.getFdsRefNum(),
						numberFormat.format(Double.parseDouble(row.getAmount())), row.getVarRemarks() + "\n"));
				no++;
			}
		}

		return writer;

	}

	private String addSpace(int strLength, boolean hyphen) {
		String st = "";
		for (int i = 0; i <= strLength; i++) {
			if (hyphen) {
				st = st + "-";
			} else {
				st = st + " ";
			}
		}
		return st;
	}

}
