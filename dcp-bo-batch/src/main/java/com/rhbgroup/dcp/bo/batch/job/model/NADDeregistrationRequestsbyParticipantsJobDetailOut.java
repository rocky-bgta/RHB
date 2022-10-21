package com.rhbgroup.dcp.bo.batch.job.model;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class NADDeregistrationRequestsbyParticipantsJobDetailOut extends BaseModel  {

    private String id;
    private String auditId;
    private String details;
    private String proxyId;
    private String proxyType;
    private String secondaryId;
    private String secondaryType;
}
