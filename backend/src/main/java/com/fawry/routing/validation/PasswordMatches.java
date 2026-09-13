package com.fawry.routing.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordMatchesConstraintVaildator.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {

    public String message() default "Passwords do not match";

    // groups: is where you can actually group validation constraints together
    public Class<?>[] groups() default {};

    // payload: is where you can give additional information about the validation error
    public Class<? extends Payload>[] payload() default {};

}
