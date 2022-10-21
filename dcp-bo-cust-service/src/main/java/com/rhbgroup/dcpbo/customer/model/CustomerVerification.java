package com.rhbgroup.dcpbo.customer.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_CUST_VERIFY_ATTEMPT")
public class CustomerVerification {
	
	@Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 @Column(name = "id", nullable = false, unique = true)
	 private Integer id;
	 
	 @Column(name = "INPUT_NUMBER", nullable = false)
	 private String inputNumber;
	 
	 @Column(name = "TOKEN", nullable = false)
	 private String token;
	 
	 @Column(name = "IS_ACTIVE", nullable = false)
	 private Boolean isActive;
	 
	 @Column(name = "CREATED_TIME", nullable = false)
	 private Date createdTime;
	 
	 @Column(name = "UPDATED_TIME", nullable = false)
	 private Date updatedTime;
	 
}
