package com.rhbgroup.dcp.bo.batch.job.model;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EAISessionTokenResponse implements Serializable {

	private String sessionToken;

}
