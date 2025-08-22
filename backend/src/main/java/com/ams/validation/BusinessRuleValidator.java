package com.ams.validation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ams.entity.LeaveRequest;
import com.ams.entity.TimeModificationRequest;
import com.ams.entity.TimeRecord;
import com.ams.entity.User;
import com.ams.entity.enums.LeaveType;
import com.ams.entity.enums.RequestStatus;
import com.ams.exception.BusinessRuleViolationException;
import com.ams.repository.LeaveRequestRepository;
import com.ams.repository.TimeModificationRequestRepository;
import com.ams.repository.TimeRecordRepository;

@Component
public class BusinessRuleValidator {

    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleValidator.class);

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private TimeModificationRequestRepository timeModificationRequestRepository;

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    // Leave request validation rules
    public void validateLeaveRequest(User user, LeaveType leaveType, LocalDate startDate, LocalDate endDate, String reason) {
        logger.debug("Validating leave request for user: {} from {} to {}", user.getEmployeeId(), startDate, endDate);

        // Rule 1: Cannot request leave for weekends (Saturday/Sunday)
        validateNoWeekendLeave(startDate, endDate);

        // Rule 2: Cannot have overlapping leave requests
        validateNoOverlappingLeaveRequests(user, startDate, endDate);

        // Rule 3: Minimum advance notice required (2 days for annual leave, 1 day for sick leave)
        validateAdvanceNotice(leaveType, startDate);

        // Rule 4: Maximum consecutive leave days based on leave type
        validateMaxConsecutiveDays(leaveType, startDate, endDate);

        // Rule 5: Cannot exceed annual leave balance (simplified - assume 20 days per year)
        validateLeaveBalance(user, leaveType, startDate, endDate);

        // Rule 6: Cannot request leave for past dates
        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException("過去の日付の休暇申請はできません");
        }

        // Rule 7: Sick leave can only be requested for single days or short periods
        if (leaveType == LeaveType.SICK && ChronoUnit.DAYS.between(startDate, endDate) > 7) {
            throw new BusinessRuleViolationException("病気休暇は7日以内で申請してください");
        }
    }

    // Time modification validation rules
    public void validateTimeModificationRequest(User user, LocalDate requestDate, 
                                              LocalDateTime requestedClockIn, LocalDateTime requestedClockOut, String reason) {
        logger.debug("Validating time modification request for user: {} for date: {}", user.getEmployeeId(), requestDate);

        // Rule 1: Cannot modify future dates
        if (requestDate.isAfter(LocalDate.now())) {
            throw new BusinessRuleViolationException("未来の日付の勤務時間修正はできません");
        }

        // Rule 2: Cannot modify dates older than 30 days
        if (requestDate.isBefore(LocalDate.now().minusDays(30))) {
            throw new BusinessRuleViolationException("30日以上前の勤務時間修正はできません");
        }

        // Rule 3: Cannot have multiple pending requests for the same date
        validateNoPendingTimeModifications(user, requestDate);

        // Rule 4: Cannot request modification for non-working days (weekends)
        if (requestDate.getDayOfWeek() == DayOfWeek.SATURDAY || requestDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BusinessRuleViolationException("休日の勤務時間修正はできません");
        }

        // Rule 5: If both times provided, validate working hours
        if (requestedClockIn != null && requestedClockOut != null) {
            validateWorkingHours(requestedClockIn, requestedClockOut);
        }

        // Rule 6: Cannot modify if user was on leave that day
        validateNotOnLeave(user, requestDate);
    }

    // Clock-in/out validation rules
    public void validateClockIn(User user, LocalDateTime clockInTime) {
        logger.debug("Validating clock-in for user: {} at {}", user.getEmployeeId(), clockInTime);

        LocalDate clockInDate = clockInTime.toLocalDate();

        // Rule 1: Cannot clock in on weekends
        if (clockInDate.getDayOfWeek() == DayOfWeek.SATURDAY || clockInDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BusinessRuleViolationException("休日は出勤打刻できません");
        }

        // Rule 2: Cannot clock in if already clocked in today
        validateNotAlreadyClockedIn(user, clockInDate);

        // Rule 3: Cannot clock in if on approved leave
        validateNotOnApprovedLeave(user, clockInDate);

        // Rule 4: Cannot clock in too early (before 6:00 AM) or too late (after 11:00 PM)
        int hour = clockInTime.getHour();
        if (hour < 6 || hour > 23) {
            throw new BusinessRuleViolationException("出勤打刻は6時から23時の間で行ってください");
        }

        // Rule 5: Cannot clock in for future dates
        if (clockInDate.isAfter(LocalDate.now())) {
            throw new BusinessRuleViolationException("未来の日付での出勤打刻はできません");
        }
    }

    public void validateClockOut(User user, LocalDateTime clockOutTime) {
        logger.debug("Validating clock-out for user: {} at {}", user.getEmployeeId(), clockOutTime);

        LocalDate clockOutDate = clockOutTime.toLocalDate();

        // Rule 1: Must have clocked in first
        validateHasClockedIn(user, clockOutDate);

        // Rule 2: Cannot clock out before clocking in
        validateClockOutAfterClockIn(user, clockOutTime);

        // Rule 3: Minimum working time (30 minutes)
        validateMinimumWorkingTime(user, clockOutTime);

        // Rule 4: Cannot clock out for future dates
        if (clockOutDate.isAfter(LocalDate.now())) {
            throw new BusinessRuleViolationException("未来の日付での退勤打刻はできません");
        }
    }

    // Helper validation methods
    private void validateNoWeekendLeave(LocalDate startDate, LocalDate endDate) {
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek() == DayOfWeek.SATURDAY || current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                throw new BusinessRuleViolationException("休日を含む休暇申請はできません");
            }
            current = current.plusDays(1);
        }
    }

    private void validateNoOverlappingLeaveRequests(User user, LocalDate startDate, LocalDate endDate) {
        List<RequestStatus> statuses = List.of(RequestStatus.PENDING, RequestStatus.APPROVED);
        List<LeaveRequest> overlappingRequests = leaveRequestRepository
                .findOverlappingRequests(user.getId(), startDate, endDate, statuses);
        
        if (!overlappingRequests.isEmpty()) {
            throw new BusinessRuleViolationException("指定期間に既に休暇申請があります");
        }
    }

    private void validateAdvanceNotice(LeaveType leaveType, LocalDate startDate) {
        LocalDate today = LocalDate.now();
        long daysUntilLeave = ChronoUnit.DAYS.between(today, startDate);
        
        switch (leaveType) {
            case ANNUAL, SPECIAL -> {
                if (daysUntilLeave < 2) {
                    throw new BusinessRuleViolationException("年次休暇・特別休暇は2日前までに申請してください");
                }
            }
            case SICK -> {
            }
            case MATERNITY, PATERNITY -> {
                if (daysUntilLeave < 14) {
                    throw new BusinessRuleViolationException("産休・育休は14日前までに申請してください");
                }
            }
        }
        // Sick leave can be applied for current day in emergencies
    }

    private void validateMaxConsecutiveDays(LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        switch (leaveType) {
            case ANNUAL -> {
                if (days > 10) {
                    throw new BusinessRuleViolationException("年次休暇は連続10日以内で申請してください");
                }
            }
            case SICK -> {
                if (days > 7) {
                    throw new BusinessRuleViolationException("病気休暇は連続7日以内で申請してください");
                }
            }
            case SPECIAL -> {
                if (days > 5) {
                    throw new BusinessRuleViolationException("特別休暇は連続5日以内で申請してください");
                }
            }
        }
    }

    private void validateLeaveBalance(User user, LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        if (leaveType == LeaveType.ANNUAL) {
            long requestedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            
            // Get used annual leave days this year
            LocalDate yearStart = LocalDate.of(LocalDate.now().getYear(), 1, 1);
            LocalDate yearEnd = LocalDate.of(LocalDate.now().getYear(), 12, 31);
            
            long usedDays = leaveRequestRepository.countApprovedLeaveDays(
                    user.getId(), LeaveType.ANNUAL, yearStart, yearEnd);
            
            // Assume 20 days annual leave entitlement
            if (usedDays + requestedDays > 20) {
                throw new BusinessRuleViolationException(
                        String.format("年次休暇の残日数が不足しています（使用済み: %d日、申請: %d日、上限: 20日）", 
                                    usedDays, requestedDays));
            }
        }
    }

    private void validateNoPendingTimeModifications(User user, LocalDate requestDate) {
        List<TimeModificationRequest> pendingRequests = timeModificationRequestRepository
                .findActiveRequestsByUserAndDate(user.getId(), requestDate);
        
        if (!pendingRequests.isEmpty()) {
            throw new BusinessRuleViolationException("この日付に既に勤務時間修正申請があります");
        }
    }

    private void validateWorkingHours(LocalDateTime clockIn, LocalDateTime clockOut) {
        if (!clockIn.isBefore(clockOut)) {
            throw new BusinessRuleViolationException("出勤時刻は退勤時刻より前である必要があります");
        }

        long hours = ChronoUnit.HOURS.between(clockIn, clockOut);
        if (hours > 16) {
            throw new BusinessRuleViolationException("勤務時間は16時間以内である必要があります");
        }

        long minutes = ChronoUnit.MINUTES.between(clockIn, clockOut);
        if (minutes < 30) {
            throw new BusinessRuleViolationException("勤務時間は30分以上である必要があります");
        }
    }

    private void validateNotOnLeave(User user, LocalDate date) {
        List<LeaveRequest> approvedLeaves = leaveRequestRepository
                .findApprovedLeaveForDate(user.getId(), date);
        
        if (!approvedLeaves.isEmpty()) {
            throw new BusinessRuleViolationException("休暇中の日付の勤務時間修正はできません");
        }
    }

    private void validateNotAlreadyClockedIn(User user, LocalDate date) {
        Optional<TimeRecord> existingRecord = timeRecordRepository
                .findByUserIdAndRecordDate(user.getId(), date);
        
        if (existingRecord.isPresent() && existingRecord.get().getClockIn() != null) {
            throw new BusinessRuleViolationException("既に出勤打刻済みです");
        }
    }

    private void validateNotOnApprovedLeave(User user, LocalDate date) {
        List<LeaveRequest> approvedLeaves = leaveRequestRepository
                .findApprovedLeaveForDate(user.getId(), date);
        
        if (!approvedLeaves.isEmpty()) {
            throw new BusinessRuleViolationException("休暇中は出勤打刻できません");
        }
    }

    private void validateHasClockedIn(User user, LocalDate date) {
        Optional<TimeRecord> record = timeRecordRepository
                .findByUserIdAndRecordDate(user.getId(), date);
        
        if (record.isEmpty() || record.get().getClockIn() == null) {
            throw new BusinessRuleViolationException("出勤打刻を先に行ってください");
        }
    }

    private void validateClockOutAfterClockIn(User user, LocalDateTime clockOutTime) {
        LocalDate date = clockOutTime.toLocalDate();
        Optional<TimeRecord> record = timeRecordRepository
                .findByUserIdAndRecordDate(user.getId(), date);
        
        if (record.isPresent() && record.get().getClockIn() != null && 
            !record.get().getClockIn().isBefore(clockOutTime)) {
            throw new BusinessRuleViolationException("退勤時刻は出勤時刻より後である必要があります");
        }
    }

    private void validateMinimumWorkingTime(User user, LocalDateTime clockOutTime) {
        LocalDate date = clockOutTime.toLocalDate();
        Optional<TimeRecord> record = timeRecordRepository
                .findByUserIdAndRecordDate(user.getId(), date);
        
        if (record.isPresent() && record.get().getClockIn() != null) {
            long minutes = ChronoUnit.MINUTES.between(record.get().getClockIn(), clockOutTime);
            if (minutes < 30) {
                throw new BusinessRuleViolationException("最低30分の勤務時間が必要です");
            }
        }
    }
}