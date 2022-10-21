package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_USER_PROFILE")
public class UserProfile implements Serializable {

	private static final long serialVersionUID = -2040142760477119898L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
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

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "ID_TYPE")
	private String idType;

	@Column(name = "ID_NO")
	private String idNo;

	@Column(name = "IS_QUICK_LOGIN_PIN_ENABLED")
	private Boolean isQuickLoginPinEnabled = Boolean.FALSE;

	@Column(name = "USER_TYPE")
	private String userType;

	@Column(name = "PROFILE_PICTURE")
	private Blob profilePicture;

	@Column(name = "FAILED_LOGIN_COUNT")
	private Integer failedLoginCount;

	@Column(name = "FAILED_PIN_LOGIN_COUNT")
	private Integer failedPinLoginCount;

	@Column(name = "FAILED_CHALLENGE_COUNT")
	private Integer failedChallengeCount;

	@Column(name = "USER_STATUS")
	private String userStatus;

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

	@Column(name = "TNC_ACCEPTED_SECUREPLUS_VERSION")
	private Integer tncAcceptedSecureplusVersion;

	@Column(name = "TXN_SIGNING_DEVICE")
	private Integer txnSigningDevice;

	@Column(name = "CONSENT_RHB_GROUP")
	private Boolean consentRhbGroup = Boolean.FALSE;

	@Column(name = "CONSENT_RHB_PARTNERS")
	private Boolean consentRhbPartners = Boolean.FALSE;

	@Column(name = "ENCRYPTED_AN_PIN_1")
	private String encryptedAnPin1;

	@Column(name = "ENCRYPTED_AN_PIN_2")
	private String encryptedAnPin2;

	@Column(name = "PROFILE_PICTURE_UPDATE_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date profilePictureUpdateTime;

	@Column(name = "ENCRYPTED_QUICK_LOGIN_PIN")
	private String encryptedQuickLoginPin;

	@Column(name = "IS_STAFF")
	private boolean isStaff;

	@Column(name = "IS_PREMIER")
	private String isPremier;




}
