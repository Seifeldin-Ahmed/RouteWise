package com.fawry.routing.dto.request;

import com.fawry.routing.enums.Urgency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {

    private Integer billerId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "amount supports at most 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "urgency is required and must be INSTANT or CAN_WAIT")
    private Urgency urgency;
}
