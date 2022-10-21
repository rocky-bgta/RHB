package com.rhbgroup.dcpbo.user.common;

import java.io.Serializable;
import java.sql.Timestamp;

@lombok.Getter
@lombok.Setter
public class UsergroupAccessDTO implements Serializable {

    private String userGroupId;
    private String functionId;
    private String scopeId;
    private String moduleId;
    private String accessType;
    private String status;
    private String createdTime;
    private String createdBy;
    private String updatedTime;
    private String updatedBy;
}