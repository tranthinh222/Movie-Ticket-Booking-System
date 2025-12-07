package com.cinema.ticketbooking.util.annotation;

import com.cinema.ticketbooking.domain.request.ReqCreateShowTimeDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateShowTimeDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;

public class ShowTimeValidator implements ConstraintValidator<ValidShowTime, Object> {
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){
        LocalTime startTime = null;
        LocalTime endTime = null;

        if (obj instanceof ReqCreateShowTimeDto dto) {
            startTime = dto.getStartTime();
            endTime = dto.getEndTime();
        }

        if (obj instanceof ReqUpdateShowTimeDto dto) {
            startTime = dto.getStartTime();
            endTime = dto.getEndTime();
        }

        if (startTime == null || endTime == null)
            return true;

        return startTime.isBefore(endTime);
    }
}
