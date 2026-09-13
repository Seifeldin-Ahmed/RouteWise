package com.fawry.routing.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = GatewayAvailabilityConstraintValidator.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGatewayAvailability {

    String message() default "availableFrom cannot be later than availableTo on a single-day range; "
            + "to run overnight, set availableDayTo to the day the window ends on";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
