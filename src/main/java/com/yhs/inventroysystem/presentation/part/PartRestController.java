package com.yhs.inventroysystem.presentation.part;

import com.yhs.inventroysystem.application.part.PartCommands;
import com.yhs.inventroysystem.application.part.PartCommands.PartStockUpdateCommand;
import com.yhs.inventroysystem.application.part.PartService;
import com.yhs.inventroysystem.domain.part.Part;
import com.yhs.inventroysystem.domain.part.PartStockTransaction;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.presentation.part.PartDtos.*;
import com.yhs.inventroysystem.presentation.part.PartTransactionDtos.PartTransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.yhs.inventroysystem.application.part.PartCommands.PartRegisterCommand;
import static com.yhs.inventroysystem.application.part.PartCommands.PartUpdateCommand;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartRestController {

    private final PartService partService;

    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PartResponse> registerPart(
            @RequestPart("data") @Valid PartRegisterRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        PartRegisterCommand command = new PartRegisterCommand(
                request.partCode(),
                request.name(),
                request.specification(),
                request.initialStock(),
                request.unit(),
                imageFile
        );

        Part part = partService.registerPart(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PartResponse.from(part));
    }

    /**
     * 전체 부품 조회
     */
    @GetMapping("/all")
    public ResponseEntity<List<PartResponse>> getAllParts(
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction) {

        List<Part> parts = partService.findAllPart(sortBy, direction);

        List<PartResponse> responses = parts.stream()
                .map(PartResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 부품 목록 조회 (페이지네이션)
     */
    @GetMapping
    public ResponseEntity<PagedPartResponse> getPartsPaged(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword) {

        Page<Part> partPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            partPage = partService.searchParts(keyword, page, size, sortBy, direction);
        } else {
            partPage = partService.findAllPartPaged(page, size, sortBy, direction);
        }

        return ResponseEntity.ok(PagedPartResponse.from(partPage));
    }

    @GetMapping("/{partId}")
    public ResponseEntity<PartResponse> getPart(@PathVariable Long partId) {
        Part part = partService.findPartById(partId);
        return ResponseEntity.ok(PartResponse.from(part));
    }

    @PatchMapping(value = "/{partId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PartResponse> updatePart(
            @PathVariable Long partId,
            @RequestPart("data") @Valid PartUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        PartUpdateCommand command = new PartUpdateCommand(
                request.name(),
                request.specification(),
                request.stockQuantity(),
                request.unit(),
                imageFile
        );

        Part part = partService.updatePart(partId, command);
        return ResponseEntity.ok(PartResponse.from(part));
    }

    @PostMapping("/{partId}/adjust-stock")
    public ResponseEntity<PartResponse> adjustPartStock(
            @PathVariable Long partId,
            @Valid @RequestBody StockQuantityUpdateRequest request) {

        PartStockUpdateCommand command = new PartStockUpdateCommand(
                request.adjustmentQuantity(),
                request.note()
        );

        Part part = partService.adjustPartStock(partId, command);
        return ResponseEntity.ok(PartResponse.from(part));
    }

    @DeleteMapping("/{partId}/image")
    public ResponseEntity<Void> deletePartImage(@PathVariable Long partId) {
        partService.deletePartImage(partId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{partId}/image")
    public ResponseEntity<Resource> getPartImage(@PathVariable Long partId) {
        Part part = partService.findPartById(partId);

        if (part.getImagePath() == null) {
            return ResponseEntity.notFound().build();
        }

        // 파일 데이터 로드
        byte[] imageData = fileStorageService.load(part.getImagePath());
        ByteArrayResource resource = new ByteArrayResource(imageData);

        // 한글 파일명을 URL 인코딩
        String encodedFileName = URLEncoder.encode(
                part.getOriginalImageName(),
                StandardCharsets.UTF_8
        ).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + encodedFileName)
                .contentLength(imageData.length)
                .body(resource);
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
