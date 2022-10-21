package com.rhbgroup.dcpbo.user.common.model.bo;

import com.rhbgroup.dcpbo.user.common.UsergroupAccessIdClass;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@lombok.Getter
@lombok.Setter
@Entity
@IdClass(UsergroupAccessIdClass.class)
@Table(name = "TBL_BO_USERGROUP_ACCESS")
public class UsergroupAccess implements Serializable {

    @Id
    @Column(name = "user_group_id",nullable = false)
    private Integer userGroupId;

    @Id
    @Column(name = "function_id",nullable = false)
    private Integer functionId;

    @Column(name = "scope_id" , nullable = false)
    private String scopeId;

    @Column(name = "access_type" , nullable = false)
    private String accessType;

    @Column(name = "status" , nullable = false)
    private String status;

    @Column(name = "created_time", nullable = false)
    private Timestamp createdTime;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_time", nullable = false)
    private Timestamp updatedTime;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
}