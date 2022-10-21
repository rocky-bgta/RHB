package com.rhbgroup.dcpbo.customer.enums;

import java.util.HashMap;
import java.util.Map;

public enum FavouriteToIdTypeDesc {
	MBNO("MBNO", "Mobile Number"), NRIC("NRIC", "IC Number"), ARMN("ARMN", "Army/Police Number"), PSPT("PSPT",
			"Passport Number"), BREG("BREG", "Business Registration Number"), W("W", "New IC Number");

	private final String code;
	private final String description;

	private FavouriteToIdTypeDesc(String statusCode, String description) {
		this.code = statusCode;
		this.description = description;
	}

	private static Map<String, Object> map = new HashMap<String, Object>();
	static {
		for (FavouriteToIdTypeDesc description : FavouriteToIdTypeDesc.values()) {
			map.put(description.code, description);
		}
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public static FavouriteToIdTypeDesc getDescription (String code) {
		return map.get(code) != null ? (FavouriteToIdTypeDesc) map.get(code) : null;
	}
}
