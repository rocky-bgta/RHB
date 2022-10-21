package com.rhbgroup.dcpbo.user.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Pagination {
	private int activityCount;
	private int pageNum;
	private int totalPageNum;
}
