package com.ams.entity.enums;

public enum RequestStatus {
    PENDING("承認待ち"),
    APPROVED("承認済み"),
    REJECTED("却下");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}