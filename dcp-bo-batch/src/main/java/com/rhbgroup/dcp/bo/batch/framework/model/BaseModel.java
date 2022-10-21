package com.rhbgroup.dcp.bo.batch.framework.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class BaseModel {
    protected String space;
    protected String jobExecutionId;
}
