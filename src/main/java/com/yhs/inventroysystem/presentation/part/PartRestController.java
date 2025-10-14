package com.yhs.inventroysystem.presentation.part;

import com.yhs.inventroysystem.domain.part.Part;
import com.yhs.inventroysystem.application.part.PartService;
import com.yhs.inventroysystem.domain.part.PartStockTransaction;
import com.yhs.inventroysystem.presentation.part.PartDtos.*;
import com.yhs.inventroysystem.presentation.part.PartTransactionDtos.PartTransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yhs.inventroysystem.application.part.PartCommands.*;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartRestController {

    private final PartService partService;

    @PostMapping
    public ResponseEntity<PartResponse> registerPart(@Valid @RequestBody PartRegisterRequest request) {
        PartRegisterCommand command = new PartRegisterCommand(
                request.partCode(),
                request.name(),
                request.specification(),
                request.initialStock(),
                request.unit()
        );

        Part part = partService.registerPart(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PartResponse.from(part));
    }

    @GetMapping
    public ResponseEntity<List<PartResponse>> getAllParts() {
        List<Part> parts = partService.findAllPart();

        List<PartResponse> responses = parts.stream()
                .map(PartResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{partId}")
    public ResponseEntity<PartResponse> getPart(@PathVariable Long partId) {
        Part part = partService.findPartById(partId);
        return ResponseEntity.ok(PartResponse.from(part));
    }

    @PatchMapping("/{partId}")
    public ResponseEntity<PartResponse> updatePart(
            @PathVariable Long partId,
            @Valid @RequestBody PartUpdateRequest request) {

        PartUpdateCommand command = new PartUpdateCommand(
                request.name(),
                request.specification(),
                request.unit()
        );

        Part part = partService.updatePart(partId, command);
        return ResponseEntity.ok(PartResponse.from(part));

    }

    @PatchMapping("/{partId}/stock/increase")
    public ResponseEntity<Void> increaseStock(
            @PathVariable Long partId,
            @Valid @RequestBody StockUpdateRequest request) {
        partService.increaseStock(partId, request.quantity());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{partId}/stock/decrease")
    public ResponseEntity<Void> decreaseStock(
            @PathVariable Long partId,
            @Valid @RequestBody StockUpdateRequest request) {
        partService.decreaseStock(partId, request.quantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{partId}")
    public ResponseEntity<Void> deletePart(@PathVariable Long partId) {
        partService.deletePart(partId);
        return ResponseEntity.noContent().build();
    }

    // ========= 트랜잭션 조회 ========= //
    @GetMapping("/{partId}/transactions")
    public ResponseEntity<List<PartTransactionResponse>> getPartStockTransactions(@PathVariable Long partId) {
        List<PartStockTransaction> transactions = partService.getPartStockTransactions(partId);

        List<PartTransactionResponse> responses = transactions.stream()
                .map(PartTransactionResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<PartTransactionResponse>> getAllStockTransactions() {
        List<PartStockTransaction> transactions = partService.getAllStockTransactions();

        List<PartTransactionResponse> responses = transactions.stream()
                .map(PartTransactionResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
