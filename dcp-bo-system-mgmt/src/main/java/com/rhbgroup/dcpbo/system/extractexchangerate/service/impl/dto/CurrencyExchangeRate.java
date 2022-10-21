package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CurrencyExchangeRate implements Serializable {
	private List<String> code;
}
