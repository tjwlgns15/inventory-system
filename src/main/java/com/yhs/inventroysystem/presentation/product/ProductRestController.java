package com.yhs.inventroysystem.presentation.product;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.domain.product.Product;
import com.yhs.inventroysystem.application.product.ProductService;
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

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    /**
     * 제품 등록
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<ProductResponse> registerProduct(@Valid @RequestBody ProductRegisterRequest request) {
        ProductRegisterCommand command = new ProductRegisterCommand(
                request.productCode(),
                request.name(),
                request.defaultUnitPrice(),
                request.description(),
                request.initialStock(),
                request.partMappings().stream()
                        .map(pm -> new PartMappingInfo(pm.partId(), pm.requiredQuantity()))
                        .collect(Collectors.toList())
        );

        Product product = productService.registerProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.from(product));
    }

    /**
     * 모든 제품 조회
     * @return
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productService.findAllProduct();
        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * 제품 조회
     * @param productId
     * @return
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        Product product = productService.findProductById(productId);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    /**
     * 제품 조회 - 부품 정보까지 포함
     * @param productId
     * @return
     */
    @GetMapping("/{productId}/with-parts")
    public ResponseEntity<ProductDetailResponse> getProductWithParts(@PathVariable Long productId) {
        Product product = productService.findProductWithParts(productId);
        return ResponseEntity.ok(ProductDetailResponse.from(product));
    }

    /**
     * 제품 생산
     * @param request
     * @return
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
                productId,
                request.name(),
                request.defaultUnitPrice(),
                request.description(),
                request.partMappings().stream()
                        .map(pm -> new PartMappingInfo(pm.partId(), pm.requiredQuantity()))
                        .collect(Collectors.toList())
        );

        Product product = productService.updateProduct(command);
        return ResponseEntity.ok(ProductResponse.from(product));
    }
}
