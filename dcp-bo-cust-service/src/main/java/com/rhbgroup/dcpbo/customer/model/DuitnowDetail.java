package com.rhbgroup.dcpbo.customer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DuitnowDetail implements BoData {
    private DuitnowSenderInfo senders;
    private DuitnowProxyInfo proxy;
}
