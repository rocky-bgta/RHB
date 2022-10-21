package com.rhbgroup.dcpbo.user.info.model.bo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A user.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TBL_BO_USER")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements Serializable {

	public static final String TIMESTAMP_FORMAT  = "yyyy-MM-dd'T'HH:mm:ssXXX";
	public static final String TIMEZONE          = "Asia/Kuala_Lumpur";
	
	private static final long serialVersionUID = 6891908065661402667L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
	@JsonProperty("userId")
    private Integer id;

    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Column(name = "EMAIL", nullable = false)
    private String email;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "USER_DEPARTMENT_ID", nullable = false)
	@JsonProperty("departmentId")
    private Integer userDepartmentId;

    @Column(name = "USER_STATUS_ID", nullable = false)
	@JsonProperty("status")
    private String userStatusId;

    @Column(name = "LAST_LOGIN_TIME")
    @JsonFormat(pattern = TIMESTAMP_FORMAT, timezone = TIMEZONE)
    private Timestamp lastLoginTime;

    @Column(name = "FAILED_LOGIN_COUNT", nullable = false)
    private Integer failedLoginCount;

    @Column(name = "CREATED_TIME", nullable = false)
    @JsonFormat(pattern = TIMESTAMP_FORMAT, timezone = TIMEZONE)
    private Timestamp createdTime;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "UPDATED_TIME", nullable = false)
    @JsonFormat(pattern = TIMESTAMP_FORMAT, timezone = TIMEZONE)
    private Timestamp updatedTime;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;
    
    @Transient
    private String departmentName;
    
    @Transient
    private List<Usergroup> usergroup;
}
