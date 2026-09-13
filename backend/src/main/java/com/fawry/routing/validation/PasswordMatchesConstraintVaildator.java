package com.fawry.routing.validation;

import com.fawry.routing.dto.request.SignupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesConstraintVaildator implements ConstraintValidator<PasswordMatches, SignupRequest> {
    @Override
    public boolean isValid(SignupRequest SignedUser, ConstraintValidatorContext constraintValidatorContext) {
        return SignedUser.getPassword() != null && SignedUser.getPassword().equals(SignedUser.getConfirmPassword());
    }
}
