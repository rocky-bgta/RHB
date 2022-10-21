package com.rhbgroup.dcp.bo.batch.job.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BatchStagedIBKJompayEmatchingTrailer extends BatchStagedIBKJompayEmatching  {
	private int totalRecord;
	private double totalAmount;
}
