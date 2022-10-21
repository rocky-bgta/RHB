package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BoConfigGeneric {
	private int id;
	private String configType;
	private String configCode;
	private String configDesc;
	private Date createdTime;
	private String createdBy;
	private Date updatedTime;
	private String updatedBy;
}
