package com.yhs.inventroysystem.entity;

import com.yhs.inventroysystem.common.BaseTimeEntity;
import com.yhs.inventroysystem.entity.enumerate.ProductTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor @AllArgsConstructor
public class ProductStockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductTransactionType type;

    @Column(nullable = false)
    private int beforeStock;

    @Column(nullable = false)
    private int delta;

    @Column(nullable = false)
    private int afterStock;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 팩토리 메서드
    public static ProductStockTransaction create(Product product, ProductTransactionType type, int beforeStock, int delta, int afterStock) {
        validateProduct(product);
        validateType(type);
        validateStockValues(beforeStock, delta, afterStock);

        return new ProductStockTransaction(null, product, type, beforeStock, delta, afterStock, LocalDateTime.now());
    }

    // 검증 메서드
    private static void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("제품은 필수입니다");
        }
    }

    private static void validateType(ProductTransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("거래 유형은 필수입니다");
        }
    }

    private static void validateStockValues(int beforeStock, int delta, int afterStock) {
        if (beforeStock < 0) {
            throw new IllegalArgumentException("이전 재고는 0 이상이어야 합니다");
        }
        if (afterStock < 0) {
            throw new IllegalArgumentException("이후 재고는 0 이상이어야 합니다");
        }
        if (beforeStock + delta != afterStock) {
            throw new IllegalArgumentException("재고 계산이 올바르지 않습니다");
        }
    }
}
