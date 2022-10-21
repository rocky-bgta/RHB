package com.rhbgroup.dcpbo.customer.dto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.rhbgroup.dcpbo.customer.model.TelemetryLogPayload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class TelemetryLogPayloadDetails {
	private String payload;
    private String auditParam;
    private String hostname;
    private String cisNumber;
    private String auditDateTime;
    private String deviceId;
    
    public TelemetryLogPayloadDetails(TelemetryLogPayload telemetryLogPayload) {
    	if (telemetryLogPayload != null) {
    		
            byte[] bytePayload = telemetryLogPayload.getPayload();

            try {
            	payload = new String(bytePayload, "UTF-8");
            }
            catch (Exception e){
            	payload = "";
                e.printStackTrace();
            }
    		auditParam = telemetryLogPayload.getAuditParam();
    		hostname = telemetryLogPayload.getHostname();
    		cisNumber = telemetryLogPayload.getCisNumber();
    		deviceId = telemetryLogPayload.getDeviceId();

    		if (telemetryLogPayload.getAuditDateTime() != null) {
    			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    			auditDateTime = dateFormat.format(telemetryLogPayload.getAuditDateTime()); 
    		}
    	}
    }
}
