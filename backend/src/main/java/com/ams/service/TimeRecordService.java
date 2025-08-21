package com.ams.service;

import com.ams.entity.TimeRecord;
import com.ams.entity.User;
import com.ams.entity.enums.AttendanceStatus;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.TimeRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Transactional
public class TimeRecordService {

    private static final Logger logger = LoggerFactory.getLogger(TimeRecordService.class);

    // Business constants
    private static final LocalTime STANDARD_START_TIME = LocalTime.of(9, 0); // 09:00
    private static final int LATE_THRESHOLD_MINUTES = 15; // 15 minutes late threshold

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private com.ams.validation.BusinessRuleValidator businessRuleValidator;

    public TimeRecord clockIn(String employeeId, LocalDateTime clockInTime) {
        User user = userService.getUserByEmployeeId(employeeId);
        LocalDate recordDate = clockInTime.toLocalDate();
        
        // Apply business rule validation
        businessRuleValidator.validateClockIn(user, clockInTime);
        
        // Get or create time record for today
        TimeRecord timeRecord = getOrCreateTimeRecord(user, recordDate);
        
        // Validate clock-in
        if (timeRecord.getClockIn() != null) {
            throw new IllegalStateException("既に出勤打刻済みです");
        }
        
        // Set clock-in time
        timeRecord.clockIn(clockInTime);
        
        // Determine attendance status
        determineAttendanceStatus(timeRecord, clockInTime);
        
        TimeRecord savedRecord = timeRecordRepository.save(timeRecord);
        
        // Generate alerts if necessary
        if (savedRecord.getStatus() == AttendanceStatus.LATE) {
            alertService.createLateAlert(user, recordDate, clockInTime);
        }
        
        logger.info("Clock-in recorded for user: {} at {}", employeeId, clockInTime);
        return savedRecord;
    }

    public TimeRecord clockOut(String employeeId, LocalDateTime clockOutTime) {
        User user = userService.getUserByEmployeeId(employeeId);
        LocalDate recordDate = clockOutTime.toLocalDate();
        
        // Apply business rule validation
        businessRuleValidator.validateClockOut(user, clockOutTime);
        
        // Get today's record
        TimeRecord timeRecord = getTodayRecord(user.getId(), recordDate)
                .orElseThrow(() -> new IllegalStateException("出勤打刻がありません"));
        
        // Validate clock-out
        if (timeRecord.getClockOut() != null) {
            throw new IllegalStateException("既に退勤打刻済みです");
        }
        
        if (timeRecord.getClockIn() == null) {
            throw new IllegalStateException("出勤打刻が必要です");
        }
        
        // End break if still on break
        if (timeRecord.isOnBreak()) {
            timeRecord.endBreak(clockOutTime);
            logger.warn("Auto-ended break for user: {} during clock-out", employeeId);
        }
        
        // Set clock-out time
        timeRecord.clockOut(clockOutTime);
        
        TimeRecord savedRecord = timeRecordRepository.save(timeRecord);
        logger.info("Clock-out recorded for user: {} at {}", employeeId, clockOutTime);
        return savedRecord;
    }

    public TimeRecord startBreak(String employeeId, LocalDateTime breakStartTime) {
        User user = userService.getUserByEmployeeId(employeeId);
        LocalDate recordDate = breakStartTime.toLocalDate();
        
        TimeRecord timeRecord = getTodayRecord(user.getId(), recordDate)
                .orElseThrow(() -> new IllegalStateException("出勤打刻がありません"));
        
        // Validate break start
        if (timeRecord.getClockIn() == null) {
            throw new IllegalStateException("出勤打刻が必要です");
        }
        
        if (timeRecord.getClockOut() != null) {
            throw new IllegalStateException("既に退勤済みです");
        }
        
        if (timeRecord.isOnBreak()) {
            throw new IllegalStateException("既に休憩中です");
        }
        
        timeRecord.startBreak(breakStartTime);
        
        TimeRecord savedRecord = timeRecordRepository.save(timeRecord);
        logger.info("Break started for user: {} at {}", employeeId, breakStartTime);
        return savedRecord;
    }

    public TimeRecord endBreak(String employeeId, LocalDateTime breakEndTime) {
        User user = userService.getUserByEmployeeId(employeeId);
        LocalDate recordDate = breakEndTime.toLocalDate();
        
        TimeRecord timeRecord = getTodayRecord(user.getId(), recordDate)
                .orElseThrow(() -> new IllegalStateException("出勤打刻がありません"));
        
        // Validate break end
        if (!timeRecord.isOnBreak()) {
            throw new IllegalStateException("休憩中ではありません");
        }
        
        timeRecord.endBreak(breakEndTime);
        
        TimeRecord savedRecord = timeRecordRepository.save(timeRecord);
        logger.info("Break ended for user: {} at {}", employeeId, breakEndTime);
        return savedRecord;
    }

    @Transactional(readOnly = true)
    public Optional<TimeRecord> getTodayRecord(UUID userId, LocalDate date) {
        return timeRecordRepository.findByUserIdAndRecordDate(userId, date);
    }

    @Transactional(readOnly = true)
    public Optional<TimeRecord> getTodayRecord(String employeeId) {
        User user = userService.getUserByEmployeeId(employeeId);
        return getTodayRecord(user.getId(), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<TimeRecord> getTimeRecords(String employeeId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
                user.getId(), startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<TimeRecord> getTeamRecords(String managerId, LocalDate date) {
        return timeRecordRepository.findByManagerIdAndDate(managerId, date);
    }

    @Transactional(readOnly = true)
    public List<TimeRecord> getTeamRecords(String managerId, LocalDate startDate, LocalDate endDate) {
        return timeRecordRepository.findByManagerIdAndDateRange(managerId, startDate, endDate);
    }

    private TimeRecord getOrCreateTimeRecord(User user, LocalDate recordDate) {
        return timeRecordRepository.findByUserIdAndRecordDate(user.getId(), recordDate)
                .orElseGet(() -> {
                    TimeRecord newRecord = new TimeRecord(user, recordDate);
                    return timeRecordRepository.save(newRecord);
                });
    }

    private void determineAttendanceStatus(TimeRecord timeRecord, LocalDateTime clockInTime) {
        LocalTime clockInTimeOnly = clockInTime.toLocalTime();
        
        if (clockInTimeOnly.isAfter(STANDARD_START_TIME.plusMinutes(LATE_THRESHOLD_MINUTES))) {
            timeRecord.setStatus(AttendanceStatus.LATE);
        } else {
            timeRecord.setStatus(AttendanceStatus.PRESENT);
        }
    }

    public TimeRecord updateTimeRecord(UUID recordId, TimeRecord updates) {
        TimeRecord existingRecord = timeRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Time record not found with id: " + recordId));
        
        // Update allowed fields
        if (updates.getClockIn() != null) {
            existingRecord.setClockIn(updates.getClockIn());
        }
        if (updates.getClockOut() != null) {
            existingRecord.setClockOut(updates.getClockOut());
        }
        if (updates.getBreakStart() != null) {
            existingRecord.setBreakStart(updates.getBreakStart());
        }
        if (updates.getBreakEnd() != null) {
            existingRecord.setBreakEnd(updates.getBreakEnd());
        }
        if (updates.getNotes() != null) {
            existingRecord.setNotes(updates.getNotes());
        }
        
        // Recalculate total hours and status
        existingRecord.calculateTotalHours();
        if (existingRecord.getClockIn() != null) {
            determineAttendanceStatus(existingRecord, existingRecord.getClockIn());
        }
        
        TimeRecord savedRecord = timeRecordRepository.save(existingRecord);
        logger.info("Updated time record: {} for user: {}", recordId, existingRecord.getUser().getEmployeeId());
        return savedRecord;
    }

    @Transactional(readOnly = true)
    public boolean hasTimeRecordForDate(String employeeId, LocalDate date) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeRecordRepository.existsByUserIdAndRecordDate(user.getId(), date);
    }

    @Transactional(readOnly = true)
    public List<TimeRecord> getIncompleteRecordsForDate(LocalDate date) {
        return timeRecordRepository.findIncompleteRecordsForDate(date);
    }

    // Statistics methods
    @Transactional(readOnly = true)
    public long countAttendanceByStatus(LocalDate date, AttendanceStatus status) {
        return timeRecordRepository.countByDateAndStatus(date, status);
    }

    @Transactional(readOnly = true)
    public Double getAverageWorkingHours(String employeeId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeRecordRepository.getAverageHoursByUserAndDateRange(user.getId(), startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Double getTotalWorkingHours(String employeeId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeRecordRepository.getTotalHoursByUserAndDateRange(user.getId(), startDate, endDate);
    }

    // Background job to create missing clock-out alerts
    public void checkMissingClockOuts(LocalDate date) {
        List<TimeRecord> incompleteRecords = getIncompleteRecordsForDate(date);
        
        for (TimeRecord record : incompleteRecords) {
            alertService.createMissingClockOutAlert(record.getUser(), date);
        }
        
        if (!incompleteRecords.isEmpty()) {
            logger.info("Created missing clock-out alerts for {} records on {}", 
                       incompleteRecords.size(), date);
        }
    }

    // CSV Export support methods
    @Transactional(readOnly = true)
    public Stream<TimeRecord> getTimeRecordsStream(String employeeId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeRecordRepository.findByUserIdAndDateRangeStream(user.getId(), startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Stream<TimeRecord> getAllTimeRecordsStream(LocalDate startDate, LocalDate endDate) {
        return timeRecordRepository.findByDateRangeStream(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public long countPresentDays(String employeeId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeRecordRepository.countByUserIdAndDateRangeAndStatus(user.getId(), startDate, endDate, AttendanceStatus.PRESENT);
    }

    @Transactional(readOnly = true)
    public long countLateDays(String employeeId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeRecordRepository.countByUserIdAndDateRangeAndStatus(user.getId(), startDate, endDate, AttendanceStatus.LATE);
    }

    @Transactional(readOnly = true)
    public long countAbsentDays(String employeeId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeRecordRepository.countByUserIdAndDateRangeAndStatus(user.getId(), startDate, endDate, AttendanceStatus.ABSENT);
    }
}