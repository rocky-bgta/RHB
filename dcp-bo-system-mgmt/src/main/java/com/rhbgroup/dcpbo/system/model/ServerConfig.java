package com.rhbgroup.dcpbo.system.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "TBL_SERVER_CONFIG")
public class ServerConfig implements Serializable{

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "DESCRIPTION") @NotNull @Size(max = 50)
    private String description;

    @Column(name = "PARAMETER_KEY") @NotNull @Size(max = 100)
    private String parameterKey;

    @Column(name = "PARAMETER_VALUE") @NotNull @Size(max = 300)
    private String parameterValue;

    @Column(name = "CREATED_TIME")
    private Timestamp createdTime;

    @Column(name = "CREATED_BY") @Size(max = 50)
    private String createdBy;

    @Column(name = "UPDATED_TIME")
    private Timestamp updatedTime;

    @Column(name = "UPDATED_BY") @Size(max = 50)
    private String updatedBy;
}
