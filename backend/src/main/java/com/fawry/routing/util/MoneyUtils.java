package com.fawry.routing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;


public final class MoneyUtils {

    private MoneyUtils() {
    }

    /** Null in, null out: a gateway with no maximum has no amount to round. */
    public static BigDecimal round(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static BigDecimal remainingFrom(BigDecimal limit, BigDecimal used) {
        return MoneyUtils.round(limit.subtract(used).max(BigDecimal.ZERO));
    }

}
