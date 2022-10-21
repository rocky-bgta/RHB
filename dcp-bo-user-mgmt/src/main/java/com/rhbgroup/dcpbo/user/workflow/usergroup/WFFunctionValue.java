package com.rhbgroup.dcpbo.user.workflow.usergroup;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WFFunctionValue {

	private List<String> functionName;

	public WFFunctionValue(List<String> functionName) {
		super();
		this.functionName = functionName;
	}

}
