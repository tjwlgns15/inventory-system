package com.yhs.inventroysystem.repository;

import com.yhs.inventroysystem.entity.Product;
import com.yhs.inventroysystem.entity.ProductStockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 완제품 재고 이력(ProductStockTransaction) 엔티티를 위한 JPA 리포지토리
 * - 특정 제품의 재고 변동 이력 조회
 * - 전체 재고 이력 조회 (최신순)
 */
public interface ProductStockTransactionRepository extends JpaRepository<ProductStockTransaction, Long> {

    /**
     * 특정 제품 ID에 해당하는 재고 이력 조회 (최신순 정렬)
     * @param productId 제품 ID
     * @return 재고 이력 리스트
     */
    List<ProductStockTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);

    /**
     * 전체 제품 재고 이력 조회 (최신순 정렬)
     * @return 재고 이력 리스트
     */
    List<ProductStockTransaction> findAllByOrderByCreatedAtDesc();

    void deleteByProduct(Product product);
}
