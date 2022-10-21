package com.rhbgroup.dcpbo.system.downtime.service;

import org.springframework.http.ResponseEntity;

import com.rhbgroup.dcpbo.system.common.BoData;

public interface ServerDateService {
	public ResponseEntity<BoData> getServerDate();
}
