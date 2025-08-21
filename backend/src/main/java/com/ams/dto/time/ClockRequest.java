package com.ams.dto.time;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ClockRequest {

    @NotNull(message = "打刻時刻は必須です")
    private LocalDateTime timestamp;

    @Size(max = 100, message = "場所は100文字以内で入力してください")
    private String location; // Optional: GPS location or office location

    @Size(max = 500, message = "備考は500文字以内で入力してください")
    private String notes; // Optional: Additional notes

    // Default constructor
    public ClockRequest() {
    }

    // Constructor
    public ClockRequest(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "ClockRequest{" +
                "timestamp=" + timestamp +
                ", location='" + location + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}