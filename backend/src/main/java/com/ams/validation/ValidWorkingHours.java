package com.ams.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = WorkingHoursValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidWorkingHours {
    
    String message() default "勤務時間が不正です";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    String clockInField() default "requestedClockIn";
    
    String clockOutField() default "requestedClockOut";
    
    int maxWorkingHours() default 16; // Maximum working hours per day
    
    int minWorkingMinutes() default 30; // Minimum working time in minutes
}