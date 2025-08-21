package com.ams.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    
    String message() default "開始日は終了日より前である必要があります";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    String startField() default "startDate";
    
    String endField() default "endDate";
    
    boolean allowSameDate() default true;
    
    int maxDaysRange() default -1; // -1 means no limit
}