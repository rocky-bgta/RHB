package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BoBillerTemplateConfig {
    private int templateId;
    private String templateName;
    private String templateCode;
    private String viewName;
    private Date createdTime;
    private String createdBy;
    private Date updatedTime;
    private String updatedBy;
    private int lineSkipFromTop;
    private int lineSkipFromBottom;
}
