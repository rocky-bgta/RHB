package com.rhbgroup.dcpbo.customer.service.impl;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.BillerResponse;
import com.rhbgroup.dcpbo.customer.dto.TotalBillersResponse;
import com.rhbgroup.dcpbo.customer.repository.BillerRepo;
import com.rhbgroup.dcpbo.customer.repository.TopupBillerRepo;
import com.rhbgroup.dcpbo.customer.service.BillerService;

@Service
public class BillerServiceImpl implements BillerService {

	@Autowired
	private BillerRepo billerRepo;
	
	@Autowired
	private TopupBillerRepo topupBillerRepo;
	
	private static Logger logger = LogManager.getLogger(BillerServiceImpl.class);
	
	@Override
	public BoData getBillerCount() {
		logger.info("inside getBillerCount()");
		int billerCount = billerRepo.getCount();
		int topupBillerCount = topupBillerRepo.getCount();
		BillerResponse billerResponse = new BillerResponse();
		TotalBillersResponse totalBiller = new TotalBillersResponse();
		totalBiller.setTotal(billerCount + topupBillerCount);
		billerResponse.setBiller(totalBiller);
		return billerResponse;
	}

}
