package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;

@ToString
@Setter
@Getter
@Entity
@Table(name="TBL_OLA_TOKEN")
public class OlaToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;
    @Column(name = "username")
    private String username;
    @Column(name = "token")
    private String token;
    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(name = "mobile_no")
    private String mobileNo;
    @Column(name = "id_type")
    private String idType;
    @Column(name = "id_no")
    private String idNo;
    @Column(name = "status")
    private String status;
    @Column(name = "updated_time")
    private String updatedTime;
    @Column(name = "created_time")
    private String createdTime;
    @Column(name="channel")
    private String channel;
    @Column(name="api_status_desc")
    private String apiStatusDesc;
    @Column(name="api_status_code")
    private String apiStatusCode;
    @Column(name="ref_id")
    private String refId;
    @Column(name="txn_status_code")
    private String txnStatusCode;
    @Column(name="ip_address")
    private String ipAddress;
    @Column(name="audit_additionaldata")
    private String auditAdditionalData;
    @Column(name="aml_screening_result")
    private String amlScreeningResult;
    @Column(name ="assessment_risk_level")
    private String assessmentRiskLevel;
}
