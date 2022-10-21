package com.rhbgroup.dcpbo.user.search;

import java.sql.Timestamp;

import com.rhbgroup.dcpbo.user.common.BoData;

public interface SearchService {
	public BoData search(String keyword, Integer pageNum, String departmentId, String status, String userGroupId, Timestamp fromTimestamp, Timestamp toTimestamp, String sortOrder);
}
