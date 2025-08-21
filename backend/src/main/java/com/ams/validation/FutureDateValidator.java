package com.ams.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class FutureDateValidator implements ConstraintValidator<ValidFutureDate, LocalDate> {

    private boolean allowToday;
    private int maxDaysInFuture;

    @Override
    public void initialize(ValidFutureDate annotation) {
        this.allowToday = annotation.allowToday();
        this.maxDaysInFuture = annotation.maxDaysInFuture();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let other validators handle null checks
        }

        LocalDate today = LocalDate.now();
        
        // Check if date is in the past
        if (value.isBefore(today)) {
            addConstraintViolation(context, "過去の日付は指定できません");
            return false;
        }
        
        // Check if today is allowed
        if (!allowToday && value.equals(today)) {
            addConstraintViolation(context, "本日の日付は指定できません");
            return false;
        }
        
        // Check maximum future date
        LocalDate maxFutureDate = today.plusDays(maxDaysInFuture);
        if (value.isAfter(maxFutureDate)) {
            addConstraintViolation(context, String.format("%d日以降の日付は指定できません", maxDaysInFuture));
            return false;
        }
        
        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}