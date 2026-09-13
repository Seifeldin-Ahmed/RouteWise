package com.fawry.routing.service;

import com.fawry.routing.dto.request.PaymentRequest;
import com.fawry.routing.dto.response.GatewayCandidateResponse;
import com.fawry.routing.dto.response.RecommendResponse;
import com.fawry.routing.dto.response.SplitResponse;
import com.fawry.routing.entity.User;
import com.fawry.routing.enums.Urgency;
import com.fawry.routing.util.CommissionCalculator;
import com.fawry.routing.util.GatewayRanker;
import com.fawry.routing.util.MoneyUtils;
import com.fawry.routing.util.SplitCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RoutingService routingService;
    private final QuotaService quotaService;
    private final UserService userService;
    private final TransactionService transactionService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @PreAuthorize("hasAuthority('ADMIN') or #request.billerId == authentication.principal")
    public RecommendResponse recommend(PaymentRequest request) {

        var biller = userService.getById(request.getBillerId());
        var amount = MoneyUtils.round(request.getAmount());
        var candidates = new ArrayList<GatewayCandidateResponse>();
        var requiresSplitting = false;

        for (var candidate : routingService.findValidGateways(biller.getId(), amount)) {
            var gateway = candidate.getGateway();
            if (gateway.getMaxTransactionAmount() != null && amount.compareTo(gateway.getMaxTransactionAmount()) > 0) {
                requiresSplitting = true;
                continue;
            }
            var commission = CommissionCalculator.commissionFor(
                    amount, gateway.getFixedCommission(), gateway.getPercentageCommission());
            candidate.setChunks(List.of(amount));
            candidate.setTotalCommission(commission);
            candidates.add(candidate);
        }

        if (candidates.isEmpty()) {
            return new RecommendResponse(null, requiresSplitting, List.of(),
                    noCandidateMessage(amount, requiresSplitting));
        }

        candidates.sort(GatewayRanker.forUrgency(request.getUrgency()));
        var best = candidates.getFirst();
        var alternatives = candidates.stream().skip(1).toList();
        persist(biller, best, amount, request.getUrgency());

        return new RecommendResponse(best, false, alternatives, null);
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    @PreAuthorize("hasAuthority('ADMIN') or #request.billerId == authentication.principal")
    public SplitResponse split(PaymentRequest request) {

        var biller = userService.getById(request.getBillerId());
        var amount = MoneyUtils.round(request.getAmount());
        var plans = new ArrayList<GatewayCandidateResponse>();

        for (var candidate : routingService.findValidGateways(biller.getId(), amount)) {
            var gateway = candidate.getGateway();
            var plan = SplitCalculator.split(amount, gateway.getMinTransactionAmount(), gateway.getMaxTransactionAmount());
            if (plan.isEmpty()) {
                continue;
            }

            var chunks = plan.get();
            var totalCommission = CommissionCalculator.totalCommissionFor(chunks, gateway.getFixedCommission(), gateway.getPercentageCommission());
            candidate.setChunks(chunks);
            candidate.setTotalCommission(totalCommission);
            plans.add(candidate);
        }

        if (plans.isEmpty()) {
            return new SplitResponse(null, List.of(), MoneyUtils.zero(),
                    "No gateway can take " + amount + " EGP, either in one payment or split into "
                            + "smaller ones.");
        }

        plans.sort(GatewayRanker.forUrgency(request.getUrgency()));

        var best = plans.getFirst();
        persist(biller, best, amount, request.getUrgency());

        return new SplitResponse(best, best.getChunks(), best.getTotalCommission(), null);
    }


    private void persist(User biller, GatewayCandidateResponse candidate, BigDecimal amount, Urgency urgency) {
        var businessDate = LocalDate.now();
        quotaService.consume(biller, candidate.getGateway(), businessDate, amount);
        candidate.setRemainingQuota(candidate.getRemainingQuota().subtract(amount));

        var commissions = candidate.getChunks().stream()
                .map(chunk -> CommissionCalculator.commissionFor(
                        chunk, candidate.getGateway().getFixedCommission(), candidate.getGateway().getPercentageCommission()))
                .toList();

        transactionService.record(biller, candidate.getGateway(), candidate.getChunks(), commissions,
                urgency, businessDate);
    }

    private String noCandidateMessage(BigDecimal amount, boolean requiresSplitting) {
        if (requiresSplitting) {
            return "No gateway can take " + amount + " EGP in a single transaction. It may still "
                    + "go through as several smaller payments.";
        }
        return "No gateway can process " + amount + " EGP right now. It may be outside the gateway "
                + "limits, over your remaining allowance for today, or outside opening hours.";
    }

}
