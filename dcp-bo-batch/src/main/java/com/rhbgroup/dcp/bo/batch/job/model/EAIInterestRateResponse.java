package com.rhbgroup.dcp.bo.batch.job.model;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EAIInterestRateResponse implements Serializable{

	private static final long serialVersionUID = 1L;

	private List<InterestRate> rate;
}
