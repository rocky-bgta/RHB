package com.rhbgroup.dcpbo.user.workflow.user.delete.dto;


import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FundVo implements BoData {
	
    private int id;
    private String fundId;
    private String fundLongName;
    private String fundShortName;
    private String fundType;
    private String collectionAccountNumber;
    private String suspensionFromDateTime;
    private String suspensionToDateTime;
    private Boolean isTopEnable;
    private BigDecimal bankChargeAmount;
    private BigDecimal bankChargePercentage;
    private String bankChargeType;
    private BigDecimal bankChargeValue;
    private String suspensionFromDateFormat;
    private String suspensionToDateFormat;
	private int functionId;
	private BigDecimal dailyOwnAccountLimit;
    private BigDecimal dailyThirdPartyAccountLimit;
    private BigDecimal perTransactionMinLimit;
    private BigDecimal perTransactionMaxLimit;
    private String taxChargeType;
    private BigDecimal taxChargeValue;
    private Map<String,GenDTO> fundTypeMap = new LinkedHashMap<String, GenDTO>();
    private List<Topup> topup;
    private String imageUrl;

}
