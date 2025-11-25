package com.yhs.inventroysystem.presentation.delivery;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.delivery.DeliveryCommands.*;
import com.yhs.inventroysystem.application.delivery.DeliveryExcelService;
import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.application.delivery.DeliveryService;
import com.yhs.inventroysystem.presentation.delivery.DeliveryDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryRestController {

    private final DeliveryService deliveryService;
    private final DeliveryExcelService deliveryExcelService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(
            @Valid @RequestBody DeliveryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        DeliveryCreateCommand command = new DeliveryCreateCommand(
                request.clientId(),
                request.items().stream()
                        .map(item -> new DeliveryItemInfo(
                                item.productId(),
                                item.quantity(),
                                item.actualUnitPrice(),
                                item.priceNote()
                        ))
                        .collect(Collectors.toList()),
                request.orderedAt(),
                request.requestedAt()
        );

        Delivery delivery = deliveryService.createDelivery(command, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DeliveryResponse.from(delivery));
    }

    @GetMapping
    public ResponseEntity<PageDeliveryResponse> getDeliveriesPaged(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "25") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword) {

        Page<Delivery> deliveryPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            deliveryPage = deliveryService.searchDeliveries(keyword, page, size, sortBy, direction);
        } else {
            deliveryPage = deliveryService.findAllDeliveriesPaged(page, size, sortBy, direction);
        }

        return ResponseEntity.ok(PageDeliveryResponse.from(deliveryPage));
    }

    @PatchMapping("/{deliveryId}/memo")
    public ResponseEntity<DeliveryResponse> updateMemo(
            @PathVariable Long deliveryId,
            @Valid @RequestBody DeliveryMemoUpdateRequest request) {
        Delivery delivery = deliveryService.updateMemo(deliveryId, request.memo());
        return ResponseEntity.ok(DeliveryResponse.from(delivery));
    }

    @PostMapping("/{deliveryId}/discount")
    public ResponseEntity<DeliveryResponse> applyDiscount(
            @PathVariable Long deliveryId,
            @Valid @RequestBody DeliveryDiscountRequest request) {

        DeliveryDiscountCommand command = new DeliveryDiscountCommand(
                request.discountAmount(),
                request.note()
        );

        Delivery delivery = deliveryService.applyDiscount(deliveryId, command);
        return ResponseEntity.ok(DeliveryResponse.from(delivery));
    }

    @PostMapping("/{deliveryId}/discount-rate")
    public ResponseEntity<DeliveryResponse> applyDiscountRate(
            @PathVariable Long deliveryId,
            @Valid @RequestBody DeliveryDiscountRateRequest request) {

        DeliveryDiscountRateCommand command = new DeliveryDiscountRateCommand(
                request.discountRate(),
                request.note()
        );

        Delivery delivery = deliveryService.applyDiscountRate(deliveryId, command);
        return ResponseEntity.ok(DeliveryResponse.from(delivery));
    }

    @DeleteMapping("/{deliveryId}/discount")
    public ResponseEntity<DeliveryResponse> clearDiscount(@PathVariable Long deliveryId) {
        Delivery delivery = deliveryService.clearDiscount(deliveryId);
        return ResponseEntity.ok(DeliveryResponse.from(delivery));
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

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long deliveryId) {
        deliveryService.deleteDelivery(deliveryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable Long deliveryId) {
        Delivery delivery = deliveryService.findDeliveryById(deliveryId);
        return ResponseEntity.ok(DeliveryResponse.from(delivery));
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportAllDeliveriesToExcel(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        byte[] excelData = deliveryExcelService.exportAllDeliveriesToExcel(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("deliveries_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    @GetMapping("/{deliveryId}/export/excel")
    public ResponseEntity<byte[]> exportDeliveryByIdToExcel(@PathVariable Long deliveryId) {
        byte[] excelData = deliveryExcelService.exportDeliveryByIdToExcel(deliveryId);

        Delivery delivery = deliveryService.findDeliveryById(deliveryId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("delivery_" + delivery.getDeliveryNumber() + ".xlsx", StandardCharsets.UTF_8)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
