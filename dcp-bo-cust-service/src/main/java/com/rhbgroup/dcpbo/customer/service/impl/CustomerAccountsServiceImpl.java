package com.rhbgroup.dcpbo.customer.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rhbgroup.dcp.deposits.mca.model.bizlogic.response.McaAccountsDcpResponse;
import com.rhbgroup.dcp.investments.bizlogic.GetUnitTrustLogic;
import com.rhbgroup.dcp.investments.model.DcpGetUnitTrustResponse;
import com.rhbgroup.dcp.profiles.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.asnb.model.DcpAsnbAccount;
import com.rhbgroup.dcp.asnb.model.DcpAsnbAccountInqRequest;
import com.rhbgroup.dcp.asnb.model.DcpAsnbAccountInqResponse;
import com.rhbgroup.dcp.asnb.model.DcpAsnbAccountResponse;
import com.rhbgroup.dcp.creditcards.bizlogic.GetCreditCardsLogic;
import com.rhbgroup.dcp.creditcards.model.CreditCard;
import com.rhbgroup.dcp.creditcards.model.CreditCardDcpRequest;
import com.rhbgroup.dcp.creditcards.model.CreditCardDcpResponse;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.casa.bizlogic.GetCasaDepositsSummaryLogic;
import com.rhbgroup.dcp.deposits.casa.model.AccountsSummary;
import com.rhbgroup.dcp.deposits.casa.model.TermDepositsDcpResponse;
import com.rhbgroup.dcp.loans.bizlogic.GetLoansLogic;
import com.rhbgroup.dcp.loans.model.LoansDcpRequest;
import com.rhbgroup.dcp.loans.model.LoansDcpResponse;
import com.rhbgroup.dcp.loans.model.PersonalFinanceAccountsInquiry;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.profiles.bizlogic.GetAccountProfilesLogic;
import com.rhbgroup.dcp.uber.asnb.bizlogic.GetAsnbAccountInquiryLogic;
import com.rhbgroup.dcp.uber.deposits.casa.bizlogic.GetTermDepositsLogic;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.AsnbAccounts;
import com.rhbgroup.dcpbo.customer.dto.CustomerAccounts;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.service.CustomerAccountsService;
import com.rhbgroup.dcpbo.customer.model.AppConfig;
import com.rhbgroup.dcpbo.customer.repository.AppConfigRepository;
import com.rhbgroup.dcpbo.customer.utils.CustomerServiceConstant;

@Service
public class CustomerAccountsServiceImpl implements CustomerAccountsService {
	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private AppConfigRepository appConfigRepository;

	@Autowired
	private GetAccountProfilesLogic getAccountProfilesLogic;
	

	@Autowired
	private GetCasaDepositsSummaryLogic getCasaDepositsSummaryLogic;

	@Autowired
	private GetCreditCardsLogic getCreditCardsLogic;

	@Autowired
	private GetLoansLogic getLoansLogic;

	@Autowired
	private GetTermDepositsLogic getTermDepositsLogic;

	@Autowired
	@Qualifier(value = "getMcaDepositsLogic")
	private BusinessAdaptor getMcaDepositsLogic;

	@Autowired
	private GetUnitTrustLogic getUnitTrustLogic;
	
	@Autowired
	private GetAsnbAccountInquiryLogic getAsnbAccountInquiryLogic;

	private static Logger logger = LogManager.getLogger(CustomerAccountsServiceImpl.class);

	private static final String JOINT_ACCOUNT = "UT Joint Account";
	private static final String INDIVIDUAL_ACCOUNT = "UT Individual Account";

	/*
	 * This is implemented as part of DCPBL-7820 user story
	 */
	public BoData getCustomerAccounts(@RequestHeader("customerId") Integer customerId) {	

		/*
		 * Get cisNo from DCP ProfileRepository. In DCP, customerId is userId.
		 */
		int userId = customerId;
		UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);
		
		if (userProfile == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE);

		/*
		 * Create CustomerAccounts instance, then add various account lists to it
		 */
		CustomerAccounts customerAccounts = new CustomerAccounts();

		/*
		 * Retrieve full account list
		 */
		try {
			String jsonStr = callDcpBusinessLogic(getAccountProfilesLogic, userProfile, "{}");
			DcpGetAccountsProfileResponse accounts = JsonUtil.jsonToObject(jsonStr,
					DcpGetAccountsProfileResponse.class);

			accounts = checkNickname(accounts);
			customerAccounts.setAccounts(accounts);

			/*
			 * Retrieve CASA details
			 */

			casaBusinesslogic(userProfile,accounts,customerAccounts);

			/*
			 * Retrieve credit card details
			 */
			creditCardBusinesslogic(userProfile,accounts,customerAccounts);
			
			/*
			 * Retrieve prepaid card details
			 */
			List<DcpPrepaidCardsAccount> prepaidCardList = accounts.getPrepaidCards();
			if (prepaidCardList != null && !prepaidCardList.isEmpty()) {
				List<String> cardNoList = new LinkedList<String>();
				prepaidCardList.forEach(prepaidCard -> cardNoList.add(prepaidCard.getCardNo()));

				CreditCardDcpRequest prepaidCardDcpRequest = new CreditCardDcpRequest();
				prepaidCardDcpRequest.setCardNo(cardNoList);
				jsonStr = JsonUtil.objectToJson(prepaidCardDcpRequest);
				prepaidCardBusinesslogic(userProfile,jsonStr,accounts,customerAccounts);
			}
			
			/*
			 * Retrieve debit card details
			 */
			List<DcpDebitCardsAccount> debitCardList = accounts.getDebitCards();
			if (debitCardList != null && !debitCardList.isEmpty()) {
				List<String> cardNoList = new LinkedList<String>();
				debitCardList.forEach(debitCard -> cardNoList.add(debitCard.getCardNo()));
				CreditCardDcpRequest debitCardDcpRequest = new CreditCardDcpRequest();
				debitCardDcpRequest.setCardNo(cardNoList);
				jsonStr = JsonUtil.objectToJson(debitCardDcpRequest);
				debitCardBusinesslogic(userProfile,jsonStr,accounts,customerAccounts);
			}

			/*
			 * Retrieve term deposit details
			 */
			List<DcpTermDepositsAccount> termDepositList = accounts.getTermDeposits();		
			if (termDepositList != null && !termDepositList.isEmpty()) {
				List<String> accountNoList = new LinkedList<String>();
				termDepositList.forEach(termDeposit -> accountNoList.add(termDeposit.getAccountNo()));

				PersonalFinanceAccountsInquiry termDepositRequest = new PersonalFinanceAccountsInquiry();
				termDepositRequest.setAccountNo(accountNoList);
				jsonStr = JsonUtil.objectToJson(termDepositRequest);
				termBusinesslogic(userProfile,jsonStr,customerAccounts);
			}

			/*
			 * Retrieve loan details
			 */
			loanBusinesslogic(userProfile,accounts,customerAccounts);
			
			// Retrieve mca details - DCPBL-15063
			mcaBusinesslogic(userProfile,accounts,customerAccounts);

			// Retrieve Unit Trust Account details
			unitTrustBusinesslogic(userProfile,accounts,customerAccounts);
			
			/*
			 * Retrieve Invest details
			 */
			asnbBusinesslogic(userProfile,accounts,customerAccounts);
	
		} catch (Exception e) {
			logger.error("Calling DcpBusinessLogic failed with error", e);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Error calling " + e.getMessage() + ".executeBusinessLogic()");
		}
		return customerAccounts;
	}

	private String callDcpBusinessLogic(BusinessAdaptor businessAdaptor, UserProfile userProfile, String currentMessage)
			throws DcpBusinessLogicException {
		String cisNo = userProfile.getCisNo();

		int userId = userProfile.getId();
		
		String username = userProfile.getUsername();

		Capsule capsule = new Capsule();
		capsule.setCisNumber(cisNo);
		capsule.setUserId(userId);
		capsule.setUserName(username);
		capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
		capsule.setMessageId(UUID.randomUUID().toString());
		capsule.setProperty(Constants.OPERATION_NAME, businessAdaptor.getClass().getSimpleName());
		capsule.updateCurrentMessage(currentMessage);

		String businessLogicName = businessAdaptor.getClass().getSimpleName();

		capsule = businessAdaptor.executeBusinessLogic(capsule);
		
		//capsule.getCurrentMessage().get

		if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful()) {
			throw new DcpBusinessLogicException("Error calling " + businessLogicName + ".executeBusinessLogic()",
					businessLogicName);
		}

		String jsonStr = capsule.getCurrentMessage();

		return jsonStr;
	}

	@SuppressWarnings("serial")
	class DcpBusinessLogicException extends Exception {
		private String businessLogicName;

		public DcpBusinessLogicException(String message, String businessLogicName) {
			super(message);
		}

		public String getBusinessLogicName() {
			return businessLogicName;
		}
	}

	private DcpGetAccountsProfileResponse checkNickname(DcpGetAccountsProfileResponse accounts) {
		accounts.getUnitTrusts().forEach(dcpUnitTrustAccount -> {
			if (dcpUnitTrustAccount.getNickname() == null) {
				if (dcpUnitTrustAccount.getJointType() != null && dcpUnitTrustAccount.getJointType().equals("I")) {
					dcpUnitTrustAccount.setNickname(INDIVIDUAL_ACCOUNT + " - " + dcpUnitTrustAccount.getAccountType());
				} else if (dcpUnitTrustAccount.getJointType() != null
						&& dcpUnitTrustAccount.getJointType().equals("J")) {
					dcpUnitTrustAccount.setNickname(JOINT_ACCOUNT + " - " + dcpUnitTrustAccount.getAccountType());
				}
			}
		});

		return accounts;
	}
	
	private void prepaidCardBusinesslogic(UserProfile userProfile, String jsonStr, DcpGetAccountsProfileResponse accounts, CustomerAccounts customerAccounts) {
		try {
			jsonStr = callDcpBusinessLogic(getCreditCardsLogic, userProfile, jsonStr);
			CreditCardDcpResponse prepaidCardDcpResponse = JsonUtil.jsonToObject(jsonStr,
					CreditCardDcpResponse.class);

			List<String> primaryCards = accounts.getPrepaidCards().stream()
					.filter(x -> x.getConnectorCode().equals("PRIIND")).map(x -> x.getCardNo())
					.collect(Collectors.toList());
	
			List<CreditCard> modPrepaidCardList = prepaidCardDcpResponse.getPrepaidCards().stream().map(x -> {
				CreditCard modifiedPC = new CreditCard();
				modifiedPC = x;
				
				if (!primaryCards.contains(modifiedPC.getCardNo().trim())) {
					modifiedPC.setLabelTagging("Supplementary");
					modifiedPC.setIsPrincipal(false);
				} else {
					modifiedPC.setIsPrincipal(true);
				}
				return modifiedPC;
			}).collect(Collectors.toList());
			
			prepaidCardDcpResponse.setPrepaidCards(modPrepaidCardList);
			
			CreditCardDcpResponse cardsResponse = customerAccounts.getCards();
			if(cardsResponse!=null) {
				prepaidCardDcpResponse.setCreditCards(cardsResponse.getCreditCards());
			}
			
			customerAccounts.setCards(prepaidCardDcpResponse);			
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void debitCardBusinesslogic(UserProfile userProfile, String jsonStr, DcpGetAccountsProfileResponse accounts, CustomerAccounts customerAccounts) {
		try {
			jsonStr = callDcpBusinessLogic(getCreditCardsLogic, userProfile, jsonStr);
			CreditCardDcpResponse debitCardDcpResponse = JsonUtil.jsonToObject(jsonStr,
					CreditCardDcpResponse.class);

			List<String> primaryCards = accounts.getDebitCards().stream()
					.map(x -> x.getCardNo())
					.collect(Collectors.toList());
			
			List<CreditCard> modDebitCardList = debitCardDcpResponse.getDebitCards().stream().map(x -> {
				CreditCard modifiedPC = new CreditCard();
				modifiedPC = x;
				
				if (!primaryCards.contains(modifiedPC.getCardNo().trim())) {
					modifiedPC.setLabelTagging("Supplementary");
					modifiedPC.setIsPrincipal(false);
				} else {
					modifiedPC.setIsPrincipal(true);
				}
				return modifiedPC;
			}).collect(Collectors.toList());
			
			debitCardDcpResponse.setDebitCards(modDebitCardList);
			
			CreditCardDcpResponse cardsResponse = customerAccounts.getCards();
			if(cardsResponse!=null) {
				debitCardDcpResponse.setCreditCards(cardsResponse.getCreditCards());
				debitCardDcpResponse.setPrepaidCards(cardsResponse.getPrepaidCards());
			}
			
			customerAccounts.setCards(debitCardDcpResponse);			
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void termBusinesslogic(UserProfile userProfile, String jsonStr, CustomerAccounts customerAccounts) {
		try {
			jsonStr = callDcpBusinessLogic(getTermDepositsLogic, userProfile, jsonStr);
			TermDepositsDcpResponse termDepositsDcpResponse = JsonUtil.jsonToObject(jsonStr,
					TermDepositsDcpResponse.class);

			customerAccounts.setTermDeposits(termDepositsDcpResponse);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private void casaBusinesslogic(UserProfile userProfile, DcpGetAccountsProfileResponse accounts, CustomerAccounts customerAccounts) {
		try {

			List<String> accountList = new ArrayList<>();
			if (accounts.getCurrents() != null) {
				for (DcpCurrentsAccount currentsAccount : accounts.getCurrents()) {
					accountList.add(currentsAccount.getAccountNo());
				}
			}

			if (accounts.getSavings() != null) {
				for (DcpSavingsAccount savingsAccount : accounts.getSavings()) {
					accountList.add(savingsAccount.getAccountNo());
				}
			}

			Map<String, List<String>> accountNoList = new HashMap<>();
			accountNoList.put("accountNo", accountList);

			String body = JsonUtil.objectToJson(accountNoList);

			String jsonStr = callDcpBusinessLogic(getCasaDepositsSummaryLogic, userProfile, body);
			AccountsSummary casaAccountsSummary = JsonUtil.jsonToObject(jsonStr, AccountsSummary.class);

			customerAccounts.setCasa(casaAccountsSummary);

			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private void creditCardBusinesslogic(UserProfile userProfile, DcpGetAccountsProfileResponse accounts, CustomerAccounts customerAccounts) {
		List<DcpCreditCardsAccount> creditCardList = accounts.getCreditCards();
			
		if (creditCardList != null && !creditCardList.isEmpty()) {
			List<String> cardNoList = new LinkedList<>();
			creditCardList.forEach(creditCard -> cardNoList.add(creditCard.getCardNo()));

			CreditCardDcpRequest creditCardDcpRequest = new CreditCardDcpRequest();
			creditCardDcpRequest.setCardNo(cardNoList);
			String jsonStr = JsonUtil.objectToJson(creditCardDcpRequest);

			try {
				jsonStr = callDcpBusinessLogic(getCreditCardsLogic, userProfile, jsonStr);
				CreditCardDcpResponse creditCardDcpResponse = JsonUtil.jsonToObject(jsonStr,
						CreditCardDcpResponse.class);					

				List<String> primaryCards = accounts.getCreditCards().stream()
						.filter(x -> x.getConnectorCode().equals("PRIIND")).map(x -> x.getCardNo())
						.collect(Collectors.toList());
				
				List<CreditCard> modCreditCardList  = creditCardDcpResponse.getCreditCards().stream().map(x -> {
					CreditCard modifiedCC = new CreditCard();
					modifiedCC = x;
					
					if (!primaryCards.contains(modifiedCC.getCardNo().trim())) {
						modifiedCC.setLabelTagging("Supplementary");
						modifiedCC.setIsPrincipal(false);
					} else {
						modifiedCC.setIsPrincipal(true);
					}
					return modifiedCC;
				}).collect(Collectors.toList());
				creditCardDcpResponse.setCreditCards(modCreditCardList);				
				customerAccounts.setCards(creditCardDcpResponse);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	private void loanBusinesslogic(UserProfile userProfile, DcpGetAccountsProfileResponse accounts, CustomerAccounts customerAccounts) {
		LoansDcpRequest loansDcpRequest = new LoansDcpRequest();
			
		Boolean EAINeeded = Boolean.FALSE;

		List<DcpPersonalFinancesAccount> personalFinanceList = accounts.getPersonalFinances();
		if (personalFinanceList != null && !personalFinanceList.isEmpty()) {
			List<String> accountNoList = new LinkedList<String>();
			personalFinanceList.forEach(personalFinance -> accountNoList.add(personalFinance.getAccountNo()));
			loansDcpRequest.setPersonalFinanceAccount(accountNoList);
			EAINeeded = Boolean.TRUE;
		}

		List<DcpAsbAccount> asbList = accounts.getAsb();
		if (asbList != null && !asbList.isEmpty()) {
			List<String> accountNoList = new LinkedList<String>();
			asbList.forEach(asb -> accountNoList.add(asb.getAccountNo()));
			loansDcpRequest.setAsbAccount(accountNoList);
			EAINeeded = Boolean.TRUE;
		}
			
		List<DcpMortgagesAccount> mortgageList = accounts.getMortgages();
		if (mortgageList != null && !mortgageList.isEmpty()) {
			List<String> accountNoList = new LinkedList<String>();
			mortgageList.forEach(mortgage -> accountNoList.add(mortgage.getAccountNo()));
			loansDcpRequest.setMortgageAccount(accountNoList);
			EAINeeded = Boolean.TRUE;
		}

		List<DcpHirePurchasesAccount> hirePurchaseList = accounts.getHirePurchases();
		if (hirePurchaseList != null && !hirePurchaseList.isEmpty()) {
			List<String> accountNoList = new LinkedList<String>();
			hirePurchaseList.forEach(hirePurchase -> accountNoList.add(hirePurchase.getAccountNo()));
			loansDcpRequest.setHirePurchaseAccount(accountNoList);
			EAINeeded = Boolean.TRUE;
		}

		String jsonStr = JsonUtil.objectToJson(loansDcpRequest);		

		if (EAINeeded == Boolean.TRUE) {
			try {
				jsonStr = callDcpBusinessLogic(getLoansLogic, userProfile, jsonStr);
				LoansDcpResponse loansDcpResponse = JsonUtil.jsonToObject(jsonStr, LoansDcpResponse.class);

				customerAccounts.setLoans(loansDcpResponse);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	private void mcaBusinesslogic(UserProfile userProfile, DcpGetAccountsProfileResponse accounts, CustomerAccounts customerAccounts) {
		List<DcpMultiCurrencyAccount> mcaList = accounts.getMca();
			if (mcaList != null && !mcaList.isEmpty()) {
				List<String> accountNoList = new LinkedList<String>();
				mcaList.forEach(mca -> accountNoList.add(mca.getAccountNo()));

				PersonalFinanceAccountsInquiry mcaRequest = new PersonalFinanceAccountsInquiry();
				mcaRequest.setAccountNo(accountNoList);
				String jsonStr = JsonUtil.objectToJson(mcaRequest);			

				try {
					jsonStr = callDcpBusinessLogic(getMcaDepositsLogic, userProfile, jsonStr);
					McaAccountsDcpResponse mcaAccountsDcpResponse = JsonUtil.jsonToObject(jsonStr,
							McaAccountsDcpResponse.class);
					customerAccounts.setMca(mcaAccountsDcpResponse.getMca());
				} catch (Exception e) {
					logger.error(e);
				}
			}
	}

	private void unitTrustBusinesslogic(UserProfile userProfile, DcpGetAccountsProfileResponse accounts, CustomerAccounts customerAccounts) {
		List<DcpUnitTrustAccount> unitTrustAccList = accounts.getUnitTrusts();

		if (unitTrustAccList != null && !unitTrustAccList.isEmpty()) {
			List<String> accountNoList = new LinkedList<String>();
			unitTrustAccList.forEach(unitTrustAcc -> accountNoList.add(unitTrustAcc.getAccountNo()));

			PersonalFinanceAccountsInquiry utRequest = new PersonalFinanceAccountsInquiry();

			utRequest.setAccountNo(accountNoList);
			String jsonStr = JsonUtil.objectToJson(utRequest);

			try {
				jsonStr = callDcpBusinessLogic(getUnitTrustLogic, userProfile, jsonStr);
				DcpGetUnitTrustResponse dcpGetUnitTrustResponse = JsonUtil.jsonToObject(jsonStr,
						DcpGetUnitTrustResponse.class);

				customerAccounts.setUnitTrust(dcpGetUnitTrustResponse);
			} catch (Exception e) {
				logger.error(e);
			}
		}		
	}

	private void asnbBusinesslogic(UserProfile userProfile, DcpGetAccountsProfileResponse accounts, CustomerAccounts customerAccounts) {
		List<com.rhbgroup.dcp.profiles.model.DcpAsnbAccount> asnbList = accounts.getAsnb();
			List<DcpAsnbAccount> accountList = new ArrayList<>();
			List<AsnbAccounts> asnbAccountsLst = new ArrayList<>();

			Map<String, Boolean> tmpMap = new HashMap<>();
			
			if (asnbList != null && !asnbList.isEmpty()) {
				for (com.rhbgroup.dcp.profiles.model.DcpAsnbAccount asnbaccount : asnbList) {
					DcpAsnbAccount account = new DcpAsnbAccount();
					tmpMap.put(asnbaccount.getMembershipNumber(), asnbaccount.getIsMinor());
					account.setMemberIdNumber(asnbaccount.getIdentificationNumber());
					account.setMemberIdType(asnbaccount.getIdentificationType());
					account.setMembershipNumber(asnbaccount.getMembershipNumber());
					Boolean b = asnbaccount.getIsMinor();
					if(Boolean.TRUE.equals(b)) {
						account.setGuardianIdNumber(asnbaccount.getGuardianIdNumber());
						account.setGuardianIdType(asnbaccount.getGuardianIdType());
						
					}
					accountList.add(account);
				}
				
				DcpAsnbAccountInqRequest asnbInquiryRequest = new DcpAsnbAccountInqRequest();
				asnbInquiryRequest.setAsnb(accountList);
				
				String jsonStr = JsonUtil.objectToJson(asnbInquiryRequest);	

				try {
					jsonStr = callDcpBusinessLogic(getAsnbAccountInquiryLogic, userProfile, jsonStr);
					DcpAsnbAccountResponse asnbAccountsDcpResponse = JsonUtil.jsonToObject(jsonStr,
							DcpAsnbAccountResponse.class);
					
					/*
					* Retrieve CMS Url
					*/
					AppConfig appConfig = appConfigRepository.getParameterValue(CustomerServiceConstant.CMS_URL);

					for (DcpAsnbAccountInqResponse inqResponse :  asnbAccountsDcpResponse.getAsnbFunds()) {

						inqResponse.getFundDetail().forEach(fund -> {
							if (appConfig != null){
								fund.setImageUrl(appConfig.getParameterValue()+fund.getImageUrl());
							}
						});

						AsnbAccounts asnbAccounts = new AsnbAccounts();
						if(tmpMap.containsKey(inqResponse.getMembershipNumber())) {
							asnbAccounts.setMinor(tmpMap.get(inqResponse.getMembershipNumber()));
							asnbAccounts.setGrandtotalHoldings(inqResponse.getGrandTotalUnitHoldings());
							asnbAccounts.setFunds(inqResponse.getFundDetail());
							asnbAccounts.setMembershipNumber(inqResponse.getMembershipNumber());
							asnbAccounts.setGuardianIdNumber(inqResponse.getGuardianIdNumber());
							asnbAccounts.setMemberIdentificationNumber(inqResponse.getIdentificationNumber());
							asnbAccounts.setMemberIdType(inqResponse.getIdentificationType());
							asnbAccounts.setAccountHolderName(inqResponse.getMemberName());
							asnbAccounts.setNickName(inqResponse.getNickName());
							asnbAccountsLst.add(asnbAccounts);
						}
												
					}
					customerAccounts.setAsnb(asnbAccountsLst);
				
				} catch (Exception e) {
					logger.error(e);
				}
			}
	}
}
