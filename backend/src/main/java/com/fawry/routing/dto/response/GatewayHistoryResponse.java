package com.fawry.routing.dto.response;

import com.fawry.routing.util.MoneyUtils;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
public class GatewayHistoryResponse {

    private final Integer gatewayId;

    private final String gatewayName;

    private final long transactionCount;

    private final BigDecimal totalAmount;

    private final BigDecimal totalCommission;

    private final BigDecimal dailyLimitPerBiller;

    public GatewayHistoryResponse(Integer gatewayId,
                                  String gatewayName,
                                  Long transactionCount,
                                  BigDecimal totalAmount,
                                  BigDecimal totalCommission,
                                  BigDecimal dailyLimitPerBiller) {
        this.gatewayId = gatewayId;
        this.gatewayName = gatewayName;
        this.transactionCount = transactionCount;
        this.totalAmount = MoneyUtils.round(totalAmount);
        this.totalCommission = MoneyUtils.round(totalCommission);
        this.dailyLimitPerBiller = dailyLimitPerBiller;
    }
}
