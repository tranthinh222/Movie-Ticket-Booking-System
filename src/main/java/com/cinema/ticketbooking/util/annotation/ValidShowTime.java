package com.cinema.ticketbooking.util.annotation;

import com.nimbusds.jose.Payload;
import jakarta.validation.Constraint;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Constraint(validatedBy = ShowTimeValidator.class)
public @interface ValidShowTime {
    String message() default "startTime must be before endTime";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
