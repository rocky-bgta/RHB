package com.rhbgroup.dcpbo.system.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TBL_BO_CONFIG_FUNCTION.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TBL_SYSTEM_DOWNTIME_CONFIG")
public class SystemDowntimeConfig implements Serializable {
	
	private static final long serialVersionUID = 6891908065661402237L;
	
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
	private static final String STATUS_ACTIVE = "Active";
	
	private static final String STATUS_INACTIVE = "Inactive";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "name", nullable = true)
	private String name;

	@Column(name = "header", nullable = true)
	private String header;

	@Column(name = "message_top", nullable = true)
	private String messageTop;

	@Column(name = "message_bottom", nullable = true)
	private String messageBottom;

	@Column(name = "start_time", nullable = false)
	private Timestamp startTime;
	
	@Column(name = "end_time", nullable = false)
	private Timestamp endTime;

	@Column(name = "is_active", nullable = false)
	private String isActive;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "adhoc_type", nullable = true)
	private String adhocType;
	
	@Column(name = "is_push_notification", nullable = true)
	private boolean isPushNotification;
	
	@Column(name = "push_date", nullable = false)
	private Date pushDate;
	
	@Column(name = "created_time", nullable = false)
	private Timestamp createdTime;

	@Column(name = "created_by", nullable = false)
	private String createdBy;
	
	@Column(name = "updated_time", nullable = false)
	private Timestamp updatedTime;

	@Column(name = "updated_by", nullable = false)
	private String updatedBy;
	
	@Column(name = "bank_id", nullable = true)
	private Integer bankId;
	
	@Column(name = "adhoc_type_category", nullable = true)
	private String adhocTypeCategory;
	
    public String getStartTimeString() {
    	DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
    	String strDate = dateFormat.format(getStartTime()); 
    	return strDate;
    }
    
    public String getEndTimeString() {
    	DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
    	String strDate = dateFormat.format(getEndTime()); 
    	return strDate;
    }
    
    public String getPushDateString() {
    	DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    	String strDate = null;
    	if (getPushDate() != null) {
    		strDate = dateFormat.format(getPushDate());
    	}
    	return strDate;
    }
    
    public String getStatus(Timestamp now) {
    	if ((getStartTime().before(now) || getStartTime().equals(now)) &&
    			(getEndTime().after(now) || getEndTime().equals(now))) {
    		return STATUS_ACTIVE;
    	} else if (getStartTime().after(now)) {
    		return STATUS_INACTIVE;
    	}
    	return null;
    }
    
    
}