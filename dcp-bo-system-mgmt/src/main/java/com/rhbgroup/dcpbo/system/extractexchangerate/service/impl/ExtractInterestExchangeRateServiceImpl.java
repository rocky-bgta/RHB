package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.data.repository.CommonRepository;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaInterestRateLogic;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaRateLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractExchangeRateService;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractInterestExchangeRateService;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.CurrencyExchangeRate;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.ExtractExchangeRateResponse;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.ExtractInterestExchangeRateRequest;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.ExtractInterestExchangeRateResponse;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.InterestRate;

@Service
public class ExtractInterestExchangeRateServiceImpl implements ExtractInterestExchangeRateService {
	private static Logger logger = LogManager.getLogger(ExtractInterestExchangeRateServiceImpl.class);

	private GetMcaInterestRateLogic getMcaInterestRateLogic;

    public ExtractInterestExchangeRateServiceImpl(
    		GetMcaInterestRateLogic getMcaInterestRateLogic) {
        this.getMcaInterestRateLogic = getMcaInterestRateLogic;
    }

	@Override
	public BoData getInterestExchangeRate(ExtractInterestExchangeRateRequest request) {

		ExtractInterestExchangeRateResponse extractInterestExchangeRateResponse = new ExtractInterestExchangeRateResponse();

		try {
			String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

			List<InterestRate> interestRate = request.getRate();
			for(InterestRate singleInterestRate:interestRate) {
				singleInterestRate.setCode(singleInterestRate.getCode().toUpperCase());
				singleInterestRate.setPrincipalAmount("1.00");
				singleInterestRate.setValueDate(date);
			}
			
			request.setRate(interestRate);
			
			if (interestRate.size() > 0) {

				String jsonStr = JsonUtil.objectToJson(request);

				Capsule capsule = new Capsule();
				capsule.updateCurrentMessage(jsonStr);
				capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
				capsule.setMessageId(UUID.randomUUID().toString());
				capsule.setProperty(Constants.OPERATION_NAME, "GetMcaInterestRate");
				logger.debug("capsule before: {}", capsule);
				capsule = callBusinessLogic(capsule);
				
				if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful()) {
					throw new CommonException("50009", "Currency codes not found.");
				}

				jsonStr = capsule.getCurrentMessage();
				logger.debug("        jsonStr: " + jsonStr);

				extractInterestExchangeRateResponse = JsonUtil.jsonToObject(jsonStr, ExtractInterestExchangeRateResponse.class);
			}

		} catch (Exception e) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Error calling " + e.getMessage() + ".executeBusinessLogic()");
		}
		logger.debug("    extractInterestExchangeRateResponse: " + extractInterestExchangeRateResponse);

		return extractInterestExchangeRateResponse;
	}
	
	private Capsule callBusinessLogic(Capsule capsule) {
		try {
			capsule = getMcaInterestRateLogic.executeBusinessLogic(capsule);
			logger.debug("    capsule: " + capsule);
			logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());
		} catch (Exception ex) {
			logger.error(ex);
		}
		return capsule;
	}
}
