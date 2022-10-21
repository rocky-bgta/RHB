package com.rhbgroup.dcpbo.system.downtime.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class AdhocType implements BoData {
	private List<String> adhocType=new ArrayList<>();
	private List<String> adhocTypeNames=new ArrayList<>();;
	
}
