package com.rhbgroup.dcpbo.system.termDeposit.service;

import org.springframework.http.ResponseEntity;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.termDeposit.dto.TermDepositPlacementConfirmationRequest;

public interface TermDepositPlacementService {
	public ResponseEntity<BoData> termDepositPlacement(TermDepositPlacementConfirmationRequest request);
}
