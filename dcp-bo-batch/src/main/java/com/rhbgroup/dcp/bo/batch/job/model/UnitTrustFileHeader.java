package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UnitTrustFileHeader extends UnitTrustFileAbs {
	private String processingDate;
	private String systemDate;
	private String systemTime;
}
