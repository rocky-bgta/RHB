package com.rhbgroup.dcpbo.user.useraccess;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class UsergroupFunctionListDetails {
    private Integer functionId;
    private String functionName;
}
