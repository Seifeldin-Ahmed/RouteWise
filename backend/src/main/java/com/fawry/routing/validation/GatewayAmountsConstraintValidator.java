package com.fawry.routing.validation;

import com.fawry.routing.dto.request.GatewayRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class GatewayAmountsConstraintValidator
        implements ConstraintValidator<ValidGatewayAmounts, GatewayRequest> {


    @Override
    public boolean isValid(GatewayRequest gateway, ConstraintValidatorContext context) {
        var min = gateway.getMinTransactionAmount();
        var max = gateway.getMaxTransactionAmount();
        var dailyLimit = gateway.getDailyLimitPerBiller();
        return min == null
                || ((max == null || max.compareTo(min) >= 0) && (dailyLimit == null || dailyLimit.compareTo(min) >= 0));
    }
}
