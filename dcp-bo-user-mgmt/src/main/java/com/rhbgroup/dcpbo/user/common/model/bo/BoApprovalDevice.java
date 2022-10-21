package com.rhbgroup.dcpbo.user.common.model.bo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TBL_BO_APPROVAL_DEVICE")
public class BoApprovalDevice {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true)
    private Integer Id;

    @Column(name = "APPROVAL_ID", nullable = false)
    private Integer approvalId;

    @Column(name = "STATE", nullable = false)
    private String state;

    @Column(name = "LOCKING_ID", nullable = false)
    private String lockingId;

    @Column(name = "PAYLOAD	")
    private String payload;

    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "UPDATED_TIME", nullable = false)
    private Date updatedTime;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;

}
