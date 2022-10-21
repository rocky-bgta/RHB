package com.rhbgroup.dcpbo.customer.dto;

import java.util.ArrayList;
import java.util.List;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelemetryErrorLogs implements BoData {
	
	private List<TelemetryErrorLog> telemetryErrorLogs;
	
	private AuditPagination pagination;
	
	public TelemetryErrorLogs() {
		telemetryErrorLogs = new ArrayList<>();
	}
	
	public void addTelemetryErrorLog(TelemetryErrorLog telemetryErrorLog) {
		this.telemetryErrorLogs.add(telemetryErrorLog);
	}
	
}
