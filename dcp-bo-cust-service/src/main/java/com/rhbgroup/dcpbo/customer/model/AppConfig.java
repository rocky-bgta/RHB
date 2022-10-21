package com.rhbgroup.dcpbo.customer.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_APP_CONFIG")
public class AppConfig {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "id", nullable = false, unique = true)
	    private Integer id;
	    
        @Column(name = "PARAMETER_KEY", nullable = false)
	    private String parameterKey;

	    @Column(name = "PARAMETER_VALUE", nullable = false)
	    private String parameterValue;
}
