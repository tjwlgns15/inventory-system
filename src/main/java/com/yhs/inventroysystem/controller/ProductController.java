package com.yhs.inventroysystem.controller;

import com.yhs.inventroysystem.dto.ChangeProductStockRequest;
import com.yhs.inventroysystem.dto.ProductRequest;
import com.yhs.inventroysystem.dto.ProductResponse;
import com.yhs.inventroysystem.dto.ProductStockHistoryDto;
import com.yhs.inventroysystem.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 제품 등록
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<Void> registerProduct(@Valid @RequestBody ProductRequest request){
        productService.registerProduct(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 전체 제품 목록 조회
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * 제품 정보 수정
     * PATCH /api/products/product/{id}
     */
    @PatchMapping("/product/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request){
        productService.updateProduct(id, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 재고 생산
     * PATCH /api/products/produce
     */
    @PatchMapping("/produce")
    public ResponseEntity<?> changeStock(@RequestBody ChangeProductStockRequest request) {
        productService.produceProduct(request.getProductId(), request.getDelta());
        return ResponseEntity.ok("재고 변경 완료");
    }

    /**
     * 제품 출고 처리
     * PATCH /api/products/dispatch
     */
    @PatchMapping("/dispatch")
    public ResponseEntity<?> dispatchProduct(@RequestBody ChangeProductStockRequest request) {
        productService.dispatchProduct(request.getProductId(), request.getDelta());
        return ResponseEntity.ok("출고 완료");
    }


    /**
     * 제품 삭제
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("삭제 완료");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 특정 제품의 재고 변경 이력 조회
     * GET /api/products/product/{id}/history
     */
    @GetMapping("/product/{id}/history")
    public ResponseEntity<List<ProductStockHistoryDto>> getProductHistory(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductHistory(id));
    }

    /**
     * 전체 제품의 재고 변경 이력 조회
     * GET /api/products/history/all
     */
    @GetMapping("/history/all")
    public ResponseEntity<List<ProductStockHistoryDto>> getAllProductHistory() {
        return ResponseEntity.ok(productService.getAllProductHistories());
    }


}
