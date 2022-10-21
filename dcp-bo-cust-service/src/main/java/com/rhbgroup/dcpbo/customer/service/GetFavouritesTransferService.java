package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.data.common.TransactionType;
import com.rhbgroup.dcp.data.repository.CommonRepository;
import com.rhbgroup.dcpbo.customer.dto.FavouritesTransfer;
import com.rhbgroup.dcpbo.customer.dto.FavouritesTransferPaymentType;
import com.rhbgroup.dcpbo.customer.enums.DuitNowIdType;
import com.rhbgroup.dcpbo.customer.enums.FundTransferMainFunctionType;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.Bank;
import com.rhbgroup.dcpbo.customer.model.ProfileFavourite;
import com.rhbgroup.dcpbo.customer.repository.BankRepository;
import com.rhbgroup.dcpbo.customer.repository.CountryRepository;
import com.rhbgroup.dcpbo.customer.repository.LookupListOfValueRepository;
import com.rhbgroup.dcpbo.customer.repository.ProfileFavouriteRepo;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
public class GetFavouritesTransferService {

	@Autowired
	ProfileFavouriteRepo profileFavouriteRepo;

	@Autowired
	BankRepository bankRepository;

	@Autowired
	CountryRepository countryRepository;

	@Autowired
	LookupListOfValueRepository lookupListOfValueRepository;

	public static final String type="pay-banks-payment-type";

	//Retrieve customer's favourite transfer transaction
	public FavouritesTransfer retrieveFavouritesTransfer(Integer favouritesId){
		ProfileFavourite profileFavourite = profileFavouriteRepo.findOne(favouritesId);
		if (profileFavourite == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "No favourite profile for favourites id " + favouritesId);

		String txnType = profileFavourite.getTxnType();
		if (!txnType.equals(TransactionType.TRANSFER))
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Transaction time is not transfer! Please call the correct endpoint for transaction type " + txnType);

		String mainFunction = profileFavourite.getMainFunction();
		String toldType = profileFavourite.getToIdType();
		FavouritesTransfer favouritesTransfer;
		FavouritesTransferPaymentType favouritesTransferPaymentType = new FavouritesTransferPaymentType();
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		favouritesTransfer = modelMapper.map(profileFavourite, FavouritesTransfer.class	);
		favouritesTransfer.setRecipientRef(profileFavourite.getRef1());
		favouritesTransfer.setToldType(profileFavourite.getToIdType());
		favouritesTransfer.setToldNo(profileFavourite.getToIdNo());

		if (mainFunction.equals(FundTransferMainFunctionType.OTHER_RHB.name()) || mainFunction.equals(FundTransferMainFunctionType.IBG.name()) || mainFunction.equals(FundTransferMainFunctionType.INSTANT.name())){
			Integer payeeId = profileFavourite.getPayeeId();

			Bank bank = bankRepository.findOne(payeeId);
			if (bank == null ) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Bank not found for payee id " + payeeId);
			}
			String payeeName = bank.getBankName();
			favouritesTransfer.setPayeeName(payeeName);
		}

		if (mainFunction.equals(FundTransferMainFunctionType.IBG.name()) || mainFunction.equals(FundTransferMainFunctionType.IBFT.name()) || mainFunction.equals(FundTransferMainFunctionType.INSTANT.name())){
			String description;
			String code =profileFavourite.getSubFunction();
			try {
				description = lookupListOfValueRepository.findDescriptionByTypeAndCode(type,code);
			}catch (Exception e ){
				e.printStackTrace();
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Function getListofValueLookup failed with type = " + type
				+ " and code = " + profileFavourite.getSubFunction() );
			}
			favouritesTransferPaymentType.setCode(code);
			favouritesTransferPaymentType.setDescription(description);

			favouritesTransfer.setPaymentType(favouritesTransferPaymentType);
		}

		if (mainFunction.equals(FundTransferMainFunctionType.DUITNOW.name()) && toldType.equals(DuitNowIdType.PSPT.name())) {
			String countryCode = profileFavourite.getDuitnowCountryCode();
			String countryName = countryRepository.findCountryNameByCountryCode(countryCode);
			if (countryName == null)
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Country name not found for country code " + countryCode);
			favouritesTransfer.setDuitnowCountryName(countryName);
		}

		return favouritesTransfer;
	}
}
