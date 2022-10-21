package com.rhbgroup.dcpbo.system.downtime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class BankDetailsInfo implements BoData{
	
	private List<BankDetails> bank;
	private AuditPagination pagination;

}
