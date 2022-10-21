package com.rhbgroup.dcp.bo.batch.job.model;

import org.springframework.batch.core.scope.context.ChunkContext;

import lombok.Getter;

@Getter
public class SuspenseDetailsBuilder {

	private ChunkContext chunkContext;
	
	private BatchSuspense batchSuspense;
	
	private String logMessage;
	
	private String suspenseColumn;
	
	private String suspenseType;
	
	private String value;
	
	private String group;
	
	private boolean isLookUp;
	
	public SuspenseDetailsBuilder chunkContext(ChunkContext chunkContext) {
		this.chunkContext = chunkContext;
		return this;
	}
	
	public SuspenseDetailsBuilder batchSuspense(BatchSuspense batchSuspense) {
		this.batchSuspense = batchSuspense;
		return this;
	}
	
	public SuspenseDetailsBuilder logMessage(String logMessage) {
		this.logMessage = logMessage;
		return this;
	}
	
	public SuspenseDetailsBuilder suspenseColumn(String suspenseColumn) {
		this.suspenseColumn = suspenseColumn;
		return this;
	}
	
	public SuspenseDetailsBuilder suspenseType(String suspenseType) {
		this.suspenseType = suspenseType;
		return this;
	}

	public SuspenseDetailsBuilder value(String value) {
		this.value = value;
		return this;
	}

	public SuspenseDetailsBuilder group(String group) {
		this.group = group;
		return this;
	}
	
	public SuspenseDetailsBuilder lookUp(boolean isLookUp) {
		this.isLookUp = isLookUp;
		return this;
	}
	
}
