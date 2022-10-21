package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlacklistedMsicHeader extends BlacklistedMsic{
	private String fileName;
	private String creationDate;
}
