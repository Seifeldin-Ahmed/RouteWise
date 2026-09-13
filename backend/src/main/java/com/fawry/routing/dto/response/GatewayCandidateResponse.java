package com.fawry.routing.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fawry.routing.entity.Gateway;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class GatewayCandidateResponse {

    private final Gateway gateway;

    @Setter
    private BigDecimal remainingQuota;

    @JsonIgnore
    @Setter
    private List<BigDecimal> chunks;

    @Setter
    private BigDecimal totalCommission;

    public GatewayCandidateResponse(Gateway gateway, BigDecimal remainingQuota) {
        this.gateway = gateway;
        this.remainingQuota = remainingQuota;
    }

    @JsonProperty("requiresSplitting")
    public boolean requiresSplitting() {
        return chunks != null && chunks.size() > 1;
    }

    @JsonProperty("splitCount")
    public int splitCount() {
        return chunks == null ? 0 : chunks.size();
    }
}
