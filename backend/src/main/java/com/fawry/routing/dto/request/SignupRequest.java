package com.fawry.routing.dto.request;

import com.fawry.routing.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@PasswordMatches
@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Size(max = 180)
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$",
            message = "Password must be at least 6 characters, with 1 uppercase and 1 lowercase"
    )
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
