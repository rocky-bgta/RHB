package com.rhbgroup.dcpbo.customer.dcpbo;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * A telemetry operation name.
 */
@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "TBL_telemetry_operation_name")
public class TelemetryOperationName implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;

    @Column(name = "operation_name", nullable = false)
    private String operationName;

    @Column(name = "created_time", nullable = false)
    private Date createdTime;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_time", nullable = false)
    private Date updatedTime;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
}
