package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.data.repository.CommonRepository;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaRateLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractExchangeRateService;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.CurrencyExchangeRate;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.ExtractExchangeRateResponse;

@Service
public class ExtractExchangeRateServiceImpl implements ExtractExchangeRateService {
	private static Logger logger = LogManager.getLogger(ExtractExchangeRateServiceImpl.class);

	private GetMcaRateLogic getMcaRateLogic;

    public ExtractExchangeRateServiceImpl(
            GetMcaRateLogic getMcaRateLogic) {
        this.getMcaRateLogic = getMcaRateLogic;
    }

	@Override
	public BoData getExchangeRate(String code) {

		ExtractExchangeRateResponse extractExchangeRateResponse = new ExtractExchangeRateResponse();

		try {
			logger.debug("#############   code: |" + code + "|");
			String[] currencyCodeArr = code.toUpperCase().split(",");

			List<String> currencyList = new LinkedList<String>();
			for (int i = 0; i < currencyCodeArr.length; i++) {
				currencyList.add(currencyCodeArr[i]);
			}

			if (currencyList.size() > 0) {
				CurrencyExchangeRate currencyCodeList = new CurrencyExchangeRate();
				currencyCodeList.setCode(currencyList);

				String jsonStr = JsonUtil.objectToJson(currencyCodeList);


				Capsule capsule = new Capsule();
				capsule.updateCurrentMessage(jsonStr);
				capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
				capsule.setMessageId(UUID.randomUUID().toString());
				capsule.setProperty(Constants.OPERATION_NAME, "GetMcaRate");
				logger.debug("capsule before: {}", capsule);

				try {
					capsule = getMcaRateLogic.executeBusinessLogic(capsule);
					logger.debug("    capsule: " + capsule);
					logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());
				} catch (Exception ex) {
					logger.error(ex);
				}

				if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful()) {
					throw new CommonException("50009", "Currency codes not found.");
				}

				jsonStr = capsule.getCurrentMessage();
				logger.debug("        jsonStr: " + jsonStr);

				extractExchangeRateResponse = JsonUtil.jsonToObject(jsonStr, ExtractExchangeRateResponse.class);
			}

		} catch (Exception e) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Error calling " + e.getMessage() + ".executeBusinessLogic()");
		}
		logger.debug("    extractExchangeRateResponse: " + extractExchangeRateResponse);

		return extractExchangeRateResponse;
	}
}
