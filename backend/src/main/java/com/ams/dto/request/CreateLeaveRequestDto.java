package com.ams.dto.request;

import com.ams.entity.enums.LeaveType;
import com.ams.validation.ValidDateRange;
import com.ams.validation.ValidFutureDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@ValidDateRange(maxDaysRange = 365, message = "休暇期間は1年以内で指定してください")
public class CreateLeaveRequestDto {

    @NotNull(message = "休暇種別は必須です")
    private LeaveType type;

    @NotNull(message = "開始日は必須です")
    @ValidFutureDate(allowToday = false, message = "開始日は明日以降の日付を指定してください")
    private LocalDate startDate;

    @NotNull(message = "終了日は必須です")
    @ValidFutureDate(allowToday = false, message = "終了日は明日以降の日付を指定してください")
    private LocalDate endDate;

    @NotBlank(message = "理由は必須です")
    @Size(max = 1000, message = "理由は1000文字以内で入力してください")
    private String reason;

    // Default constructor
    public CreateLeaveRequestDto() {
    }

    // Constructor
    public CreateLeaveRequestDto(LeaveType type, LocalDate startDate, LocalDate endDate, String reason) {
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    // Getters and Setters
    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "CreateLeaveRequestDto{" +
                "type=" + type +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", reason='" + reason + '\'' +
                '}';
    }
}