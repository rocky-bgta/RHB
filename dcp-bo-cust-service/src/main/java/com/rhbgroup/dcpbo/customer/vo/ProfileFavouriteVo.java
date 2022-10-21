package com.rhbgroup.dcpbo.customer.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileFavouriteVo {

	private String id;
	private String txnType;
	private String mainFunction;
	private String payeeName;
	private String toAccountNo;
	private String nickname;
	private String mainLabel;
	private String ref1;
	private Boolean isQuickLink;
	private Boolean isQuickPay;
	private String duitnowCountryName;
	private String toIdType;
	private String toIdDescription;
	private String toIdNo;
	private Integer payeeId;
}
