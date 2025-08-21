package com.ams.entity;

import com.ams.entity.enums.AlertType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_user", columnList = "user_id"),
    @Index(name = "idx_alert_type", columnList = "type"),
    @Index(name = "idx_alert_date", columnList = "alert_date"),
    @Index(name = "idx_alert_read", columnList = "is_read")
})
public class Alert extends BaseEntity {

    @NotNull(message = "アラート種別は必須です")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AlertType type;

    @NotNull(message = "ユーザーは必須です")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "アラート対象日は必須です")
    @Column(name = "alert_date", nullable = false)
    private LocalDate alertDate;

    @NotBlank(message = "アラートメッセージは必須です")
    @Size(max = 500, message = "アラートメッセージは500文字以内で入力してください")
    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    // Default constructor
    public Alert() {
    }

    // Constructor with essential fields
    public Alert(AlertType type, User user, LocalDate alertDate, String message) {
        this.type = type;
        this.user = user;
        this.alertDate = alertDate;
        this.message = message;
    }

    // Business methods
    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsUnread() {
        this.isRead = false;
    }

    public boolean isUnread() {
        return !isRead;
    }

    // Static factory methods for common alert types
    public static Alert createLateAlert(User user, LocalDate date, String details) {
        String message = String.format("%sさんが%sに遅刻しました。%s", 
            user.getName(), date.toString(), details);
        return new Alert(AlertType.LATE, user, date, message);
    }

    public static Alert createAbsentAlert(User user, LocalDate date) {
        String message = String.format("%sさんが%sに欠勤しています。", 
            user.getName(), date.toString());
        return new Alert(AlertType.ABSENT, user, date, message);
    }

    public static Alert createMissingClockOutAlert(User user, LocalDate date) {
        String message = String.format("%sさんが%sの退勤打刻を忘れています。", 
            user.getName(), date.toString());
        return new Alert(AlertType.MISSING_CLOCK_OUT, user, date, message);
    }

    // Getters and Setters
    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getAlertDate() {
        return alertDate;
    }

    public void setAlertDate(LocalDate alertDate) {
        this.alertDate = alertDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "type=" + type +
                ", alertDate=" + alertDate +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", id=" + getId() +
                '}';
    }
}