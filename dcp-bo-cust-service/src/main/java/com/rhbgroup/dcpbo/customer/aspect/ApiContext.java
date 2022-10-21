package com.rhbgroup.dcpbo.customer.aspect;


import com.rhbgroup.dcp.model.Capsule;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiContext {

    private Capsule capsule;
    private String user;
    private String uniqueId;

}
