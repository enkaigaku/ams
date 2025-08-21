package com.ams.entity.enums;

public enum AlertType {
    LATE("遅刻"),
    ABSENT("欠勤"),
    MISSING_CLOCK_OUT("退勤打刻忘れ"),
    OVERTIME("残業"),
    LONG_BREAK("長時間休憩");

    private final String displayName;

    AlertType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}