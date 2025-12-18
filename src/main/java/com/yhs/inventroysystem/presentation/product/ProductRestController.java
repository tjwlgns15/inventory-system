package com.yhs.inventroysystem.presentation.product;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.product.ProductService;
import com.yhs.inventroysystem.domain.product.entity.Product;
import com.yhs.inventroysystem.domain.product.entity.ProductStockTransaction;
import com.yhs.inventroysystem.presentation.product.ProductDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
                request.productCategory(),
                request.productLineId(),
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
    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction) {

        List<Product> products = productService.findAllProduct(sortBy, direction);

        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<PageProductResponse> getProductPaged(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "15") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword) {

        Page<Product> productPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productService.searchProducts(keyword, page, size, sortBy, direction);
        } else {
            productPage = productService.findAllProductPaged(page, size, sortBy, direction);
        }

        return ResponseEntity.ok(PageProductResponse.from(productPage));
    }

    @GetMapping("/with-parts")
    public ResponseEntity<List<ProductDetailResponse>> getAllProductsWithParts() {
        List<Product> products = productService.findAllProductWithParts();
        List<ProductDetailResponse> responses = products.stream()
                .map(ProductDetailResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{productId}/display-order")
    public ResponseEntity<ProductResponse> updateDisplayOrder(
            @PathVariable Long productId,
            @RequestBody @Valid DisplayOrderUpdateRequest request) {

        DisplayOrderUpdateCommand command = new DisplayOrderUpdateCommand(request.displayOrder());
        Product product = productService.updateDisplayOrder(productId, command);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PatchMapping("/display-orders")
    public ResponseEntity<Void> updateDisplayOrders(@Valid @RequestBody BulkDisplayOrderUpdateRequest request) {
        List<ProductOrderUpdate> updates = request.orders().stream()
                .map(info -> new ProductOrderUpdate(info.productId(), info.displayOrder()))
                .toList();

        productService.updateDisplayOrders(updates);
        return ResponseEntity.ok().build();
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
     * 제품의 최대 생산 가능 수량 조회
     */
    @GetMapping("/{productId}/max-producible")
    public ResponseEntity<MaxProducibleResponse> getMaxProducibleQuantity(@PathVariable Long productId) {
        Integer maxQuantity = productService.calculateMaxProducibleQuantity(productId);
        return ResponseEntity.ok(new MaxProducibleResponse(productId, maxQuantity));
    }

    /**
     * 생산 가능 여부 검증 (부족 부품 정보 포함)
     */
    @PostMapping("/{productId}/validate-production")
    public ResponseEntity<ProductionValidationResponse> validateProduction(
            @PathVariable Long productId,
            @RequestBody ProductionValidationRequest request) {

        List<InsufficientPartDetail> insufficientParts =
                productService.calculateInsufficientParts(productId, request.quantity());

        Integer maxQuantity = productService.calculateMaxProducibleQuantity(productId);

        ProductionValidationResponse response = new ProductionValidationResponse(
                insufficientParts.isEmpty(),
                request.quantity(),
                maxQuantity,
                insufficientParts.stream()
                        .map(detail -> new InsufficientPartInfo(
                                detail.partId(),
                                detail.partName(),
                                detail.partCode(),
                                detail.requiredPerProduct(),
                                detail.totalRequired(),
                                detail.availableStock(),
                                detail.shortage()
                        ))
                        .toList()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 제품 정보 수정
     */
    @PatchMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductUpdateCommand command = new ProductUpdateCommand(
                request.productCategory(),
                request.productLineId(),
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

    @PostMapping("/{productId}/adjust-stock")
    public ResponseEntity<ProductResponse> adjustProductStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockQuantityUpdateRequest request) {

        ProductStockUpdateCommand productStockUpdateCommand = new ProductStockUpdateCommand(
                request.adjustmentQuantity(),
                request.note()
        );

        Product product = productService.adjustProductStock(productId, productStockUpdateCommand);
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

    /**
     * 주요 제품 토글
     */
    @PatchMapping("/{productId}/toggle-featured")
    public ResponseEntity<ProductResponse> toggleProductFeatured(@PathVariable Long productId) {
        Product product = productService.toggleProductFeatured(productId);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PatchMapping("/{productId}/toggle-featured2")
    public ResponseEntity<ProductResponse> toggleProductFeatured2(@PathVariable Long productId) {
        Product product = productService.toggleProductFeatured2(productId);
        return ResponseEntity.ok(ProductResponse.from(product));
    }
}