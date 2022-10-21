package com.rhbgroup.dcpbo.system.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Getter
@lombok.Setter
@Entity
@Table(name = "TBL_BO_USERGROUP")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usergroup implements Serializable {

	private static final long serialVersionUID = 8097997037655395549L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JsonProperty("groupId")
    private Integer id;

    @Column(name = "group_name",nullable = false)
    private String groupName;

    @Column(name = "group_status" , nullable = false)
    private String groupStatus;

    @Column(name = "created_time", nullable = false)
    private Timestamp createdTime;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_time", nullable = false)
    private Timestamp updatedTime;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
}
