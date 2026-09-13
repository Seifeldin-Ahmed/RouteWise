package com.fawry.routing.service;

import com.fawry.routing.dto.response.GatewayHistoryResponse;
import com.fawry.routing.dto.response.TransactionHistoryResponse;
import com.fawry.routing.entity.Gateway;
import com.fawry.routing.entity.Transaction;
import com.fawry.routing.entity.User;
import com.fawry.routing.enums.Urgency;
import com.fawry.routing.repository.TransactionRepository;
import com.fawry.routing.util.MoneyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int PAGE_SIZE = 10;

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Transactional(propagation = Propagation.MANDATORY)
    public List<Transaction> record(User biller,
                                    Gateway gateway,
                                    List<BigDecimal> chunks,
                                    List<BigDecimal> commissions,
                                    Urgency urgency,
                                    LocalDate businessDate) {
        var rows = new ArrayList<Transaction>(chunks.size());
        for (var index = 0; index < chunks.size(); index++) {
            var transaction = new Transaction();
            transaction.setBiller(biller);
            transaction.setGateway(gateway);
            transaction.setAmount(chunks.get(index));
            transaction.setCommission(commissions.get(index));
            transaction.setUrgency(urgency);
            transaction.setBusinessDate(businessDate);
            rows.add(transaction);
        }
        return transactionRepository.saveAll(rows);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or #billerId == authentication.principal")
    public TransactionHistoryResponse history(Integer billerId, LocalDate date, Integer gatewayId, int page) {

        userService.getById(billerId);

        var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);

        List<Transaction> transactions;
        List<GatewayHistoryResponse> gateways;

        if (date != null && gatewayId != null) {
            transactions = transactionRepository.findByBillerIdAndBusinessDateAndGatewayIdOrderByCreatedAtDescIdDesc(billerId, date, gatewayId, pageable);
            gateways = transactionRepository.groupByGatewayAndBusinessDateAndGatewayId(billerId, date, gatewayId);
        } else if (date != null) {
            transactions = transactionRepository.findByBillerIdAndBusinessDateOrderByCreatedAtDescIdDesc(billerId, date, pageable);
            gateways = transactionRepository.groupByGatewayAndBusinessDate(billerId, date);
        } else if (gatewayId != null) {
            transactions = transactionRepository.findByBillerIdAndGatewayIdOrderByCreatedAtDescIdDesc(billerId, gatewayId, pageable);
            gateways = transactionRepository.groupByGatewayAndGatewayId(billerId, gatewayId);
        } else {
            transactions = transactionRepository.findByBillerIdOrderByCreatedAtDescIdDesc(billerId, pageable);
            gateways = transactionRepository.groupByGateway(billerId);
        }

        var totalTransactions = 0L;
        var totalAmount = MoneyUtils.zero();
        var totalCommission = MoneyUtils.zero();

        for (var gateway : gateways) {
            totalTransactions += gateway.getTransactionCount();
            totalAmount = totalAmount.add(gateway.getTotalAmount());
            totalCommission = totalCommission.add(gateway.getTotalCommission());
        }

        return new TransactionHistoryResponse(billerId, date, gatewayId,
                totalTransactions, totalAmount, totalCommission, gateways,
                pageable.getPageNumber(), (int) Math.ceil((double) totalTransactions / PAGE_SIZE),
                transactions);
    }
}
