package com.yhs.inventroysystem.domain.part.repository;

import com.yhs.inventroysystem.domain.part.entity.Part;
import com.yhs.inventroysystem.domain.part.entity.PartStockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 부품 재고 변동 이력(StockTransaction) 엔티티를 위한 JPA 리포지토리
 * - 특정 부품의 입출고 이력 조회
 * - 전체 이력 조회
 * - 특정 부품의 이력 삭제
 */
public interface PartStockTransactionRepository extends JpaRepository<PartStockTransaction, Long> {

    /**
     * 특정 부품의 입출고 이력을 최신순으로 조회
     */
    @Query("SELECT pst FROM PartStockTransaction pst " +
            "JOIN FETCH pst.part " +
            "WHERE pst.part.id = :partId " +
            "ORDER BY pst.createdAt DESC")
    List<PartStockTransaction> findByPartIdOrderByCreatedAtDesc(Long partId);

    /**
     * 전체 입출고 이력을 최신순으로 조회
     */
    @Query("SELECT pst FROM PartStockTransaction pst " +
            "JOIN FETCH pst.part " +
            "ORDER BY pst.createdAt DESC")
    List<PartStockTransaction> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 부품의 입출고 이력 전체 삭제
     */
    void deleteByPart(Part part);
}
