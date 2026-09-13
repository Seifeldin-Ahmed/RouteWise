package com.fawry.routing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;


@Entity
@Table(name = "gateways")
@Getter
@Setter
public class Gateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "fixed_commission", nullable = false, precision = 19, scale = 2)
    private BigDecimal fixedCommission;

    @Column(name = "percentage_commission", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentageCommission;

    @Column(name = "min_transaction_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal minTransactionAmount;

    @Column(name = "max_transaction_amount", precision = 19, scale = 2)
    private BigDecimal maxTransactionAmount;

    @Column(name = "daily_limit_per_biller", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyLimitPerBiller;

    @Column(name = "available_from", nullable = false)
    private LocalTime availableFrom;

    @Column(name = "available_to", nullable = false)
    private LocalTime availableTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "available_day_from", nullable = false, length = 10)
    private DayOfWeek availableDayFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "available_day_to", nullable = false, length = 10)
    private DayOfWeek availableDayTo;

    @Column(name = "processing_time_hours", nullable = false)
    private Integer processingTimeHours;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
