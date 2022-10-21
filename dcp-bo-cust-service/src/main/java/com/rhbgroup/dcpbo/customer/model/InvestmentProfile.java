package com.rhbgroup.dcpbo.customer.model;

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

@Setter
@Getter
@ToString
@Entity
@Table(name = "TBL_INVESTMENT_PROFILE")
public class InvestmentProfile implements Serializable {
	private static final long serialVersionUID = 6784441808546600712L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;

	@Column(name = "user_id", nullable = false)
	private Integer userId;

	@Column(name = "account_no", nullable = false)
	private String accountNo;

	@Column(name = "nickname")
	private String nickname;

	@Column(name = "is_hidden", nullable = false)
	private Boolean isHidden;
}
