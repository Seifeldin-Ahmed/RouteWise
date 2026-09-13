package com.fawry.routing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "biller_quota_usage",
        uniqueConstraints = @UniqueConstraint(name = "uq_biller_quota",
                columnNames = {"biller_id", "gateway_id", "usage_date"}))
@Getter
@Setter
public class BillerQuotaUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biller_id", nullable = false)
    private User biller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gateway_id", nullable = false)
    private Gateway gateway;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "used_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal usedAmount = BigDecimal.ZERO;
}
