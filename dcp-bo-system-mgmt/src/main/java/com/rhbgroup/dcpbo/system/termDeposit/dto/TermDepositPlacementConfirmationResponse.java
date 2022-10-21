package com.rhbgroup.dcpbo.system.termDeposit.dto;

import com.rhbgroup.dcpbo.system.common.BoData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermDepositPlacementConfirmationResponse implements BoData{
	String code;
	String statusType;
}
