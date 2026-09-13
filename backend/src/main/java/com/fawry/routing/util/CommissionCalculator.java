package com.fawry.routing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


public final class CommissionCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private CommissionCalculator() {
    }


    public static BigDecimal commissionFor(BigDecimal amount, BigDecimal fixed, BigDecimal percentage) {
        var proportional = amount.multiply(percentage).divide(HUNDRED, 10, RoundingMode.HALF_UP);
        return MoneyUtils.round(fixed.add(proportional));
    }


    public static BigDecimal totalCommissionFor(List<BigDecimal> chunks, BigDecimal fixed, BigDecimal percentage) {
        var total = MoneyUtils.zero();
        for (var chunk : chunks) {
            total = total.add(commissionFor(chunk, fixed, percentage));
        }
        return total;
    }
}
