package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto;

import com.rhbgroup.dcpbo.system.common.BoData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtractExchangeRateResponse implements BoData {
	/*String code;
	String statusType;
	String title;
	String description;*/
	Rate[] rate;
}
