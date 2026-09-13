package com.fawry.routing.service;

import com.fawry.routing.dto.request.GatewayRequest;
import com.fawry.routing.entity.Gateway;
import com.fawry.routing.exception.ResourceNotFoundException;
import com.fawry.routing.repository.BillerQuotaUsageRepository;
import com.fawry.routing.repository.GatewayRepository;
import com.fawry.routing.repository.TransactionRepository;
import com.fawry.routing.util.MoneyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GatewayService {

    private final GatewayRepository gatewayRepository;
    private final BillerQuotaUsageRepository billerQuotaUsageRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<Gateway> findAll(boolean includeInactive) {
        return includeInactive
                ? gatewayRepository.findAllByOrderByIdAsc()
                : gatewayRepository.findAllByActiveTrueOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Gateway findById(Integer id) {
        return gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No gateway found with id " + id));
    }


    @Transactional
    public void save(Integer id, GatewayRequest request) {
        var gateway = id == null ? new Gateway() : findById(id);

        gateway.setName(request.getName().toLowerCase(Locale.ROOT));
        gateway.setFixedCommission(MoneyUtils.round(request.getFixedCommission()));
        gateway.setPercentageCommission(MoneyUtils.round(request.getPercentageCommission()));
        gateway.setMinTransactionAmount(MoneyUtils.round(request.getMinTransactionAmount()));
        gateway.setMaxTransactionAmount(MoneyUtils.round(request.getMaxTransactionAmount()));
        gateway.setDailyLimitPerBiller(MoneyUtils.round(request.getDailyLimitPerBiller()));
        gateway.setAvailableFrom(request.getAvailableFrom().withSecond(0).withNano(0));
        gateway.setAvailableTo(request.getAvailableTo().withSecond(0).withNano(0));
        gateway.setAvailableDayFrom(request.getAvailableDayFrom());
        gateway.setAvailableDayTo(request.getAvailableDayTo());
        gateway.setProcessingTimeHours(request.getProcessingTimeHours());

        if (request.getActive() != null) {
            gateway.setActive(request.getActive());
        }
        gatewayRepository.save(gateway);
    }

    @Transactional
    public void setActive(Integer id, boolean active) {
        var gateway = findById(id);
        gateway.setActive(active);
        gatewayRepository.save(gateway);
    }


    /**
     * Takes the gateway's history with it: nothing references gateways with {@code ON DELETE},
     * so the rows have to go first or the database refuses the delete.
     */
    @Transactional
    public void delete(Integer id) {
        var gateway = findById(id);
        transactionRepository.deleteByGatewayId(id);
        billerQuotaUsageRepository.deleteByGatewayId(id);
        gatewayRepository.delete(gateway);
    }

}
