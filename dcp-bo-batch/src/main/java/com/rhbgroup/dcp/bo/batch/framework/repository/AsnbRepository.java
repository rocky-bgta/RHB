package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ASNBReconSettlementJobParameter;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbBatchDetails;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbSuccessList;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbVarianceDetails;
import com.rhbgroup.dcp.bo.batch.job.model.DcpPendingDto;
import com.rhbgroup.dcp.bo.batch.job.model.AsnbHelperDTO;

@Component
public class AsnbRepository extends BaseRepositoryImpl implements InitializingBean {

	private static final Logger logger = Logger.getLogger(AsnbRepository.class);
	private static final String AMOUNT = "amount";
	private static final String FUNDID = "fund_id";
	private static final String TXNNO = "TXN_NO";
	private static final String TXNSTATUS = "TXN_STATUS";
	private static final String PENDING = "PENDING";
	private static final String REFID = "REF_ID";
	private static final String TXNTOKENID = "TXN_TOKEN_ID";
	private static final String BNK_SUCCESS_PNB_MISSING = " PNB=Missing Bank=Successful";
	private static final String RECORD_UPDATED = "Record Updated";
	private static final String MEMBERSHIP_NO = "membership_number";
	private static final String ACC_HOLDER_ID_NO = "account_holder_id_no";
	private static final String REF_ID = "ref_id";
	private static final String TXN_TIME = "txn_time";
	SimpleDateFormat dFormat = new SimpleDateFormat("dd/MM/yyyy");
	SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	SimpleDateFormat tFormat = new SimpleDateFormat("hh:mm:ss aa");
	
	private static final String MOBILEAPP = "MOBILEAPP";

	public int updateDailyFileProcessorTracker(int rowValue) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		int row = 0;
		try {

			String sql = "UPDATE dcpbo.dbo.TBL_BO_DAILY_FILE_PROCESS_TRACKER set PROCESS_STATUS = 'Completed', "
					+ "PROCESS_END_TIME=? WHERE id =?";

			logger.info("SQL is: " + sql);

			row = getJdbcTemplate().update(sql, now, rowValue);

		} catch (Exception e) {
			logger.error(e);
		}

		return row;

	}

	public int insertDailyFileProcessorTracker(File file) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_DAILY_FILE_PROCESS_TRACKER"
					+ "(TRANSACTION_DATE,MAIN_FUNCTION,FILE_NAME,FILE_LOCATION,PROCESS_STATUS,PROCESS_START_TIME,PROCESS_END_TIME,CREATED_TIME,CREATED_BY,"
					+ "UPDATED_TIME,UPDATED_BY,IS_ACTIVE)" + " VALUES " + "(?,?,?,?,?,?,?,?,?,?,?,?)";

			getJdbcTemplate().update(new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });

					try {
						ps.setTimestamp(1, now);
						ps.setString(2, "ASNB");
						ps.setString(3, file.getName());
						ps.setString(4, file.getAbsolutePath());
						ps.setString(5, "Pending");
						ps.setTimestamp(6, now);
						ps.setString(7, "");
						ps.setTimestamp(8, now);
						ps.setString(9, "Admin");
						ps.setTimestamp(10, now);
						ps.setString(11, "Admin");
						ps.setString(12, "1");
					} catch (SQLException e) {
						logger.error(e.getMessage());
					}

					return ps;
				}
			}, keyHolder);
			logger.info(sql);
		} catch (Exception e) {
			logger.error(e);
		}

		return keyHolder.getKey().intValue();

	}

	public List<Map<String, Object>> getVarainceDetails(List<String> refIdList) {

		String query = String.format("SELECT  \r\n" + "FT.CHANNEL AS T_CHANNEL,\r\n"
				+ "ASNB.MEMBERSHIP_NUMBER AS ASNB_ID,\r\n" + "ASNB.ACCOUNT_HOLDER_ID_NO AS ID_NUMBER,\r\n"
				+ "ASNB.FUND_ID,\r\n" + "ASNB.FUND_LONG_NAME AS PRODUCT_NAME,\r\n"
				+ "CAST(FT.TXN_TIME AS DATE ) AS T_DATE,\r\n" + "CAST(FT.TXN_TIME AS TIME ) AS T_TIME,\r\n"
				+ "FT.REF_ID AS BNK_REF_NUM,\r\n" + "ASNB.TXN_NO AS FDS_REF_NUM,\r\n" + "FT.AMOUNT AS T_AMOUNT,\r\n"
				+ "FT.TXN_TOKEN_ID AS TOKEN_ID\r\n" + "FROM DCP.DBO.TBL_INVEST_TXN FT\r\n"
				+ "JOIN DCP.DBO.TBL_ASNB_TXN ASNB\r\n" + "ON FT.TXN_TOKEN_ID = ASNB.TXN_TOKEN_ID\r\n"
				+ "JOIN DCP.DBO.TBL_TXN_TOKEN TK\r\n" + "ON TK.ID = ASNB.TXN_TOKEN_ID"
				+ " where   ft.REF_ID not in (%s)", refIdList);
		query = query.replace("[", "");
		query = query.replace("]", "");

		return getJdbcTemplate().queryForList(query);
	}

	public AsnbHelperDTO getTranscationDetails(List<AsnbBatchDetails> detailsLst, String now, String channelType) {
		
		String channelTypeWithOutFile = "";
		List<AsnbVarianceDetails> varList = new ArrayList<>();
		List<DcpPendingDto> pendingDtos = new ArrayList<>();
		Map<String, AsnbSuccessList> tmpMap = new HashMap<>();
		List<String> fundList = new ArrayList<String>();
		List<String> fundIdList = new ArrayList<String>();
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setGroupingUsed(true);
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);
		List<String> tempList = new ArrayList<String>();
		boolean checkForPostingRecords =false;
		List<String> pnbFundList = new ArrayList<String>();
		List<String> bnkRefIdlist = new ArrayList<String>();

		String channelTypeStr = MOBILEAPP.equals(channelType)?ASNBReconSettlementJobParameter.ASNB_MOBILE_BANKING:ASNBReconSettlementJobParameter.ASNB_INTERNET_BANKING;

		String query = "select * from dcp.dbo.TBL_ASNB_TXN a join dcp.dbo.TBL_INVEST_TXN b on  a.txn_token_id = b.txn_token_id"
				+ " where txn_id !='' and txn_status != 'Failed' and IS_RECONCILED is NULL"
				+ " and convert(varchar,a.updated_time,23) =? and b.channel =? ";

		logger.info("Query1 is:::: " + query);
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(query, now, channelTypeStr);
		for (Map row : rows) {
			fundIdList.add(row.get(FUNDID).toString());
		}
		for (AsnbBatchDetails fund : detailsLst) {
			pnbFundList.add(fund.getFundId());
			pnbFundList.add(fund.getBnkTxnRefNumber());
		}

		for (String fundId : fundIdList) {
			checkForPostingRecords = true;
			boolean chk = checkTxnNum(tempList, fundId);
			if (!chk) {
				continue;
			}
			tempList.add(fundId);

			String query2 = "select * from dcp.dbo.TBL_ASNB_TXN a join dcp.dbo.TBL_INVEST_TXN b on"
					+ " a.txn_token_id = b.txn_token_id where txn_id !='' and IS_RECONCILED is NULL"
					+ " and txn_status in ('SUCCESS', 'PENDING')  and fund_id =?"
					+ " and convert(varchar,a.updated_time,23) =? and b.channel =? ";

			logger.info("Query2 is:::: " + query2);
			List<Map<String, Object>> rows2 = getJdbcTemplate().queryForList(query2, fundId, now, channelTypeStr);

			for (Map row : rows2) {
				
				channelTypeWithOutFile = row.get("CHANNEL").toString();
				getVarianceList(row, varList);
				
				if (!row.get(TXNSTATUS).equals(PENDING)) {
					getSummaryRecords(fundList, row, tmpMap, detailsLst, varList, bnkRefIdlist);
				}
				getBnkPendingTxnList(row, pendingDtos);
				removeTxnNumber(row, detailsLst);

			}

		}

		getPnbUniqueResults(detailsLst, fundList, varList, tmpMap, now, pendingDtos, channelType);

		AsnbHelperDTO helperDTO = new AsnbHelperDTO();
		helperDTO.setAsnbMap(tmpMap);
		helperDTO.setAnsbVarDetails(varList);
		helperDTO.setChannelTypeWithOutFile(channelTypeWithOutFile);
		helperDTO.setCheckForPnbRecords(checkForPostingRecords);
		helperDTO.setPnbFundList(pnbFundList);

		return helperDTO;
	}

	private void getVarianceList(Map row, List<AsnbVarianceDetails> varList) {
		if (row.get(TXNNO) == null || row.get(TXNNO).equals("") && !row.get(TXNSTATUS).equals(PENDING)) {

			logger.info("Inside If::");
			AsnbVarianceDetails varDetails = new AsnbVarianceDetails();
			varDetails.setUhBenificiaryAsnbId(row.get(MEMBERSHIP_NO).toString());
			varDetails.setUhBenificiaryIcNo(row.get(ACC_HOLDER_ID_NO).toString());
			String d = row.get(TXN_TIME).toString();
			Date date = null;

			try {
				date = format2.parse(d);
			} catch (ParseException e) {
				logger.error(e);
			}
			varDetails.setFund(row.get(FUNDID).toString());
			varDetails.setDate(dFormat.format(date));
			varDetails.setTime(tFormat.format(date));
			varDetails.setBnkRefNum(row.get(REF_ID).toString());
			varDetails.setFdsRefNum("");
			varDetails.setAmount((row.get(AMOUNT).toString()));
			varDetails.setVarRemarks(BNK_SUCCESS_PNB_MISSING);
			varList.add(varDetails);

		}
		
	}

	private void getBnkPendingTxnList(Map row, List<DcpPendingDto> pendingDtos) {
		
		if (row.get(TXNSTATUS).equals(PENDING)) {
			DcpPendingDto dcpPendingDto = new DcpPendingDto();
			dcpPendingDto.setUhBenificiaryId(row.get(MEMBERSHIP_NO).toString());
			dcpPendingDto.setUhBenificiaryIcNo(row.get(ACC_HOLDER_ID_NO).toString());
			String d = row.get(TXN_TIME).toString();
			Date date = null;

			try {
				date = format2.parse(d);
			} catch (ParseException e) {
				logger.error(e);
			}
			dcpPendingDto.setFundName(row.get(FUNDID).toString());
			dcpPendingDto.setDate(dFormat.format(date));
			dcpPendingDto.setTime(tFormat.format(date));
			dcpPendingDto.setBnkRefNo(row.get(REF_ID).toString());
			//dcpPendingDto.setFdsRefNum("");// txn number should be empty
			dcpPendingDto.setAmount((row.get(AMOUNT).toString()));
			pendingDtos.add(dcpPendingDto);
		}
		
	}

	private void getSummaryRecords(List<String> fundList, Map row, Map<String, AsnbSuccessList> tmpMap,
			List<AsnbBatchDetails> detailsLst, List<AsnbVarianceDetails> varList, List<String> bnkRefIdlist) {

		
		
		if (fundList.contains(row.get(FUNDID))) {
			AsnbSuccessList listObj = tmpMap.get(row.get(FUNDID));
			listObj.setBankAmount(listObj.getBankAmount() + Double.parseDouble(row.get(AMOUNT).toString()));
			listObj.setBankNoOfTran(listObj.getBankNoOfTran() + 1);
			bnkRefIdlist.add(row.get(REF_ID).toString());
			listObj.setBnkTxnRefNum(bnkRefIdlist);
			
			if (row.get(TXNNO) != null && row.get(TXNNO) != "" && checkTxnNum(row.get(TXNNO).toString(), detailsLst)) {
				listObj.setAsnbAmount(listObj.getAsnbAmount() + Double.parseDouble(row.get(AMOUNT).toString()));
				listObj.setAsnbNoOfTran(listObj.getAsnbNoOfTran() + 1);
				
				int row2 = updateRecordStatus(Integer.parseInt(row.get(TXNTOKENID).toString()));
				if (row2 > 0) {
					logger.info(RECORD_UPDATED + row2);
				}
			}
			tmpMap.put(row.get(FUNDID).toString(), listObj);
			

		} else {
			fundList.add(row.get(FUNDID).toString());
			AsnbSuccessList asnbSuccessList = new AsnbSuccessList();
			asnbSuccessList.setName(row.get(FUNDID).toString());
			asnbSuccessList.setBankAmount(Double.parseDouble(row.get(AMOUNT).toString()));
			asnbSuccessList.setBankNoOfTran(1);
			bnkRefIdlist.add(row.get(REF_ID).toString());
			asnbSuccessList.setBnkTxnRefNum(bnkRefIdlist);
			
			if (row.get(TXNNO) != null && row.get(TXNNO) != "" && checkTxnNum(row.get(TXNNO).toString(), detailsLst)) {
				asnbSuccessList.setAsnbAmount(Double.parseDouble(row.get(AMOUNT).toString()));
				asnbSuccessList.setAsnbNoOfTran(1);
				
				int row2 = updateRecordStatus(Integer.parseInt(row.get(TXNTOKENID).toString()));
				if (row2 > 0) {
					logger.info(RECORD_UPDATED + row2);
				}

			}
			tmpMap.put(row.get(FUNDID).toString(), asnbSuccessList);

		}
		generateVarianceForBankExtraTxn(row, detailsLst, varList);

	}

	private void generateVarianceForBankExtraTxn(Map row, List<AsnbBatchDetails> detailsLst,
			List<AsnbVarianceDetails> varList) {
		if (row.get(TXNNO) != null && !checkTxnNum(row.get(TXNNO).toString(), detailsLst)) {

			AsnbVarianceDetails varDetails = new AsnbVarianceDetails();
			varDetails.setUhBenificiaryAsnbId(row.get(MEMBERSHIP_NO).toString());
			varDetails.setUhBenificiaryIcNo(row.get(ACC_HOLDER_ID_NO).toString());
			String d = row.get(TXN_TIME).toString();
			Date date = null;

			try {
				date = format2.parse(d);
			} catch (ParseException e) {
				logger.error(e);
			}
			varDetails.setFund(row.get(FUNDID).toString());
			varDetails.setDate(dFormat.format(date));
			varDetails.setTime(tFormat.format(date));
			varDetails.setBnkRefNum(row.get(REF_ID).toString());
			varDetails.setFdsRefNum("");// txn number should be empty
			varDetails.setAmount((row.get(AMOUNT).toString()));
			varDetails.setVarRemarks(BNK_SUCCESS_PNB_MISSING);
			varList.add(varDetails);

		}

	}

	private boolean checkTxnNum(String txnNo, List<AsnbBatchDetails> detailsLst) {
		for (AsnbBatchDetails b : detailsLst) {
			String transactionNo = getTxnNumber(b.getTransactionNumber());
			if (transactionNo != null && transactionNo.equals(txnNo)) {
				return true;
			}
		}

		return false;
	}

	private void removeTxnNumber(Map row, List<AsnbBatchDetails> detailsLst) {
	
			if (row.get(TXNNO) != null  && !row.get(TXNNO).equals("")) {
				logger.info("Txn no to Del : " + row.get(TXNNO));
				detailsLst.removeIf(s -> getTxnNumber(s.getTransactionNumber()).equals(row.get(TXNNO).toString()));

			}
		

	}

	private void getPnbUniqueResults(List<AsnbBatchDetails> detailsLst, List<String> fundList,
			List<AsnbVarianceDetails> varList, Map<String, AsnbSuccessList> tmpMap, String now, List<DcpPendingDto> pendingDtos, String channelType) {
		logger.info("PNB unique data size should be " + detailsLst.size());

		if (!detailsLst.isEmpty()) {

			String channelTypeStr = MOBILEAPP.equals(channelType)?ASNBReconSettlementJobParameter.ASNB_MOBILE_BANKING:ASNBReconSettlementJobParameter.ASNB_INTERNET_BANKING;

			for (AsnbBatchDetails details : detailsLst) {
				
				logger.info("Unique Txn number of Pnb is ::::::::::::: " + details.getBnkTxnRefNumber()
						+ "                  " + details.getTransactionNumber());

				String query2 = "select * from dcp.dbo.TBL_ASNB_TXN a join dcp.dbo.TBL_INVEST_TXN b on"
						+ " a.txn_token_id = b.txn_token_id where ref_id =?"
						+ " and IS_RECONCILED is NULL and convert(varchar,a.updated_time,23) =?"
						+ " and b.channel =? ";

				logger.info("Query for PNB UNique ONly is :::::::::::::: "+query2);

				List<Map<String, Object>> rows2 = getJdbcTemplate().queryForList(query2, details.getBnkTxnRefNumber(), now, channelTypeStr);

				getPnbVarAndSummaryRecords(details, fundList, varList, tmpMap, rows2);

				
				removeUniqueTxnNumbersFromPnb(details.getTransactionNumber(), detailsLst);
				removePendingTxnFromDcp(details.getBnkTxnRefNumber(), pendingDtos);

			}

		}
		if(!pendingDtos.isEmpty()) {
			getBankPendingSummaryAndvariance(pendingDtos, fundList, varList, tmpMap);
		}

	}

	private void getBankPendingSummaryAndvariance(List<DcpPendingDto> pendingDtos, List<String> fundList, List<AsnbVarianceDetails> varList, Map<String, AsnbSuccessList> tmpMap) {
		
		for(DcpPendingDto pendingDto :pendingDtos) {
			
			if (fundList.contains(pendingDto.getFundName())) {
				AsnbSuccessList listObj = tmpMap.get(pendingDto.getFundName());
				listObj.setBankAmount(
						listObj.getBankAmount() + Double.parseDouble(pendingDto.getAmount()));
				listObj.setBankNoOfTran(listObj.getBankNoOfTran() + 1);
				tmpMap.put(pendingDto.getFundName(), listObj);
				

			} else {
				fundList.add(pendingDto.getFundName());
				AsnbSuccessList asnbSuccessList = new AsnbSuccessList();
				asnbSuccessList.setName(pendingDto.getFundName());
				asnbSuccessList.setBankAmount(Double.parseDouble(pendingDto.getAmount()));
				asnbSuccessList.setBankNoOfTran(1);

				tmpMap.put(pendingDto.getFundName(), asnbSuccessList);
			}

			AsnbVarianceDetails varDetails = new AsnbVarianceDetails();
			varDetails.setUhBenificiaryAsnbId(getTxnNumber(pendingDto.getUhBenificiaryId()));
			varDetails.setUhBenificiaryIcNo(pendingDto.getUhBenificiaryIcNo());
			varDetails.setFund(pendingDto.getFundName());
			varDetails.setDate(pendingDto.getDate());
			varDetails.setTime(pendingDto.getTime());
			varDetails.setBnkRefNum(pendingDto.getBnkRefNo());
			varDetails.setFdsRefNum("");
			varDetails.setAmount(pendingDto.getAmount());
			varDetails.setVarRemarks(BNK_SUCCESS_PNB_MISSING);
			varList.add(varDetails);

		}

			
		}
				

	private void removePendingTxnFromDcp(String transactionNumber, List<DcpPendingDto> pendingDtos) {
		
		if (!transactionNumber.equals("")) {
			logger.info("Txn no Del : " + transactionNumber);
			pendingDtos.removeIf(s -> getTxnNumber(s.getBnkRefNo()).equals(transactionNumber));

		}
		
	}

	private void removeUniqueTxnNumbersFromPnb(String transactionNumber, List<AsnbBatchDetails> detailsLst) {
		
		if (!transactionNumber.equals("")) {
			logger.info("Txn no Del : " + transactionNumber);
			detailsLst.removeIf(s -> getTxnNumber(s.getTransactionNumber()).equals(transactionNumber));

		}
	
	}

	private void getPnbVarAndSummaryRecords(AsnbBatchDetails details, List<String> fundList,
			List<AsnbVarianceDetails> varList, Map<String, AsnbSuccessList> tmpMap, List<Map<String, Object>> rows2) {
		SimpleDateFormat format1 = new SimpleDateFormat("hh:mm:ss");
		SimpleDateFormat tformat2 = new SimpleDateFormat("hh:mm:ss aa");

		if (!rows2.isEmpty()) {
			
			if (fundList.contains(details.getFundId())) {
				AsnbSuccessList listObj = tmpMap.get(details.getFundId());

				listObj.setAsnbAmount(listObj.getAsnbAmount() + Double.parseDouble(details.getAmountApplied()));
				listObj.setAsnbNoOfTran(listObj.getAsnbNoOfTran() + 1);
				listObj.setBankAmount(
						listObj.getBankAmount() + Double.parseDouble(rows2.get(0).get(AMOUNT).toString()));
				listObj.setBankNoOfTran(listObj.getBankNoOfTran() + 1);
				tmpMap.put(details.getFundId(), listObj);
				

			} else {
				fundList.add(details.getFundId());
				AsnbSuccessList asnbSuccessList = new AsnbSuccessList();
				asnbSuccessList.setName(details.getFundId());
				asnbSuccessList.setAsnbAmount(Double.parseDouble(details.getAmountApplied()));
				asnbSuccessList.setAsnbNoOfTran(1);
				asnbSuccessList.setBankAmount(Double.parseDouble(rows2.get(0).get(AMOUNT).toString()));
				asnbSuccessList.setBankNoOfTran(1);

				tmpMap.put(details.getFundId(), asnbSuccessList);
			}

			// Update Txn status and update MBK
			 updateStatusToSuccess(rows2);

		} else {

			if (fundList.contains(details.getFundId())) {
				AsnbSuccessList listObj = tmpMap.get(details.getFundId());

				listObj.setAsnbAmount(listObj.getAsnbAmount() + Double.parseDouble(details.getAmountApplied()));
				listObj.setAsnbNoOfTran(listObj.getAsnbNoOfTran() + 1);
				tmpMap.put(details.getFundId(), listObj);
				logger.info("Asnb Amount " + listObj.getAsnbAmount());
				logger.info("Asnb Tnx Count " + listObj.getAsnbNoOfTran());

			} else {
				fundList.add(details.getFundId());
				AsnbSuccessList asnbSuccessList = new AsnbSuccessList();
				asnbSuccessList.setName(details.getFundId());
				asnbSuccessList.setAsnbAmount(Double.parseDouble(details.getAmountApplied()));
				asnbSuccessList.setAsnbNoOfTran(1);

				tmpMap.put(details.getFundId(), asnbSuccessList);
			}

			AsnbVarianceDetails varDetails = new AsnbVarianceDetails();
			varDetails.setUhBenificiaryAsnbId(details.getUnitHolderId());
			varDetails.setUhBenificiaryIcNo(details.getIdentificationNumber());
			varDetails.setFund(details.getFundId());
			varDetails.setDate(details.getTransactionDate());
			String d = details.getTransactionTime();
			Date date = null;
			try {
				date = format1.parse(d);
			} catch (ParseException e) {
				logger.error(e);
			}
			varDetails.setTime(tformat2.format(date));
			varDetails.setBnkRefNum("");
			varDetails.setFdsRefNum(details.getTransactionNumber());
			varDetails.setAmount(details.getAmountApplied());
			varDetails.setVarRemarks(" PNB=Successful Bank=Missing");
			varList.add(varDetails);

		}

	}

	private void updateStatusToSuccess(List<Map<String, Object>> rows2) {
		String query = "update dcp.dbo.TBL_INVEST_TXN set TXN_STATUS ='SUCCESS' where REF_ID = '" + rows2.get(0).get(REFID) + "'";
		logger.info("Update Query Is:  "+query);
		int row = getJdbcTemplate().update(query);
		if(row >0) {
			logger.info("Record status updated to success:");
		}
		
		int row2 = updateRecordStatus(Integer.parseInt(rows2.get(0).get(TXNTOKENID).toString()));
		if (row2 > 0) {
			logger.info(RECORD_UPDATED + row2);
		}
		
	}

	private String getTxnNumber(String transactionNumber) {

		if (transactionNumber != null && transactionNumber.substring(0, 1).equals("0")) {
			transactionNumber = transactionNumber.substring(1, transactionNumber.length());
			return transactionNumber;
		}
		return transactionNumber;
	}

	public int getCount(int tokenId) {

		String query = "SELECT count(*) FROM dcp.dbo.TBL_INVEST_TXN where TXN_TOKEN_ID =?";
		return getJdbcTemplate().queryForObject(query, new Object[] { tokenId }, Integer.class);

	}

	public int checkFileProcessValidation(File file) {

		String query = "SELECT count(*) FROM dcpbo.dbo.TBL_BO_DAILY_FILE_PROCESS_TRACKER where process_status != 'Completed' and  file_name ='"
				+ file.getName() + "'";
		logger.info("count is for db is : " + query);
		return getJdbcTemplate().queryForObject(query, Integer.class);

	}

	public String getAmount(int tokenId) {

		String query = "SELECT SUM(AMOUNT) FROM dcp.dbo.TBL_INVEST_TXN where TXN_TOKEN_ID  =?";
		return getJdbcTemplate().queryForObject(query, new Object[] { tokenId }, String.class);
	}

	public String getCollectionAccountNUmber(String fundId) {

		String query = "SELECT COLLECTION_ACCT_NUMBER FROM dcp.dbo.TBL_FUND_DETAILS where fund_id  =?";
		return getJdbcTemplate().queryForObject(query, new Object[] { fundId }, String.class);
	}
	
	public String getCtl3Number(String num) {

		String query  = "select ctrl3 from dcp.dbo.tbl_bnm_ctrl3 where bnm  =?";
		return getJdbcTemplate().queryForObject(query, new Object[] { num }, String.class);
	}

	public int updateRecordStatus(int tokenId) {
		int row = 0;
		try {

			String sql = "UPDATE dcp.dbo.TBL_ASNB_TXN set IS_RECONCILED = 1 WHERE TXN_TOKEN_ID =?";
			logger.error("Update record : " + sql);
			row = getJdbcTemplate().update(sql, tokenId);

		} catch (Exception e) {
			logger.error(e);
		}

		return row;
	}

	public static boolean checkTxnNum(List<String> templsst, String num) {

		for (String val : templsst) {
			if (num.equals(val)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Auto-generated method stub

	}

}
