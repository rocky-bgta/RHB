package com.rhbgroup.dcpbo.user.common.model.bo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "TBL_BO_APPROVAL_DEVICE")
public class ApprovalDevice implements Serializable {

	private static final long serialVersionUID = -1498526068558291372L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;
	
    @Column(name = "approval_id", nullable = false)
    private Integer approvalId;

    @Column(name = "state",nullable = false)
	private String state;
	
    @Column(name = "payload",nullable = true)
	private String payload;

}
