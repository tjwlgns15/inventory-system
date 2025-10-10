package com.yhs.inventroysystem.domain.part;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 부품 재고 변동 이력(StockTransaction) 엔티티를 위한 JPA 리포지토리
 * - 특정 부품의 입출고 이력 조회
 * - 전체 이력 조회
 * - 특정 부품의 이력 삭제
 */
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    /**
     * 특정 부품의 입출고 이력을 최신순으로 조회
     * @param partId 부품 ID
     * @return 입출고 이력 리스트
     */
    List<StockTransaction> findByPartIdOrderByCreatedAtDesc(Long partId);

    /**
     * 전체 입출고 이력을 최신순으로 조회
     * @return 입출고 이력 리스트
     */
    List<StockTransaction> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 부품의 입출고 이력 전체 삭제
     * @param part 부품 엔티티
     */
    void deleteByPart(Part part);
}
