package com.yhs.inventroysystem.controller;

import com.yhs.inventroysystem.dto.*;
import com.yhs.inventroysystem.entity.StockTransaction;
import com.yhs.inventroysystem.service.PartService;
import com.yhs.inventroysystem.service.StockTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;
    private final StockTransactionService stockTransactionService;


    /**
     * 부품 분류 목록 조회
     * GET /api/parts/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<PartCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(partService.findAllPartCategories());
    }

    /**
     * 새로운 부품 분류 추가
     * POST /api/parts/categories
     */
    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody Map<String, String> body) {
        partService.addCategory(body.get("name"));
        return ResponseEntity.ok().build();
    }

    /**
     * 부품 분류 삭제
     * DELETE /api/parts/categories/{id}
     */
    @DeleteMapping("/categories/{id}")
    public void deleteCategory(@PathVariable Long id) {
        partService.deleteCategory(id);
    }

    /**
     * 전체 부품 목록 조회
     * GET /api/parts
     */
    @GetMapping
    public ResponseEntity<List<PartResponse>> getAllParts() {
        return ResponseEntity.ok(partService.findAllParts());
    }

    /**
     * 새로운 부품 등록
     * POST /api/parts
     */
    @PostMapping
    public ResponseEntity<?> addPart(@Valid @RequestBody PartRequest request) {
        partService.addPart(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 부품 정보 일부 수정
     * PATCH /api/parts/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePart(@PathVariable Long id, @RequestBody PartUpdateRequest updates) {
        partService.updatePart(id, updates);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 부품 삭제
     * DELETE /api/parts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePart(@PathVariable Long id) {
        try {
            partService.deletePart(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // 삭제 실패 시 클라이언트에 에러 메시지 전달
            return ResponseEntity.badRequest().body("삭제 실패: " + e.getMessage());
        }
    }

    /**
     * 부품 재고 입고 또는 출고 처리
     * POST /api/parts/{id}/stock
     * - INBOUND이면 수량 증가
     * - OUTBOUND이면 수량 감소
     */
    @PostMapping("/{id}/stock")
    public ResponseEntity<?> changeStock(@PathVariable Long id, @RequestBody StockChangeRequest request) {
        int delta = request.getType().equalsIgnoreCase("INBOUND")
                ? request.getQuantity()
                : -request.getQuantity();

        partService.changeStock(id, delta, request.getType());
        return ResponseEntity.ok().build();
    }


    // todo: 응답 ResponseEntity 래핑 -> 받는 곳 수정
    /**
     * 전체 입출고 이력 조회
     * GET /api/parts/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<StockTransactionResponse>> getAllHistory() {
        return ResponseEntity.ok(stockTransactionService.findAll());
    }

    /**
     * 특정 부품의 입출고 이력 조회
     * GET /api/parts/{id}/history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<StockTransactionResponse>> getHistoryByPart(@PathVariable Long id) {
        return ResponseEntity.ok(stockTransactionService.findByPartId(id));
    }
}
