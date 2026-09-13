package com.fawry.routing.dto.response;

import java.util.List;


public record RecommendResponse(GatewayCandidateResponse recommendedGateway,
                                boolean requiresSplitting,
                                List<GatewayCandidateResponse> alternatives,
                                String message) {
}
