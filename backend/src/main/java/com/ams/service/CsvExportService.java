package com.ams.service;

import com.ams.entity.TimeRecord;
import com.ams.entity.User;
import com.ams.entity.LeaveRequest;
import com.ams.entity.TimeModificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class CsvExportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private TimeRecordService timeRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private TimeModificationRequestService timeModificationRequestService;

    public void exportAttendanceRecords(Writer writer, LocalDate startDate, LocalDate endDate, String managerId) 
            throws IOException {
        logger.info("Starting attendance records CSV export for manager: {} from {} to {}", 
                   managerId, startDate, endDate);

        // Write CSV header
        writeAttendanceHeader(writer);

        // Get team members for this manager
        List<User> teamMembers = userService.getTeamMembersByManagerId(managerId);
        
        for (User member : teamMembers) {
            // Stream attendance records for each team member
            try (Stream<TimeRecord> recordStream = timeRecordService.getTimeRecordsStream(
                    member.getEmployeeId(), startDate, endDate)) {
                
                recordStream.forEach(record -> {
                    try {
                        writeAttendanceRecord(writer, record);
                    } catch (IOException e) {
                        logger.error("Error writing attendance record for employee: {}", 
                                   member.getEmployeeId(), e);
                        throw new RuntimeException("CSV書き込みエラー", e);
                    }
                });
            }
        }

        writer.flush();
        logger.info("Completed attendance records CSV export");
    }

    public void exportAllAttendanceRecords(Writer writer, LocalDate startDate, LocalDate endDate) 
            throws IOException {
        logger.info("Starting all attendance records CSV export from {} to {}", startDate, endDate);

        // Write CSV header
        writeAttendanceHeader(writer);

        // Stream all attendance records for the date range
        try (Stream<TimeRecord> recordStream = timeRecordService.getAllTimeRecordsStream(startDate, endDate)) {
            recordStream.forEach(record -> {
                try {
                    writeAttendanceRecord(writer, record);
                } catch (IOException e) {
                    logger.error("Error writing attendance record", e);
                    throw new RuntimeException("CSV書き込みエラー", e);
                }
            });
        }

        writer.flush();
        logger.info("Completed all attendance records CSV export");
    }

    public void exportLeaveRequests(Writer writer, LocalDate startDate, LocalDate endDate, String managerId) 
            throws IOException {
        logger.info("Starting leave requests CSV export for manager: {} from {} to {}", 
                   managerId, startDate, endDate);

        // Write CSV header
        writeLeaveRequestHeader(writer);

        // Get leave requests for team members
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsForManager(managerId, startDate, endDate);
        
        for (LeaveRequest request : leaveRequests) {
            writeLeaveRequestRecord(writer, request);
        }

        writer.flush();
        logger.info("Completed leave requests CSV export");
    }

    public void exportTimeModificationRequests(Writer writer, LocalDate startDate, LocalDate endDate, String managerId) 
            throws IOException {
        logger.info("Starting time modification requests CSV export for manager: {} from {} to {}", 
                   managerId, startDate, endDate);

        // Write CSV header
        writeTimeModificationHeader(writer);

        // Get time modification requests for team members
        List<TimeModificationRequest> requests = timeModificationRequestService.getTimeModificationRequestsForManager(managerId);
        
        // Filter by date range
        requests.stream()
                .filter(req -> !req.getRequestDate().isBefore(startDate) && !req.getRequestDate().isAfter(endDate))
                .forEach(request -> {
                    try {
                        writeTimeModificationRecord(writer, request);
                    } catch (IOException e) {
                        logger.error("Error writing time modification record", e);
                        throw new RuntimeException("CSV書き込みエラー", e);
                    }
                });

        writer.flush();
        logger.info("Completed time modification requests CSV export");
    }

    public void exportTeamSummary(Writer writer, LocalDate startDate, LocalDate endDate, String managerId) 
            throws IOException {
        logger.info("Starting team summary CSV export for manager: {} from {} to {}", 
                   managerId, startDate, endDate);

        // Write CSV header
        writeTeamSummaryHeader(writer);

        // Get team members
        List<User> teamMembers = userService.getTeamMembersByManagerId(managerId);
        
        for (User member : teamMembers) {
            // Calculate summary for each member
            Double totalHours = timeRecordService.getTotalWorkingHours(member.getEmployeeId(), startDate, endDate);
            Double avgHours = timeRecordService.getAverageWorkingHours(member.getEmployeeId(), startDate, endDate);
            long presentDays = timeRecordService.countPresentDays(member.getEmployeeId(), startDate, endDate);
            long lateDays = timeRecordService.countLateDays(member.getEmployeeId(), startDate, endDate);
            long absentDays = timeRecordService.countAbsentDays(member.getEmployeeId(), startDate, endDate);
            
            writeTeamSummaryRecord(writer, member, totalHours, avgHours, presentDays, lateDays, absentDays);
        }

        writer.flush();
        logger.info("Completed team summary CSV export");
    }

    private void writeAttendanceHeader(Writer writer) throws IOException {
        writer.write("従業員ID,氏名,部署,日付,出勤時刻,退勤時刻,休憩時間,総労働時間,出勤状況,備考\n");
    }

    private void writeAttendanceRecord(Writer writer, TimeRecord record) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        // Employee ID
        sb.append(escapeField(record.getUser().getEmployeeId())).append(",");
        
        // Employee Name
        sb.append(escapeField(record.getUser().getName())).append(",");
        
        // Department
        String departmentName = record.getUser().getDepartment() != null ? 
                               record.getUser().getDepartment().getName() : "";
        sb.append(escapeField(departmentName)).append(",");
        
        // Date
        sb.append(record.getRecordDate().format(DATE_FORMATTER)).append(",");
        
        // Clock In
        String clockIn = record.getClockIn() != null ? 
                        record.getClockIn().format(DATETIME_FORMATTER) : "";
        sb.append(escapeField(clockIn)).append(",");
        
        // Clock Out
        String clockOut = record.getClockOut() != null ? 
                         record.getClockOut().format(DATETIME_FORMATTER) : "";
        sb.append(escapeField(clockOut)).append(",");
        
        // Break Duration
        String breakDuration = "0";
        if (record.getBreakStart() != null && record.getBreakEnd() != null) {
            Duration duration = Duration.between(record.getBreakStart(), record.getBreakEnd());
            breakDuration = String.valueOf(duration.toMinutes());
        }
        sb.append(escapeField(breakDuration)).append(",");
        
        // Total Hours
        String totalHours = record.getTotalHours() != null ? 
                           record.getTotalHours().toString() : "0";
        sb.append(escapeField(totalHours)).append(",");
        
        // Status
        String status = record.getStatus() != null ? record.getStatus().name() : "";
        sb.append(escapeField(status)).append(",");
        
        // Notes
        String notes = record.getNotes() != null ? record.getNotes() : "";
        sb.append(escapeField(notes));
        
        sb.append("\n");
        writer.write(sb.toString());
    }

    private void writeLeaveRequestHeader(Writer writer) throws IOException {
        writer.write("申請ID,従業員ID,氏名,申請日,休暇開始日,休暇終了日,休暇種別,理由,状況,承認者,承認日,却下理由\n");
    }

    private void writeLeaveRequestRecord(Writer writer, LeaveRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        sb.append(escapeField(request.getId().toString())).append(",");
        sb.append(escapeField(request.getUser().getEmployeeId())).append(",");
        sb.append(escapeField(request.getUser().getName())).append(",");
        sb.append(request.getCreatedAt().format(DATETIME_FORMATTER)).append(",");
        sb.append(request.getStartDate().format(DATE_FORMATTER)).append(",");
        sb.append(request.getEndDate().format(DATE_FORMATTER)).append(",");
        sb.append(escapeField(request.getType().name())).append(",");
        sb.append(escapeField(request.getReason())).append(",");
        sb.append(escapeField(request.getStatus().name())).append(",");
        
        String approver = request.getApprovedBy() != null ? request.getApprovedBy() : "";
        sb.append(escapeField(approver)).append(",");
        
        String approvedAt = request.getApprovedAt() != null ? 
                          request.getApprovedAt().format(DATETIME_FORMATTER) : "";
        sb.append(escapeField(approvedAt)).append(",");
        
        String rejectionReason = request.getRejectionReason() != null ? request.getRejectionReason() : "";
        sb.append(escapeField(rejectionReason));
        
        sb.append("\n");
        writer.write(sb.toString());
    }

    private void writeTimeModificationHeader(Writer writer) throws IOException {
        writer.write("申請ID,従業員ID,氏名,対象日,元の出勤時刻,元の退勤時刻,修正後出勤時刻,修正後退勤時刻,理由,状況,申請日,承認者,承認日,却下理由\n");
    }

    private void writeTimeModificationRecord(Writer writer, TimeModificationRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        sb.append(escapeField(request.getId().toString())).append(",");
        sb.append(escapeField(request.getUser().getEmployeeId())).append(",");
        sb.append(escapeField(request.getUser().getName())).append(",");
        sb.append(request.getRequestDate().format(DATE_FORMATTER)).append(",");
        
        String originalClockIn = request.getOriginalClockIn() != null ? 
                               request.getOriginalClockIn().format(DATETIME_FORMATTER) : "";
        sb.append(escapeField(originalClockIn)).append(",");
        
        String originalClockOut = request.getOriginalClockOut() != null ? 
                                request.getOriginalClockOut().format(DATETIME_FORMATTER) : "";
        sb.append(escapeField(originalClockOut)).append(",");
        
        String requestedClockIn = request.getRequestedClockIn() != null ? 
                                request.getRequestedClockIn().format(DATETIME_FORMATTER) : "";
        sb.append(escapeField(requestedClockIn)).append(",");
        
        String requestedClockOut = request.getRequestedClockOut() != null ? 
                                 request.getRequestedClockOut().format(DATETIME_FORMATTER) : "";
        sb.append(escapeField(requestedClockOut)).append(",");
        
        sb.append(escapeField(request.getReason())).append(",");
        sb.append(escapeField(request.getStatus().name())).append(",");
        sb.append(request.getCreatedAt().format(DATETIME_FORMATTER)).append(",");
        
        String approver = request.getApprovedBy() != null ? request.getApprovedBy() : "";
        sb.append(escapeField(approver)).append(",");
        
        String approvedAt = request.getApprovedAt() != null ? 
                          request.getApprovedAt().format(DATETIME_FORMATTER) : "";
        sb.append(escapeField(approvedAt)).append(",");
        
        String rejectionReason = request.getRejectionReason() != null ? request.getRejectionReason() : "";
        sb.append(escapeField(rejectionReason));
        
        sb.append("\n");
        writer.write(sb.toString());
    }

    private void writeTeamSummaryHeader(Writer writer) throws IOException {
        writer.write("従業員ID,氏名,部署,総労働時間,平均労働時間,出勤日数,遅刻日数,欠勤日数\n");
    }

    private void writeTeamSummaryRecord(Writer writer, User user, Double totalHours, Double avgHours, 
                                      long presentDays, long lateDays, long absentDays) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        sb.append(escapeField(user.getEmployeeId())).append(",");
        sb.append(escapeField(user.getName())).append(",");
        
        String departmentName = user.getDepartment() != null ? user.getDepartment().getName() : "";
        sb.append(escapeField(departmentName)).append(",");
        
        sb.append(totalHours != null ? String.format("%.2f", totalHours) : "0.00").append(",");
        sb.append(avgHours != null ? String.format("%.2f", avgHours) : "0.00").append(",");
        sb.append(presentDays).append(",");
        sb.append(lateDays).append(",");
        sb.append(absentDays);
        
        sb.append("\n");
        writer.write(sb.toString());
    }

    private String escapeField(String field) {
        if (field == null) {
            return "";
        }
        
        // Escape double quotes and wrap in quotes if necessary
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        
        return field;
    }
}