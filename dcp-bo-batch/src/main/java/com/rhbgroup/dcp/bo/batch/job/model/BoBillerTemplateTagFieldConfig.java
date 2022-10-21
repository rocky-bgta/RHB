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
public class BoBillerTemplateTagFieldConfig {
    private int templateFieldId;
    private int templateTagId;
    private String fieldName;
    private String fieldType;
    private int length;
    private boolean isMandatory;
    private String valueType;
    private String defaultValue;
    private boolean isAggregationRequired;
    private String aggregationType;
    private boolean isPaddingRequired;
    private String paddingType;
    private String paddingFillValue;
    private String viewFieldName;
    private int sequence;
}
