package com.yhs.inventroysystem.presentation.shipment;

import com.yhs.inventroysystem.application.shipment.ShipmentExcelService;
import com.yhs.inventroysystem.application.shipment.ShipmentService;
import com.yhs.inventroysystem.domain.shipment.entity.Shipment;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static com.yhs.inventroysystem.application.shipment.ShipmentCommand.ShipmentCreateCommand;
import static com.yhs.inventroysystem.application.shipment.ShipmentCommand.ShipmentUpdateCommand;
import static com.yhs.inventroysystem.presentation.shipment.ShipmentDtos.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipments")
public class ShipmentRestController {

    private final ShipmentService shipmentService;
    private final ShipmentExcelService shipmentExcelService;


    /**
     * 선적 생성
     */
    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentCreate request) {
        ShipmentCreateCommand command = request.toCommand();
        Shipment shipment = shipmentService.createShipment(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ShipmentResponse.from(shipment));
    }

    /**
     * 선적 상세 조회
     */
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.findShipment(shipmentId);
        return ResponseEntity.ok(ShipmentResponse.from(shipment));
    }

    /**
     * 선적 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<ShipmentSummary>> getAllShipments() {
        List<Shipment> shipments = shipmentService.findAllShipments();
        List<ShipmentSummary> responses = shipments.stream()
                .map(ShipmentSummary::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/paged")
    public ResponseEntity<PageShipmentResponse> getShipmentsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ShipmentType shipmentType) {

        Page<Shipment> shipmentPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            shipmentPage = shipmentService.searchShipments(keyword, shipmentType, page, size, sortBy, direction);
        } else {
            shipmentPage = shipmentService.findAllShipmentsPaged(shipmentType, page, size, sortBy, direction);
        }

        return ResponseEntity.ok(PageShipmentResponse.from(shipmentPage));
    }

    /**
     * 기간별 선적 목록 조회
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<ShipmentSummary>> getShipmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Shipment> shipments = shipmentService.findByDateRange(startDate, endDate);
        List<ShipmentSummary> responses = shipments.stream()
                .map(ShipmentSummary::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 연도별 선적 목록 조회
     */
    @GetMapping("/year/{year}")
    public ResponseEntity<List<ShipmentSummary>> getShipmentsByYear(@PathVariable Integer year) {
        List<Shipment> shipments = shipmentService.findByYear(year);
        List<ShipmentSummary> responses = shipments.stream()
                .map(ShipmentSummary::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 선적 수정
     */
    @PutMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> updateShipment(
            @PathVariable Long shipmentId,
            @Valid @RequestBody ShipmentUpdate request) {
        ShipmentUpdateCommand command = request.toCommand();
        Shipment shipment = shipmentService.updateShipment(shipmentId, command);
        return ResponseEntity.ok(ShipmentResponse.from(shipment));
    }

    /**
     * 선적 삭제
     */
    @DeleteMapping("/{shipmentId}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long shipmentId) {
        shipmentService.deleteShipment(shipmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 내보내기
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportAllShipmentToExcel(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        byte[] excelData = shipmentExcelService.exportAllShipmentsToExcel(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("shipments_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
