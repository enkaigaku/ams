package com.ams.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateTimeModificationRequestDto {

    @NotNull(message = "対象日は必須です")
    private LocalDate requestDate;

    private LocalDateTime requestedClockIn;

    private LocalDateTime requestedClockOut;

    @NotBlank(message = "修正理由は必須です")
    @Size(max = 1000, message = "修正理由は1000文字以内で入力してください")
    private String reason;

    // Default constructor
    public CreateTimeModificationRequestDto() {
    }

    // Constructor
    public CreateTimeModificationRequestDto(LocalDate requestDate, LocalDateTime requestedClockIn,
                                          LocalDateTime requestedClockOut, String reason) {
        this.requestDate = requestDate;
        this.requestedClockIn = requestedClockIn;
        this.requestedClockOut = requestedClockOut;
        this.reason = reason;
    }

    // Getters and Setters
    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getRequestedClockIn() {
        return requestedClockIn;
    }

    public void setRequestedClockIn(LocalDateTime requestedClockIn) {
        this.requestedClockIn = requestedClockIn;
    }

    public LocalDateTime getRequestedClockOut() {
        return requestedClockOut;
    }

    public void setRequestedClockOut(LocalDateTime requestedClockOut) {
        this.requestedClockOut = requestedClockOut;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "CreateTimeModificationRequestDto{" +
                "requestDate=" + requestDate +
                ", requestedClockIn=" + requestedClockIn +
                ", requestedClockOut=" + requestedClockOut +
                ", reason='" + reason + '\'' +
                '}';
    }
}