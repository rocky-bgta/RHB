package com.rhbgroup.dcpbo.customer.dcpbo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TBL_BO_USERGROUP_ACCESS")
public class BoUsergroupAccess {

    @Id
    @Column(name = "USER_GROUP_ID", nullable = false)
    private Integer userGroupId;

    @Column(name = "FUNCTION_ID	", nullable = false)
    private Integer functionId;

    @Column(name = "MODULE_ID", nullable = false)
    private Integer moduleId;

    @Column(name = "SCOPE_ID", nullable = false)
    private String scopeId;

    @Column(name = "ACCESS_TYPE	", nullable = false)
    private String accessType;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "UPDATED_TIME", nullable = false)
    private Date updatedTime;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;

}

