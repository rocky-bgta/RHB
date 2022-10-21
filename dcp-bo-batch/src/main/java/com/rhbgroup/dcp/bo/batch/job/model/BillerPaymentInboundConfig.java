package com.rhbgroup.dcp.bo.batch.job.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode
@ToString
public class BillerPaymentInboundConfig  {

	private int id;
	private String billerCode;
	private String templateName;
	private String ftpFolder ;
	private String ibkFtpFolder;
	private String fileNameFormat ;
	private String reportUnitUri;
	private String status ;
	private String billerAccNo;
	private String billerAccName;
	private Integer categoryId;
	private String reportNameFormat;

}
