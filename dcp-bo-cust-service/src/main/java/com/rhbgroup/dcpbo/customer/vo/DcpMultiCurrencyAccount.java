package com.rhbgroup.dcpbo.customer.vo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DcpMultiCurrencyAccount implements Serializable {

	private String accountNo;
	private String productCode;
	private String nickname;
	private String productName;
	private Boolean isHidden;
	private String permission;
}
