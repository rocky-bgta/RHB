package com.rhbgroup.dcp.bo.batch.framework.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoadDynamicBiller {

    int previousHeaderLength = 1;
    int previousBodyLength = 1;
    int previousTrailerLength = 1;

    String headerNames = "";

    String headerColumns = "";

    String detailNames = "";
    String detailColumns = "";

    String trailerNames = "";
    String trailerColumns = "";

    String headerPrefixPattern = "";
    String detailPrefixPattern = "";
    String trailerPrefixPattern = "";

}
