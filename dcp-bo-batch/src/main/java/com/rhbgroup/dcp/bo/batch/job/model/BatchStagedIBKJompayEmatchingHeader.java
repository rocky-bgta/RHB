package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BatchStagedIBKJompayEmatchingHeader extends BatchStagedIBKJompayEmatching  {
	private String programName;
	private String processingDate;
	private String systemDate;
	private String systemTime;
	private String ematching;
}
