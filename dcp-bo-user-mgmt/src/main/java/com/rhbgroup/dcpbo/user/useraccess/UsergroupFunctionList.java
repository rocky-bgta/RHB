package com.rhbgroup.dcpbo.user.useraccess;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.useraccess.UsergroupFunctionListDetails;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Setter
@Getter
@ToString
@JsonInclude
public class UsergroupFunctionList {

    private Integer groupId;
    private String groupName;
    private String accessType;
    private List<UsergroupFunctionListDetails> function;

}
