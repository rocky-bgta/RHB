package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Setter
@Getter
@Entity
public class OlaTokenUser {

    @Id
    @Column(name="id")
    private Integer custid;
    private String username;
    private String name;
    private String email;
    private String mobileNo;
    private String cisNo;
    @Column(name="token")
    private String aaoip;
    private String idType;
    private String idNo;
    private String status;
    private String isPremier;
    private Date lastLogin;

}
