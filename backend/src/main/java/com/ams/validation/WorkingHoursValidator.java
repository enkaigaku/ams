package com.ams.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;

public class WorkingHoursValidator implements ConstraintValidator<ValidWorkingHours, Object> {

    private String clockInField;
    private String clockOutField;
    private int maxWorkingHours;
    private int minWorkingMinutes;

    @Override
    public void initialize(ValidWorkingHours annotation) {
        this.clockInField = annotation.clockInField();
        this.clockOutField = annotation.clockOutField();
        this.maxWorkingHours = annotation.maxWorkingHours();
        this.minWorkingMinutes = annotation.minWorkingMinutes();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let other validators handle null checks
        }

        try {
            Field clockInFieldRef = value.getClass().getDeclaredField(clockInField);
            Field clockOutFieldRef = value.getClass().getDeclaredField(clockOutField);
            
            clockInFieldRef.setAccessible(true);
            clockOutFieldRef.setAccessible(true);
            
            LocalDateTime clockIn = (LocalDateTime) clockInFieldRef.get(value);
            LocalDateTime clockOut = (LocalDateTime) clockOutFieldRef.get(value);
            
            // If either time is null, skip validation (allow partial modifications)
            if (clockIn == null || clockOut == null) {
                return true;
            }
            
            // Check if clock-in is before clock-out
            if (!clockIn.isBefore(clockOut)) {
                addConstraintViolation(context, "出勤時刻は退勤時刻より前である必要があります");
                return false;
            }
            
            // Check working hours duration
            Duration workingDuration = Duration.between(clockIn, clockOut);
            
            // Check minimum working time
            if (workingDuration.toMinutes() < minWorkingMinutes) {
                addConstraintViolation(context, String.format("勤務時間は最低%d分以上である必要があります", minWorkingMinutes));
                return false;
            }
            
            // Check maximum working hours
            if (workingDuration.toHours() > maxWorkingHours) {
                addConstraintViolation(context, String.format("勤務時間は%d時間以内である必要があります", maxWorkingHours));
                return false;
            }
            
            // Check if both times are on the same date
            if (!clockIn.toLocalDate().equals(clockOut.toLocalDate())) {
                addConstraintViolation(context, "出勤時刻と退勤時刻は同じ日である必要があります");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            // If we can't access the fields, validation fails
            return false;
        }
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}