package com.rhbgroup.dcpbo.customer.model;

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
@Table(name = "TBL_CARD_PROFILE")
public class CardProfile {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;
	
	@Column(name = "user_id", nullable = false)
	private Integer userId;
	
	@Column(name = "card_no", nullable = false)
	private String cardNo;
	
	@Column(name = "nickname")
	private String nickName;
	
	@Column(name = "is_default_account", nullable = false)
	private Boolean isDefaultAccount;
	
	@Column(name = "is_hidden", nullable = false)
	private Boolean isHidden;
	
	@Column(name = "card_product_id", nullable = false)
	private Integer cardProductId;
	
	@Column(name = "connector_code", nullable = false)
	private String connectorCode;
	
	@Column(name = "failed_card_tpin_count")
	private Integer failedCardTpinCount;
	
	@Column(name = "account_no")
	private String accountNo;
	
	@Column(name = "IS_ACTV_BLOCKED")
	private Boolean isActvBlocked;
	
}
