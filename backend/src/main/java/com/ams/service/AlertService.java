package com.ams.service;

import com.ams.entity.Alert;
import com.ams.entity.User;
import com.ams.entity.enums.AlertType;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Autowired
    private AlertRepository alertRepository;

    public Alert createLateAlert(User user, LocalDate date, LocalDateTime clockInTime) {
        // Check if alert already exists to avoid duplicates
        if (alertRepository.existsByUserIdAndTypeAndAlertDate(user.getId(), AlertType.LATE, date)) {
            return null;
        }

        String timeStr = clockInTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        String message = String.format("%sさんが%sに遅刻しました。出勤時刻: %s", 
                user.getName(), date.format(DATE_FORMATTER), timeStr);
        
        Alert alert = new Alert(AlertType.LATE, user, date, message);
        Alert savedAlert = alertRepository.save(alert);
        
        logger.info("Created late alert for user: {} on {}", user.getEmployeeId(), date);
        return savedAlert;
    }

    public Alert createAbsentAlert(User user, LocalDate date) {
        // Check if alert already exists
        if (alertRepository.existsByUserIdAndTypeAndAlertDate(user.getId(), AlertType.ABSENT, date)) {
            return null;
        }

        String message = String.format("%sさんが%sに欠勤しています。", 
                user.getName(), date.format(DATE_FORMATTER));
        
        Alert alert = new Alert(AlertType.ABSENT, user, date, message);
        Alert savedAlert = alertRepository.save(alert);
        
        logger.info("Created absent alert for user: {} on {}", user.getEmployeeId(), date);
        return savedAlert;
    }

    public Alert createMissingClockOutAlert(User user, LocalDate date) {
        // Check if alert already exists
        if (alertRepository.existsByUserIdAndTypeAndAlertDate(user.getId(), AlertType.MISSING_CLOCK_OUT, date)) {
            return null;
        }

        String message = String.format("%sさんが%sの退勤打刻を忘れています。", 
                user.getName(), date.format(DATE_FORMATTER));
        
        Alert alert = new Alert(AlertType.MISSING_CLOCK_OUT, user, date, message);
        Alert savedAlert = alertRepository.save(alert);
        
        logger.info("Created missing clock-out alert for user: {} on {}", user.getEmployeeId(), date);
        return savedAlert;
    }

    public Alert createOvertimeAlert(User user, LocalDate date, double overtimeHours) {
        String message = String.format("%sさんが%sに%.1f時間の残業をしています。", 
                user.getName(), date.format(DATE_FORMATTER), overtimeHours);
        
        Alert alert = new Alert(AlertType.OVERTIME, user, date, message);
        Alert savedAlert = alertRepository.save(alert);
        
        logger.info("Created overtime alert for user: {} on {} ({}h)", user.getEmployeeId(), date, overtimeHours);
        return savedAlert;
    }

    public Alert createCustomAlert(User user, LocalDate date, AlertType type, String message) {
        Alert alert = new Alert(type, user, date, message);
        Alert savedAlert = alertRepository.save(alert);
        
        logger.info("Created custom alert for user: {} on {} type: {}", user.getEmployeeId(), date, type);
        return savedAlert;
    }

    @Transactional(readOnly = true)
    public List<Alert> getAlertsForUser(UUID userId) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Alert> getAlertsForManager(String managerId) {
        return alertRepository.findByManagerIdOrderByCreatedAtDesc(managerId);
    }

    @Transactional(readOnly = true)
    public List<Alert> getUnreadAlertsForManager(String managerId) {
        return alertRepository.findUnreadByManagerIdOrderByCreatedAtDesc(managerId);
    }

    @Transactional(readOnly = true)
    public List<Alert> getAllUnreadAlerts() {
        return alertRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Alert> getAlertsByType(AlertType type, LocalDate date) {
        return alertRepository.findByTypeAndAlertDateOrderByCreatedAtDesc(type, date);
    }

    @Transactional(readOnly = true)
    public List<Alert> getAlertsByDateRange(LocalDate startDate, LocalDate endDate) {
        return alertRepository.findByDateRangeOrderByCreatedAtDesc(startDate, endDate);
    }

    public Alert markAlertAsRead(UUID alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        
        if (!alert.getIsRead()) {
            alert.markAsRead();
            Alert savedAlert = alertRepository.save(alert);
            logger.debug("Marked alert as read: {}", alertId);
            return savedAlert;
        }
        
        return alert;
    }

    public void markAlertsAsRead(List<UUID> alertIds) {
        alertRepository.markAlertsAsRead(alertIds);
        logger.info("Marked {} alerts as read", alertIds.size());
    }

    public void markAllAlertsAsReadForUser(UUID userId) {
        alertRepository.markAllAlertsAsReadByUserId(userId);
        logger.info("Marked all alerts as read for user: {}", userId);
    }

    public void markAllAlertsAsReadForManager(String managerId) {
        alertRepository.markAllAlertsAsReadByManagerId(managerId);
        logger.info("Marked all alerts as read for manager: {}", managerId);
    }

    public void deleteAlert(UUID alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        
        alertRepository.delete(alert);
        logger.info("Deleted alert: {}", alertId);
    }

    @Transactional(readOnly = true)
    public long countUnreadAlerts() {
        return alertRepository.countUnreadAlerts();
    }

    @Transactional(readOnly = true)
    public long countUnreadAlertsForManager(String managerId) {
        return alertRepository.countUnreadAlertsByManagerId(managerId);
    }

    @Transactional(readOnly = true)
    public long countUnreadAlertsForUser(UUID userId) {
        return alertRepository.countUnreadAlertsByUserId(userId);
    }

    // Scheduled task to clean up old alerts (runs daily at 3 AM)
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldAlerts() {
        // Delete alerts older than 90 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        alertRepository.deleteOldAlerts(cutoffDate);
        logger.info("Cleaned up alerts older than {}", cutoffDate.toLocalDate());
    }

    // Method to check for absent employees and create alerts
    public void checkAbsentEmployees(LocalDate date, List<User> allActiveUsers) {
        logger.info("Checking for absent employees on {}", date);
        
        // This would typically be called by a scheduled job
        // Implementation would check which users have no attendance record for the date
        // and create absent alerts for them
        
        for (User user : allActiveUsers) {
            // Logic to check if user has attendance record for the date
            // If not, create absent alert
            // This would be implemented based on your business rules
        }
    }
}