package com.fawry.routing.dto.response;

import com.fawry.routing.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public record TransactionHistoryResponse(Integer billerId,
                                         LocalDate date,
                                         Integer gatewayId,
                                         long totalTransactions,
                                         BigDecimal totalAmountProcessed,
                                         BigDecimal totalCommissionCharged,
                                         List<GatewayHistoryResponse> gatewayBreakdown,
                                         int page,
                                         int totalPages,
                                         List<Transaction> transactions) {
}
