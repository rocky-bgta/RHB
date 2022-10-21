package com.rhbgroup.dcp.bo.batch.job.vo;

import java.util.List;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;
import com.rhbgroup.dcp.bo.batch.job.model.BoBillerTemplateTagConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BoBillerTemplateTagFieldConfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileTemplate extends BaseModel{

	private String templateName;
	
	private String viewName;
	
	List<BoBillerTemplateTagConfig> templateTags;
	
	List<BoBillerTemplateTagFieldConfig> templateTagFields;

	
}