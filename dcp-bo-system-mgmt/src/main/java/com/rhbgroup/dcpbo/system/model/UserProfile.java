/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rhbgroup.dcpbo.system.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Faizal Musa
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TBL_USER_PROFILE")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserProfile implements Serializable, Comparable<UserProfile>{
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "NAME")
    private String name;
    @Column(name = "NICKNAME")
    private String nickname;
    @Column(name = "MOBILE_NO")
    private String mobileNo;
    @Column(name = "EMAIL")
    private String email;
    @Column(name = "CIS_NO")
    private String cisNo;
    @Basic(optional = false)
    @Column(name = "PASSWORD")
    private String password;
    @Basic(optional = false)
    @Column(name = "ID_TYPE")
    private String idType;
    @Basic(optional = false)
    @Column(name = "ID_NO")
    private String idNo;
    @Basic(optional = false)
    @Column(name = "IS_QUICK_LOGIN_PIN_ENABLED")
    private boolean isQuickLoginPinEnabled;
    @Basic(optional = false)
    @Column(name = "USER_TYPE")
    private String userType;
    @Lob
    @Column(name = "PROFILE_PICTURE")
    private byte[] profilePicture;
    @Basic(optional = false)
    @Column(name = "FAILED_LOGIN_COUNT")
    private int failedLoginCount;
    @Basic(optional = false)
    @Column(name = "FAILED_PIN_LOGIN_COUNT")
    private int failedPinLoginCount;
    @Basic(optional = false)
    @Column(name = "FAILED_CHALLENGE_COUNT")
    private int failedChallengeCount;
    @Basic(optional = false)
    @Column(name = "USER_STATUS")
    private String userStatus;
    @Basic(optional = false)
    @Column(name = "LANGUAGE")
    private String language;
    @Column(name = "LAST_LOGIN")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;
    @Column(name = "DATE_OF_BIRTH")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    @Column(name = "TNC_ACCEPTED_PDPA_VERSION")
    private Integer tncAcceptedPdpaVersion;
    @Column(name = "TNC_ACCEPTED_GENERAL_VERSION")
    private Integer tncAcceptedGeneralVersion;
    @Column(name = "TNC_ACCEPTED_PRIVACY_VERSION")
    private Integer tncAcceptedPrivacyVersion;
    @Column(name = "CONSENT_RHB_GROUP")
    private Boolean consentRhbGroup;
    @Column(name = "CONSENT_RHB_PARTNERS")
    private Boolean consentRhbPartners;
    @Column(name = "ENCRYPTED_AN_PIN_1")
    private String encryptedAnPin1;
    @Column(name = "ENCRYPTED_AN_PIN_2")
    private String encryptedAnPin2;
    @Column(name = "PROFILE_PICTURE_UPDATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date profilePictureUpdateTime;
    @Column(name = "ENCRYPTED_QUICK_LOGIN_PIN")
    private String encryptedQuickLoginPin;
    @Column(name = "TNC_ACCEPTED_SECUREPLUS_VERSION")
    private Integer tncAcceptedSecureplusVersion;
    @Column(name = "TXN_SIGNING_DEVICE")
    private Integer txnSigningDevice;
    @Column(name = "IS_PREMIER")
    private Boolean isPremier;
    @Column(name = "UUID")
    private String uuid;
    @Basic(optional = false)
    @Column(name = "IS_STAFF")
    private boolean isStaff;
    @Column(name = "IS_PROFILE_INITIALISED")
    private Boolean isProfileInitialised;
    @Column(name = "MIGRATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date migrationDate;
    @Column(name = "DD_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ddDate;
    @Column(name = "POD_STATUS")
    private String podStatus;
    @Column(name = "POD_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date podDate;
    @Column(name = "AUDIENCE_ID")
    private String audienceId;

    @Override
    public int compareTo(UserProfile o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}

