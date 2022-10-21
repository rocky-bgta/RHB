package com.rhbgroup.dcpbo.user.workflow.overview;

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
public class WorkflowOverviewModule implements BoData {
    private String moduleId;
    private String moduleName;
    private List<WorkflowOverviewModuleFunction> function;
}