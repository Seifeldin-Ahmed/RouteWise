package com.fawry.routing.service;

import com.fawry.routing.entity.BillerQuotaUsage;
import com.fawry.routing.entity.Gateway;
import com.fawry.routing.entity.User;
import com.fawry.routing.repository.BillerQuotaUsageRepository;
import com.fawry.routing.util.MoneyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class QuotaService {

    private final BillerQuotaUsageRepository quotaRepository;

    @Transactional(readOnly = true)
    public Map<Integer, BigDecimal> usedByGateway(Integer billerId, LocalDate businessDate) {
        var used = new HashMap<Integer, BigDecimal>();
        for (var usage : quotaRepository.findAllByBillerIdAndUsageDate(billerId, businessDate)) {
            used.put(usage.getGateway().getId(), usage.getUsedAmount());
        }
        return used;
    }


    @Transactional(propagation = Propagation.MANDATORY)
    public void consume(User biller, Gateway gateway, LocalDate businessDate, BigDecimal amount) {
        var usage = quotaRepository
                .findByBillerIdAndGatewayIdAndUsageDate(biller.getId(), gateway.getId(), businessDate);

        if (usage == null) {
            usage = new BillerQuotaUsage();
            usage.setBiller(biller);
            usage.setGateway(gateway);
            usage.setUsageDate(businessDate);
            usage.setUsedAmount(MoneyUtils.zero());
        }

        usage.setUsedAmount(usage.getUsedAmount().add(amount));
        quotaRepository.save(usage);
    }


}
