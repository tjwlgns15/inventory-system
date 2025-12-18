package com.yhs.inventroysystem.domain.product.repository;

import com.yhs.inventroysystem.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 제품(Product) 엔티티를 위한 JPA 리포지토리
 * - 기본 CRUD 기능 제공
 * - 조회 시 삭제 되지 않은 것만 검색
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 주어진 제품 코드가 이미 존재하는지 확인
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.productCode = :productCode AND p.deletedAt IS NULL")
    boolean existsByProductCodeAndNotDeleted(String productCode);

    /**
     * 주어진 제품 이름이 이미 존재하는지 확인
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.name = :name AND p.deletedAt IS NULL")
    boolean existsByNameAndNotDeleted(String name);

    /**
     * 제품 코드로 제품 조회
     */
    @Query("SELECT p FROM Product p WHERE p.productCode = :productCode AND p.deletedAt IS NULL")
    Optional<Product> findByProductCodeAndNotDeleted(String productCode);

    /**
     * 제품 이름으로 제품 조회
     */
    @Query("SELECT p FROM Product p WHERE p.name = :name AND p.deletedAt IS NULL")
    Optional<Product> findByNameAndNotDeleted(String name);

    /**
     * ID로 제품과 부품 매핑 정보를 함께 조회
     * LEFT JOIN FETCH를 사용하여 부품이 없는 제품도 조회 가능
     */
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.partMappings pm " +
            "LEFT JOIN FETCH pm.part " +
            "WHERE p.id = :productId AND p.deletedAt IS NULL")
    Optional<Product> findByIdWithPartsAndNotDeleted(@Param("productId") Long productId);

    /**
     * 모든 제품 조회
     */
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Product> findAllActive();
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    List<Product> findAllActive(Sort sort);
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    Page<Product> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL " +
            "AND (LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(String keyword, Pageable pageable);

    /**
     * 모든 제품과 부품 매핑 정보를 함께 조회
     * LEFT JOIN FETCH를 사용하여 부품이 없는 제품도 조회 가능
     */
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.partMappings pm " +
            "LEFT JOIN FETCH pm.part " +
            "WHERE p.deletedAt IS NULL " +
            "ORDER BY p.displayOrder ASC, p.createdAt DESC")
    List<Product> findAllActiveWithPartOrderByDisplayOrder();

    @Query("""
        SELECT p FROM Product p
        WHERE
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Product> findByNameContainingIgnoreCase(@Param("keyword") String keyword);
}