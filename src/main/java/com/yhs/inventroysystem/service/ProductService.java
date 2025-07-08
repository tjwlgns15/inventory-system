package com.yhs.inventroysystem.service;

import com.yhs.inventroysystem.dto.ProductRequest;
import com.yhs.inventroysystem.dto.ProductResponse;
import com.yhs.inventroysystem.dto.ProductStockHistoryDto;
import com.yhs.inventroysystem.entity.Part;
import com.yhs.inventroysystem.entity.StockTransaction;
import com.yhs.inventroysystem.entity.Product;
import com.yhs.inventroysystem.entity.ProductPart;
import com.yhs.inventroysystem.entity.ProductStockTransaction;
import com.yhs.inventroysystem.entity.enumerate.ProductTransactionType;
import com.yhs.inventroysystem.entity.enumerate.TransactionType;
import com.yhs.inventroysystem.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yhs.inventroysystem.entity.enumerate.ProductTransactionType.ADJUSTMENT;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepo;
    private final PartRepository partRepo;
    private final StockTransactionRepository stockTransactionRepo;
    private final ProductStockTransactionRepository productStockTransactionRepo;

    /**
     * 제품 등록
     * - 이름, 재고 설정
     * - 부품 코드 기반으로 ProductPart 구성
     * - 재고 초기값이 0 이상이면 재고 이력도 저장
     */

    public void registerProduct(ProductRequest request) {
        if (productRepo.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 제품명입니다: " + request.getName());
        }

        Product product = Product.create(request.getName(), request.getStock());

        for (ProductRequest.PartInput partInput : request.getParts()) {
            Part part = partRepo.findByPartCode(partInput.getCode())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부품 코드입니다: " + partInput.getCode()));

            if (partInput.getCount() <= 0) {
                throw new IllegalArgumentException(
                        "부품 수량은 1 이상이어야 합니다. 부품: " + partInput.getCode());
            }

            product.addProductPart(part, partInput.getCount());
        }

        productRepo.save(product);

        if (request.getStock() > 0) {
            ProductStockTransaction initialTransaction = ProductStockTransaction.create(
                    product,
                    ProductTransactionType.INITIAL,
                    0,
                    request.getStock(),
                    request.getStock()
            );

            productStockTransactionRepo.save(initialTransaction);
        }
    }

    /**
     * 모든 제품 목록 조회
     * - 제품명, 부품 목록, 재고 포함
     */
    public List<ProductResponse> getAllProducts(){
        return productRepo.findAll().stream().map(p -> {
            List<ProductResponse.PartInfo> parts = p.getProductParts().stream()
                    .map(pp -> new ProductResponse.PartInfo(
                            pp.getPart().getPartCode(),
                            pp.getPart().getName(),
                            pp.getCount()
                    ))
                    .collect(Collectors.toList());

            return new ProductResponse(p.getId(), p.getName(), parts, p.getStock());
        }).collect(Collectors.toList());
    }

    /**
     * 제품 수정
     * - 이름, 재고, 부품 목록 수정
     * - 재고 변경 시 변경 이력 기록
     */
    @Transactional
    public void updateProduct(Long productId, ProductRequest request){
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 제품이 존재하지 않습니다."));

        int beforeStock = product.getStock();
        int afterStock = request.getStock();

        product.updateName(request.getName());
        product.adjustStock(afterStock);

        // 부품 재설정
        List<ProductPart> productParts = request.getParts().stream()
                .map(input -> {
                    Part part = partRepo.findByPartCode(input.getCode())
                            .orElseThrow(() -> new RuntimeException("부품 없음 : " + input.getCode()));

                    return ProductPart.create(product, part, input.getCount());
                }).toList();

        product.getProductParts().clear();
        product.getProductParts().addAll(productParts);
        productRepo.save(product);

        // 재고 변경 시 이력 저장
        if (beforeStock != afterStock) {
            int delta = afterStock -  beforeStock;
            ProductStockTransaction productStockTransaction = ProductStockTransaction.create(product, ADJUSTMENT, beforeStock, delta, afterStock);

            productStockTransactionRepo.save(productStockTransaction);
        }
    }

    /**
     * 제품 생산 처리
     * - 부품 재고 충분성 검증 및 차감
     * - 제품 재고 증가
     * - 모든 트랜잭션 이력 자동 기록
     */
    @Transactional
    public void produceProduct(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("생산 수량은 1 이상이어야 합니다.");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 제품입니다."));

        int beforeStock = product.getStock();

        // 부품 재고 이전 상태 저장 (트랜잭션 기록용)
        List<PartStockInfo> partStockInfos = new ArrayList<>();
        for (ProductPart productPart : product.getProductParts()) {
            Part part = productPart.getPart();
            int requiredQuantity = productPart.getTotalRequiredQuantity(quantity);
            partStockInfos.add(new PartStockInfo(part, part.getStock(), requiredQuantity));
        }

        // 제품 생산 (부품 차감 + 제품 증가)
        product.produceProduct(quantity);

        // 부품 트랜잭션 기록
        for (PartStockInfo info : partStockInfos) {
            savePartTransaction(info.part, info.beforeStock, -info.requiredQuantity, info.part.getStock());
        }

        // 제품 생산 이력 저장
        saveProductTransaction(product, ProductTransactionType.PRODUCE,
                beforeStock, quantity, product.getStock());

        productRepo.save(product);
    }

    /**
     * 제품 출고 처리
     * - delta는 출고 수량 (양수)
     * - 재고 부족 시 예외 발생
     * - 이력 기록
     */
    public void dispatchProduct(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("출고 수량은 1 이상이어야 합니다.");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 제품입니다."));

        int beforeStock = product.getStock();

        product.removeProduct(quantity);

        // 제품 출고 이력 저장
        saveProductTransaction(product, ProductTransactionType.REMOVE,
                beforeStock, -quantity, product.getStock());

        productRepo.save(product);
    }

    /**
     * 제품 삭제
     * - 연관된 부품 연결 정보 초기화 후 삭제
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 제품이 존재하지 않습니다."));

        productRepo.delete(product);
    }

    /**
     * 특정 제품의 재고 변경 이력 조회
     */
    public List<ProductStockHistoryDto> getProductHistory(Long productId) {
        List<ProductStockTransaction> txList =
                productStockTransactionRepo.findByProductIdOrderByCreatedAtDesc(productId);

        return txList.stream()
                .map(tx -> new ProductStockHistoryDto(
                        tx.getProduct().getName(),
                        tx.getType().getDisplayName(),
                        tx.getBeforeStock(),
                        tx.getDelta(),
                        tx.getAfterStock(),
                        tx.getCreatedAt()
                )).toList();
    }

    /**
     * 전체 제품의 재고 변경 이력 조회
     */
    public List<ProductStockHistoryDto> getAllProductHistories() {
        return productStockTransactionRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(tx -> new ProductStockHistoryDto(
                        tx.getProduct().getName(),
                        tx.getType().getDisplayName(),
                        tx.getBeforeStock(),
                        tx.getDelta(),
                        tx.getAfterStock(),
                        tx.getCreatedAt()
                )).toList();
    }

    /**
     * 부품 재고 차감 및 트랜잭션 기록 처리
     */
    private void processPartStockDeduction(Product product, int productQuantity) {
        for (ProductPart productPart : product.getProductParts()) {
            Part part = productPart.getPart();
            int requiredQuantity = productPart.getTotalRequiredQuantity(productQuantity);

            int partBeforeStock = part.getStock();

            // 부품 재고 차감
            part.decreaseStock(requiredQuantity);

            // 부품 재고 차감 후 수량
            int partAfterStock = part.getStock();

            // 부품 출고 이력 저장
            savePartTransaction(part, partBeforeStock, -requiredQuantity, partAfterStock);
        }
    }

    /**
     * 부품 트랜잭션 저장
     */
    private void savePartTransaction(Part part, int beforeStock, int delta, int afterStock) {
        StockTransaction transaction = StockTransaction.create(
                part, TransactionType.OUTBOUND, beforeStock, delta, afterStock
        );
        stockTransactionRepo.save(transaction);
    }

    /**
     * 제품 트랜잭션 저장
     */
    private void saveProductTransaction(Product product, ProductTransactionType type,
                                        int beforeStock, int delta, int afterStock) {
        ProductStockTransaction transaction = ProductStockTransaction.create(
                product, type, beforeStock, delta, afterStock
        );
        productStockTransactionRepo.save(transaction);
    }


    // 헬퍼 클래스
    private static class PartStockInfo {
        final Part part;
        final int beforeStock;
        final int requiredQuantity;

        PartStockInfo(Part part, int beforeStock, int requiredQuantity) {
            this.part = part;
            this.beforeStock = beforeStock;
            this.requiredQuantity = requiredQuantity;
        }
    }

}
