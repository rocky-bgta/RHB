package com.rhbgroup.dcpbo.user.usergroupdelete;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class UsergroupDeleteResponseVo implements BoData {

    private Integer approvalId;
    private String isWritten;
}
