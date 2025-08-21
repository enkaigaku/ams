package com.ams.service;

import com.ams.entity.Alert;
import com.ams.entity.TimeRecord;
import com.ams.entity.User;
import com.ams.entity.enums.AttendanceStatus;
import com.ams.entity.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ManagerDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(ManagerDashboardService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TimeRecordService timeRecordService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ApprovalWorkflowService approvalWorkflowService;

    public DashboardOverview getDashboardOverview(String managerId) {
        // Get team members
        List<User> teamMembers = userService.getTeamMembersByManagerId(managerId);
        
        // Get today's attendance
        List<TimeRecord> todayAttendance = timeRecordService.getTeamRecords(managerId, LocalDate.now());
        
        // Get alerts
        List<Alert> unreadAlerts = alertService.getUnreadAlertsForManager(managerId);
        
        // Get approval statistics
        ApprovalWorkflowService.ApprovalStatistics approvalStats = 
                approvalWorkflowService.getApprovalStatistics(managerId);
        
        // Calculate team statistics
        TeamStatistics teamStats = calculateTeamStatistics(teamMembers, todayAttendance);
        
        DashboardOverview overview = new DashboardOverview();
        overview.setTeamSize(teamMembers.size());
        overview.setTodayPresent(teamStats.getPresentToday());
        overview.setTodayLate(teamStats.getLateToday());
        overview.setTodayAbsent(teamStats.getAbsentToday());
        overview.setUnreadAlerts(unreadAlerts.size());
        overview.setPendingApprovals(approvalStats.getTotalPendingRequests());
        overview.setTeamMembers(teamMembers);
        overview.setTodayAttendance(todayAttendance);
        overview.setRecentAlerts(unreadAlerts.stream().limit(10).toList());
        
        logger.debug("Generated dashboard overview for manager: {}", managerId);
        return overview;
    }

    public TeamStatistics getTeamStatistics(String managerId, LocalDate startDate, LocalDate endDate) {
        List<User> teamMembers = userService.getTeamMembersByManagerId(managerId);
        List<TimeRecord> attendanceRecords = timeRecordService.getTeamRecords(managerId, startDate, endDate);
        
        return calculateDetailedTeamStatistics(teamMembers, attendanceRecords, startDate, endDate);
    }

    public Map<String, Object> getAttendanceSummary(String managerId, LocalDate date) {
        List<TimeRecord> attendanceRecords = timeRecordService.getTeamRecords(managerId, date);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date);
        summary.put("totalRecords", attendanceRecords.size());
        
        Map<AttendanceStatus, Long> statusCounts = attendanceRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        TimeRecord::getStatus,
                        java.util.stream.Collectors.counting()
                ));
        
        summary.put("present", statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L));
        summary.put("late", statusCounts.getOrDefault(AttendanceStatus.LATE, 0L));
        summary.put("absent", statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L));
        summary.put("earlyLeave", statusCounts.getOrDefault(AttendanceStatus.EARLY_LEAVE, 0L));
        
        // Calculate average working hours
        double avgHours = attendanceRecords.stream()
                .filter(record -> record.getTotalHours() != null)
                .mapToDouble(record -> record.getTotalHours().doubleValue())
                .average()
                .orElse(0.0);
        
        summary.put("averageWorkingHours", Math.round(avgHours * 100.0) / 100.0);
        
        return summary;
    }

    public List<PerformanceMetric> getTeamPerformanceMetrics(String managerId, LocalDate startDate, LocalDate endDate) {
        List<User> teamMembers = userService.getTeamMembersByManagerId(managerId);
        
        return teamMembers.stream()
                .map(user -> calculateUserPerformanceMetric(user, startDate, endDate))
                .toList();
    }

    private TeamStatistics calculateTeamStatistics(List<User> teamMembers, List<TimeRecord> todayAttendance) {
        TeamStatistics stats = new TeamStatistics();
        
        stats.setTotalEmployees(teamMembers.size());
        
        Map<AttendanceStatus, Long> statusCounts = todayAttendance.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        TimeRecord::getStatus,
                        java.util.stream.Collectors.counting()
                ));
        
        stats.setPresentToday(statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L).intValue());
        stats.setLateToday(statusCounts.getOrDefault(AttendanceStatus.LATE, 0L).intValue());
        stats.setAbsentToday(teamMembers.size() - todayAttendance.size());
        
        return stats;
    }

    private TeamStatistics calculateDetailedTeamStatistics(List<User> teamMembers, 
                                                          List<TimeRecord> attendanceRecords,
                                                          LocalDate startDate, LocalDate endDate) {
        TeamStatistics stats = new TeamStatistics();
        
        stats.setTotalEmployees(teamMembers.size());
        
        // Group records by status
        Map<AttendanceStatus, Long> statusCounts = attendanceRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        TimeRecord::getStatus,
                        java.util.stream.Collectors.counting()
                ));
        
        stats.setPresentDays(statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L).intValue());
        stats.setLateDays(statusCounts.getOrDefault(AttendanceStatus.LATE, 0L).intValue());
        stats.setAbsentDays(statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L).intValue());
        stats.setEarlyLeaveDays(statusCounts.getOrDefault(AttendanceStatus.EARLY_LEAVE, 0L).intValue());
        
        // Calculate average working hours
        double avgHours = attendanceRecords.stream()
                .filter(record -> record.getTotalHours() != null)
                .mapToDouble(record -> record.getTotalHours().doubleValue())
                .average()
                .orElse(0.0);
        
        stats.setAverageWorkingHours(Math.round(avgHours * 100.0) / 100.0);
        
        // Calculate total working days in period
        long workingDays = startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek().getValue() <= 5) // Weekdays only
                .count();
        
        stats.setWorkingDaysInPeriod(workingDays);
        
        // Calculate attendance rate
        if (workingDays > 0 && !teamMembers.isEmpty()) {
            double expectedAttendance = teamMembers.size() * workingDays;
            double actualAttendance = stats.getPresentDays() + stats.getLateDays();
            stats.setAttendanceRate(Math.round((actualAttendance / expectedAttendance) * 10000.0) / 100.0);
        }
        
        return stats;
    }

    private PerformanceMetric calculateUserPerformanceMetric(User user, LocalDate startDate, LocalDate endDate) {
        PerformanceMetric metric = new PerformanceMetric();
        metric.setUserId(user.getId());
        metric.setUserName(user.getName());
        metric.setEmployeeId(user.getEmployeeId());
        
        // Get user's attendance records for the period
        List<TimeRecord> userRecords = timeRecordService.getTimeRecords(user.getEmployeeId(), startDate, endDate);
        
        // Calculate statistics
        Map<AttendanceStatus, Long> statusCounts = userRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        TimeRecord::getStatus,
                        java.util.stream.Collectors.counting()
                ));
        
        metric.setPresentDays(statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L).intValue());
        metric.setLateDays(statusCounts.getOrDefault(AttendanceStatus.LATE, 0L).intValue());
        metric.setAbsentDays(statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L).intValue());
        
        // Calculate total and average working hours
        Double totalHours = timeRecordService.getTotalWorkingHours(user.getEmployeeId(), startDate, endDate);
        Double avgHours = timeRecordService.getAverageWorkingHours(user.getEmployeeId(), startDate, endDate);
        
        metric.setTotalWorkingHours(totalHours != null ? totalHours : 0.0);
        metric.setAverageWorkingHours(avgHours != null ? avgHours : 0.0);
        
        // Calculate working days and attendance rate
        long workingDays = startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek().getValue() <= 5)
                .count();
        
        if (workingDays > 0) {
            double attendanceRate = ((double) (metric.getPresentDays() + metric.getLateDays())) / workingDays * 100;
            metric.setAttendanceRate(Math.round(attendanceRate * 100.0) / 100.0);
        }
        
        return metric;
    }

    // Inner classes for response DTOs
    public static class DashboardOverview {
        private int teamSize;
        private int todayPresent;
        private int todayLate;
        private int todayAbsent;
        private int unreadAlerts;
        private long pendingApprovals;
        private List<User> teamMembers;
        private List<TimeRecord> todayAttendance;
        private List<Alert> recentAlerts;

        // Getters and setters
        public int getTeamSize() { return teamSize; }
        public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
        public int getTodayPresent() { return todayPresent; }
        public void setTodayPresent(int todayPresent) { this.todayPresent = todayPresent; }
        public int getTodayLate() { return todayLate; }
        public void setTodayLate(int todayLate) { this.todayLate = todayLate; }
        public int getTodayAbsent() { return todayAbsent; }
        public void setTodayAbsent(int todayAbsent) { this.todayAbsent = todayAbsent; }
        public int getUnreadAlerts() { return unreadAlerts; }
        public void setUnreadAlerts(int unreadAlerts) { this.unreadAlerts = unreadAlerts; }
        public long getPendingApprovals() { return pendingApprovals; }
        public void setPendingApprovals(long pendingApprovals) { this.pendingApprovals = pendingApprovals; }
        public List<User> getTeamMembers() { return teamMembers; }
        public void setTeamMembers(List<User> teamMembers) { this.teamMembers = teamMembers; }
        public List<TimeRecord> getTodayAttendance() { return todayAttendance; }
        public void setTodayAttendance(List<TimeRecord> todayAttendance) { this.todayAttendance = todayAttendance; }
        public List<Alert> getRecentAlerts() { return recentAlerts; }
        public void setRecentAlerts(List<Alert> recentAlerts) { this.recentAlerts = recentAlerts; }
    }

    public static class TeamStatistics {
        private int totalEmployees;
        private int presentToday;
        private int lateToday;
        private int absentToday;
        private int presentDays;
        private int lateDays;
        private int absentDays;
        private int earlyLeaveDays;
        private double averageWorkingHours;
        private long workingDaysInPeriod;
        private double attendanceRate;

        // Getters and setters
        public int getTotalEmployees() { return totalEmployees; }
        public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }
        public int getPresentToday() { return presentToday; }
        public void setPresentToday(int presentToday) { this.presentToday = presentToday; }
        public int getLateToday() { return lateToday; }
        public void setLateToday(int lateToday) { this.lateToday = lateToday; }
        public int getAbsentToday() { return absentToday; }
        public void setAbsentToday(int absentToday) { this.absentToday = absentToday; }
        public int getPresentDays() { return presentDays; }
        public void setPresentDays(int presentDays) { this.presentDays = presentDays; }
        public int getLateDays() { return lateDays; }
        public void setLateDays(int lateDays) { this.lateDays = lateDays; }
        public int getAbsentDays() { return absentDays; }
        public void setAbsentDays(int absentDays) { this.absentDays = absentDays; }
        public int getEarlyLeaveDays() { return earlyLeaveDays; }
        public void setEarlyLeaveDays(int earlyLeaveDays) { this.earlyLeaveDays = earlyLeaveDays; }
        public double getAverageWorkingHours() { return averageWorkingHours; }
        public void setAverageWorkingHours(double averageWorkingHours) { this.averageWorkingHours = averageWorkingHours; }
        public long getWorkingDaysInPeriod() { return workingDaysInPeriod; }
        public void setWorkingDaysInPeriod(long workingDaysInPeriod) { this.workingDaysInPeriod = workingDaysInPeriod; }
        public double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
    }

    public static class PerformanceMetric {
        private java.util.UUID userId;
        private String userName;
        private String employeeId;
        private int presentDays;
        private int lateDays;
        private int absentDays;
        private double totalWorkingHours;
        private double averageWorkingHours;
        private double attendanceRate;

        // Getters and setters
        public java.util.UUID getUserId() { return userId; }
        public void setUserId(java.util.UUID userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public int getPresentDays() { return presentDays; }
        public void setPresentDays(int presentDays) { this.presentDays = presentDays; }
        public int getLateDays() { return lateDays; }
        public void setLateDays(int lateDays) { this.lateDays = lateDays; }
        public int getAbsentDays() { return absentDays; }
        public void setAbsentDays(int absentDays) { this.absentDays = absentDays; }
        public double getTotalWorkingHours() { return totalWorkingHours; }
        public void setTotalWorkingHours(double totalWorkingHours) { this.totalWorkingHours = totalWorkingHours; }
        public double getAverageWorkingHours() { return averageWorkingHours; }
        public void setAverageWorkingHours(double averageWorkingHours) { this.averageWorkingHours = averageWorkingHours; }
        public double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
    }
}