package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Date;
import java.sql.Timestamp;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter

public class Biller extends BaseModel {
	private int id;
	private String status;
	private Timestamp suspendedStartDate;
	private Timestamp suspendedEndDate;
	private Timestamp effectiveEndDate;	
	private String paymentAccountNo;
	private String billerCollectionAccountNo;
	private String paymentMethod;
	private String paymentMode;
	private String billerName;
	private String billerCode;
	private int categoryId;
	private String billerType;
	private Integer paymentBankId;
}
