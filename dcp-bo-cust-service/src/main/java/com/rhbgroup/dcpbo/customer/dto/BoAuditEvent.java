package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Setter
@Getter
@Entity
@ToString
public class BoAuditEvent {
    @Id
    private Integer id;
    private String moduleName;
    private String activityName;
    private String customerSignOnId;
    private String event;
    private String details;
    private String makerActivity;
    private String approverActivity;
    private String accessStaffAccount;
    private String username;
    private String timestamp;
}
