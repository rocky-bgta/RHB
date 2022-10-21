package com.rhbgroup.dcpbo.user.useraccess;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UsergroupPagination {

    private int activityCount;
    private int pageNumber;
    private int totalPageNum;
}
