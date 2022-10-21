package com.rhbgroup.dcpbo.customer.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * A user.
 */
@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "DCP_audit")
public class Audit implements Serializable {

    //    @OneToMany(mappedBy = "DCP_AUDIT", cascade = CascadeType.ALL)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;

    @Column(name = "event_code", nullable = false)
    private String eventCode;

    @Column(name = "user_id", nullable = false) //profile user manytoone
    private Integer userId;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "username", nullable = false) //profile user manytoone
    private String username;

    @Column(name = "cis_no") //profile user manytoone
    private String cisNo;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "status_code")
    private String statusCode;

    @Column(name = "status_description")
    private String statusDescription;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private Date timestamp;
}
