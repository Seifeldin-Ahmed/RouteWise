package com.fawry.routing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fawry.routing.enums.Urgency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biller_id", nullable = false)
    private User biller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gateway_id", nullable = false)
    private Gateway gateway;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal commission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgency urgency;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
