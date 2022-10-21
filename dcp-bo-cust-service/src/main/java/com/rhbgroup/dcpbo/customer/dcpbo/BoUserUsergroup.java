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
@Table(name = "TBL_BO_USER_USERGROUP")
public class BoUserUsergroup {

    @Id
    @Column(name = "USER_ID", nullable = false)
    private Integer userId;

    @Column(name = "USER_GROUP_ID", nullable = false)
    private Integer userGroupId;

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