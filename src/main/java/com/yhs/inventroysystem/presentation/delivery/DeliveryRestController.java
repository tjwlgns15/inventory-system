package com.yhs.inventroysystem.presentation.delivery;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.delivery.DeliveryCommands.*;
import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.application.delivery.DeliveryService;
import com.yhs.inventroysystem.presentation.delivery.DeliveryDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryRestController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(
            @Valid @RequestBody DeliveryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        DeliveryCreateCommand command = new DeliveryCreateCommand(
                request.clientId(),
                request.items().stream()
                        .map(item -> new DeliveryItemInfo(item.productId(), item.quantity()))
                        .collect(Collectors.toList())
        );

        Delivery delivery = deliveryService.createDelivery(command, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DeliveryResponse.from(delivery));
    }

    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> findAllDeliveries() {
        List<Delivery> deliveries = deliveryService.findAllDelivery();
        List<DeliveryResponse> responses = deliveries.stream()
                .map(DeliveryResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{deliveryId}/complete")
    public ResponseEntity<Void> completeDelivery(@PathVariable Long deliveryId) {
        deliveryService.completeDelivery(deliveryId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deliveryId}/cancel")
    public ResponseEntity<Void> cancelDelivery(@PathVariable Long deliveryId) {
        deliveryService.cancelDelivery(deliveryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable Long deliveryId) {
        Delivery delivery = deliveryService.findDeliveryById(deliveryId);
        return ResponseEntity.ok(DeliveryResponse.from(delivery));
    }
}
