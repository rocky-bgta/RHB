package com.rhbgroup.dcpbo.customer.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DefaultValue {
    FAVOURITE_EMPTY_AMOUNT("-1.00");

    @Getter private final String value;
}
