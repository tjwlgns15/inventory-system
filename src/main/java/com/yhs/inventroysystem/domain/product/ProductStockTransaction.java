package com.yhs.inventroysystem.domain.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity @Getter
@Table(name = "product_stock_transactions")
@NoArgsConstructor
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
    private int changeQuantity;

    @Column(nullable = false)
    private int afterStock;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private String note = null;


    // 팩토리 메서드
    public static ProductStockTransaction create(Product product, ProductTransactionType type,
                                                 int beforeStock, int changeQuantity,
                                                 int afterStock) {
        validatePart(product);
        validateType(type);
        validateStockValues(beforeStock, changeQuantity, afterStock);

        return new ProductStockTransaction(
                product,
                type,
                beforeStock,
                changeQuantity,
                afterStock,
                LocalDateTime.now()
        );
    }

    /**
     * 사유를 포함한 트랜잭션 생성 (재고 조정용)
     */
    public static ProductStockTransaction createWithNote(Product product, ProductTransactionType type,
                                                         int beforeStock, int changeQuantity,
                                                         int afterStock, String note) {
        validatePart(product);
        validateType(type);
        validateStockValues(beforeStock, changeQuantity, afterStock);

        ProductStockTransaction transaction = new ProductStockTransaction(
                product,
                type,
                beforeStock,
                changeQuantity,
                afterStock,
                LocalDateTime.now()
        );
        transaction.note = note;
        return transaction;
    }

    public ProductStockTransaction(Product product, ProductTransactionType type, int beforeStock, int changeQuantity, int afterStock, LocalDateTime createdAt) {
        this.product = product;
        this.type = type;
        this.beforeStock = beforeStock;
        this.changeQuantity = changeQuantity;
        this.afterStock = afterStock;
        this.createdAt = createdAt;
    }

    // 검증 메서드
    private static void validatePart(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("부품은 필수입니다");
        }
    }

    private static void validateType(ProductTransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("거래 유형은 필수입니다");
        }
    }

    private static void validateStockValues(int beforeStock, int changeQuantity, int afterStock) {
        if (beforeStock < 0) {
            throw new IllegalArgumentException("이전 재고는 0 이상이어야 합니다");
        }
        if (afterStock < 0) {
            throw new IllegalArgumentException("이후 재고는 0 이상이어야 합니다");
        }
        if (beforeStock + changeQuantity != afterStock) {
            throw new IllegalArgumentException("재고 계산이 올바르지 않습니다");
        }
    }
}


