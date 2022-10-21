package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BoBillerTemplateTagConfig {
    private int templateTagId;
    private int templateId;
    private String tagName;
    private boolean isRecurring;
    private int sequence;
    List<BoBillerTemplateTagFieldConfig> tagFields=new ArrayList<>();
}

