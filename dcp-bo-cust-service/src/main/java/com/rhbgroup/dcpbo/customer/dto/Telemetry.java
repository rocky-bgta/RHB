package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Telemetry implements BoData {
    private String messageId;
    private String operationName;
    private String auditType;
    private String auditDateTime;
    private String username;
    private String totalError;
}
