package com.rhbgroup.dcpbo.user.common.model.bo;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * A user.
 */
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Entity
@Table(name = "TBL_BO_APPROVAL")
public class Approval implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "function_id", nullable = false)
    private Integer functionId;

    @Column(name = "creator_id", nullable = false)
    private Integer creatorId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "status", columnDefinition ="VARCHAR", length = 1 , nullable = false)
    private String status;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_time", nullable = false)
    private Timestamp createdTime;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_time", nullable = false)
    private Timestamp updatedTime;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
}