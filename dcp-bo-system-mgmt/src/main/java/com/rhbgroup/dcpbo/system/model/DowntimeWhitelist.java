
package com.rhbgroup.dcpbo.system.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "TBL_SYSTEM_DOWNTIME_WHITELIST")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DowntimeWhitelist implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    
    @Basic(optional = false)
    @Column(name = "TYPE")
    private String type;
    
    @Basic(optional = false)
    @Column(name = "CREATED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    
    @Basic(optional = false)
    @Column(name = "CREATED_BY")
    private String createdBy;
    
    @Basic(optional = false)
    @Column(name = "UPDATED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;
    
    @Basic(optional = false)
    @Column(name = "UPDATED_BY")
    private String updatedBy;
    
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private UserProfile userId;
}