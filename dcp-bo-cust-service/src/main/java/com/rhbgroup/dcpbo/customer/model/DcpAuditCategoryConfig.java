package com.rhbgroup.dcpbo.customer.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "DCP_AUDIT_CATEGORY_CONFIG")
public class DcpAuditCategoryConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @Column(name = "CATEGORY_NAME", nullable = false)
    private String categoryName;

}