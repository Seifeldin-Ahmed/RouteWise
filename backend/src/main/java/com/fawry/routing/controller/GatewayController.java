package com.fawry.routing.controller;

import com.fawry.routing.dto.request.GatewayRequest;
import com.fawry.routing.entity.Gateway;
import com.fawry.routing.service.GatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/gateways")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class GatewayController {

    private final GatewayService gatewayService;

    @GetMapping
    public List<Gateway> findAll(@RequestParam(defaultValue = "true") boolean includeInactive) {
        return gatewayService.findAll(includeInactive);
    }

    @GetMapping("/{id}")
    public Gateway findById(@PathVariable Integer id) {
        return gatewayService.findById(id);
    }

    @PostMapping
    public void create(@Valid @RequestBody GatewayRequest request) {
        gatewayService.save(null, request);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Integer id, @Valid @RequestBody GatewayRequest request) {
        gatewayService.save(id, request);
    }

    @PatchMapping("/{id}/active")
    public void setActive(@PathVariable Integer id, @RequestParam boolean active) {
        gatewayService.setActive(id, active);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        gatewayService.delete(id);
    }
}
