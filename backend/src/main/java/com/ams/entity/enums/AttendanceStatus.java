package com.ams.entity.enums;

public enum AttendanceStatus {
    PRESENT("出勤"),
    ABSENT("欠勤"),
    LATE("遅刻"),
    EARLY_LEAVE("早退");

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}