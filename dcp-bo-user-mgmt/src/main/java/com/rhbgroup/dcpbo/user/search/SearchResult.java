package com.rhbgroup.dcpbo.user.search;

import java.util.List;

import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.common.model.bo.ConfigDepartment;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SearchResult implements BoData {
	private List<User> user;
	private List<ConfigDepartment> departmentList;
	private Pagination pagination;
}
