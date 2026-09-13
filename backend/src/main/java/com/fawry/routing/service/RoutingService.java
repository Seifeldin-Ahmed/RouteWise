package com.fawry.routing.service;

import com.fawry.routing.repository.GatewayRepository;
import com.fawry.routing.util.AvailabilityChecker;
import com.fawry.routing.dto.response.GatewayCandidateResponse;
import com.fawry.routing.util.MoneyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RoutingService {

    private final GatewayRepository gatewayRepository;
    private final QuotaService quotaService;

    @Transactional(readOnly = true)
    public List<GatewayCandidateResponse> findValidGateways(Integer billerId, BigDecimal amount) {

        var businessDate = LocalDate.now();
        var now = ZonedDateTime.now();
        var available = new ArrayList<GatewayCandidateResponse>();

        var usedByGateway = quotaService.usedByGateway(billerId, businessDate);

        for (var gateway : gatewayRepository.findAllByActiveTrueOrderByIdAsc()) {
            if (!AvailabilityChecker.isAvailableAt(
                    gateway.getAvailableDayFrom(), gateway.getAvailableDayTo(),
                    gateway.getAvailableFrom(), gateway.getAvailableTo(), now)) {
                continue;
            }

            var used = usedByGateway.getOrDefault(gateway.getId(), MoneyUtils.zero());
            var remaining = MoneyUtils.remainingFrom(gateway.getDailyLimitPerBiller(), used);

            if (remaining.compareTo(amount) < 0) {
                continue;
            }

            if (amount.compareTo(gateway.getMinTransactionAmount()) < 0) {
                continue;
            }

            available.add(new GatewayCandidateResponse(gateway, remaining));
        }

        return available;
    }
}
