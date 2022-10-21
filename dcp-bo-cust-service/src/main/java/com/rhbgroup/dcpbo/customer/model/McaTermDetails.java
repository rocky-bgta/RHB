package com.rhbgroup.dcpbo.customer.model;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McaTermDetails implements BoData {
   private McaTermStatus status;
   private McaTermData data;
   
}
