package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Setter
@Getter
@Entity
public class OlaTokenGroup {

    public OlaTokenGroup(String name, String idType, String idNo, String username) {
        this.name = name;
        this.idType = idType;
        this.idNo = idNo;
        this.username = username;
    }

    @Id
    private String username;
    private String name;
    private String idType;
    private String idNo;

}
