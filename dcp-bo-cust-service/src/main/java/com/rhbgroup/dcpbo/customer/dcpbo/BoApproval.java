package com.rhbgroup.dcpbo.customer.dcpbo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TBL_BO_APPROVAL")
public class BoApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true )
    private Integer id;

    @Column(name = "FUNCTION_ID", nullable = false)
    private Integer functionId;

    @Column(name = "CREATOR_ID", nullable = false)
    private Integer creatorId;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "ACTION_TYPE", nullable = false)
    private String actionType;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "UPDATED_TIME", nullable = false)
    private Date updatedTime;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;
}