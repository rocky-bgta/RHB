package com.rhbgroup.dcpbo.customer.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum UserStatus {
    ACTIVE("A"),
    EXPIRED("E"),
    LOCKED_LIMIT("L"),
    CHALLENGE_LOCKED("C"),
    INACTIVE("I");

    @Getter private final String value;
}
