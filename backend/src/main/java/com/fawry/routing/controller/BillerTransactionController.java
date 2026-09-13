package com.fawry.routing.controller;

import com.fawry.routing.dto.response.TransactionHistoryResponse;
import com.fawry.routing.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/billers")
@PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
@RequiredArgsConstructor
public class BillerTransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{billerId}/transactions")
    public TransactionHistoryResponse transactions(
            @PathVariable Integer billerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer gatewayId,
            @RequestParam(defaultValue = "0") int page) {
        return transactionService.history(billerId, date, gatewayId, page);
    }
}
