package com.rhbgroup.dcpbo.user.common;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "TBL_DEVICE_PROFILE")
public class DeviceProfile implements Serializable {

	private static final long serialVersionUID = 5462863594603447267L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;
	
    @Column(name = "name")
	@JsonProperty(value = "deviceName")
	private String name;
	
    @Column(name = "os")
	private String os;
	
    @Column(name = "last_login")
	private String lastSigned;
	
    @Column(name = "created_time")
	private String registered;

}
