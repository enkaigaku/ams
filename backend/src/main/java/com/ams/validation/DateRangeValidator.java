package com.ams.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startField;
    private String endField;
    private boolean allowSameDate;
    private int maxDaysRange;

    @Override
    public void initialize(ValidDateRange annotation) {
        this.startField = annotation.startField();
        this.endField = annotation.endField();
        this.allowSameDate = annotation.allowSameDate();
        this.maxDaysRange = annotation.maxDaysRange();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let other validators handle null checks
        }

        try {
            Field startDateField = value.getClass().getDeclaredField(startField);
            Field endDateField = value.getClass().getDeclaredField(endField);
            
            startDateField.setAccessible(true);
            endDateField.setAccessible(true);
            
            LocalDate startDate = (LocalDate) startDateField.get(value);
            LocalDate endDate = (LocalDate) endDateField.get(value);
            
            // If either date is null, let other validators handle it
            if (startDate == null || endDate == null) {
                return true;
            }
            
            // Check if start date is before or equal to end date
            if (allowSameDate) {
                if (startDate.isAfter(endDate)) {
                    addConstraintViolation(context, "開始日は終了日より前または同じ日である必要があります");
                    return false;
                }
            } else {
                if (!startDate.isBefore(endDate)) {
                    addConstraintViolation(context, "開始日は終了日より前である必要があります");
                    return false;
                }
            }
            
            // Check maximum range if specified
            if (maxDaysRange > 0) {
                long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
                if (daysBetween > maxDaysRange) {
                    addConstraintViolation(context, String.format("期間は%d日以内で指定してください", maxDaysRange));
                    return false;
                }
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