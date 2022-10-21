package com.rhbgroup.dcp.bo.batch.job.model;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter

public class BillerPaymentOutboundConfig extends BaseModel {
	private int id;
	private String billerCode; //biller_code
	private String templateName; //template_name
	private String ftpFolder ; //ftp_folder
	private String fileNameFormat ; //file_name_format
	private String reportUnitUri; //report_unit_uri
	private String status ;
	private String billerAccNo;
	private String billerAccName;
	private Integer categoryId;
	//is_required_to_execute=1
	
}
