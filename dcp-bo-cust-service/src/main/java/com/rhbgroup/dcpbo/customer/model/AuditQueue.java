package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AuditQueue implements Serializable {

    private final HashMap<Object, Object> header = new HashMap<>();
    private final List<HashMap> data = new ArrayList<>();

    public void setHeader(String eventcode, String staffId, String department, String ip, String statusCode, String statusDesc){
        this.header.put("eventcode", eventcode);
        this.header.put("staffid", staffId);
        this.header.put("department", department);
        this.header.put("IP", ip);
        this.header.put("statuscode", statusCode);
        this.header.put("statusdesc", statusDesc);
    }

    public void setData(String deviceId, Map<Object, Object> response, Integer userId, String deviceName, String os){

        HashMap<Object, Object> deviceIdMap = new HashMap<>();
        HashMap<Object, Object> content = new HashMap<>();
        HashMap<Object, Object> additionalData = new HashMap<>();

        deviceIdMap.put("deviceId", deviceId);

        additionalData.put("userId", userId);
        additionalData.put("deviceName", deviceName);
        additionalData.put("os", os);

        content.put("request", deviceIdMap);
        content.put("response", response);
        content.put("additionalData", additionalData);

        this.data.add(content);
    }
}
