package com.rhbgroup.dcpbo.user.usergroupdelete;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class UsergroupDeleteRequestVo {

    private Integer functionId;
    private String groupName;
    private String accessType;

}
