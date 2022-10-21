package com.rhbgroup.dcp.bo.batch.job.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Rate implements Serializable {
	String code;
	Double buyTT;
	Double sellTT;
	Integer unit;
}
