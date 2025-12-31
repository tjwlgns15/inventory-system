package com.yhs.inventroysystem.presentation.shipment;

import com.yhs.inventroysystem.application.shipment.ShipmentBoxCommand.ShipmentBoxCreateCommand;
import com.yhs.inventroysystem.application.shipment.ShipmentBoxCommand.ShipmentBoxUpdateCommand;
import com.yhs.inventroysystem.application.shipment.ShipmentBoxService;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentBox;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yhs.inventroysystem.presentation.shipment.ShipmentBoxDtos.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipment-boxes")
public class ShipmentBoxRestController {

    private final ShipmentBoxService shipmentBoxService;


    /**
     * 선적용 박스 생성
     */
    @PostMapping
    public ResponseEntity<ShipmentBoxResponse> createBoxTemplate(@Valid @RequestBody ShipmentBoxCreate request) {
        ShipmentBoxCreateCommand command = request.toCommand();
        ShipmentBox boxTemplate = shipmentBoxService.createBoxTemplate(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ShipmentBoxResponse.from(boxTemplate));
    }

    /**
     * 상세 조회
     */
    @GetMapping("/{boxItemId}")
    public ResponseEntity<ShipmentBoxResponse> getBoxTemplate(@PathVariable Long boxItemId) {
        ShipmentBox boxTemplate = shipmentBoxService.findBoxTemplate(boxItemId);
        return ResponseEntity.ok(ShipmentBoxResponse.from(boxTemplate));
    }

    /**
     * 활성화된 박스 조회
     */
    @GetMapping
    public ResponseEntity<List<ShipmentBoxResponse>> getAllActivateBoxTemplates() {
        List<ShipmentBox> boxTemplates = shipmentBoxService.findActiveBoxTemplates();
        List<ShipmentBoxResponse> responses = boxTemplates.stream()
                .map(ShipmentBoxResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 모든 박스 조회
     */
    @GetMapping("/all")
    public ResponseEntity<List<ShipmentBoxResponse>> getAllBoxTemplates() {
        List<ShipmentBox> boxTemplates = shipmentBoxService.findAllBoxTemplates();
        List<ShipmentBoxResponse> responses = boxTemplates.stream()
                .map(ShipmentBoxResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 수정
     */
    @PutMapping("/{boxItemId}")
    public ResponseEntity<ShipmentBoxResponse> updateShipment(
            @PathVariable Long boxItemId,
            @Valid @RequestBody ShipmentBoxUpdate request) {

        ShipmentBoxUpdateCommand command = request.toCommand();
        ShipmentBox shipmentBox = shipmentBoxService.updateBoxTemplate(boxItemId, command);
        return ResponseEntity.ok(ShipmentBoxResponse.from(shipmentBox));
    }

    @PatchMapping("{shipmentBoxId}/activate")
    public ResponseEntity<ShipmentBoxResponse> activate(@PathVariable Long shipmentBoxId) {
        ShipmentBox shipmentBox = shipmentBoxService.activateBoxTemplate(shipmentBoxId);
        return ResponseEntity.ok(ShipmentBoxResponse.from(shipmentBox));
    }

    @PatchMapping("{shipmentBoxId}/deactivate")
    public ResponseEntity<ShipmentBoxResponse> deactivate(@PathVariable Long shipmentBoxId) {
        ShipmentBox shipmentBox = shipmentBoxService.deactivateBoxTemplate(shipmentBoxId);
        return ResponseEntity.ok(ShipmentBoxResponse.from(shipmentBox));
    }

    /**
     * 삭제
     */
    @DeleteMapping("/{shipmentBoxId}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long shipmentBoxId) {
        shipmentBoxService.deleteBoxTemplate(shipmentBoxId);
        return ResponseEntity.noContent().build();
    }
}
