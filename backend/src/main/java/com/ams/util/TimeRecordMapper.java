package com.ams.util;

import com.ams.dto.time.TimeRecordDto;
import com.ams.entity.TimeRecord;
import org.springframework.stereotype.Component;

@Component
public class TimeRecordMapper {

    public TimeRecordDto toDto(TimeRecord timeRecord) {
        if (timeRecord == null) {
            return null;
        }

        TimeRecordDto dto = new TimeRecordDto();
        dto.setId(timeRecord.getId());
        dto.setRecordDate(timeRecord.getRecordDate());
        dto.setClockIn(timeRecord.getClockIn());
        dto.setClockOut(timeRecord.getClockOut());
        dto.setBreakStart(timeRecord.getBreakStart());
        dto.setBreakEnd(timeRecord.getBreakEnd());
        dto.setTotalHours(timeRecord.getTotalHours());
        dto.setStatus(timeRecord.getStatus());
        dto.setNotes(timeRecord.getNotes());
        dto.setCreatedAt(timeRecord.getCreatedAt());
        dto.setUpdatedAt(timeRecord.getUpdatedAt());

        // Set user information if available
        if (timeRecord.getUser() != null) {
            dto.setUserId(timeRecord.getUser().getId());
            dto.setUserName(timeRecord.getUser().getName());
            dto.setEmployeeId(timeRecord.getUser().getEmployeeId());
        }

        return dto;
    }

    public TimeRecord toEntity(TimeRecordDto dto) {
        if (dto == null) {
            return null;
        }

        TimeRecord timeRecord = new TimeRecord();
        timeRecord.setId(dto.getId());
        timeRecord.setRecordDate(dto.getRecordDate());
        timeRecord.setClockIn(dto.getClockIn());
        timeRecord.setClockOut(dto.getClockOut());
        timeRecord.setBreakStart(dto.getBreakStart());
        timeRecord.setBreakEnd(dto.getBreakEnd());
        timeRecord.setTotalHours(dto.getTotalHours());
        timeRecord.setStatus(dto.getStatus());
        timeRecord.setNotes(dto.getNotes());

        return timeRecord;
    }
}