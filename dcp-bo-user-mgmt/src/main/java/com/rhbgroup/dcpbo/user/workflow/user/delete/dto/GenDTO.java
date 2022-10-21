package com.rhbgroup.dcpbo.user.workflow.user.delete.dto;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@JsonInclude
@Getter
@Setter
public class GenDTO {
	
	private String labelName;
	private List<Profiles> profiles = new  CopyOnWriteArrayList<>();
	

}
