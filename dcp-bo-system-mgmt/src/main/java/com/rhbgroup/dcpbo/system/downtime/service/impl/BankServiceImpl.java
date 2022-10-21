package com.rhbgroup.dcpbo.system.downtime.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.AuditPagination;
import com.rhbgroup.dcpbo.system.downtime.dto.BankDetails;
import com.rhbgroup.dcpbo.system.downtime.dto.BankDetailsInfo;
import com.rhbgroup.dcpbo.system.downtime.repository.BankPaymentTypeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BankRepository;
import com.rhbgroup.dcpbo.system.downtime.service.BankService;
import com.rhbgroup.dcpbo.system.model.Bank;
import com.rhbgroup.dcpbo.system.model.BankPaymentTypeConfig;

@Service
public class BankServiceImpl implements BankService {

	@Autowired
	private BankRepository bankRepository;

	@Autowired
	private BankPaymentTypeConfigRepository bankPaymentTypeConfigRepository;

	private static final int PAGE_SIZE = 30;
	private static final String PAGINATION_LAST = "L";
	private static final String PAGINATION_NOT_LAST = "N";

	private static final String FLAG = "Y";
	private static final String FLAG_ON = "ON";
	private static final String FLAG_OFF = "OFF";

	@Override
	public BoData getBankPaymentType(Integer pageNum) {

		AuditPagination auditPagination = new AuditPagination();
		Integer totalPageNum = 0;
		Integer offset = 0;
		auditPagination.setPageNum(1);

		offset = (pageNum - 1) * PAGE_SIZE;
		auditPagination.setPageNum(pageNum);

		Integer recordCount = 0;

		List<Bank> bankList = bankRepository.getBankList(offset, PAGE_SIZE);
		recordCount = bankRepository.getBankListCount();
		totalPageNum = (int) Math.ceil((double) recordCount / PAGE_SIZE);

		BankDetailsInfo bankDetailsInfo = new BankDetailsInfo();

		List<BankDetails> bankdetails = new ArrayList();

		if (!bankList.isEmpty()) {

			bankList.forEach(x -> {
				BankDetails bank = new BankDetails();
				bank.setId(x.getId());
				bank.setName(x.getBankName());
				bank.setShortName(x.getBankNameShort());

				List<BankPaymentTypeConfig> bankPaymentTypeConfigs = bankPaymentTypeConfigRepository
						.getBankPaymentTypeConfigDetail(x.getId());

				bank.setIsIbg(FLAG_OFF);
				bank.setIsInstant(FLAG_OFF);
				bankPaymentTypeConfigs.forEach(b -> {
					if (b.getIbgFlag().equals(FLAG))
						bank.setIsIbg(FLAG_ON);

					if (b.getInstantFlag().equals(FLAG))
						bank.setIsInstant(FLAG_ON);
				});

				bankdetails.add(bank);
			});

		} else {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "No records in Bank Table");

		}
		if (bankList.size() < PAGE_SIZE) {
			auditPagination.setPageIndicator(PAGINATION_LAST);
		} else {
			auditPagination.setPageIndicator(PAGINATION_NOT_LAST);
		}

		auditPagination.setActivityCount(recordCount);
		auditPagination.setPageNum(pageNum);
		auditPagination.setTotalPageNum(totalPageNum);
		bankDetailsInfo.setBank(bankdetails);
		bankDetailsInfo.setPagination(auditPagination);
		return bankDetailsInfo;
	}

}
