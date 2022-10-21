package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NotNull
public class LoadIbkPaymentTemplate {

	String templateName;
    String viewName;
	List<BoBillerTemplateTagConfig> templateTags=new ArrayList<>();
	
}
