package com.rhbgroup.dcpbo.user.useraccess;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
@JsonInclude
public class UsergroupFunction implements BoData{

    private List<UsergroupFunctionList> usergroup;
    private UsergroupPagination pagination;
}








