package com.rhbgroup.dcpbo.customer.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_LOOKUP_STATUS")
public class LookupStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @Column(name = "TYPE", nullable = false)
    private String type;
    
    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "STATUS_TYPE", nullable = false)
    private String statusType;  
    
}
