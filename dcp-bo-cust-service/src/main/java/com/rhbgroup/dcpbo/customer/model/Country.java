package com.rhbgroup.dcpbo.customer.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * A user.
 */
@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "TBL_COUNTRY")
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;

    @Column(name = "COUNTRY_CODE", nullable = false)
    private String countryCode;

    @Column(name = "COUNTRY_CIS", nullable = false)
    private String countryCis;

    @Column(name = "COUNTRY_NAME", nullable = false)
    private String countryName;

    @Column(name = "CREATED_TIME", nullable = false)
    private Timestamp createdTime;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "updated_time", nullable = false)
    private Timestamp updatedTime;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;
}
