package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlacklistedMsicTrailer  extends BlacklistedMsic{
	private String totalRecordCount;
}
