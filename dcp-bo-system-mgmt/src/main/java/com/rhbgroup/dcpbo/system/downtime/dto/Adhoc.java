package com.rhbgroup.dcpbo.system.downtime.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@JsonInclude
@AllArgsConstructor
@NoArgsConstructor
public class Adhoc implements BoData {
	private int id;
    private String name;
    private String startTime;
    private String endTime;
    private Boolean isPushNotification;
    private String pushDate;
    private String status;
    private String adhocType;
    private String adhocCategory;
    private String bankName;
    private String bankId;
}
