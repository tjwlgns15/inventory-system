package com.yhs.inventroysystem.presentation.product;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.product.ProductService;
import com.yhs.inventroysystem.domain.product.Product;
import com.yhs.inventroysystem.domain.product.ProductStockTransaction;
import com.yhs.inventroysystem.presentation.product.ProductDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.yhs.inventroysystem.application.product.ProductCommands.*;
import static com.yhs.inventroysystem.presentation.product.ProductTransactionDtos.ProductTransactionResponse;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    /**
     * 제품 등록
     */
    @PostMapping
    public ResponseEntity<ProductResponse> registerProduct(@Valid @RequestBody ProductRegisterRequest request) {

        // 부품 미등록 체크
        List<PartMappingInfo> partMappings;
        if (request.partMappings() != null) {
            partMappings = request.partMappings().stream()
                    .map(pm -> new PartMappingInfo(pm.partId(), pm.requiredQuantity()))
                    .toList();
        } else {
            partMappings = List.of();
        }

        ProductRegisterCommand command = new ProductRegisterCommand(
                request.productCode(),
                request.name(),
                request.defaultUnitPrice(),
                request.description(),
                request.initialStock(),
                partMappings
        );

        Product product = productService.registerProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.from(product));
    }

    /**
     * 모든 제품 조회
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productService.findAllProduct();
        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/with-parts")
    public ResponseEntity<List<ProductDetailResponse>> getAllProductsWithParts() {
        List<Product> products = productService.findAllProductWithParts();
        List<ProductDetailResponse> responses = products.stream()
                .map(ProductDetailResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 제품 조회
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        Product product = productService.findProductById(productId);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    /**
     * 제품 조회 - 부품 정보까지 포함
     */
    @GetMapping("/{productId}/with-parts")
    public ResponseEntity<ProductDetailResponse> getProductWithParts(@PathVariable Long productId) {
        Product product = productService.findProductWithParts(productId);
        return ResponseEntity.ok(ProductDetailResponse.from(product));
    }

    /**
     * 제품 생산
     */
    @PostMapping("/produce")
    public ResponseEntity<ProductResponse> produceProduct(
            @Valid @RequestBody ProductionRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ProductProduceCommand command = new ProductProduceCommand(
                request.id(),
                request.quantity()
        );

        Product product = productService.produceProduct(command, currentUser);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    /**
     * 제품 정보 수정
     */
    @PatchMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductUpdateCommand command = new ProductUpdateCommand(
                request.name(),
                request.defaultUnitPrice(),
                request.description(),
                request.partMappings().stream()
                        .map(pm -> new PartMappingInfo(pm.partId(), pm.requiredQuantity()))
                        .collect(Collectors.toList())
        );

        Product product = productService.updateProduct(productId, command);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    /**
     * 제품 삭제(소프트)
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // ========= 트랜잭션 조회 ========= //
    @GetMapping("/{partId}/transactions")
    public ResponseEntity<List<ProductTransactionResponse>> getPartStockTransactions(@PathVariable Long partId) {
        List<ProductStockTransaction> transactions = productService.getProductStockTransactions(partId);

        List<ProductTransactionResponse> responses = transactions.stream()
                .map(ProductTransactionResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<ProductTransactionResponse>> getAllStockTransactions() {
        List<ProductStockTransaction> transactions = productService.getAllStockTransactions();

        List<ProductTransactionResponse> responses = transactions.stream()
                .map(ProductTransactionResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }
}