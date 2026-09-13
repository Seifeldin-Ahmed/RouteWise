package com.fawry.routing.util;

import com.fawry.routing.dto.response.GatewayCandidateResponse;
import com.fawry.routing.enums.Urgency;

import java.util.Comparator;


public final class GatewayRanker {

    private static final Comparator<GatewayCandidateResponse> BY_SPEED =
            (a, b) -> Integer.compare(a.getGateway().getProcessingTimeHours(), b.getGateway().getProcessingTimeHours());

    private static final Comparator<GatewayCandidateResponse> BY_COST =
            (a, b) -> a.getTotalCommission().compareTo(b.getTotalCommission());

    private static final Comparator<GatewayCandidateResponse> BY_ID =
            (a, b) -> Integer.compare(a.getGateway().getId(), b.getGateway().getId());

    private GatewayRanker() {
    }

    public static Comparator<GatewayCandidateResponse> forUrgency(Urgency urgency) {
        return urgency == Urgency.INSTANT
                ? BY_SPEED.thenComparing(BY_COST).thenComparing(BY_ID)
                : BY_COST.thenComparing(BY_SPEED).thenComparing(BY_ID);
    }
}
