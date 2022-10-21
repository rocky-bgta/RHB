package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BatchConfig {
	private Integer id;
    private String parameterKey;
    private String parameterValue;
}
