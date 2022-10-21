package com.rhbgroup.dcpbo.system.downtime.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.ServerDate;
import com.rhbgroup.dcpbo.system.downtime.service.ServerDateService;

@Service
public class ServerDateServiceImpl implements ServerDateService {

	private static Logger logger = LogManager.getLogger(ServerDateServiceImpl.class);
	
	private static final String FORMAT_DATE = "yyyy-MM-dd";
	
	@Override
	public ResponseEntity<BoData> getServerDate() {
		logger.debug("getServerDate()");
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATE);
		
		ServerDate serverDate = new ServerDate();
		serverDate.setServerDate(simpleDateFormat.format(Calendar.getInstance().getTime()));
		logger.debug("    serverDate: " + serverDate);

		return new ResponseEntity<BoData>(serverDate, HttpStatus.OK);
	}
	
}
