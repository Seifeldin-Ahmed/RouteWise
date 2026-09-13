package com.fawry.routing.validation;

import com.fawry.routing.dto.request.GatewayRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class GatewayAvailabilityConstraintValidator
        implements ConstraintValidator<ValidGatewayAvailability, GatewayRequest> {


    @Override
    public boolean isValid(GatewayRequest gateway, ConstraintValidatorContext context) {
        var dayFrom = gateway.getAvailableDayFrom();
        var dayTo = gateway.getAvailableDayTo();
        var from = gateway.getAvailableFrom();
        var to = gateway.getAvailableTo();
        if (dayFrom == null || dayTo == null || from == null || to == null) {
            return true;
        }
        // On a one-day range an end before the start would run backwards, not overnight.
        return dayFrom != dayTo || !from.isAfter(to);
    }
}
