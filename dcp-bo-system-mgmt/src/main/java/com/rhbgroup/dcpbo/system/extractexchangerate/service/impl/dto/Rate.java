package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rate implements Serializable {
	String code;
	double buyTT;
	double sellTT;
	int unit;
}
