package com.ams.dto.time;

public class AttendanceStatus {

    private String status;
    private String message;
    private boolean canClockIn;
    private boolean canClockOut;
    private boolean canStartBreak;
    private boolean canEndBreak;
    private TimeRecordDto todayRecord;

    // Default constructor
    public AttendanceStatus() {
    }

    // Constructor
    public AttendanceStatus(String status, String message, boolean canClockIn, boolean canClockOut,
                           boolean canStartBreak, boolean canEndBreak, TimeRecordDto todayRecord) {
        this.status = status;
        this.message = message;
        this.canClockIn = canClockIn;
        this.canClockOut = canClockOut;
        this.canStartBreak = canStartBreak;
        this.canEndBreak = canEndBreak;
        this.todayRecord = todayRecord;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCanClockIn() {
        return canClockIn;
    }

    public void setCanClockIn(boolean canClockIn) {
        this.canClockIn = canClockIn;
    }

    public boolean isCanClockOut() {
        return canClockOut;
    }

    public void setCanClockOut(boolean canClockOut) {
        this.canClockOut = canClockOut;
    }

    public boolean isCanStartBreak() {
        return canStartBreak;
    }

    public void setCanStartBreak(boolean canStartBreak) {
        this.canStartBreak = canStartBreak;
    }

    public boolean isCanEndBreak() {
        return canEndBreak;
    }

    public void setCanEndBreak(boolean canEndBreak) {
        this.canEndBreak = canEndBreak;
    }

    public TimeRecordDto getTodayRecord() {
        return todayRecord;
    }

    public void setTodayRecord(TimeRecordDto todayRecord) {
        this.todayRecord = todayRecord;
    }

    @Override
    public String toString() {
        return "AttendanceStatus{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", canClockIn=" + canClockIn +
                ", canClockOut=" + canClockOut +
                ", canStartBreak=" + canStartBreak +
                ", canEndBreak=" + canEndBreak +
                '}';
    }
}