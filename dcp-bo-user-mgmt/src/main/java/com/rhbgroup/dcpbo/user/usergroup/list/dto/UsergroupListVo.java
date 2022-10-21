package com.rhbgroup.dcpbo.user.usergroup.list.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.usergroup.dto.UsergroupVo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class UsergroupListVo implements BoData {
	List<UsergroupVo> usergroup;
}
