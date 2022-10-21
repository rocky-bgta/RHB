package com.rhbgroup.dcp.bo.batch.job.model;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ExtractAdhocDowntime implements Serializable {
	private static final long serialVersionUID = -733612669338297044L;

	private String name;
	private String adhocType;
	private String type;
	private Timestamp startTime;
	private Timestamp endTime;
	private int bankId;
	private String adhocTypeCategory;

}
