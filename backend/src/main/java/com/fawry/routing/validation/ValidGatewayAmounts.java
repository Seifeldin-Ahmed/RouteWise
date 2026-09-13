package com.fawry.routing.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = GatewayAmountsConstraintValidator.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGatewayAmounts {

    String message() default "maxTransactionAmount and dailyLimitPerBiller must each be at least minTransactionAmount";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
