package com.rhbgroup.dcpbo.customer.exception;

public class AsnbTransactionException extends Exception {
	
	private String businessLogicName = "";

	public AsnbTransactionException(String message) {
		super(message);
		
	}

	public String getBusinessLogicName() {
		return businessLogicName;
	}
	
	public void setBusinessLogicName(String businessLogicName) {
		this.businessLogicName = businessLogicName;
	}
}