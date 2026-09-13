package com.fawry.routing.dto.request;

import com.fawry.routing.validation.ValidGatewayAmounts;
import com.fawry.routing.validation.ValidGatewayAvailability;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;


@Getter
@Setter
@ValidGatewayAmounts
@ValidGatewayAvailability
public class GatewayRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120)
    private String name;

    @NotNull(message = "fixedCommission is required")
    @DecimalMin(value = "0.00", message = "fixedCommission cannot be negative")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal fixedCommission;

    @NotNull(message = "percentageCommission is required")
    @DecimalMin(value = "0.00", message = "percentageCommission cannot be negative")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal percentageCommission;

    @NotNull(message = "minTransactionAmount is required")
    @DecimalMin(value = "0.01", message = "minTransactionAmount must be greater than zero")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal minTransactionAmount;

    @DecimalMin(value = "0.01", message = "maxTransactionAmount must be greater than zero")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal maxTransactionAmount;

    @NotNull(message = "dailyLimitPerBiller is required")
    @DecimalMin(value = "0.01", message = "dailyLimitPerBiller must be greater than zero")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal dailyLimitPerBiller;

    @NotNull(message = "availableFrom is required (HH:mm)")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime availableFrom;

    @NotNull(message = "availableTo is required (HH:mm)")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime availableTo;

    @NotNull(message = "availableDayFrom is required")
    private DayOfWeek availableDayFrom;

    @NotNull(message = "availableDayTo is required")
    private DayOfWeek availableDayTo;

    @NotNull(message = "processingTimeHours is required")
    @Min(value = 0, message = "processingTimeHours cannot be negative")
    private Integer processingTimeHours;

    private Boolean active;
}
