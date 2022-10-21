package com.rhbgroup.dcpbo.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BoUserStatus {
    ACTIVE("A"),
    INACTIVE("I"),
    DELETED("D"),
    LOCKED("L");

    @Getter private final String value;
}
