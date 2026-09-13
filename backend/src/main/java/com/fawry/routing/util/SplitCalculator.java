package com.fawry.routing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public final class SplitCalculator {

    public static final int MAX_CHUNKS = 1000;

    private SplitCalculator() {
    }

    public static Optional<List<BigDecimal>> split(BigDecimal amount, BigDecimal min, BigDecimal max) {
        if (max == null) {
            return Optional.of(List.of(amount));
        }

        var chunkCount = amount.divide(max, 0, RoundingMode.CEILING).intValueExact();
        if (chunkCount > MAX_CHUNKS) {
            return Optional.empty();
        }
        if (min.multiply(BigDecimal.valueOf(chunkCount)).compareTo(amount) > 0) {
            return Optional.empty();
        }

        var chunks = new ArrayList<BigDecimal>(chunkCount);
        for (var i = 0; i < chunkCount - 1; i++) {
            chunks.add(max);
        }
        chunks.add(amount.subtract(max.multiply(BigDecimal.valueOf(chunkCount - 1L))));

        rebalance(chunks, min);

        return isValidPlan(chunks, amount, min, max) ? Optional.of(chunks) : Optional.empty();
    }

    private static void rebalance(List<BigDecimal> chunks, BigDecimal min) {
        for (var i = chunks.size() - 1; i >= 0; i--) {
            var shortfall = min.subtract(chunks.get(i));
            if (shortfall.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            for (var donor = i - 1; donor >= 0 && shortfall.compareTo(BigDecimal.ZERO) > 0; donor--) {
                var spare = chunks.get(donor).subtract(min);
                if (spare.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                var taken = spare.min(shortfall);
                chunks.set(donor, chunks.get(donor).subtract(taken));
                chunks.set(i, chunks.get(i).add(taken));
                shortfall = shortfall.subtract(taken);
            }
        }
    }


    private static boolean isValidPlan(List<BigDecimal> chunks, BigDecimal total, BigDecimal min, BigDecimal max) {
        var sum = MoneyUtils.zero();
        for (var chunk : chunks) {
            if (chunk.compareTo(min) < 0 || chunk.compareTo(max) > 0) {
                return false;
            }
            sum = sum.add(chunk);
        }
        return sum.compareTo(total) == 0;
    }
}
