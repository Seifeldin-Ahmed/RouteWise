package com.fawry.routing.dto.response;

import java.math.BigDecimal;
import java.util.List;


public record SplitResponse(GatewayCandidateResponse selectedGateway,
                            List<BigDecimal> splits,
                            BigDecimal totalCommission,
                            String message) {
}
