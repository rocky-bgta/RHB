package com.rhbgroup.dcpbo.customer.dcpbo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TBL_BO_CONFIG_FUNCTION")
public class BoConfigFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true )
    private Integer id;

    @Column(name = "FUNCTION_NAME", nullable = false)
    private String functionName;

    @Column(name = "MODULE_ID", nullable = false)
    private Integer moduleId;

    @Column(name = "CHECKER_SCOPE", nullable = false)
    private String checkerScope;

    @Column(name = "MAKER_SCOPE", nullable = false)
    private String makerScope;

    @Column(name = "INQUIRER_SCOPE", nullable = false)
    private String inquirerScope;

    @Column(name = "APPROVAL_REQUIRED", nullable = false)
    private Boolean approvalRequired;

    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @Column(name = "UPDATED_TIME", nullable = false)
    private Date updatedTime;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;
}