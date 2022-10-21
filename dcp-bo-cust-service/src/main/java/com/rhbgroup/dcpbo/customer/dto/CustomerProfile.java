package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerProfile {

	Integer id;
	String titleEn;
	String descriptionEn;
	String actionURL;
	String button;
	String cardNo;
	String cardType;
	
	
}
