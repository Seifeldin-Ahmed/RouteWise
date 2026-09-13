package com.fawry.routing.repository;

import com.fawry.routing.entity.BillerQuotaUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BillerQuotaUsageRepository extends JpaRepository<BillerQuotaUsage, Integer> {

    List<BillerQuotaUsage> findAllByBillerIdAndUsageDate(Integer billerId, LocalDate usageDate);

    void deleteByGatewayId(Integer gatewayId);

    BillerQuotaUsage findByBillerIdAndGatewayIdAndUsageDate(Integer billerId,
                                                            Integer gatewayId,
                                                            LocalDate usageDate);
}
