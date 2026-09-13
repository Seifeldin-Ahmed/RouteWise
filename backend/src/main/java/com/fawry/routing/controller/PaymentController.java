package com.fawry.routing.controller;

import com.fawry.routing.dto.request.PaymentRequest;
import com.fawry.routing.dto.response.RecommendResponse;
import com.fawry.routing.dto.response.SplitResponse;
import com.fawry.routing.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/recommend")
    public RecommendResponse recommend(@AuthenticationPrincipal Integer callerId, @Valid @RequestBody PaymentRequest request) {
        if (request.getBillerId() == null) {
            request.setBillerId(callerId);
        }
        return paymentService.recommend(request);
    }

    @PostMapping("/split")
    public SplitResponse split(@AuthenticationPrincipal Integer callerId, @Valid @RequestBody PaymentRequest request) {
        if (request.getBillerId() == null) {
            request.setBillerId(callerId);
        }
        return paymentService.split(request);
    }
}
